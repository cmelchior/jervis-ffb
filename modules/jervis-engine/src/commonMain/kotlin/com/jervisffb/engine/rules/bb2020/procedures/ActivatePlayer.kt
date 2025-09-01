package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.actions.DeselectPlayer
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetActivePlayer
import com.jervisffb.engine.commands.SetAvailableActions
import com.jervisffb.engine.commands.SetAvailableSpecialActions
import com.jervisffb.engine.commands.SetHasTackleZones
import com.jervisffb.engine.commands.SetPlayerAvailability
import com.jervisffb.engine.commands.SetSpecialActionSkillUsed
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.modifiers.TemporaryEffectType
import com.jervisffb.engine.reports.ReportActionEnded
import com.jervisffb.engine.reports.ReportActionSelected
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.AnimalSavagery
import com.jervisffb.engine.rules.bb2020.skills.BloodLust
import com.jervisffb.engine.rules.bb2020.skills.BoneHead
import com.jervisffb.engine.rules.bb2020.skills.MultipleBlock
import com.jervisffb.engine.rules.bb2020.skills.ProjectileVomit
import com.jervisffb.engine.rules.bb2020.skills.ReallyStupid
import com.jervisffb.engine.rules.bb2020.skills.Stab
import com.jervisffb.engine.rules.bb2020.skills.UnchannelledFury
import com.jervisffb.engine.rules.common.actions.PlayerAction
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.utils.INVALID_ACTION

data class ActivatePlayerContext(
    // The player being activated
    val player: Player,
    // As part of activating the player, some negative effect was cleared
    val clearedNegativeEffects: Boolean = false,
    // As part of activating the player, some negatrait was rolled for
    val rolledForNegaTrait: Boolean = false,
    // If some effect caused the activation to "end immediately". This does not include turn overs.
    // Example: Unchannelled Fury
    val activationEndsImmediately: Boolean = false,
    // Which action does the player want to perform?
    val declaredAction: PlayerAction? = null,
    // The target of the action, if any is required.
    val target: Player? = null,
    // `true` if the action should count as being used, regardless of how the activation ended
    val markActionAsUsed: Boolean = false,
): ProcedureContext

/**
 * Procedure for activating a player and declaring their action as described on
 * page 42 in the rulebook.
 *
 * The exact sequence for activating a player is not really clear in the rules.
 * This is, e.g., a problem with regard to regaining tackle zones (which affect
 * if Pro can be used for Bone Head).
 *
 * The following sequence is being used in this implementation.
 *
 * 1. Select Player (which triggers this procedure).
 * 2. Mark them as Activate and clear any negative effect that last until the
 *    next activation (like missing tackle zones).
 * 3. Declare an Action to perform. For some actions, like Blitz or Foul, this
 *    includes selecting a target.
 * 5. Roll for all Nega-traits in order, stop at the first failure (no player
 *    normally has multiple of these).
 *    a. Bone Head / Really Stupid: They might end the activation here, loose tackle zones and the action is used.
 *    b. Unchannelled Fury: They might end the activation here and the action is used.
 *    c. Animal Savagery: They might hit a nearby player or end their activation and loose tackle zones.
 *    d. Blood Lust: If failed, they might change the declared action to Move.
 * 6. If action has a target, roll for all opponent skills like Foul Appearance and Dump Off.
 * 7. Perform the action.
 * 8. End Action.
 * 9. End Activation.
 *
 * We allow a player to take back selecting an action as long as no side effects
 * have occurred. I.e., a Player is allowed to regret selecting an action as
 * long as no dice has been rolled, no moves have been taken, or no negative
 * state has been removed as part of Step 2.
 *
 * Note, this sequence is debatable and FUMBBL and BB3 doesn't agree on it.
 * See https://fumbbl.com/index.php?name=PNphpBB2&file=viewtopic&t=32167&postdays=0&postorder=asc&start=0
 */
