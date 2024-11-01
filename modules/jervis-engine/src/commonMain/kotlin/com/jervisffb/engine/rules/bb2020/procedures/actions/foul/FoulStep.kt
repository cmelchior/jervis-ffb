package com.jervisffb.engine.rules.bb2020.procedures.actions.foul

import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetCoachBanned
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.modifiers.DefensiveAssistsModifier
import com.jervisffb.engine.model.modifiers.OffensiveAssistModifier
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryRoll
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.tables.ArgueTheCallResult
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE


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
            val spottedByRefArmour: Boolean = (injuryContext.armourRoll[0] == 1.d6 && injuryContext.armourRoll[1] == 1.d6)
            val spottedByRefInjury: Boolean = (injuryContext.injuryRoll.isNotEmpty() && injuryContext.injuryRoll[0] == 1.d6 && injuryContext.injuryRoll[1] == 1.d6)
            val spottedByRef = spottedByRefArmour || spottedByRefInjury
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
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
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
                SetTurnOver(TurnOver.STANDARD),
                ExitProcedure()
            )
        }
    }
}
