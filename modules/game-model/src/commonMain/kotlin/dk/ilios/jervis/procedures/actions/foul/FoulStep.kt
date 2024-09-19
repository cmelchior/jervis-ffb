package dk.ilios.jervis.procedures.actions.foul

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CancelWhenReady
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetCoachBanned
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.commands.SetTurnOver
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.ext.d6
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.locations.DogOut
import dk.ilios.jervis.model.modifiers.DefensiveAssistsModifier
import dk.ilios.jervis.model.modifiers.OffensiveAssistModifier
import dk.ilios.jervis.procedures.tables.injury.RiskingInjuryContext
import dk.ilios.jervis.procedures.tables.injury.RiskingInjuryMode
import dk.ilios.jervis.procedures.tables.injury.RiskingInjuryRoll
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.ArgueTheCallResult
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE


/**
 * Procedure for handling the Foul part of a [FoulAction].
 */
object FoulStep: Procedure() {
    override val initialNode: Node = CalculateAssists
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<FoulContext>()

    object CalculateAssists: ComputationNode() {
        // TODO For now, assume that both sides want all assists to count
        //  Could there be a case where the defender wants the foul to succeed?
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<FoulContext>()
            val offensiveAssists =
                context.victim!!.coordinates.getSurroundingCoordinates(rules)
                    .mapNotNull { state.field[it].player }
                    .count { player -> rules.canOfferAssistAgainst(player, context.victim) }

            val defensiveAssists =
                context.fouler.coordinates.getSurroundingCoordinates(rules)
                    .mapNotNull { state.field[it].player }
                    .count { player -> rules.canOfferAssistAgainst(player, context.fouler) }

            return compositeCommandOf(
                SetContext(context.copy(foulAssists = offensiveAssists, defensiveAssists = defensiveAssists)),
                GotoNode(RollForFoul)
            )
        }
    }

    object RollForFoul: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val foulContext = state.getContext<FoulContext>()
            val injuryContext = RiskingInjuryContext(
                player = foulContext.victim!!,
                mode = RiskingInjuryMode.FOUL,
                armourModifiers = listOf(
                    OffensiveAssistModifier(foulContext.foulAssists),
                    DefensiveAssistsModifier(foulContext.defensiveAssists)
                )
            )
            return SetContext(injuryContext)
        }

        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll

        override fun onExitNode(state: Game, rules: Rules): Command {
            val foulContext =state.getContext<FoulContext>()
            val injuryContext = state.getContext<RiskingInjuryContext>()
            val spottedByRef: Boolean =
                (injuryContext.armourRoll[0] == 1.d6 && injuryContext.armourRoll[1] == 1.d6) ||
                (injuryContext.injuryRoll[0] == 1.d6 && injuryContext.injuryRoll[1] == 1.d6)
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                SetContext(foulContext.copy(
                    injuryRoll = injuryContext,
                    spottedByTheRef = spottedByRef)),
                if (spottedByRef) {
                    GotoNode(DecideToArgueTheCall)
                } else {
                    ExitProcedure()
                },
            )
        }
    }

    object DecideToArgueTheCall: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<FoulContext>().fouler.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return if (state.activeTeam.coachBanned) {
                // If the coach was already banned, they cannot argue the call again.
                listOf(CancelWhenReady)
            } else {
                listOf(ConfirmWhenReady, CancelWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val foulContext = state.getContext<FoulContext>()
            return when (action) {
                Cancel -> {
                    compositeCommandOf(
                        SetContext(foulContext.copy(argueTheCall = false)),
                        ExitProcedure()
                    )
                }
                Confirm -> {
                    compositeCommandOf(
                        SetContext(foulContext.copy(argueTheCall = true)),
                        GotoNode(RollForArgueThCall)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object RollForArgueThCall: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ArgueTheCallRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<FoulContext>()
            val resultCommand = when (context.argueTheCallResult) {
                ArgueTheCallResult.YOURE_OUTTA_HERE -> {
                    compositeCommandOf(
                        SetCoachBanned(context.fouler.team, true),
                        SetPlayerState(context.fouler, PlayerState.BANNED),
                        SetPlayerLocation(context.fouler, DogOut),
                    )
                }
                ArgueTheCallResult.I_DONT_CARE -> {
                    compositeCommandOf(
                        SetPlayerState(context.fouler, PlayerState.BANNED),
                        SetPlayerLocation(context.fouler, DogOut),
                    )
                }
                ArgueTheCallResult.WELL_IF_YOU_PUT_IT_LIKE_THAT -> {
                    null // Nothing happens to the player
                }
                null -> INVALID_GAME_STATE("Missing argue the call result")
            }
            return compositeCommandOf(
                resultCommand,
                SetTurnOver(true),
                ExitProcedure()
            )
        }
    }
}
