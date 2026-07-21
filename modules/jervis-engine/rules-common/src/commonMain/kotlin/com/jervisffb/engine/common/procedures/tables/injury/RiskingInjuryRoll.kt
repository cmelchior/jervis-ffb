package com.jervisffb.engine.common.procedures.tables.injury

import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.CompositeCommand
import com.jervisffb.engine.common.commands.SetPlayerIntermediateState
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetSkillUsed
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.common.context.FoulContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.inducements.Apothecary
import com.jervisffb.engine.model.inducements.MortuaryAssistant
import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.common.procedures.RegenerationRoll
import com.jervisffb.engine.common.procedures.getResetChompedStateCommands
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.CasualtyResult
import com.jervisffb.engine.rules.common.tables.InjuryResult
import com.jervisffb.engine.rules.common.tables.LastingInjuryResult
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.sum
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

enum class RiskingInjuryMode {
    PLACED_PRONE, // Not really an Injury, but easier to track through this
    FALLING_OVER,
    KNOCKED_DOWN,
    PUSHED_INTO_CROWD, // Or fallen through a Trapdoor
    FOUL,
    HIT_BY_ROCK,
    // Player is injured after being thrown (normally the same as Falling Over),
    // but we need to know the difference in order to trigger turnovers
    // correctly.
    BAD_LANDING,
    STAB, // Armour/Injury is rolled as part of a Stab
    PROJECTILE_VOMIT, // Armour/Injury is rolled as part of a Projectile Vomit attack
    CHAINSAW, // Armour/Injury is rolled as part of a Chainsaw attack
}

// What do we need to track?
data class RiskingInjuryContext(
    val player: Player,
    // If the Injury is caused by another player. This allows this player to use their
    // skills to modify the armour/injury rolls.
    val causedBy: Player? = null,
    val isPartOfMultipleBlock: Boolean = false,
    val mode: RiskingInjuryMode = RiskingInjuryMode.KNOCKED_DOWN,

    // For mode = KNOCKED_DOWN, there is a chance that the player avoids being knocked down.
    // This boolean allow callers to respond to that.
    val isKnockedDown: Boolean = false,

    // When rolling for Armour and Injury and using Arm Bar, we need to know the starting
    // coordinates, as that determines which players can participate.
    val startingCoordinatesForArmBar: PitchCoordinate? = null,

    // Armour roll
    val usedIronHardSkin: Boolean = false,
    val armourRoll: PersistentList<D6DieRoll> = persistentListOf(),
    val armourModifiers: PersistentList<DiceModifier> = persistentListOf(),
    val useClawsOnArmourRoll: Boolean = false,
    // An effect caused the Armour Roll to be aborted.
    // E.g. a Chainsaw player having a Kickback during a Foul.
    val armourRollAborted: Boolean = false,

    // Injury roll
    val injuryRoll: PersistentList<D6Result> = persistentListOf(),
    val injuryModifiers: PersistentList<DiceModifier> = persistentListOf(),
    val injuryResult: InjuryResult? = null,
    val useThickSkullOnInjuryRoll: Boolean = false,

    // Casualty roll
    val casualtyRoll: D16Result? = null,
    // Modifiers are also applied to `apothecary*` rolls.
    val casualtyModifiers: PersistentList<DiceModifier> = persistentListOf(),
    val casualtyResult: CasualtyResult? = null,
    val lastingInjuryRoll: D6Result? = null,
    val lastingInjuryResult: LastingInjuryResult? = null,

    // Apothecary + selecting a final casualty result
    val apothecaryUsed: Apothecary? = null,

    // BB7 Apothecary roll
    val apothecaryInjuryRoll: D6Result? = null,
    val apothecaryInjuryRollSuccess: Boolean = false,

    // BB11 Apothecary roll
    val apothecaryCasualtyRoll: D16Result? = null,
    val apothecaryCasualtyResult: CasualtyResult? = null,
    val apothecaryLastingInjuryRoll: D6Result? = null,
    val apothecaryLastingInjuryResult: LastingInjuryResult? = null,

    // Store final casualty rolls result here
    val finalCasualtyResult: CasualtyResult? = null,
    val finalLastingInjury: LastingInjuryResult? = null,

    // Regeneration
    val regenerationRoll: D6DieRoll? = null,
    val regenerationApothecaryUsed: Apothecary? = null,
    val regenerationMortuaryAssistantUsed: MortuaryAssistant? = null,
    val regenerationSuccess: Boolean = false
): ProcedureContext {

    private fun checkIfOpponentCanUseActiveSkills(): Boolean {
        if (causedBy == null) return false
        val rules = causedBy.team.game.rules
        return rules.isStanding(causedBy) && !rules.isDistracted(causedBy)
    }

    // Returns `true` if this injury is caused by another player that is able to use
    // their skills to modify the armour/injury roll
    val canOpponentUseSkills = checkIfOpponentCanUseActiveSkills()

    val injuryRollResult: Int
        get() = injuryRoll.sum() + injuryModifiers.sum()
    val armourResult: Int
        get() = armourRoll.sum() + armourModifiers.sum()
    val armourBroken: Boolean
        get() = (player.armorValue <= armourResult)
}

