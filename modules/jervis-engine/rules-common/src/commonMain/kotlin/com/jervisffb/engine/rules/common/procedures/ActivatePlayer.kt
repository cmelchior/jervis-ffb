package com.jervisffb.engine.rules.common.procedures

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
import com.jervisffb.engine.commands.context.UpdateContext
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
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.PlayerStatusEffectType
import com.jervisffb.engine.reports.ReportActionEnded
import com.jervisffb.engine.reports.ReportActionSelected
import com.jervisffb.engine.reports.ReportFailedBoneHead
import com.jervisffb.engine.reports.ReportFailedReallyStupid
import com.jervisffb.engine.reports.ReportFailedUnchannelledFury
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION

/**
 * Procedure for activating a player and declaring their action as described on
 * page 42 in the rulebook.
 *
 * The exact sequence for activating a player is not really clear in the rules,
 * but is clarified in this Developer's Commentary (May 2026).
 *
 * It still lacks some minor details though, so some freedom has been taken
 * in determining the sequence. In Jervis, the sequence is as follows:
 *
 * 1. Select Player (which triggers this procedure).
 * 2. Mark the player as Active
 * 3. Remove any negative effects that last until the next activation (like
 *    Distracted).
 * 4. Declare an Action to perform. For some actions, like Blitz or Foul, this
 *    includes selecting a target.
 * 5. Roll for all Nega-traits in order, stop at the first failure (no player
 *    normally has multiple of these).
 *    a. Take Root: They might become rooted, but are otherwise free to act.
 *    a. Bone Head / Really Stupid: They might end the activation here, loose
 *       tackle zones and the action is used.
 *    b. Unchannelled Fury: They might end the activation here and the action is
 *       used.
 *    c. Animal Savagery: They might hit a nearby player or end their activation
 *       and loose tackle zones.
 *    d. Blood Lust: If failed, they might change the declared action to Move.
 * 6. If action has a target, roll for all opponent skills like Foul Appearance.
 * 7. Perform the action.
 * 8. End Action.
 * 9. End Activation.
 *
 * We allow a player to take back selecting an action as long as no side effects
 * have occurred. I.e., a Player is allowed to regret selecting an action as
 * long as no dice has been rolled, no moves have been taken, or no negative
 * state has been removed as part of Step 3.
 */
