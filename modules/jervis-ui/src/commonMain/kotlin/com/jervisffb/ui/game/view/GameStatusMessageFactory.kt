package com.jervisffb.ui.game.view

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.SteadyFootingRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BB2020PushStepInitialMoveSequence
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BB2020Stumble
import com.jervisffb.engine.rules.bb2020.procedures.actions.pass.AccuracyRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.BB2025BothDown
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.HitAndRunStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.JumpUpRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.push.CreatePushChainStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.push.FollowUpStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.push.UseStripBallStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.singleblock.SingleStandardBlockChooseReroll
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.singleblock.SingleStandardBlockChooseResult
import com.jervisffb.engine.rules.bb2025.procedures.actions.move.JumpStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.move.LeapRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.move.LeapStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.move.PogoRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.move.PogoStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.pass.PassStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.securetheball.SecureTheBallRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.securetheball.SecureTheBallStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate.ThrowPlayerStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate.ThrowTeammateAccuracyRoll
import com.jervisffb.engine.rules.bb2025.procedures.skills.SafePairOfHandsStep
import com.jervisffb.engine.rules.bb2025.procedures.skills.ShadowingRoll
import com.jervisffb.engine.rules.bb2025.procedures.skills.UseShadowingStep
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.common.procedures.BoneHeadRoll
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.procedures.Catch
import com.jervisffb.engine.rules.common.procedures.CatchRoll
import com.jervisffb.engine.rules.common.procedures.DeviateRoll
import com.jervisffb.engine.rules.common.procedures.DeviateRollContext
import com.jervisffb.engine.rules.common.procedures.Pickup
import com.jervisffb.engine.rules.common.procedures.PickupRoll
import com.jervisffb.engine.rules.common.procedures.ReallyStupidRoll
import com.jervisffb.engine.rules.common.procedures.ResolveBallLandingOnField
import com.jervisffb.engine.rules.common.procedures.ScatterRoll
import com.jervisffb.engine.rules.common.procedures.SteadyFootingRoll
import com.jervisffb.engine.rules.common.procedures.TakeRootRoll
import com.jervisffb.engine.rules.common.procedures.TheKickOff
import com.jervisffb.engine.rules.common.procedures.actions.blitz.BlitzAction
import com.jervisffb.engine.rules.common.procedures.actions.block.BreatheFireRoll
import com.jervisffb.engine.rules.common.procedures.actions.block.DauntlessRoll
import com.jervisffb.engine.rules.common.procedures.actions.block.FoulAppearanceRoll
import com.jervisffb.engine.rules.common.procedures.actions.block.ProjectileVomitRoll
import com.jervisffb.engine.rules.common.procedures.actions.foul.FoulStep
import com.jervisffb.engine.rules.common.procedures.actions.move.DodgeRoll
import com.jervisffb.engine.rules.common.procedures.actions.move.JumpRoll
import com.jervisffb.engine.rules.common.procedures.actions.move.RushRoll
import com.jervisffb.engine.rules.common.procedures.actions.move.StandardMoveStep
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.LandingRoll
import com.jervisffb.engine.rules.common.procedures.tables.injury.ArmourRoll
import com.jervisffb.engine.rules.common.procedures.tables.injury.InjuryRoll
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.common.procedures.tables.injury.UseBB11Apothecary
import com.jervisffb.engine.rules.common.procedures.tables.injury.UseBB7Apothecary
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.LocalActionProvider
import com.jervisffb.ui.game.state.P2PActionProvider
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.viewmodel.MenuViewModel

/**
 * Class responsible for setting the game status message for the current step.
 *
 * For Hotseat games it will always show the message for the active coach.
 * For P2P games, each client will see the message relevant for the respective
 * coach.
 */
class GameStatusMessageFactory(private val menuViewModel: MenuViewModel, private val state: Game) {

    private val messageFactories = buildFactoryMap()

