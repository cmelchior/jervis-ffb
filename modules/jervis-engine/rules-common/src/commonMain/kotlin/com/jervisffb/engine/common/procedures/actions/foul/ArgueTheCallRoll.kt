package com.jervisffb.engine.common.procedures.actions.foul

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.commands.probabiliy.AddChanceObservation
import com.jervisffb.engine.common.context.BeingSentOffContext
import com.jervisffb.engine.common.procedures.dicerolls.createFinalTableLookupObservation
import com.jervisffb.engine.common.reports.ReportDiceRoll
import com.jervisffb.engine.common.tables.PrayerToNuffleTableResult
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.tables.ArgueTheCallResult
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationHandler
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Implement the Argue The Call roll as described on page 63 in the rulebook.
 *
 * The result is stored in [com.jervisffb.engine.common.context.BeingSentOffContext] and it is up to the caller to
 * determine what to do with the result.
 */
object ArgueTheCallRoll: Procedure(), ChanceObservationHandler {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<BeingSentOffContext>()

    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BeingSentOffContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val table = rules.argueTheCallTable
            return castDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<BeingSentOffContext>()
                val result = table.roll(d6)
                val chanceObservation = createFinalTableLookupObservation(
                    state = state,
                    team = context.player.team,
                    rollType = DiceRollType.ARGUE_THE_CALL,
                    dice = listOf(d6),
                    favorableOutcomes = table.entries.count { it.value == result },
                    possibleOutcomes = table.entries.size
                )

                // While weirdly worded "Friends with the Ref" just means that roll 5
                // can be changed to "Well, When You Put It Like That..."
                val nextNodeCommand = if (
                    context.player.team.activePrayersToNuffle.contains(PrayerToNuffleTableResult.FRIENDS_WITH_THE_REF)
                    && d6.value == 5
                ) {
                    GotoNode(ResolveFriendsWithTheReferences)
                } else {
                    ExitProcedure()
                }

                val updatedContext = context.copy(
                    argueTheCallRoll = D6DieRoll.create(d6),
                    argueTheCallResult = result
                )
                return compositeCommandOf(
                    ReportDiceRoll(DiceRollType.ARGUE_THE_CALL, d6),
                    chanceObservation?.let(::AddChanceObservation),
                    UpdateContext(updatedContext),
                    nextNodeCommand
                )
            }
        }
    }

    // If the team rolled "Friends with the Ref" on Prayers of Nuffle, they have the
    // option of modifying the final result. This choice is handled here.
    object ResolveFriendsWithTheReferences : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BeingSentOffContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(ConfirmWhenReady, CancelWhenReady)
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Cancel -> ExitProcedure()
                Confirm -> {
                    val context = state.getContext<BeingSentOffContext>()
                    if (context.argueTheCallRoll?.result != 5.d6) {
                        INVALID_GAME_STATE("Wrong value for Friends with the Ref: ${context.argueTheCallRoll}")
                    }
                    compositeCommandOf(
                        UpdateContext(context.copy(argueTheCallResult = ArgueTheCallResult.WELL_IF_YOU_PUT_IT_LIKE_THAT)),
                        ExitProcedure()
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }
}
