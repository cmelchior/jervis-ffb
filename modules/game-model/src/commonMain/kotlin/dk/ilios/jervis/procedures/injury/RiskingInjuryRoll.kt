package dk.ilios.jervis.procedures.injury

import compositeCommandOf
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetNigglingInjuries
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.SetPlayerState
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
    val armourResult: Int,
    val armourModifiers: List<Skill> = listOf(),
    val armourBroken: Boolean = false,
    val injuryRoll: List<D6Result> = listOf(),
    val injuryModifiers: List<DiceModifier> = listOf(),
    val injuryResult: Int = -1,
    val casultyRoll: List<D6Result> = listOf(),
    val casultyModifiers: List<Skill> = listOf(),
    val casultyResult: Int = -1,
)

/**
 * Implement Armour and Injury Rolls as described on page 60 in the rulebook.
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
                in 2 .. 7 -> {
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
                in 8 .. 9 -> {
                    // TODO Add handling of things that might modify KO results (like thick skull)
                    compositeCommandOf(
                        SetPlayerLocation(context.player, DogOut),
                        SetPlayerState(context.player, PlayerState.KNOCKED_OUT),
                        ExitProcedure(),
                    )
                }
                in 10 .. Int.MAX_VALUE -> {
                    GotoNode(RollForCasulty)
                }
                else -> INVALID_GAME_STATE("Unsupported value: $context.injuryResult")
            }
        }
    }

    object RollForCasulty: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = CasualtyRoll

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.riskingInjuryRollsContext!!
            val playerChange = when(context.casultyResult) {
                in 1..6 -> {
                    SetPlayerState(context.player, PlayerState.BADLY_HURT)
                }
                in 7..9 -> {
                    SetPlayerState(context.player, PlayerState.SERIOUS_INJURY)
                }
                in 10 .. 12 -> {
                    compositeCommandOf(
                        SetPlayerState(context.player, PlayerState.SERIOUS_INJURY),
                        SetNigglingInjuries(context.player, 1),
                    )
                }
                in 13 .. 14 -> {
                    // TODO Also roll for lasting injury
                    compositeCommandOf(
                        SetPlayerState(context.player, PlayerState.SERIOUS_INJURY),
                    )
                }
                in 15 .. 16 -> {
                    SetPlayerState(context.player, PlayerState.DEAD)
                }
                else -> INVALID_GAME_STATE("Unsupported value: ${context.casultyResult}")
            }

            return compositeCommandOf(
                SetPlayerLocation(context.player, DogOut),
                playerChange,
                ExitProcedure(),
            )
        }
    }
}
