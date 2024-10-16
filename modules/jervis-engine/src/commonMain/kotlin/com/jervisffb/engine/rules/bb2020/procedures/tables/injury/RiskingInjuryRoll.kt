package com.jervisffb.engine.rules.bb2020.procedures.tables.injury

import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.inducements.Apothecary
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.MultipleBlockContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.tables.CasualtyResult
import com.jervisffb.engine.rules.bb2020.tables.InjuryResult
import com.jervisffb.engine.rules.bb2020.tables.LastingInjuryResult
import com.jervisffb.engine.utils.INVALID_GAME_STATE

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
    val isPartOfMultipleBlock: Boolean = false,
    val mode: RiskingInjuryMode = RiskingInjuryMode.KNOCKED_DOWN, // Do we need this?

    // Armour roll
    val armourRoll: List<D6Result> = listOf(),
    val armourResult: Int = -1,
    val armourModifiers: List<DiceModifier> = listOf(),
    val armourBroken: Boolean = false,

    // Injury roll
    val injuryRoll: List<D6Result> = emptyList(),
    val injuryModifiers: List<DiceModifier> = listOf(),
    val injuryResult: InjuryResult? = null,

    // Casualty roll
    val casualtyRoll: D16Result? = null,
//    val casualtyModifiers: List<Skill> = emptyList(),
    val casualtyResult: CasualtyResult? = null,
    val lastingInjuryRoll: D6Result? = null,
//    val lastingInjuryModifiers: List<DiceModifier> = listOf(),
    val lastingInjuryResult: LastingInjuryResult? = null,

    // Apothecary + selecting final casualty result
    val apothecaryUsed: Apothecary? = null,
    val apothecaryCasualtyRoll: D16Result? = null,
    val apothecaryCasualtyResult: CasualtyResult? = null,
    val apothecaryLastingInjuryRoll: D6Result? = null,
    val apothecaryLastingInjuryResult: LastingInjuryResult? = null,

    // Store final result here
    val finalCasualtyResult: CasualtyResult? = null,
    val finalLastingInjury: LastingInjuryResult? = null,

    // Regeneration
    val regenerationRoll: D6Result? = null,
    val regenerationApothecaryUsed: Apothecary? = null,
    val regenerationReRoll: D6Result? = null,
    val regenerationSuccess: Boolean = false
): ProcedureContext

/**
 * Implement Armour and Injury Rolls as described on page 60-62 in the rulebook.
 * Note, the final injury result is not applied until [PatchUpPlayer.ApplyInjury]
 * is called. Up until then all context is stored in [RiskingInjuryContext].
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
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
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
                    SetPlayerState(context.player, PlayerState.PRONE, hasTackleZones = false),
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
                            SetPlayerState(context.player, PlayerState.RESERVE),
                            ExitProcedure(),
                        )
                    } else {
                        val player = context.player
                        compositeCommandOf(
                            if (state.activeTeam == player.team) {
                                SetPlayerState(player, PlayerState.STUNNED_OWN_TURN, hasTackleZones = false)
                            } else {
                                SetPlayerState(player, PlayerState.STUNNED, hasTackleZones = false)
                            },
                            ExitProcedure(),
                        )
                    }
                }
                InjuryResult.KO -> {
                    // TODO Add handling of things that might modify KO results (like thick skull)
                    compositeCommandOf(
                        SetPlayerState(context.player, PlayerState.KNOCKED_OUT),
                        GotoNode(CheckApothecary),
                    )
                }
                InjuryResult.BADLY_HURT -> {
                    compositeCommandOf(
                        SetPlayerState(context.player, PlayerState.BADLY_HURT),
                        GotoNode(CheckApothecary),
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
                    )
                }
                CasualtyResult.LASTING_INJURY -> {
                    compositeCommandOf(
                        SetPlayerState(context.player, PlayerState.LASTING_INJURY),
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
                GotoNode(CheckApothecary)
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
            return GotoNode(CheckApothecary)
        }
    }

    /**
     * Check if there are any options available for patching up the player now or later.
     *
     * "later" is only applicable when part of a Multiple Block, as in that
     * case we store the injury context inside the MultipleBlock context, so
     * it can be safely deleted from the global scope. Otherwise the injury
     * is always resolved right now.
     */
    object CheckApothecary: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return if (state.getContext<RiskingInjuryContext>().isPartOfMultipleBlock) {
                val injuryContext = state.getContext<RiskingInjuryContext>()
                val mbContext = state.getContext<MultipleBlockContext>()
                compositeCommandOf(
                    SetContext(mbContext.copyAndSetInjuryReferenceForPlayer(injuryContext.player, injuryContext)),
                    ExitProcedure(),
                )
            } else {
                GotoNode(PatchUpPlayerIfPossible)
            }
        }
    }

    object PatchUpPlayerIfPossible: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PatchUpPlayer
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