/**
 * Implement Armor and Injury Rolls as described on page 60-62 in the rulebook.
 * Note, the final injury result is not applied until [PatchUpPlayer.ApplyInjury]
 * is called. Up until then all context is stored in [RiskingInjuryContext].
 *
 * [RiskingInjuryContext] is not cleared when exiting this procedure.
 * The caller must do this.
 *
 * Also, specifically, this procedure does not control turnovers. It is up to the
 * caller of this procedure to determine if an injury is a turnover.
 */
object RiskingInjuryRoll: Procedure() {
    override val initialNode: Node = DetermineStartingRoll
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<RiskingInjuryContext>()
        val commands = mutableListOf<Command?>()

        // A player with Leader has left the pitch after an injury roll. If they
        // had Leader, we need to check if another leader is on the pitch. If
        // not, the Leader reroll should be removed
        val player = context.player
        val removeLeaderCommand = rules.calculateLeaderRerollStatusChange(player.team)
        if (removeLeaderCommand != null) {
            commands.add(removeLeaderCommand)
        }

        // If Lethal Flight was used during this Injury, it will reset now
        context.causedBy?.getSkillOrNull(SkillType.LETHAL_FLIGHT)?.let { skill ->
            commands.add(SetSkillUsed(player, skill, false))
        }