    private fun buildFactoryMap(): Map<Node, (Boolean, Boolean, Game) -> String?> {
        // We split these factories into multiple groups, mostly to work around IntelliJ
        // being slow at type resolution when combining them all into one Map.
        // Second, because it makes it easier to add similar messages.
        // New messages should go in the most narrow category. If none of the categories
        // fit, put it into the Custom group.
        return buildMap {
            putAll(buildUseSimpleActionMessages())
            putAll(buildDiceRollMessages())
            putAll(buildCustomActionMessages())
        }
    }

    // Game messages asking to use a skill or not
    private fun buildUseSimpleActionMessages(): Map<Node, (Boolean, Boolean, Game) -> String?> {
        val skills = mapOf(
            Pickup.ChooseToUseBigHand to SkillType.BIG_HAND,
            SecureTheBallStep.ChooseToUseBigHand to SkillType.BIG_HAND,
            com.jervisffb.engine.rules.bb2020.procedures.actions.move.JumpStep.ChooseToUseVeryLongLegs to SkillType.VERY_LONG_LEGS,
            com.jervisffb.engine.rules.bb2025.procedures.actions.move.JumpStep.ChooseToUseVeryLongLegs to SkillType.VERY_LONG_LEGS,
            CreatePushChainStep.DecideToUseSidestep to SkillType.SIDESTEP,
            CreatePushChainStep.DecideToUseGrab to SkillType.GRAB,
            CreatePushChainStep.DecideToUseStandFirm to SkillType.STAND_FIRM,
            FoulStep.ChooseToUseSneakyGit to SkillType.SNEAKY_GIT,
            ArmourRoll.ChooseToUseLoneFouler to SkillType.LONE_FOULER,
            CreatePushChainStep.ChooseToUseEyeGouge to SkillType.EYE_GOUGE,
            SafePairOfHandsStep.ChooseToUseSafePairOfHands to SkillType.SAFE_PAIR_OF_HANDS,
            Catch.ChooseToUseDivingCatch to SkillType.DIVING_CATCH,
            HitAndRunStep.ChooseToUseHitAndRun to SkillType.HIT_AND_RUN,
            BlitzAction.ChooseToUseSprintForBlocking to SkillType.SPRINT,
            BB2025BothDown.DefenderChooseToUseWrestle to SkillType.WRESTLE,
            BB2025BothDown.AttackerChooseToUseWrestle to SkillType.WRESTLE,
            StandardMoveStep.ChooseToUseFumblerooski to SkillType.FUMBLEROOSKI,
            JumpStep.ChooseToUseFumblerooskiAfterJumpingToTargetSquare to SkillType.FUMBLEROOSKI,
            LeapStep.ChooseToUseFumblerooskiAfterLeapingToTargetSquare to SkillType.FUMBLEROOSKI,
            PogoStep.ChooseToUseFumblerooskiAfterPogoToTargetSquare to SkillType.FUMBLEROOSKI,
            UseStripBallStep.ChooseToUseStripBall to SkillType.STRIP_BALL,
        )

        return skills.toList().associate {
            Pair<Node, (Boolean, Boolean, Game) -> String?>(it.first) { isActiveClient, _, _ ->
                val skillDescription = it.second.description
                when (isActiveClient) {
                    true -> "Use $skillDescription?"
                    false -> "Waiting for player to use $skillDescription"
                }
            }
        }
    }