object ActivatePlayer : Procedure() {
    override val initialNode: Node = MarkPlayerAsActive
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<ActivatePlayerContext>()
        val player = context.player
        return buildCompositeCommand {
            add(SetActivePlayer(null))

            // If the action was considered "used", we should remove it from the pool
            // of available actions. If an action was ended prematurely (either due
            // to a turnover or failing some roll), the action is always considered used.
            if (context.markActionAsUsed || state.endActionImmediately()) {
                val activeTeam = state.activeTeamOrThrow()
                val markActionAsUsedCommand = when (val type = context.declaredAction!!.type) {
                    PlayerStandardActionType.MOVE -> SetAvailableActions.markAsUsed(activeTeam, PlayerStandardActionType.MOVE)
                    PlayerStandardActionType.PASS -> SetAvailableActions.markAsUsed(activeTeam, PlayerStandardActionType.PASS)
                    PlayerStandardActionType.HAND_OFF -> SetAvailableActions.markAsUsed(activeTeam, PlayerStandardActionType.HAND_OFF)
                    PlayerStandardActionType.BLOCK -> SetAvailableActions.markAsUsed(activeTeam, PlayerStandardActionType.BLOCK)
                    PlayerStandardActionType.BLITZ -> SetAvailableActions.markAsUsed(activeTeam, PlayerStandardActionType.BLITZ)
                    PlayerStandardActionType.FOUL -> SetAvailableActions.markAsUsed(activeTeam, PlayerStandardActionType.FOUL)
                    PlayerStandardActionType.THROW_TEAM_MATE -> TODO()
                    PlayerStandardActionType.SPECIAL -> null
                    PlayerSpecialActionType.BALL_AND_CHAIN -> TODO()
                    PlayerSpecialActionType.BOMBARDIER -> TODO()
                    PlayerSpecialActionType.BREATHE_FIRE -> TODO()
                    PlayerSpecialActionType.CHAINSAW -> TODO()
                    PlayerSpecialActionType.HYPNOTIC_GAZE -> TODO()
                    PlayerSpecialActionType.KICK_TEAM_MATE -> TODO()
                    PlayerSpecialActionType.MULTIPLE_BLOCK -> {
                        compositeCommandOf(
                            SetSpecialActionSkillUsed(player, player.getSkill<MultipleBlock>(), true),
                            SetAvailableSpecialActions.markAsUsed(activeTeam, PlayerSpecialActionType.MULTIPLE_BLOCK)
                        )
                    }
                    PlayerSpecialActionType.PROJECTILE_VOMIT -> {
                        compositeCommandOf(
                            SetSpecialActionSkillUsed(player, player.getSkill<ProjectileVomit>(), true),
                            SetAvailableSpecialActions.markAsUsed(activeTeam, PlayerSpecialActionType.PROJECTILE_VOMIT)
                        )
                    }
                    PlayerSpecialActionType.STAB -> {
                        compositeCommandOf(
                            SetSpecialActionSkillUsed(player, player.getSkill<Stab>(), true),
                            SetAvailableSpecialActions.markAsUsed(activeTeam, PlayerSpecialActionType.STAB)
                        )
                    }
                }
                if (markActionAsUsedCommand != null) add(markActionAsUsedCommand)
                add(ReportActionEnded(state.activePlayer!!, context.declaredAction))
            }

            // If the player is still "active" it means the declared action was never
            // performed or canceled before it was "used". In that case, we allow the
            // player to be activated again if they haven't done anything else that
            // is considered irreversible.
            if (
                player.available == Availability.IS_ACTIVE &&
                (context.clearedNegativeEffects || context.rolledForNegaTrait)
            ) {
                add(SetPlayerAvailability(player, Availability.AVAILABLE))
            }
        }
    }

    object MarkPlayerAsActive : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<ActivatePlayerContext>()
            val player = context.player

            // If the player is standing and have lost their tackle zone in the last turn,
            // they will now regain it (if they are standing). All effects that cause
            // a player to temporarily lose their tackle zone behave this way:
            // Bone Head, Hypnotic Gaze, Really Stupid
            val enableTackleZonesCommand = if (
                player.state == PlayerState.STANDING &&
                !player.hasTackleZones
            ) {
                compositeCommandOf(
                    SetHasTackleZones(player, true),
                    SetContext(context.copy(clearedNegativeEffects = true))
                )
            } else {
                null
            }

            return compositeCommandOf(
                *getResetTemporaryModifiersCommands(state, rules, Duration.START_OF_ACTIVATION),
                enableTackleZonesCommand,
                SetActivePlayer(player),
                SetPlayerAvailability(player, Availability.IS_ACTIVE),
                GotoNode(DeclareActionOrDeselectPlayer)
            )
        }
    }

    object DeclareActionOrDeselectPlayer : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.activeTeamOrThrow()