        return CompositeCommand(commands.filterNotNull())
    }
    override fun isValid(state: Game, rules: Rules) = state.assertContext<RiskingInjuryContext>()

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
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ArmourRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val clawsWorked = (context.useClawsOnArmourRoll && context.armourRoll.sum() >= 8)
            return if (context.armourBroken || clawsWorked) {
                GotoNode(RollForInjury)
            } else {
                // If Amour isn't broken, we have to consider a few special cases:
                // - If Stab, Projectile Vomit or Chainsaw caused the Amour Roll, nothing happens if it fails.
                // - If an Armour Roll was made against an already Stunned player (e.g. during Throw Team-mate), not
                //   breaking armour will keep them Stunned.
                val player = context.player
                val standOnFailure = (context.mode in listOf(RiskingInjuryMode.STAB, RiskingInjuryMode.PROJECTILE_VOMIT, RiskingInjuryMode.CHAINSAW))
                val isStunned = (player.state == PlayerPitchState.STUNNED)
                val isFoul = (state.getContextOrNull<FoulContext>()?.victim == context.player)
                compositeCommandOf(
                    when {
                        context.armourRollAborted -> null // Player remains unaffected
                        isStunned -> SetPlayerIntermediateState(player, state = null) // Player remains Stunned
                        standOnFailure || isFoul -> null // Armour wasn't broken, so just keep the current player state (whatever it was)
                        else -> compositeCommandOf(
                            SetPlayerState(context.player, PlayerPitchState.PRONE, hasTackleZones = false),
                            getResetChompedStateCommands(context.player, context.player.location, forceRemoveChompedByChomper = true)
                        )
                    },
                    ExitProcedure()
                )
            }
        }
    }

    object RollForInjury: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = InjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            return when (context.injuryResult) {
                InjuryResult.STUNNED -> {
                    // If pushed into the crowed, stunned will move you to Reserves
                    // See page 61 in the rulebook.
                    if (context.mode == RiskingInjuryMode.PUSHED_INTO_CROWD) {
                        compositeCommandOf(
                            SetPlayerLocation(context.player, Dogout),
                            getResetChompedStateCommands(context.player),
                            SetPlayerState(context.player, PlayerDogoutState.RESERVE),
                            ExitProcedure(),
                        )
                    } else {
                        val player = context.player
                        compositeCommandOf(
                            if (state.activeTeamOrThrow() == player.team) {
                                SetPlayerState(player, PlayerPitchState.STUNNED_OWN_TURN, hasTackleZones = false)
                            } else {
                                SetPlayerState(player, PlayerPitchState.STUNNED, hasTackleZones = false)
                            },
                            ExitProcedure(),
                        )
                    }
                }
                InjuryResult.KO -> {
                    // TODO Add handling of things that might modify KO results (like thick skull)
                    compositeCommandOf(
                        SetPlayerState(context.player, PlayerDogoutState.KNOCKED_OUT),
                        GotoNode(CheckApothecary),
                    )
                }
                InjuryResult.BADLY_HURT -> {
                    compositeCommandOf(
                        SetPlayerState(context.player, PlayerDogoutState.BADLY_HURT),
                        GotoNode(CheckApothecary),
                    )
                }
                InjuryResult.SERIOUSLY_HURT -> {
                    compositeCommandOf(
                        SetPlayerState(context.player, PlayerDogoutState.SERIOUSLY_HURT),
                        GotoNode(CheckApothecary),
                    )
                }
                InjuryResult.DEAD -> {
                    compositeCommandOf(
                        SetPlayerState(context.player, PlayerDogoutState.DEAD),
                        GotoNode(CheckApothecary),
                    )
                }
                InjuryResult.CASUALTY -> {
                    if (context.player.hasSkill(SkillType.REGENERATION)) {
                        GotoNode(RollForRegeneration)
                    } else {
                        GotoNode(RollForCasualty)
                    }
                }
                null -> INVALID_GAME_STATE("Missing injury result")
            }
        }
    }

    /**
     * Roll for Regeneration before rolling for the actual casualty.
     * On a successful roll the player skips the Casualty Roll entirely and is
     * placed in their team's Reserves Box. On a failure we continue to
     * [RollForCasualty] as normal.
     *
     * See page 135 in the BB2025 rulebook.
     */
    object RollForRegeneration: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RegenerationRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            return if (context.regenerationSuccess) {
                compositeCommandOf(
                    SetPlayerLocation(context.player, Dogout),
                    getResetChompedStateCommands(context.player),
                    SetPlayerState(context.player, PlayerDogoutState.RESERVE, hasTackleZones = false),
                    ExitProcedure(),
                )
            } else {
                GotoNode(RollForCasualty)
            }
        }
    }

    object RollForCasualty: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = CasualtyRoll

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()

            val playerChangeCommands = when (context.casualtyResult) {
                CasualtyResult.BADLY_HURT -> {
                    SetPlayerState(context.player, PlayerDogoutState.BADLY_HURT)
                }
                CasualtyResult.SERIOUSLY_HURT -> {
                    SetPlayerState(context.player, PlayerDogoutState.SERIOUS_INJURY)
                }
                CasualtyResult.SERIOUS_INJURY -> {
                    compositeCommandOf(
                        SetPlayerState(context.player, PlayerDogoutState.SERIOUS_INJURY),
                    )
                }
                CasualtyResult.LASTING_INJURY -> {
                    compositeCommandOf(
                        SetPlayerState(context.player, PlayerDogoutState.LASTING_INJURY),
                        GotoNode(RollForLastingInjury)
                    )
                }
                CasualtyResult.DEAD -> {
                    SetPlayerState(context.player, PlayerDogoutState.DEAD)
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
                compositeCommandOf(
                    // TODO Add support for Multiple Block
                    // rules.addMultipleBlockInjuryReference(state, injuryContext.player, injuryContext),
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
