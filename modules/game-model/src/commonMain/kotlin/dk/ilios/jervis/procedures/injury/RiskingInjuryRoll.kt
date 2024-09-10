package dk.ilios.jervis.procedures.injury

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CancelWhenReady
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D16Result
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetNigglingInjuries
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.commands.UseApothecary
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.DogOut
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.inducements.ApothecaryType
import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.reports.ReportInjuryResult
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.Skill
import dk.ilios.jervis.rules.tables.CasualtyResult
import dk.ilios.jervis.rules.tables.InjuryResult
import dk.ilios.jervis.rules.tables.LastingInjuryResult
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE

enum class RiskingInjuryMode {
    FALLING_OVER,
    KNOCKED_DOWN,
    PUSHED_INTO_CROWD,
    FOUL,
    HIT_BY_ROCK
}

// What do we need to track?
data class RiskingInjuryContext(
    val player: Player,
    val mode: RiskingInjuryMode = RiskingInjuryMode.KNOCKED_DOWN, // Do we need this?
    val armourRoll: List<D6Result> = listOf(),
    val armourResult: Int = -1,
    val armourModifiers: List<DiceModifier> = listOf(),
    val armourBroken: Boolean = false,
    val injuryRoll: List<D6Result> = emptyList(),
    val injuryModifiers: List<DiceModifier> = listOf(),
    val injuryResult: InjuryResult? = null,
    val casualtyRoll: D16Result? = null,
    val casualtyModifiers: List<Skill> = emptyList(),
    val casualtyResult: CasualtyResult? = null,
    val lastingInjuryRoll: D6Result? = null,
    val lastingInjuryModifiers: List<DiceModifier> = listOf(),
    val lastingInjuryResult: LastingInjuryResult? = null,
    val useApothecary: Boolean = false,
): ProcedureContext

/**
 * Implement Armour and Injury Rolls as described on page 60-62 in the rulebook.
 *
 * [RiskingInjuryContext] is not cleared when exiting this procedure.
 * The caller must do this.
 *
 * Also, specifically, this procedure does not control turn overs. It is up to the
 * caller of this procedure to determine if an injury is a turn over.
 */
object RiskingInjuryRoll: Procedure() {
    override val initialNode: Node = DetermineStartingRoll
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        return ReportInjuryResult(state.getContext<RiskingInjuryContext>())
    }
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<RiskingInjuryContext>()
    }

    object DetermineStartingRoll: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return state.getContext<RiskingInjuryContext>().let { context ->
                if (context.mode == RiskingInjuryMode.PUSHED_INTO_CROWD) {
                    GotoNode(RollForInjury)
                } else {
                    GotoNode(RollForAmour)
                }
            }
        }
    }

    object RollForAmour: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return ArmourRoll
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            return if (context.armourBroken) {
                GotoNode(RollForInjury)
            } else {
                // If armour is not broken, player is just placed prone.
                compositeCommandOf(
                    SetPlayerState(context.player, PlayerState.PRONE),
                    ExitProcedure()
                )
            }
        }
    }

    object RollForInjury: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return InjuryRoll
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            return when (context.injuryResult) {
                InjuryResult.STUNNED -> {
                    // If pushed into the crowed, stunned will move you to Reserves
                    // See page 61 in the rulebook.
                    if (context.mode == RiskingInjuryMode.PUSHED_INTO_CROWD) {
                        compositeCommandOf(
                            SetPlayerLocation(context.player, DogOut),
                            SetPlayerState(context.player, PlayerState.STANDING),
                            ExitProcedure(),
                        )
                    } else {
                        compositeCommandOf(
                            SetPlayerState(context.player, PlayerState.STUNNED),
                            ExitProcedure(),
                        )
                    }
                }
                InjuryResult.KO -> {
                    // TODO Add handling of things that might modify KO results (like thick skull)
                    compositeCommandOf(
                        SetPlayerState(context.player, PlayerState.KNOCKED_OUT),
                        GotoNode(ChooseToUseApothecary),
                    )
                }
                InjuryResult.BADLY_HURT -> {
                    compositeCommandOf(
                        SetPlayerState(context.player, PlayerState.BADLY_HURT),
                        GotoNode(ChooseToUseApothecary),
                    )
                }
                InjuryResult.CASUALTY -> {
                    GotoNode(RollForCasualty)
                }
                null -> INVALID_GAME_STATE("Missing injury result")
            }
        }
    }

    object RollForCasualty: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = CasualtyRoll

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()

            val playerChangeCommands = when (context.casualtyResult) {
                CasualtyResult.BADLY_HURT -> {
                    SetPlayerState(context.player, PlayerState.BADLY_HURT)
                }
                CasualtyResult.SERIOUS_HURT -> {
                    SetPlayerState(context.player, PlayerState.SERIOUS_INJURY)
                }
                CasualtyResult.SERIOUS_INJURY -> {
                    compositeCommandOf(
                        SetPlayerState(context.player, PlayerState.SERIOUS_INJURY),
                        SetNigglingInjuries(context.player, 1),
                    )
                }
                CasualtyResult.LASTING_INJURY -> {
                    compositeCommandOf(
                        SetPlayerState(context.player, PlayerState.SERIOUS_INJURY),
                        GotoNode(RollForLastingInjury)
                    )
                }
                CasualtyResult.DEAD -> {
                    SetPlayerState(context.player, PlayerState.DEAD)
                }
                null -> INVALID_GAME_STATE("Missing casualty roll result")
            }

            val exitCommand = if (context.casualtyResult == CasualtyResult.LASTING_INJURY) {
                GotoNode(RollForLastingInjury)
            } else {
                GotoNode(ChooseToUseApothecary)
            }

            return compositeCommandOf(
                playerChangeCommands,
                exitCommand
            )
        }
    }

    object RollForLastingInjury: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = LastingInjuryRoll

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            return compositeCommandOf(
                // TODO Missing stat modifier
                SetPlayerState(context.player, PlayerState.SERIOUS_INJURY),
                GotoNode(ChooseToUseApothecary),
            )
        }
    }

    object ChooseToUseApothecary: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RiskingInjuryContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<RiskingInjuryContext>()
            val hasApothecary = context.player.team.teamApothecaries.count { it.type == ApothecaryType.STANDARD && !it.used } > 0
            return when (hasApothecary) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val player = context.player
            val team = player.team
            val moveToDogOut = context.armourBroken && context.injuryResult != InjuryResult.STUNNED

            return when (action) {
                Confirm -> {
                    // TODO Figure out how to handle apothecary here
                    compositeCommandOf(
                        UseApothecary(team, team.teamApothecaries.first { it.type == ApothecaryType.STANDARD && !it.used }),
                        SetPlayerState(player, PlayerState.STUNNED), // Override whatever injury they had
                        ExitProcedure()
                    )
                }
                Cancel,
                Continue -> {
                    compositeCommandOf(
                        SetPlayerLocation(player, DogOut),
                        ExitProcedure()  // Apothecary not used, just accept the result
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    // TODO Add support for other healing affects, like Regeneration?
}
