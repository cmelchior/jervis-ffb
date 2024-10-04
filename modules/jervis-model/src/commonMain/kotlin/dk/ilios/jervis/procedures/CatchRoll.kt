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
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetOldContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.CatchRollContext
import dk.ilios.jervis.model.context.UseRerollContext
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.calculateAvailableRerollsFor

/**
 * Procedure for handling a Catch Roll as described on page 51 in the rulebook.
 * It is only responsible for handling the actual dice roll. The result is stored
 * in [CatchRollContext] and it is up to the caller of the procedure to choose
 * the appropriate action depending on the outcome.
 */
object CatchRoll : Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<CatchRollContext>()

    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<CatchRollContext>().catchingPlayer.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val rollContext = state.getContext<CatchRollContext>()
                val resultContext = rollContext.copy(
                    roll = D6DieRoll(d6),
                    isSuccess = isCatchSuccess(d6, rollContext.target, rollContext)
                )
                return compositeCommandOf(
                    ReportDiceRoll(DiceRollType.CATCH, d6),
                    SetContext(resultContext),
                    GotoNode(ChooseReRollSource),
                )
            }
        }
    }

    // Team Reroll, Pro, Catch (only if failed), other skills
    object ChooseReRollSource : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<CatchRollContext>().catchingPlayer.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<CatchRollContext>()
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
                listOf(SelectNoReroll(context.isSuccess)) + availableRerolls
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue -> ExitProcedure()
                is NoRerollSelected -> ExitProcedure()
                is RerollOptionSelected -> {
                    val rerollContext = UseRerollContext(DiceRollType.CATCH, action.getRerollSource(state))
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
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return state.rerollContext!!.source.rerollProcedure
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.rerollContext!!
            return if (context.rerollAllowed) {
                GotoNode(ReRollDie)
            } else {
                ExitProcedure()
            }
        }
    }

    object ReRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<CatchRollContext>().catchingPlayer.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val rollResultContext = state.getContext<CatchRollContext>()
                val rollContext = state.getContext<CatchRollContext>()
                val target = rollContext.catchingPlayer.agility + rollContext.diceModifier()
                val rerollResult = rollResultContext.copy(
                    roll = rollResultContext.roll!!.copy(
                        rerollSource = state.rerollContext!!.source,
                        rerolledResult = d6,
                    ),
                    isSuccess = isCatchSuccess(d6, target, rollContext)
                )
                compositeCommandOf(
                    SetContext(rerollResult),
                    ExitProcedure(),
                )
            }
        }
    }

    private fun isCatchSuccess(
        it: D6Result,
        target: Int,
        rollContext: CatchRollContext,
    ): Boolean {
        return it.value != 1 && (target <= it.value + rollContext.diceModifier())
    }
}
