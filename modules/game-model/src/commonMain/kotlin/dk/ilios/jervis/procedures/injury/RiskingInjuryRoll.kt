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
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetApothecary
import dk.ilios.jervis.commands.SetNigglingInjuries
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.DiceModifier
import dk.ilios.jervis.model.DogOut
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.reports.ReportInjuryResult
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.Skill
import dk.ilios.jervis.rules.tables.CasualtyResult
import dk.ilios.jervis.rules.tables.InjuryResult
import dk.ilios.jervis.rules.tables.LastingInjuryResult
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE

enum class RiskingInjuryMode {
    KNOCKED_DOWN,
    PUSHED_INTO_CROWD,
}

// What do we need to track?
data class RiskingInjuryRollContext(
    val player: Player,
    val mode: RiskingInjuryMode = RiskingInjuryMode.KNOCKED_DOWN, // Do we need this?
    val armourRoll: List<D6Result> = listOf(),
    val armourResult: Int = -1,
    val armourModifiers: List<Skill> = listOf(),
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
)

/**
 * Implement Armour and Injury Rolls as described on page 60-62 in the rulebook.
 */
object RiskingInjuryRoll: Procedure() {
    override val initialNode: Node = DetermineStartingRoll

    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        if (state.riskingInjuryRollsContext == null) {
            INVALID_GAME_STATE("Missing injury context")
        }
        return null
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        return ReportInjuryResult(state.riskingInjuryRollsContext!!)
    }

    object DetermineStartingRoll: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return state.riskingInjuryRollsContext!!.let { context ->
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
            val context = state.riskingInjuryRollsContext!!
            return if (context.armourBroken) {
                GotoNode(RollForInjury)
            } else {
                // If armour is not broken, nothing further can happen
                ExitProcedure()
            }
        }
    }

    object RollForInjury: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return InjuryRoll
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.riskingInjuryRollsContext!!
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
            val context = state.riskingInjuryRollsContext!!

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
            val context = state.riskingInjuryRollsContext!!
            return compositeCommandOf(
                SetPlayerState(context.player, PlayerState.SERIOUS_INJURY),
                GotoNode(ChooseToUseApothecary),
            )
        }
    }

    object ChooseToUseApothecary: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.riskingInjuryRollsContext!!
            val hasApothecary = context.player.team.apothecaries > 0
            return when (hasApothecary) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.riskingInjuryRollsContext!!
            val player = context.player
            val team = player.team
            val moveToDogOut = context.armourBroken && context.injuryResult != InjuryResult.STUNNED

            return when (action) {
                Confirm -> {
                    // Figure out how to handle apothecary here
                    compositeCommandOf(
                        SetApothecary(team, team.apothecaries -1),
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