    private fun buildDiceRollMessages(): Map<Node, (Boolean, Boolean, Game) -> String?> {
        val rollDiceScenarios = listOf(
            AccuracyRoll.RollDie to "Roll D6 to Pass the Ball",
            AccuracyRoll.ReRollDie to "Re-roll D6 to Pass the Ball",
            com.jervisffb.engine.rules.bb2025.procedures.actions.pass.PassAccuracyRoll.RollDie to "Roll D6 to Pass the Ball",
            com.jervisffb.engine.rules.bb2025.procedures.actions.pass.PassAccuracyRoll.ReRollDie to "Re-roll D6 to Pass the Ball",
            Bounce.RollDirection to "Roll D8 to Bounce the Ball",
            ThrowPlayerStep.BouncePlayer to "Roll D8 to Bounce the Player",
            com.jervisffb.engine.rules.bb2020.procedures.actions.throwteammate.ThrowPlayerStep.BouncePlayer to "Roll D8 to Bounce the Player",
            CatchRoll.RollDie to "Roll D6 to Catch the Ball",
            CatchRoll.ReRollDie to "Re-roll D6 to Catch the Ball",
            DeviateRoll.RollDice to "Roll D8 + D6 to Deviate the Ball",
            DodgeRoll.RollDie to "Roll D6 to Dodge",
            DodgeRoll.ReRollDie to "Re-roll D6 to Dodge",
            PickupRoll.RollDie to "Roll D6 to Pickup the Ball",
            PickupRoll.ReRollDie to "Re-roll D6 to Pickup the Ball",
            RushRoll.RollDie to "Roll D6 to Rush",
            RushRoll.ReRollDie to "Re-roll D6 to Rush",
            SecureTheBallRoll.RollDie to "Roll D6 to Secure the Ball",
            SecureTheBallRoll.ReRollDie to "Re-roll D6 to Secure the Ball",
            ShadowingRoll.RollDie to "Roll D6 to shadow player",
            ShadowingRoll.ReRollDie to "Re-roll D6 to shadow player",
            BoneHeadRoll.RollDie to "Roll D6 for Bone Head",
            BoneHeadRoll.ReRollDie to "Re-roll D6 to avoid Bone Head",
            ReallyStupidRoll.RollDie to "Roll D6 for Really Stupid",
            ReallyStupidRoll.ReRollDie to "Re-roll D6 to avoid Really Stupid",
            JumpRoll.RollDie to "Roll D6 to Jump",
            JumpRoll.ReRollDie to "Re-roll D6 to Jump",
            LeapRoll.RollDie to "Roll D6 to Leap",
            LeapRoll.ReRollDie to "Re-roll D6 to Leap",
            PogoRoll.RollDie to "Roll D6 to use Pogo-stick",
            PogoRoll.ReRollDie to "Re-roll D6 to use Pogo-stick",
            ThrowTeammateAccuracyRoll.RollDie to "Roll D6 to Throw Player",
            ThrowTeammateAccuracyRoll.ReRollDie to "Re-roll D6 to Throw Player",
            LandingRoll.RollDie to "Roll D6 to Land",
            LandingRoll.ReRollDie to "Re-roll D6 to Land",
            ProjectileVomitRoll.RollDie to "Roll D6 for Projectile Vomit",
            ProjectileVomitRoll.ReRollDie to "Re-roll D6 for Projectile Vomit",
            BreatheFireRoll.RollDie to "Roll D6 to Breathe Fire",
            BreatheFireRoll.ReRollDie to "Re-roll D6 to Breathe Fire",
            DauntlessRoll.RollDie to "Roll D6 for Dauntless",
            DauntlessRoll.ReRollDie to "Re-roll D6 for Dauntless",
            FoulAppearanceRoll.RollDie to "Roll D6 for Foul Appearance",
            FoulAppearanceRoll.ReRollDie to "Re-roll D6 for Foul Appearance",
            TakeRootRoll.RollDie to "Roll D6 for Take Root",
            TakeRootRoll.ReRollDie to "Re-roll D6 for Take Root",
            JumpUpRoll.RollDie to "Roll D6 to Jump Up",
            JumpUpRoll.ReRollDie to "Re-roll D6 to Jump Up",
        )
        val askForRerollScenarios = listOf(
            AccuracyRoll.ChooseReRollSource to "Accept Pass Result or Reroll D6?",
            com.jervisffb.engine.rules.bb2025.procedures.actions.pass.PassAccuracyRoll.ChooseReRollSource to "Accept Pass Result or Reroll D6?",
            CatchRoll.ChooseReRollSource to "Accept Catch Result or Reroll D6?",
            DodgeRoll.ChooseReRollSource to "Accept Dodge Result or Reroll D6?",
            PickupRoll.ChooseReRollSource to "Accept Pickup Result or Reroll D6?",
            RushRoll.ChooseReRollSource to "Accept Rush Result or Reroll D6?",
            SecureTheBallRoll.ChooseReRollSource to "Accept Secure the Ball Result or Reroll D6?",
            ShadowingRoll.ChooseReRollSource to "Accept Shadowing Result or Reroll D6?",
            BoneHeadRoll.ChooseReRollSource to "Accept Bone Head Result or Reroll D6?",
            ReallyStupidRoll.RollDie to "Roll D6 for Really Stupid",
            ReallyStupidRoll.ChooseReRollSource to "Accept Really Stupid Result or Reroll D6?",
            JumpRoll.ChooseReRollSource to "Accept Jump Result or Reroll D6?",
            LeapRoll.ChooseReRollSource to "Accept Leap Result or Reroll D6?",
            PogoRoll.ChooseReRollSource to "Accept Pogo-stick Result or Reroll D6?",
            SteadyFootingRoll.ChooseReRollSource to "Accept Steady Footing Result or Reroll D6?",
            ThrowTeammateAccuracyRoll.ChooseReRollSource to "Accept Accuracy Result or Reroll D6?",
            LandingRoll.ChooseReRollSource to "Accept Landing Result or Reroll D6?",
            ProjectileVomitRoll.ChooseReRollSource to "Accept Projectile Vomit Result or Reroll D6?",
            BreatheFireRoll.ChooseReRollSource to "Accept Breathe Fire Result or Reroll D6?",
            DauntlessRoll.ChooseReRollSource to "Accept Dauntless Result or Reroll D6?",
            FoulAppearanceRoll.ChooseReRollSource to "Accept Foul Appearance Result or Reroll D6?",
            TakeRootRoll.ChooseReRollSource to "Accept Take Root Result or Reroll D6?",
            JumpUpRoll.ChooseReRollSource to "Accept Jump Up Result or Reroll D6?",
        )

        val rollMessages: Map<Node, (Boolean, Boolean, Game) -> String?> = rollDiceScenarios.associate { data ->
            data.first to { isActiveClient, serverDiceRolls, state ->
                when {
                    (isActiveClient && !serverDiceRolls) -> data.second
                    else -> null
                }
            }
        }
        val askForRerollMessages: Map<Node, (Boolean, Boolean, Game) -> String?> = askForRerollScenarios.associate { data ->
            data.first to { isActiveClient, _, state ->
                when {
                    (isActiveClient) -> data.second
                    else -> null
                }
            }
        }

        return rollMessages + askForRerollMessages
    }

