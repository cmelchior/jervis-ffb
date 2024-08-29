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
import dk.ilios.jervis.actions.SelectRerollOption
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRerollOption
import dk.ilios.jervis.rules.skills.DiceRoll
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.skills.RerollSource
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE

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
            return checkDiceRoll<D6Result>(action) {
                val rollContext = state.catchRollContext!!
                val target = rollContext.catchingPlayer.agility
                val resultContext =
                    CatchRollResultContext(
                        catchingPlayer = rollContext.catchingPlayer,
                        target = target,
                        modifiers = rollContext.modifiers,
                        roll = D6DieRoll(originalRoll = it),
                        success = isCatchSuccess(it, target, rollContext),
                    )
                return compositeCommandOf(
                    SetContext(Game::catchRollResultContext, resultContext),
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
            val context = state.catchRollResultContext!!
            val successOnFirstRoll = context.success
            val catchingPlayer = context.catchingPlayer
            val availableSkills: List<SelectRerollOption> =
                catchingPlayer.skills
                    .filter { it is RerollSource }
                    .map { it as RerollSource }
                    .filter { it.canReroll(DiceRoll.CATCH, listOf(context.roll), successOnFirstRoll) }
                    .flatMap {
                            it: RerollSource ->
                        it.calculateRerollOptions(DiceRoll.CATCH, context.roll, successOnFirstRoll)
                    }
                    .map { SelectRerollOption(it) }

            val team = catchingPlayer.team
            val hasTeamRerolls = team.availableRerollCount > 0
            val allowedToUseTeamReroll = rules.canUseTeamReroll(state, catchingPlayer)

            return if (availableSkills.isEmpty() && (!hasTeamRerolls || !allowedToUseTeamReroll)) {
                listOf(ContinueWhenReady)
            } else {
                val teamReroll =
                    if (hasTeamRerolls && allowedToUseTeamReroll) {
                        listOf(SelectRerollOption(DiceRerollOption(team.availableRerolls.last(), listOf(context.roll))))
                    } else {
                        emptyList()
                    }
                listOf(SelectNoReroll) + availableSkills + teamReroll
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
                    val rerollContext = RerollContext(DiceRollType.CatchRoll, action.option.source)
                    compositeCommandOf(
                        SetContext(Game::useRerollContext, rerollContext),
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
        ): Procedure = state.useRerollContext!!.source.rerollProcedure

        override fun onExitNode(
            state: Game,
            rules: Rules,
        ): Command {
            // useRerollResult must be set by the procedure running determing if a reroll is allowed
            return if (state.useRerollResult!!.rerollAllowed) {
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
            return checkDiceRoll<D6Result>(action) {
                val rollResultContext = state.catchRollResultContext!!
                val rollContext = state.catchRollContext!!
                val target = rollContext.catchingPlayer.agility + rollContext.diceModifier()
                val rerollResult =
                    CatchRollResultContext(
                        catchingPlayer = rollContext.catchingPlayer,
                        target = target,
                        modifiers = rollContext.modifiers,
                        roll =
                            rollResultContext.roll.copy(
                                rerollSource = state.useRerollContext!!.source,
                                rerolledResult = it,
                            ),
                        success = isCatchSuccess(it, target, rollContext),
                    )
                compositeCommandOf(
                    SetContext(Game::catchRollResultContext, rerollResult),
                    ExitProcedure(),
                )
            }
        }
    }

    private fun isCatchSuccess(
        it: D6Result,
        target: Int,
        rollContext: CatchRollContext,
    ) = it.result != 1 && (target <= it.result + rollContext.diceModifier())
}
