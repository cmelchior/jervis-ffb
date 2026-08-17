package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.DicePoolChoice
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.PlayersSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectBlockType
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectPitchLocation
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectPlayers
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.bb2020.procedures.actions.block.BB2020PushStepInitialMoveSequence
import com.jervisffb.engine.bb2020.procedures.actions.block.BB2020Stumble
import com.jervisffb.engine.bb2020.procedures.actions.block.BlockAction
import com.jervisffb.engine.bb2020.procedures.blitz.BlitzAction
import com.jervisffb.engine.bb2025.procedures.actions.block.BB2025Stumble
import com.jervisffb.engine.bb2025.procedures.actions.block.push.CreatePushChainStep
import com.jervisffb.engine.bb2025.procedures.actions.block.push.FollowUpStep
import com.jervisffb.engine.bb2025.procedures.actions.block.push.UseStripBallStep
import com.jervisffb.engine.bb2025.procedures.actions.block.singleblock.SingleStandardBlockChooseResult
import com.jervisffb.engine.bb2025.procedures.actions.move.JumpStep
import com.jervisffb.engine.bb2025.procedures.actions.move.LeapRoll
import com.jervisffb.engine.bb2025.procedures.actions.move.LeapStep
import com.jervisffb.engine.bb2025.procedures.actions.pass.PassAccuracyRoll
import com.jervisffb.engine.bb2025.procedures.actions.pass.PassStep
import com.jervisffb.engine.bb2025.procedures.actions.securetheball.SecureTheBallStep
import com.jervisffb.engine.bb2025.procedures.actions.throwteammate.ThrowPlayerStep
import com.jervisffb.engine.bb2025.procedures.actions.throwteammate.ThrowTeammateAccuracyRoll
import com.jervisffb.engine.bb2025.procedures.injury.BB2025FallingOver
import com.jervisffb.engine.bb2025.procedures.injury.BB2025KnockedDown
import com.jervisffb.engine.bb2025.procedures.rerolls.TeamCaptainRoll
import com.jervisffb.engine.bb2025.procedures.rerolls.UseBrawlerReroll
import com.jervisffb.engine.bb2025.skills.SafePairOfHandsStep
import com.jervisffb.engine.bb2025.skills.SureHands
import com.jervisffb.engine.common.actions.createRandomAction
import com.jervisffb.engine.common.context.PassContext
import com.jervisffb.engine.common.context.RiskingInjuryContext
import com.jervisffb.engine.common.context.ThrowTeamMateContext
import com.jervisffb.engine.common.procedures.Catch
import com.jervisffb.engine.common.procedures.CatchRoll
import com.jervisffb.engine.common.procedures.DealWithSecretWeaponsStep
import com.jervisffb.engine.common.procedures.Pickup
import com.jervisffb.engine.common.procedures.PickupRoll
import com.jervisffb.engine.common.procedures.RecoverKnockedOutPlayersStep
import com.jervisffb.engine.common.procedures.ResolveBallLandingOnPitch
import com.jervisffb.engine.common.procedures.TheKickOff
import com.jervisffb.engine.common.procedures.actions.foul.FoulStep
import com.jervisffb.engine.common.procedures.actions.move.DodgeRoll
import com.jervisffb.engine.common.procedures.actions.move.JumpRoll
import com.jervisffb.engine.common.procedures.actions.move.ScoringATouchdown
import com.jervisffb.engine.common.procedures.rerolls.LonerRoll
import com.jervisffb.engine.common.procedures.rerolls.ProRoll
import com.jervisffb.engine.common.procedures.tables.injury.ArmourRoll
import com.jervisffb.engine.common.procedures.tables.injury.InjuryRoll
import com.jervisffb.engine.ext.dicePoolId
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseResult
import com.jervisffb.engine.rules.common.rerolls.TeamReroll
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.Range
import com.jervisffb.engine.utils.containsActionWithRandomBehavior
import com.jervisffb.engine.utils.doDivingTackleHaveAnAffect
import com.jervisffb.ui.game.viewmodel.Feature
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import kotlin.collections.contains