object ActivatePlayer : Procedure() {
    override val initialNode: Node = MarkPlayerAsActive
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<ActivatePlayerContext>()
        val player = context.player
        return buildCompositeCommand {
            add(SetActivePlayer(null))
            addAll(
                *getResetPlayerTemporaryModifiersCommands(state, rules, player, Duration.END_OF_ACTIVATION)
            )

            // If the action was considered "used", we should remove it from the pool
            // of available actions. If an action was ended prematurely (either due
            // to a turnover or failing some roll), the action is always considered used.
            if (actionCountAsUsed(context)) {
                val activeTeam = state.activeTeamOrThrow()
                val declaredAction = context.declaredAction?.type ?: error("Missing declared action: $context")
                val markActionAsUsedCommand = when (declaredAction) {
                    PlayerStandardActionType.MOVE -> SetAvailableActions.markAsUsed(activeTeam, PlayerStandardActionType.MOVE)
                    PlayerStandardActionType.PASS -> SetAvailableActions.markAsUsed(activeTeam, PlayerStandardActionType.PASS)
                    PlayerStandardActionType.HAND_OFF -> SetAvailableActions.markAsUsed(activeTeam, PlayerStandardActionType.HAND_OFF)
                    PlayerStandardActionType.BLOCK -> SetAvailableActions.markAsUsed(activeTeam, PlayerStandardActionType.BLOCK)
                    PlayerStandardActionType.BLITZ -> SetAvailableActions.markAsUsed(activeTeam, PlayerStandardActionType.BLITZ)
                    PlayerStandardActionType.FOUL -> SetAvailableActions.markAsUsed(activeTeam, PlayerStandardActionType.FOUL)
                    PlayerStandardActionType.SECURE_THE_BALL -> SetAvailableActions.markAsUsed(activeTeam, PlayerStandardActionType.SECURE_THE_BALL)
                    PlayerStandardActionType.THROW_TEAM_MATE -> SetAvailableActions.markAsUsed(activeTeam, PlayerStandardActionType.THROW_TEAM_MATE)
                    PlayerStandardActionType.SPECIAL -> null
                    PlayerSpecialActionType.BALL_AND_CHAIN -> TODO()
                    PlayerSpecialActionType.BOMBARDIER -> TODO()
                    PlayerSpecialActionType.CHAINSAW -> {
                        compositeCommandOf(
                            SetSpecialActionSkillUsed(player, player.getSkill(SkillType.CHAINSAW), true),
                            SetAvailableSpecialActions.markAsUsed(activeTeam, PlayerSpecialActionType.CHAINSAW)
                        )
                    }
                    PlayerSpecialActionType.HYPNOTIC_GAZE -> {
                        compositeCommandOf(
                            SetSpecialActionSkillUsed(player, player.getSkill(SkillType.HYPNOTIC_GAZE), true),
                            SetAvailableSpecialActions.markAsUsed(activeTeam, PlayerSpecialActionType.HYPNOTIC_GAZE)
                        )
                    }
                    PlayerSpecialActionType.KICK_TEAM_MATE -> TODO()
                    PlayerSpecialActionType.MULTIPLE_BLOCK -> {
                        compositeCommandOf(
                            SetSpecialActionSkillUsed(player, player.getSkill(SkillType.MULTIPLE_BLOCK), true),
                            SetAvailableSpecialActions.markAsUsed(activeTeam, PlayerSpecialActionType.MULTIPLE_BLOCK)
                        )
                    }
                    PlayerSpecialActionType.BREATHE_FIRE -> {
                        compositeCommandOf(
                            SetSpecialActionSkillUsed(player, player.getSkill(SkillType.BREATHE_FIRE), true),
                            SetAvailableSpecialActions.markAsUsed(activeTeam, PlayerSpecialActionType.BREATHE_FIRE)
                        )
                    }
                    PlayerSpecialActionType.CHOMP -> {
                        compositeCommandOf(
                            SetSpecialActionSkillUsed(player, player.getSkill(SkillType.MONSTROUS_MOUTH), true),
                            SetAvailableSpecialActions.markAsUsed(activeTeam, PlayerSpecialActionType.CHOMP)
                        )
                    }
                    PlayerSpecialActionType.PROJECTILE_VOMIT -> {
                        compositeCommandOf(
                            SetSpecialActionSkillUsed(player, player.getSkill(SkillType.PROJECTILE_VOMIT), true),
                            SetAvailableSpecialActions.markAsUsed(activeTeam, PlayerSpecialActionType.PROJECTILE_VOMIT)
                        )
                    }
                    PlayerSpecialActionType.STAB -> {
                        compositeCommandOf(
                            SetSpecialActionSkillUsed(player, player.getSkill(SkillType.STAB), true),
                            SetAvailableSpecialActions.markAsUsed(activeTeam, PlayerSpecialActionType.STAB)
                        )
                    }
                    PlayerSpecialActionType.PUNT -> {
                        compositeCommandOf(
                            SetSpecialActionSkillUsed(player, player.getSkill(SkillType.PUNT), true),
                            SetAvailableSpecialActions.markAsUsed(activeTeam, PlayerSpecialActionType.PUNT)
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
            if (player.available == Availability.IS_ACTIVE && !actionCountAsUsed(context)) {
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
                player.state == PlayerPitchState.STANDING
                && !player.hasTackleZones
            ) {
                compositeCommandOf(
                    SetHasTackleZones(player, true),
                    UpdateContext(context.copy(clearedNegativeEffects = true))
                )
            } else {
                null
            }

            return compositeCommandOf(
                *getResetPlayerTemporaryModifiersCommands(state, rules, player, Duration.START_OF_ACTIVATION),
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
//         Punt (Its own action)

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
                    val activePlayerContext = state.getContext<ActivatePlayerContext>()
                    compositeCommandOf(
                        UpdateContext(activePlayerContext.copy(declaredAction = selectedAction)),
                        ReportActionSelected(activePlayer, selectedAction),
                        if (hasNegaTrait) {
                            GotoNode(CheckForTakeRoot)
                        } else {
                            GotoNode(CheckForAbortingAction)
                        }
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object CheckForTakeRoot: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val player = state.activePlayer ?: error("Missing active player")
            val hasTakeRoot = player.isSkillAvailable(SkillType.TAKE_ROOT)
            val isStanding = rules.isStanding(player)
            val isAlreadyRooted = player.hasStatusEffect(PlayerStatusEffectType.ROOTED)
            return when (hasTakeRoot && isStanding && !isAlreadyRooted) {
                true -> null
                false -> CheckForBoneHead
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = TakeRootRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ActivatePlayerContext>()
            val player = context.player
            return if (context.activationEndsImmediately) {
                compositeCommandOf(
                    SetPlayerAvailability(player, Availability.HAS_ACTIVATED),
                    GotoNode(CheckForBoneHead),
                )
            } else {
                GotoNode(CheckForBoneHead)
            }
        }
    }

    object CheckForBoneHead: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            return when (state.activePlayer!!.isSkillAvailable(SkillType.BONE_HEAD)) {
                true -> null
                false -> CheckForReallyStupid
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BoneHeadRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ActivatePlayerContext>()
            val player = context.player
            return if (context.activationEndsImmediately) {
                compositeCommandOf(
                    SetPlayerAvailability(player, Availability.HAS_ACTIVATED),
                    ReportFailedBoneHead(player),
                    ExitProcedure()
                )
            } else {
                GotoNode(CheckForReallyStupid)
            }
        }
    }

    object CheckForReallyStupid: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            return when (state.activePlayer!!.isSkillAvailable(SkillType.REALLY_STUPID)) {
                true -> null
                false -> CheckForUnchannelledFury
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ReallyStupidRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ActivatePlayerContext>()
            val player = context.player
            return if (context.activationEndsImmediately) {
                compositeCommandOf(
                    SetPlayerAvailability(player, Availability.HAS_ACTIVATED),
                    ReportFailedReallyStupid(player),
                    ExitProcedure()
                )
            } else {
                GotoNode(CheckForUnchannelledFury)
            }
        }
    }

    object CheckForUnchannelledFury: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            return when (state.activePlayer!!.isSkillAvailable(SkillType.UNCHANNELLED_FURY)) {
                true -> null
                false -> CheckForAnimalSavagery
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = UnchannelledFuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ActivatePlayerContext>()
            return if (context.activationEndsImmediately) {
                val player = context.player
                compositeCommandOf(
                    SetPlayerAvailability(player, Availability.HAS_ACTIVATED),
                    ReportFailedUnchannelledFury(player),
                    ExitProcedure()
                )
            } else {
                GotoNode(CheckForAnimalSavagery)
            }
        }
    }

    object CheckForAnimalSavagery: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            return when (state.activePlayer!!.isSkillAvailable(SkillType.ANIMAL_SAVAGERY)) {
                true -> null
                false -> CheckForBloodLust
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = AnimalSavageryStep
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

    object CheckForBloodLust: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            return when (state.activePlayer!!.isSkillAvailable(SkillType.BLOOD_LUST)) {
                true -> null
                false -> CheckForAbortingAction
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BloodLustRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Blood Lust does not cause the activation to end, it only goes into affect
            // at the end of the activation
            return GotoNode(CheckForAbortingAction)
        }
    }

    object CheckForAbortingAction: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<ActivatePlayerContext>()
            return if (context.activationEndsImmediately || state.isTurnOver()) {
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
            return if (context.player.hasStatusEffect(PlayerStatusEffectType.BLOOD_LUST)) {
                GotoNode(ResolveBloodLustAtEndOfActivation)
            } else {
                compositeCommandOf(
                    SetPlayerAvailability(
                        player = state.activePlayer!!,
                        availability = if (actionCountAsUsed(context)) {
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
            when (skill.type) {
                SkillType.ANIMAL_SAVAGERY,
                SkillType.BONE_HEAD,
                SkillType.BLOOD_LUST,
                SkillType.REALLY_STUPID,
                SkillType.TAKE_ROOT,
                SkillType.UNCHANNELLED_FURY -> true
                else -> false
            }
        }
    }

    /**
     * An action can end in a lot of ways, but basically any side-effect should
     * count the action as "used".
     */
    fun actionCountAsUsed(context: ActivatePlayerContext): Boolean {
        return context.declaredAction != null
            && (context.rolledForNegaTrait
                || context.clearedNegativeEffects
                || context.markActionAsUsed
            )
    }
}
