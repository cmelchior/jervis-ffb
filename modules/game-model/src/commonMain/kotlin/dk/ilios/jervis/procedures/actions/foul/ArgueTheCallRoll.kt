package dk.ilios.jervis.procedures.actions.foul

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CancelWhenReady
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.ext.d6
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.ArgueTheCallResult
import dk.ilios.jervis.rules.tables.PrayerToNuffle
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE

enum class ArgueTheCallRollModifier(
    override val modifier: Int,
    override val description: String
) : DiceModifier {
    I_DID_NOT_SEE_A_THING(1, "I didn't see a thing"), // Biased Referee Inducement
}

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

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<D6Result>(action) { d6 ->
                val context = state.getContext<FoulContext>()
                val result = rules.argueTheCallTable.roll(d6)

                // While weirdly worded "Friends with the Ref", just means that roll 5
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

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