/**
 * Class responsible for creating an automated action if the UI settings
 * allow it.
 *
 * Most of these are controlled through the "Auto Actions" pane in Game
 * Settings, but some are just optimizations bridging how the Rules Engine
 * exposes its flow vs. what makes an optimal user experience in the UI.
 *
 * This class should generally only be used when a human is in the loop for
 * generating actions, e.g., through [ManualActionProvider].
 */
class AutomatedActionsFactory(
    private val controller: GameEngineController,
    private val menuViewModel: MenuViewModel,
    private val gameSettings: GameSettings
) {

    private val game = controller.state

    fun createAction(actions: ActionRequest): GameAction? {

        // First, we check if we are playing Hotseat and the game is set to roll random
        // actions on the "server". In this case, they are generated here as no server exists.
        if (!gameSettings.clientSelectedDiceRolls && gameSettings.isHotseatGame && actions.containsActionWithRandomBehavior()) {
            return createRandomAction(controller)
        }

        val currentNode = controller.currentProcedure()?.currentNode()

        // We only have "Confirm" actions on Touchdowns to trigger animations (for now)
        // So we always provide it.
        if (currentNode == ScoringATouchdown.InformOfTouchdown) {
            return Confirm
        }

        // Do not reroll successful rolls that are considered "successful"
        // If we are in the middle of a Dodge, Jump or Leap, we will assume that Diving Tackle
        // will be used right after, i.e. you might want to reroll something that rolls equal to
        // agility, which is normally a success.
        if (menuViewModel.isFeatureEnabled(Feature.DO_NOT_REROLL_SUCCESSFUL_ACTIONS)) {
            if (actions.filterIsInstance<SelectNoReroll>().count { it.rollSuccessful == true} > 0) {
                // Check if Diving Tackle can be used right after the reroll, in that case, we should
                // not automate this action
                val currentProcedure = controller.currentProcedure()?.procedure
                val automateAction = when (currentProcedure != null && (currentProcedure == DodgeRoll || currentProcedure == JumpRoll || currentProcedure == LeapRoll)) {
                    true ->!doDivingTackleHaveAnAffect(controller.state)
                    false -> true
                }
                if (automateAction) {
                    return NoRerollSelected()
                }
            }
        }

        // Randomly select a kicking player
        if (currentNode == TheKickOff.NominateKickingPlayer && menuViewModel.isFeatureEnabled(
                Feature.SELECT_KICKING_PLAYER
            )) {
            return (currentNode as ActionNode).getAvailableActions(controller.state, controller.rules)
                .filterIsInstance<SelectPlayer>()
                .single()
                .getPlayers(game).let {
                    val playersWithKick = it.filter { it.isSkillAvailable(SkillType.KICK) }
                    if (playersWithKick.isNotEmpty()) {
                        PlayerSelected(playersWithKick.first())
                    } else {
                        PlayerSelected(it.random())
                    }
                }
        }

        // If a player action can only end, just end it immediately. Standard movement is
        // special as some challenge rules might prevent certain kinds of movement. For this
        // reason we need to go through the Action Planner to check if MoveType.STANDARD is
        // just a false positive here.
        if (
            menuViewModel.isFeatureEnabled(Feature.END_PLAYER_ACTION_IF_ONLY_OPTION)
            && actions.all { it is EndActionWhenReady || it is SelectMoveType }
            && actions.none { it is SelectMoveType && hasExecutableMove(it) }
        ) {
            return EndAction
        }

        // Automatically select pushback direction when only one option is available.
        if (actions.size == 1 && actions.first() is SelectPitchLocation && actions.first().createAll().size == 1 && currentNode is CreatePushChainStep.SelectPushDirection) {
            val loc = (actions.first() as SelectPitchLocation).squares.first()
            return PitchSquareSelected(loc.coordinate)
        }
        if (actions.size == 1 && actions.first() is SelectPitchLocation && actions.first().createAll().size == 1 && currentNode is BB2020PushStepInitialMoveSequence.SelectPushDirection) {
            val loc = (actions.first() as SelectPitchLocation).squares.first()
            return PitchSquareSelected(loc.coordinate)
        }

        // When selecting block results after reroll and only 1 dice is available.
        if (
            menuViewModel.isFeatureEnabled(Feature.SELECT_BLOCK_DIE_IF_ONLY_OPTION)
            && (currentNode == StandardBlockChooseResult.SelectBlockResult || currentNode == SingleStandardBlockChooseResult.SelectBlockResult)
            && actions.size == 1
        ) {
            val choices = (actions.first() as SelectDicePoolResult).pools
            if (choices.size == 1 && choices.first().dice.size == 1) {
                return DicePoolResultsSelected(listOf(
                    DicePoolChoice(id = 0.dicePoolId, listOf(choices.first().dice.single().let { DicePoolChoice.SelectedDiceRoll(it.id, it.result) }))
                ))
            }
        }

        // When there is only one block type for a block, just select that one straight away
        if (
            menuViewModel.isFeatureEnabled(Feature.SELECT_BLOCK_TYPE_IF_ONLY_OPTION)
            && (currentNode in setOf(
                BlockAction.SelectBlockType,
                com.jervisffb.engine.bb2025.procedures.actions.block.BlockAction.SelectBlockType,
                BlitzAction.SelectBlockType,
                com.jervisffb.engine.bb2025.procedures.actions.blitz.BlitzAction.SelectBlockType,
            ))
        ) {
            actions.filterIsInstance<SelectBlockType>().firstOrNull()?.let {
                if (it.size == 1) {
                    return BlockTypeSelected(it.types.first())
                }
            }
        }

        // Automatically select "Push into the crowd" when it is the only option
        if (
            menuViewModel.isFeatureEnabled(Feature.PUSH_PLAYER_INTO_CROWD)
            && actions.size == 1
            && actions.first().let {
                if (it is SelectDirection) {
                    it.directions.all { direction ->
                        val target = it.origin.move(direction, 1)
                        target.isOutOfBounds(controller.rules)
                    }
                } else {
                    false
                }
            }
        ) {
            return DirectionSelected((actions.first() as SelectDirection).directions.first())
        }

        // Automatically decide to follow op (or not), if you there really isn't a choice in the matter
        if (currentNode is FollowUpStep.ChooseToFollowUp && actions.size == 1) {
            return when (val action = actions.first()) {
                is ConfirmWhenReady -> Confirm
                is CancelWhenReady -> Cancel
                else -> error("Unexpected action: $action")
            }
        }
        if (currentNode is BB2020PushStepInitialMoveSequence.DecideToFollowUp && actions.size == 1) {
            return when (val action = actions.first()) {
                is ConfirmWhenReady -> Confirm
                is CancelWhenReady -> Cancel
                else -> error("Unexpected action: $action")
            }
        }

        // Whether to use the Big Hand skill or not
        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_BIG_HAND) && (currentNode == Pickup.ChooseToUseBigHand || currentNode == SecureTheBallStep.ChooseToUseBigHand)) {
            if (controller.state.activePlayer?.isSkillAvailable(SkillType.BIG_HAND) == true) {
                return Confirm
            }
        }

        // Whether to use the Very Long Legs skill or not
        if (
            menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_VERY_LONG_LEGS)
            && (currentNode == JumpStep.ChooseToUseVeryLongLegs || currentNode == com.jervisffb.engine.bb2020.procedures.actions.move.JumpStep.ChooseToUseVeryLongLegs)) {
            if (controller.state.getContextOrNull<MoveContext>()?.player?.isSkillAvailable(SkillType.VERY_LONG_LEGS) == true) {
                return Confirm
            }
        }

        // Use Dirty Player on Armour roll if it wasn't broken using the normal role
        if (menuViewModel.isFeatureEnabled(Feature.USE_DIRTY_PLAYER_ON_ARMOUR) && (currentNode == ArmourRoll.ChooseToUseDirtyPlayer)) {
            val context = controller.state.getContextOrNull<RiskingInjuryContext>()
            if (context?.armourBroken != true) {
                return Confirm
            } else {
                return Cancel
            }
        }

        // Always use Dirty Player on Injury (if possible)
        if (menuViewModel.isFeatureEnabled(Feature.USE_DIRTY_PLAYER_ON_INJURY) && (currentNode == InjuryRoll.ChooseToUseDirtyPlayer)) {
            return Confirm
        }

        // When rerolling Catch rolls, prefer to use the free reroll from the Catch skill
        if (menuViewModel.isFeatureEnabled(Feature.USE_CATCH_SKILL_REROLL) && (currentNode == CatchRoll.ChooseReRollSource)) {
            val availableRerollOptions = actions.getOrNull<SelectRerollOption>()
            availableRerollOptions?.options?.firstOrNull {
                val source = it.getRerollSource(controller.state)
                (source is Skill<*> && source.type == SkillType.CATCH)
            }?.let {
                return RerollOptionSelected(it, availableRerollOptions.dicePoolId)
            }
        }

        if (menuViewModel.isFeatureEnabled(Feature.USE_PASS_SKILL_REROLL) && (currentNode == PassAccuracyRoll.ChooseReRollSource)) {
            val availableRerollOptions = actions.getOrNull<SelectRerollOption>()
            availableRerollOptions?.options?.firstOrNull {
                val source = it.getRerollSource(controller.state)
                (source is Skill<*> && source.type == SkillType.PASS)
            }?.let {
                return RerollOptionSelected(it, availableRerollOptions.dicePoolId)
            }
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_SAFE_PASS) && (currentNode == PassStep.ChooseToUseSafePass)) {
            val context = controller.state.getContext<PassContext>()
            if (context.thrower.isSkillAvailable(SkillType.SAFE_PASS)) {
                return Confirm
            }
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_TACKLE_ON_DODGE) && (currentNode == DodgeRoll.ChooseToUseTackle)) {
            // Always select the first available player to use Tackle
            val selectedPlayer = actions.get<SelectPlayer>().players.first()
            return PlayerSelected(selectedPlayer)
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_TWO_HEADS) && currentNode == DodgeRoll.ChooseToUseTwoHeads) {
            return Confirm
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_TACKLE_ON_STUMBLE) && (currentNode == BB2020Stumble.ChooseToUseTackle)) {
            return Confirm
        }
        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_TACKLE_ON_STUMBLE) && (currentNode == BB2025Stumble.ChooseToUseTackle)) {
            return Confirm
        }

        if (menuViewModel.isFeatureEnabled(Feature.USE_SURE_HANDS_REROLL) && (currentNode == PickupRoll.ChooseReRollSource)) {
            val rerollOptions = actions.getOrNull<SelectRerollOption>()
            rerollOptions?.options?.firstOrNull { it.getRerollSource(controller.state) is SureHands }?.let {
                return RerollOptionSelected(it, rerollOptions.dicePoolId)
            }
        }

        if (menuViewModel.isFeatureEnabled(Feature.USE_SURE_HANDS_ON_STRIP_BALL) && (currentNode == UseStripBallStep.ChooseToUseSureHands)) {
            return Confirm
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_THICK_SKULL) && currentNode == InjuryRoll.ChooseToUseThickSkull) {
            return Confirm
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_LEAP_MODIFIER) && currentNode == LeapStep.ChooseToUseLeapModifier) {
            return Confirm
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_SIDESTEP) && currentNode == CreatePushChainStep.DecideToUseSidestep) {
            return Confirm
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_GRAB) && currentNode == CreatePushChainStep.DecideToUseGrab) {
            return Confirm
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_STAND_FIRM) && currentNode == CreatePushChainStep.DecideToUseStandFirm) {
            return Confirm
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_STEADY_FOOTING)
            && (
                currentNode == BB2025FallingOver.ChooseToUseSteadyFooting
                    || currentNode == BB2025KnockedDown.ChooseToUseSteadyFooting
            )
        ) {
            return Confirm
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_STRIP_BALL) && currentNode == UseStripBallStep.ChooseToUseStripBall) {
            return Confirm
        }

        // Use Mighty Blow if Armour isn't already broken
        if (menuViewModel.isFeatureEnabled(Feature.USE_MIGHTY_BLOW_ON_ARMOUR) && (currentNode == ArmourRoll.ChooseToUseMightyBlow)) {
            val context = controller.state.getContextOrNull<RiskingInjuryContext>()
            if (context?.armourBroken != true) {
                return Confirm
            } else {
                return Cancel
            }
        }

        // Always use Mighty Blow on Injury (if possible)
        if (menuViewModel.isFeatureEnabled(Feature.USE_MIGHTY_BLOW_ON_INJURY) && (currentNode == InjuryRoll.ChooseToUseMightyBlow)) {
            return Confirm
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_STRONG_ARM) && (currentNode == ThrowTeammateAccuracyRoll.ChooseToUseStrongArm)) {
            return Confirm
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_SNEAKY_GIT) && (currentNode == FoulStep.ChooseToUseSneakyGit)) {
            return Confirm
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_EYE_GOUGE) && (currentNode == CreatePushChainStep.ChooseToUseEyeGouge)) {
            return Confirm
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_SAFE_PAIR_OF_HANDS) && (currentNode == SafePairOfHandsStep.ChooseToUseSafePairOfHands)) {
            return Confirm
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_DIVING_CATCH_ON_TARGET) && currentNode == Catch.ChooseToUseDivingCatch) {
            return Confirm
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_DIVING_CATCH_ON_ADJACENT)
            && (currentNode == ResolveBallLandingOnPitch.InactiveTeamChoosesDivingCatchPlayers
                || currentNode == ResolveBallLandingOnPitch.ActiveTeamChoosesDivingCatchPlayers)
        ) {
            // Return all available players
            actions.getOrNull<SelectPlayers>()?.let {
                return PlayersSelected(it.players)
            }
        }

        if (menuViewModel.isFeatureEnabled(Feature.IGNORE_DIVING_TACKLE_IF_NO_EFFECT)
            && (currentNode == DodgeRoll.ChooseToUseDivingTackleAfterReRoll || currentNode == JumpRoll.ChooseToUseDivingTackleAfterReRoll || currentNode == LeapRoll.ChooseToUseDivingTackleAfterReRoll)
            && !doDivingTackleHaveAnAffect(controller.state)
        ) {
            return Cancel
        }

        // Use Lethal Flight if Armour isn't already broken
        if (menuViewModel.isFeatureEnabled(Feature.USE_LETHAL_FLIGHT_ON_ARMOUR) && (currentNode == ArmourRoll.ChooseToUseLethalFlight)) {
            val context = controller.state.getContextOrNull<RiskingInjuryContext>()
            if (context?.armourBroken != true) {
                return Confirm
            } else {
                return Cancel
            }
        }

        // Always use Lethal Flight on Injury (if possible)
        if (menuViewModel.isFeatureEnabled(Feature.USE_LETHAL_FLIGHT_ON_INJURY) && (currentNode == InjuryRoll.ChooseToUseLethalFlight)) {
            return Confirm
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_BULLSEYE) && (currentNode == ThrowPlayerStep.ChooseToUseBullseye)) {
            // If the throw is at the limit of the thrown range, the Coach should choose (as they might want to scatter beyond it)
            // Otherwise we can automatically apply Bullseye.
            val context = controller.state.getContext<ThrowTeamMateContext>()
            val target = context.target ?: PitchCoordinate.UNKNOWN
            val rules = controller.rules
            val range = rules.rangeRuler.measure(context.thrower, target)
            if (range == Range.OUT_OF_RANGE) return Confirm
            val atEdgeOfAllowedRange = target.getSurroundingCoordinates(rules, distance = 1, includeOutOfBounds = false)
                .any { neighbor ->
                    val neighborRange = rules.rangeRuler.measure(context.thrower, neighbor)
                    neighborRange == Range.LONG_PASS
                }
            if (!atEdgeOfAllowedRange) return Confirm
        }

        fun isOnlyTeamRerolls(availableActions: ActionRequest): Boolean {
            return availableActions.getOrNull<SelectRerollOption>()?.let {
                it.options.all { option -> option.getRerollSource(controller.state) is TeamReroll }
            } ?: false
        }

        if (!menuViewModel.isFeatureEnabled(Feature.ACCEPT_PRO_ROLL) && currentNode == ProRoll.ChooseReRollSource) {
            if (isOnlyTeamRerolls(actions)) {
                return NoRerollSelected()
            }
        }

        if (!menuViewModel.isFeatureEnabled(Feature.ACCEPT_LONER_ROLL) && currentNode == LonerRoll.ChooseReRollSource) {
            if (isOnlyTeamRerolls(actions)) {
                return NoRerollSelected()
            }
        }

        if (!menuViewModel.isFeatureEnabled(Feature.ACCEPT_TEAM_CAPTAIN_ROLL) && currentNode == TeamCaptainRoll.ChooseReRollSource) {
            if (isOnlyTeamRerolls(actions)) {
                return NoRerollSelected()
            }
        }

        if (menuViewModel.isFeatureEnabled(Feature.USE_ALL_OFFENSIVE_FOUL_ASSISTS) && currentNode == FoulStep.SelectOffensiveAssists) {
            actions.getOrNull<SelectPlayers>()?.let {
                return PlayersSelected(it.players)
            }
        }

        if (
            currentNode == DealWithSecretWeaponsStep.KickingTeamSelectPlayerToSendOff
            || currentNode == DealWithSecretWeaponsStep.ReceivingTeamSelectPlayerToSendOff
        ) {
            actions.filterIsInstance<SelectPlayer>().singleOrNull()?.let {
                if (it.players.size == 1) {
                    return PlayerSelected(it.players.single())
                }
            }
        }

        // Select the next player we want to roll Recover for between drives.
        // This is done automatically as there is no known scenario where choosing
        // makes sense.
        if (
            currentNode == RecoverKnockedOutPlayersStep.ReceivingTeamSelectPlayerToRecover
            || currentNode == RecoverKnockedOutPlayersStep.KickingTeamSelectPlayerToRecover
        ) {
            actions.getOrNull<SelectPlayer>()?.let {
                return PlayerSelected(it.players.first())
            }
        }

        // If a player has Brawler and rolls multiple Both Down, we automatically choose
        // one of the dice as choosing between them has no effect.
        if (currentNode == UseBrawlerReroll.SelectBothDownToReroll) {
            actions.getOrNull<SelectDicePoolResult>()?.let {
                it.pools.singleOrNull()?.dice?.first()
            }?.let {
                return DicePoolResultsSelected.fromSingleDice(it)
            }
        }

        if (menuViewModel.isFeatureEnabled(Feature.USE_ARM_BAR_ON_ARMOUR) && currentNode == ArmourRoll.ChooseToUseArmBar) {
            actions.getOrNull<SelectPlayer>()?.let {
                if (it.players.size == 1) {
                    return PlayerSelected(it.players.first())
                }
            }
        }
        if (menuViewModel.isFeatureEnabled(Feature.USE_ARM_BAR_ON_INJURY) && currentNode == InjuryRoll.ChooseToUseArmBar) {
            actions.getOrNull<SelectPlayer>()?.let {
                if (it.players.size == 1) {
                    return PlayerSelected(it.players.first())
                }
            }
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_CLAWS) && currentNode == ArmourRoll.ChooseToUseClaws) {
            return Confirm
        }

        if (menuViewModel.isFeatureEnabled(Feature.ALWAYS_USE_IRON_HARD_SKIN) && currentNode == ArmourRoll.ChooseToUseIronHardSkin) {
            return Confirm
        }

        return null
    }

    private fun hasExecutableMove(descriptor: SelectMoveType): Boolean {
        val player = game.activePlayer ?: return false
        return descriptor.types.any { type ->
            when (type) {
                MoveType.STANDARD -> {
                    val plan = game.rulesContext.actionPlanner.createMovePlan(
                        game,
                        player,
                        maxSteps = 1,
                        includeDodges = true,
                        includeRushes = true,
                    )
                    plan.neighborMoves.isNotEmpty()
                }
                else -> true
            }
        }
    }

}
