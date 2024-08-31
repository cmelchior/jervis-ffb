package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectNoReroll
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetOldContext
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.CatchRollContext
import dk.ilios.jervis.model.context.UseRerollContext
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import dk.ilios.jervis.utils.calculateAvailableRerollsFor

/**
 * Procedure for handling a Catch Roll. It is only responsible for handling the actual dice roll.
 * The result is stored in [Game.catchRollResultContext] and it is up to the caller of the procedure to
 * choose the appropriate action depending on the outcome.
 */
object CatchRoll : Procedure() {
    override fun isValid(state: Game, rules: Rules) {
        if (state.catchRollContext == null) {
            INVALID_GAME_STATE("No catch roll context found")
        }
    }

    override val initialNode: Node = RollDie

    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null

    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object RollDie : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(RollDice(Dice.D6))

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val rollContext = state.catchRollContext!!
                val resultContext = rollContext.copy(
                    roll = D6DieRoll(d6),
                    isSuccess = isCatchSuccess(d6, rollContext.target, rollContext)
                )
                return compositeCommandOf(
                    SetOldContext(Game::catchRollContext, resultContext),
                    GotoNode(ChooseReRollSource),
                )
            }
        }
    }

    // Team Reroll, Pro, Catch (only if failed), other skills
    object ChooseReRollSource : ActionNode() {
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> {
            val context = state.catchRollContext!!
            val availableRerolls = calculateAvailableRerollsFor(
                rules,
                context.catchingPlayer,
                DiceRollType.CATCH,
                context.roll!!,
                context.isSuccess
            )
            return if (availableRerolls.isEmpty()) {
                listOf(ContinueWhenReady)
            } else {
                listOf(SelectNoReroll) + availableRerolls
            }
        }

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return when (action) {
                Continue -> ExitProcedure()
                NoRerollSelected -> ExitProcedure()
                is RerollOptionSelected -> {
                    val rerollContext = UseRerollContext(DiceRollType.CATCH, action.option.source)
                    compositeCommandOf(
                        SetOldContext(Game::rerollContext, rerollContext),
                        GotoNode(UseRerollSource),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object UseRerollSource : ParentNode() {
        override fun getChildProcedure(
            state: Game,
            rules: Rules,
        ): Procedure = state.rerollContext!!.source.rerollProcedure

        override fun onExitNode(
            state: Game,
            rules: Rules,
        ): Command {
            val context = state.rerollContext!!
            return if (context.rerollAllowed) {
                GotoNode(ReRollDie)
            } else {
                ExitProcedure()
            }
        }
    }

    object ReRollDie : ActionNode() {
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> = listOf(RollDice(Dice.D6))

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val rollResultContext = state.catchRollContext!!
                val rollContext = state.catchRollContext!!
                val target = rollContext.catchingPlayer.agility + rollContext.diceModifier()
                val rerollResult = rollResultContext.copy(
                    roll = rollResultContext.roll!!.copy(
                        rerollSource = state.rerollContext!!.source,
                        rerolledResult = d6,
                    ),
                    isSuccess = isCatchSuccess(d6, target, rollContext)
                )
                compositeCommandOf(
                    SetOldContext(Game::catchRollContext, rerollResult),
                    ExitProcedure(),
                )
            }
        }
    }

    private fun isCatchSuccess(
        it: D6Result,
        target: Int,
        rollContext: CatchRollContext,
    ) = it.value != 1 && (target <= it.value + rollContext.diceModifier())
}