//         Multiple Block (Not a special action, how to use this? )
//         Ball & Chain (replace all other actions)
//         Bombardier (Its own action, 1 pr. team turn)
//         Chainsaw (Replace block action or block part of Blitz, 1. pr activation)
//         Kick Team-mate (its own action, 1 pr. team turn)
//         Projectile Vomit (Replace block action or block part of Blitz, 1. pr activation)
//         Stab (Replace block action or block part of Blitz, no limit)
//         Hypnotic Gaze (Its own action)
//         Breathe Fire (replace block or block part of blitz, once pr. activation)

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val allActions = SelectPlayerAction(actions = rules.getAvailableActions(state, state.activePlayer!!))
            val availableActions: SelectPlayerAction = allActions.actions
                // Compulsory actions take precedence over everything else
                .firstOrNull { it.compulsory }?.let { SelectPlayerAction(it) }
                ?: allActions
            val deselectPlayer = DeselectPlayer(state.activePlayer!!)
            return listOf(deselectPlayer, availableActions)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val activePlayer = state.activePlayer!!
            return when (action) {
                is PlayerDeselected ->
                    compositeCommandOf(
                        SetPlayerAvailability(activePlayer, Availability.AVAILABLE),
                        SetActivePlayer(null),
                        ExitProcedure(),
                    )
                is PlayerActionSelected -> {
                    val selectedAction = rules.teamActions[action.action]
                    val hasNegaTrait = hasNegaTrait(activePlayer)
                    compositeCommandOf(
                        SetContext(state.getContext<ActivatePlayerContext>().copy(declaredAction = selectedAction)),
                        ReportActionSelected(activePlayer, selectedAction),
                        if (hasNegaTrait) {
                            GotoNode(CheckForBoneHead)
                        } else {
                            GotoNode(CheckForOpponentInterruptSkills)
                        }
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object CheckForBoneHead: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return if (state.activePlayer!!.hasSkill<BoneHead>()) {
                GotoNode(ResolveBoneHead)
            } else {
                GotoNode(CheckForReallyStupid)
            }
        }
    }

    object ResolveBoneHead: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BoneHeadRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ActivatePlayerContext>()
            return if (context.activationEndsImmediately) {
                compositeCommandOf(
                    SetPlayerAvailability(state.activePlayer!!, Availability.HAS_ACTIVATED),
                    ExitProcedure()
                )
            } else {
                GotoNode(CheckForReallyStupid)
            }
        }
    }

    object CheckForReallyStupid: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return if (state.activePlayer!!.hasSkill<ReallyStupid>()) {
                GotoNode(ResolveReallyStupid)
            } else {
                GotoNode(CheckForUnchannelledFury)
            }
        }
    }

    object ResolveReallyStupid: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ReallyStupidRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ActivatePlayerContext>()
            return if (context.activationEndsImmediately) {
                compositeCommandOf(
                    SetPlayerAvailability(state.activePlayer!!, Availability.HAS_ACTIVATED),
                    ExitProcedure()
                )
            } else {
                GotoNode(CheckForUnchannelledFury)
            }
        }
    }

    object CheckForUnchannelledFury: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return if (state.activePlayer!!.hasSkill<UnchannelledFury>()) {
                GotoNode(ResolveUnchannelledFury)
            } else {
                GotoNode(CheckForBloodLust)
            }
        }
    }

    object ResolveUnchannelledFury: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = UnchannelledFuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ActivatePlayerContext>()
            return if (context.activationEndsImmediately) {
                compositeCommandOf(
                    SetPlayerAvailability(state.activePlayer!!, Availability.HAS_ACTIVATED),
                    ExitProcedure()
                )
            } else {
                GotoNode(CheckForAnimalSavagery)
            }
        }
    }

    object CheckForAnimalSavagery: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return if (state.activePlayer!!.hasSkill<AnimalSavagery>()) {
                GotoNode(ResolveAnimalSavagery)
            } else {
                GotoNode(CheckForBloodLust)
            }
        }
    }


    object ResolveAnimalSavagery: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = AnimalSavageryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ActivatePlayerContext>()
            return if (context.activationEndsImmediately) {
                compositeCommandOf(
                    SetPlayerAvailability(state.activePlayer!!, Availability.HAS_ACTIVATED),
                    ExitProcedure()
                )
            } else {
                GotoNode(CheckForBloodLust)
            }
        }
    }

    object CheckForBloodLust: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return if (state.activePlayer!!.hasSkill<BloodLust>()) {
                GotoNode(ResolveBloodLust)
            } else {
                GotoNode(CheckForOpponentInterruptSkills)
            }
        }
    }

    object ResolveBloodLust: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BloodLustRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Blood Lust does not cause the activation to end, it only goes into affect
            // at the end of the activation
            return GotoNode(CheckForOpponentInterruptSkills)
        }
    }

    /**
     * Some skills trigger when an opponent player are about to start their action,
     * like Dump-off and Foul Appearance. This step checks for these cases
     */
    object CheckForOpponentInterruptSkills: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = CheckForActionInterruptSkills
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ActivatePlayerContext>()
            return if (context.activationEndsImmediately) {
                ExitProcedure()
            } else {
                GotoNode(ResolveSelectedAction)
            }
        }
    }

    object ResolveSelectedAction : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return state.getContext<ActivatePlayerContext>().declaredAction!!.procedure
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ActivatePlayerContext>()
            return if (context.player.hasTemporaryEffect(TemporaryEffectType.BLOOD_LUST)) {
                GotoNode(ResolveBloodLustAtEndOfActivation)
            } else {
                return compositeCommandOf(
                    SetPlayerAvailability(
                        player = state.activePlayer!!,
                        availability = if (context.markActionAsUsed || context.activationEndsImmediately) {
                            Availability.HAS_ACTIVATED
                        } else {
                            Availability.AVAILABLE
                        }),
                    ExitProcedure()
                )
            }
        }
    }

    object ResolveBloodLustAtEndOfActivation: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BiteThrall
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetPlayerAvailability(state.activePlayer!!, Availability.HAS_ACTIVATED),
                ExitProcedure()
            )
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // HELPER FUNCTIONS

    /**
     * Rather than figuring out every permutation of nega-traits, we just check
     * if the player has _any_ and if so, we go through checking for all of them.
     * If not, we can just skip that entire chain.
     */
    private fun hasNegaTrait(player: Player): Boolean {
        return player.skills.any { skill ->
            when (skill) {
                is AnimalSavagery,
                is BoneHead,
                is BloodLust,
                is ReallyStupid,
                is UnchannelledFury -> true
                else -> false
            }
        }
    }
}