    private fun buildCustomActionMessages(): Map<Node, (Boolean, Boolean, Game) -> String?> {
        return mutableMapOf<Node, (isActiveClient: Boolean, serverDiceRolls: Boolean, game: Game) -> String?>(
            TheKickOff.PlaceTheKick to { isActiveClient, serverDiceRolls, state ->
                when (isActiveClient) {
                    true -> "Place the Kick"
                    false -> "Kick is being placed"
                }
            },
            TheKickOff.TheKickDeviates to { isActiveClient, serverDiceRolls, state ->
                "The Kick Deviates"
            },
            ScatterRoll.RollDice to { _, _, _ ->
                "Scatter Roll"
            },
            UseShadowingStep.CheckIfShadowingIsAvailable to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Select player to use Shadowing"
                    false -> "Waiting for player to use Shadowing"
                }
            },
            TheKickOff.ChooseToUseKick to { isActiveClient, _, state ->
                val d6 = state.getContext<DeviateRollContext>().deviateRoll.last() as D6Result
                when (isActiveClient) {
                    true -> "Use Kick to reduce distance from ${d6.value} (D6) to ${d6.toD3().value} (D3)?"
                    false -> "Waiting for opponent to use Kick"
                }
            },
            ArmourRoll.ChooseToUseDirtyPlayer to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Use Dirty Player on Armour Roll?"
                    false -> "Waiting for opponent to use Dirty Player"
                }
            },
            InjuryRoll.ChooseToUseDirtyPlayer to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Use Dirty Player on Injury Roll?"
                    false -> "Waiting for opponent to use Dirty Player"
                }
            },
            UseBB11Apothecary.ChooseToUseApothecary to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Use Apothecary?"
                    false -> "Waiting for opponent to use Apothecary"
                }
            },
            UseBB7Apothecary.ChooseToUseApothecary to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Use Apothecary?"
                    false -> "Waiting for opponent to use Apothecary"
                }
            },
            PassStep.ChooseToUseSafePass to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Use Safe Pass to avoid Fumble?"
                    false -> "Waiting for opponent to use Safe Pass"
                }
            },
            BB2020Stumble.ChooseToUseTackle to { isActiveClient, _, state ->
                val context = state.getContext<BlockContext>()
                when (isActiveClient) {
                    true -> "Use Tackle to knock down ${context.defender.name}?"
                    false -> "Waiting for opponent to use Tackle"
                }
            },
            DodgeRoll.ChooseToUseTackle to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Select player with Tackle to prevent Dodge from being used"
                    false -> "Waiting for opponent to use Tackle"
                }
            },
            BB2020PushStepInitialMoveSequence.DecideToFollowUp to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Follow up?"
                    false -> "Waiting for opponent to use to follow up or not"
                }
            },
            FollowUpStep.ChooseToFollowUp to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Follow up?"
                    false -> "Waiting for opponent to Follow Up or not"
                }
            },
            FollowUpStep.ChooseToUseFend to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Use Fend to prevent a Follow Up?"
                    false -> "Waiting for opponent to use to Fend or not"
                }
            },
            FollowUpStep.ChooseToUseTaunt to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Use Taunt to force attacker to follow up?"
                    false -> "Waiting for opponent to use Taunt or not"
                }
            },
            InjuryRoll.ChooseToUseThickSkull to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Use Thick Skull to avoid being Knocked Out?"
                    false -> "Waiting for opponent to use Thick Skull"
                }
            },
            LeapStep.ChooseToUseLeapModifier to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Use Leap Modifier?"
                    false -> "Waiting for opponent to use Leap Modifier"
                }
            },
            SteadyFootingRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
                val context = state.getContext<SteadyFootingRollContext>()
                when {
                    (isActiveClient && !serverDiceRolls) -> {
                        when (context.mode) {
                            RiskingInjuryMode.FALLING_OVER -> "Roll D6 to use Steady Footing to avoid Falling Over"
                            RiskingInjuryMode.KNOCKED_DOWN -> "Roll D6 to use Steady Footing to avoid being Knocked Down"
                            else -> error("Unsupported mode: ${context.mode}")
                        }
                    }
                    else -> null
                }
            },
            SteadyFootingRoll.ReRollDie to { isActiveClient, serverDiceRolls, _ ->
                when {
                    (isActiveClient && !serverDiceRolls) -> {
                        val context = state.getContext<SteadyFootingRollContext>()
                        when (context.mode) {
                            RiskingInjuryMode.FALLING_OVER -> "Re-roll D6 to use Steady Footing to avoid Falling Over"
                            RiskingInjuryMode.KNOCKED_DOWN -> "Re-roll D6 to use Steady Footing to avoid being Knocked Down"
                            else -> error("Unsupported mode: ${context.mode}")
                        }
                    }
                    else -> null
                }
            },
            UseStripBallStep.ChooseToUseSureHands to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Use Sure Hands to prevent Strip Ball from being used?"
                    false -> "Waiting for opponent to use Strip Ball"
                }
            },
            ArmourRoll.ChooseToUseMightyBlow to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Use Mighty Blow to injure opponent?"
                    false -> "Waiting for opponent to use Mighty Blow"
                }
            },
            InjuryRoll.ChooseToUseMightyBlow to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Use Mighty Blow on Injury Roll?"
                    false -> "Waiting for opponent to use Mighty Blow"
                }
            },
            ThrowTeammateAccuracyRoll.ChooseToUseStrongArm to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Use Strong Arm to improve the Accuracy Roll?"
                    false -> "Waiting for opponent to use Strong Arm"
                }
            },
            ResolveBallLandingOnField.InactiveTeamChoosesDivingCatchPlayers to { isActiveClient, _, state ->
                when {
                    (isActiveClient) -> "Select Players wanting to use Diving Catch"
                    else -> "Waiting for opponent to choose players to use Diving Catch"
                }
            },
            ResolveBallLandingOnField.ActiveTeamChoosesDivingCatchPlayers to { isActiveClient, _, state ->
                when {
                    (isActiveClient) -> "Select Players wanting to use Diving Catch"
                    else -> "Waiting for opponent to choose players to use Diving Catch"
                }
            },
            ResolveBallLandingOnField.ChooseDivingCatchPlayer to { isActiveClient, _, state ->
                when {
                    (isActiveClient) -> "Select player that should attempt to catch the ball using Diving Catch"
                    else -> "Waiting for opponent to choose player to perform a Diving Catch"
                }
            },
            DodgeRoll.ChooseToUseDivingTackleAfterReRoll to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Select a player to use Diving Tackle"
                    false -> "Waiting for opponent to use Diving Tackle"
                }
            },
            JumpRoll.ChooseToUseDivingTackleAfterReRoll to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Select a player to use Diving Tackle"
                    false -> "Waiting for opponent to use Diving Tackle"
                }
            },
            LeapRoll.ChooseToUseDivingTackleAfterReRoll to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Select a player to use Diving Tackle."
                    false -> "Waiting for opponent to use Diving Tackle"
                }
            },
            ArmourRoll.ChooseToUseLethalFlight to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Use Lethal Flight to injure opponent?"
                    false -> "Waiting for opponent to use Lethal Flight"
                }
            },
            InjuryRoll.ChooseToUseLethalFlight to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Use Lethal Flight on Injury Roll?"
                    false -> "Waiting for opponent to use Lethal Flight"
                }
            },
            SingleStandardBlockChooseReroll.ReRollSourceOrAcceptRoll to { isActiveClient, _, state ->
                val context = state.getContext<BlockContext>()
                val selectResultTeam = context.getTeamSelectingResult()
                val attackerChooseResult = (context.attacker.team == selectResultTeam)
                when {
                    (isActiveClient && attackerChooseResult) -> "Re-roll Block Dice or Select Block Result"
                    (isActiveClient && !attackerChooseResult) -> "Re-roll Block Dice or Confirm to Allow Opponent to Select Block Result"
                    (!isActiveClient && attackerChooseResult) -> "Waiting for Opponent to Re-roll or Select Block Result"
                    (!isActiveClient && !attackerChooseResult) -> "Waiting for opponent to Re-roll or Accept Rolled Block Dice"
                    else -> error("Unsupported scenario: $isActiveClient, $attackerChooseResult")
                }
            },
            SingleStandardBlockChooseResult.SelectBlockResult to { isActiveClient, _, state ->
                when {
                    isActiveClient -> "Select Block Result"
                    else -> "Waiting for opponent to Select Block Result"
                }
            }
        )
    }

    private fun isActiveStep(actionProvider: UiActionProvider): Boolean {
        return when (actionProvider) {
            is LocalActionProvider -> true
            is P2PActionProvider -> actionProvider.currentClientIsCreatingAction()
            else -> error("Unsupported action provider: $actionProvider")
        }
    }

    // Set a game status message for the current game state. This is done by going back
    // through the chain of procedure nodes, using the first node that returns an message
    // to show.
    // If we already have an automated action, we will skip showing a message as it might cause flickering.
    fun applyMessage(
        actionProvider: UiActionProvider,
        acc: UiSnapshotAccumulator
    ) {
        val serverDiceRolls = state.rules.diceRollsOwner == DiceRollOwner.ROLL_ON_SERVER
        val isActiveCoach = isActiveStep(actionProvider)
        val stack = acc.gameController.stack
        var currentIndex = 0
        var currentNode: Node? = stack.currentNode()
        var message: String? = null
        val automatedAction = actionProvider.hasQueuedActions()
        while (!automatedAction && currentNode != null && message == null) {
            val messageFactory = messageFactories[currentNode]
            if (messageFactory != null) {
                message = messageFactory(isActiveCoach, serverDiceRolls, state)
            }
            if (message == null) {
                currentIndex -= 1
                currentNode = stack.getOrNull(currentIndex)?.currentNode()
            }
        }
        acc.setGameStatusText(message)
    }
}

