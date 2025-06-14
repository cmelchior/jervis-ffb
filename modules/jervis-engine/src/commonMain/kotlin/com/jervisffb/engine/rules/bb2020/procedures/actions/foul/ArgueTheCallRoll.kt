package com.jervisffb.engine.rules.bb2020.procedures.actions.foul

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
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkType
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.tables.ArgueTheCallResult
import com.jervisffb.engine.rules.bb2020.tables.PrayerToNuffle
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Implement the Argue The Call roll as described on page 63 in the rulebook.
 *
 * The result is stored in [FoulContext] and it is up to the caller to
 * determine what to do with the result.
 */
object ArgueTheCallRoll: Procedure() {
    override val initialNode: Node = RollDice
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<FoulContext>()

    object RollDice : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<FoulContext>().fouler.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<D6Result>(action) { d6 ->
                val context = state.getContext<FoulContext>()
                val result = rules.argueTheCallTable.roll(d6)

                // While weirdly worded "Friends with the Ref" just means that roll 5
                // can be changed to "Well, When You Put It Like That..."
                val nextNodeCommand = if (
                    context.fouler.team.activePrayersToNuffle.contains(PrayerToNuffle.FRIENDS_WITH_THE_REF) &&
                    d6.value == 5
                ) {
                    GotoNode(ResolveFriendsWithTheReferences)
                } else {
                    ExitProcedure()
                }

                val updatedContext = context.copy(
                    argueTheCallRoll = d6,
                    argueTheCallResult = result
                )
                return compositeCommandOf(
                    SetContext(updatedContext),
                    nextNodeCommand
                )
            }
        }
    }

    // If the team rolled "Friends with the Ref" on Prayers of Nuffle, they have the
    // option of modifying the final result. This choice is handled here.
    object ResolveFriendsWithTheReferences : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<FoulContext>().fouler.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(ConfirmWhenReady, CancelWhenReady)
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Cancel -> ExitProcedure()
                Confirm -> {
                    val context = state.getContext<FoulContext>()
                    if (context.argueTheCallRoll != 5.d6) {
                        INVALID_GAME_STATE("Wrong value for Friends with the Ref: ${context.argueTheCallRoll}")
                    }
                    compositeCommandOf(
                        SetContext(context.copy(argueTheCallResult = ArgueTheCallResult.WELL_IF_YOU_PUT_IT_LIKE_THAT)),
                        ExitProcedure()
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }
}
