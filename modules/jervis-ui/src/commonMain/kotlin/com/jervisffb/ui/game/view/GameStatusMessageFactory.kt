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
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.HitAndRunStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.JumpUpRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.push.CreatePushChainStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.push.FollowUpStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.push.UseStripBallStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.move.LeapRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.move.LeapStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.move.PogoRoll
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
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.LandingRoll
import com.jervisffb.engine.rules.common.procedures.tables.injury.ArmourRoll
import com.jervisffb.engine.rules.common.procedures.tables.injury.InjuryRoll
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.common.procedures.tables.injury.UseBB11Apothecary
import com.jervisffb.engine.rules.common.procedures.tables.injury.UseBB7Apothecary
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

    private val messageFactories = mutableMapOf<Node, (isActiveClient: Boolean, serverDiceRolls: Boolean, game: Game) -> String?>(
        AccuracyRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to Pass the Ball"
                else -> null
            }
        },
        AccuracyRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Pass Result or Reroll D6?"
                else -> null
            }
        },
        AccuracyRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to Pass the Ball"
                else -> null
            }
        },

        com.jervisffb.engine.rules.bb2025.procedures.actions.pass.PassAccuracyRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to Pass the Ball"
                else -> null
            }
        },
        com.jervisffb.engine.rules.bb2025.procedures.actions.pass.PassAccuracyRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Pass Result or Reroll D6?"
                else -> null
            }
        },
        com.jervisffb.engine.rules.bb2025.procedures.actions.pass.PassAccuracyRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to Pass the Ball"
                else -> null
            }
        },

        Bounce.RollDirection to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D8 to Bounce the Ball"
                else -> null
            }
        },

        ThrowPlayerStep.BouncePlayer to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D8 to Bounce the Player"
                else -> null
            }
        },

        com.jervisffb.engine.rules.bb2020.procedures.actions.throwteammate.ThrowPlayerStep.BouncePlayer to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D8 to Bounce the Player"
                else -> null
            }
        },

        CatchRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to Catch the Ball"
                else -> null
            }
        },
        CatchRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Catch Result or Reroll D6?"
                else -> null
            }
        },
        CatchRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to Catch the Ball"
                else -> null
            }
        },

        DeviateRoll.RollDice to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D8 + D6 to Deviate the Ball"
                else -> "Deviate Roll"
            }
        },

        DodgeRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to Dodge"
                else -> null
            }
        },
        DodgeRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Dodge Result or Reroll D6?"
                else -> null
            }
        },
        DodgeRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to Dodge"
                else -> null
            }
        },


        TheKickOff.PlaceTheKick to { isActiveClient, serverDiceRolls, state ->
            when (isActiveClient) {
                true -> "Place the Kick"
                false -> "Kick is being placed"
            }
        },
        TheKickOff.TheKickDeviates to { isActiveClient, serverDiceRolls, state ->
            "The Kick Deviates"
        },

        PickupRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to Pickup the Ball"
                else -> null
            }
        },
        PickupRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Pickup Result or Reroll D6?"
                else -> null
            }
        },
        PickupRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to Pickup the Ball"
                else -> null
            }
        },

        RushRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to Rush"
                else -> null
            }
        },
        RushRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Rush Result or Reroll D6?"
                else -> null
            }
        },
        RushRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to Rush"
                else -> null
            }
        },

        ScatterRoll.RollDice to { _, _, _ ->
            "Scatter Roll"
        },

        SecureTheBallRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to Secure the Ball"
                else -> null
            }
        },
        SecureTheBallRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Secure the Ball Result or Reroll D6?"
                else -> null
            }
        },
        SecureTheBallRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to Secure the Ball"
                else -> null
            }
        },

        ShadowingRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to shadow player"
                else -> null
            }
        },
        ShadowingRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Shadowing Result or Reroll D6?"
                else -> null
            }
        },
        ShadowingRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to shadow player"
                else -> null
            }
        },

        UseShadowingStep.CheckIfShadowingIsAvailable to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Select player to use Shadowing"
                false -> "Waiting for player to use Shadowing"
            }
        },

        Pickup.ChooseToUseBigHand to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Big Hand?"
                false -> "Waiting for player to use Big Hand"
            }
        },
        SecureTheBallStep.ChooseToUseBigHand to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Big Hand?"
                false -> "Waiting for player to use Big Hand"
            }
        },

        com.jervisffb.engine.rules.bb2020.procedures.actions.move.JumpStep.ChooseToUseVeryLongLegs to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Very Long Legs?"
                false -> "Waiting for player to use Very Long Legs"
            }
        },

        com.jervisffb.engine.rules.bb2025.procedures.actions.move.JumpStep.ChooseToUseVeryLongLegs to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Very Long Legs?"
                false -> "Waiting for player to use Very Long Legs"
            }
        },

        JumpRoll.RollDie to { isActiveClient, serverDiceRolls, _ ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to Jump"
                else -> null
            }
        },
        JumpRoll.ChooseReRollSource to { isActiveClient, _, _ ->
            when {
                (isActiveClient) -> "Accept Jump Result or Reroll D6?"
                else -> null
            }
        },
        JumpRoll.ReRollDie to { isActiveClient, serverDiceRolls, _ ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to Jump?"
                else -> null
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
                false -> "Waiting for opponent to use to follow up or not"
            }
        },

        FollowUpStep.ChooseToUseFend to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Fend to prevent a follow up?"
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

        BoneHeadRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 for Bone Head"
                else -> null
            }
        },
        BoneHeadRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Bone Head Result or Reroll D6?"
                else -> null
            }
        },
        BoneHeadRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to avoid Bone Head"
                else -> null
            }
        },

        ReallyStupidRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 for Really Stupid"
                else -> null
            }
        },
        ReallyStupidRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Really Stupid Result or Reroll D6?"
                else -> null
            }
        },
        ReallyStupidRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to avoid Really Stupid"
                else -> null
            }
        },

        LeapRoll.RollDie to { isActiveClient, serverDiceRolls, _ ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to Leap"
                else -> null
            }
        },
        LeapRoll.ChooseReRollSource to { isActiveClient, _, _ ->
            when {
                (isActiveClient) -> "Accept Leap Result or Reroll D6?"
                else -> null
            }
        },
        LeapRoll.ReRollDie to { isActiveClient, serverDiceRolls, _ ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to Leap?"
                else -> null
            }
        },

        PogoRoll.RollDie to { isActiveClient, serverDiceRolls, _ ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to use Pogo"
                else -> null
            }
        },
        PogoRoll.ChooseReRollSource to { isActiveClient, _, _ ->
            when {
                (isActiveClient) -> "Accept Pogo Result or Reroll D6?"
                else -> null
            }
        },
        PogoRoll.ReRollDie to { isActiveClient, serverDiceRolls, _ ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to use Pogo?"
                else -> null
            }
        },

        LeapStep.ChooseToUseLeapModifier to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Leap Modifier?"
                false -> "Waiting for opponent to use Leap Modifier"
            }
        },

        CreatePushChainStep.DecideToUseSidestep to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Sidestep?"
                false -> "Waiting for opponent to use Sidestep"
            }
        },

        CreatePushChainStep.DecideToUseGrab to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Grab?"
                false -> "Waiting for opponent to use Grab"
            }
        },

        CreatePushChainStep.DecideToUseStandFirm to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Stand Firm?"
                false -> "Waiting for opponent to use Stand Firm"
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
        SteadyFootingRoll.ChooseReRollSource to { isActiveClient, _, _ ->
            when {
                (isActiveClient) -> "Accept Steady Footing Result or Reroll D6?"
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

        UseStripBallStep.ChooseToUseStripBall to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Strip Ball?"
                false -> "Waiting for opponent to use Strip Ball"
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

        ThrowTeammateAccuracyRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to Throw a Player"
                else -> null
            }
        },
        ThrowTeammateAccuracyRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Accuracy Result or Reroll D6?"
                else -> null
            }
        },
        ThrowTeammateAccuracyRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to Throw a Player"
                else -> null
            }
        },

        LandingRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to Land"
                else -> null
            }
        },
        LandingRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Landing Result or Reroll D6?"
                else -> null
            }
        },
        LandingRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to Land"
                else -> null
            }
        },

        ProjectileVomitRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 for Projectile Vomit"
                else -> null
            }
        },
        ProjectileVomitRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Projectile Vomit Result or Reroll D6?"
                else -> null
            }
        },
        ProjectileVomitRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 for Projectile Vomit"
                else -> null
            }
        },

        BreatheFireRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 for Breathe Fire"
                else -> null
            }
        },
        BreatheFireRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Breathe Fire Result or Reroll D6?"
                else -> null
            }
        },
        BreatheFireRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 for Breathe Fire"
                else -> null
            }
        },

        DauntlessRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 for Dauntless"
                else -> null
            }
        },
        DauntlessRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Dauntless Result or Reroll D6?"
                else -> null
            }
        },
        DauntlessRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 for Dauntless"
                else -> null
            }
        },

        FoulStep.ChooseToUseSneakyGit to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Sneaky Git?"
                false -> "Waiting for player to use Sneaky Git"
            }
        },

        ArmourRoll.ChooseToUseLoneFouler to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Lone Fouler?"
                false -> "Waiting for player to use Lone Fouler"
            }
        },

        CreatePushChainStep.ChooseToUseEyeGouge to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Eye Gouge?"
                false -> "Waiting for player to use Eye Gouge"
            }
        },

        SafePairOfHandsStep.ChooseToUseSafePairOfHands to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Safe Pair of Hands?"
                false -> "Waiting for player to use Safe Pair of Hands"
            }
        },

        FoulAppearanceRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 for Foul Appearance"
                else -> null
            }
        },
        FoulAppearanceRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Foul Appearance Result or Reroll D6?"
                else -> null
            }
        },
        FoulAppearanceRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 for Foul Appearance"
                else -> null
            }
        },

        TakeRootRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 for Take Root"
                else -> null
            }
        },
        TakeRootRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Take Root Result or Reroll D6?"
                else -> null
            }
        },
        TakeRootRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 for Take Root"
                else -> null
            }
        },

        JumpUpRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 for Jump Up"
                else -> null
            }
        },
        JumpUpRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Jump Up Result or Reroll D6?"
                else -> null
            }
        },
        JumpUpRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 for Jump Up"
                else -> null
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

        Catch.ChooseToUseDivingCatch to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Diving Catch?"
                false -> "Waiting for opponent to use Diving Catch"
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

        HitAndRunStep.ChooseToUseHitAndRun to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Hit and Run?"
                false -> "Waiting for opponent to use Hit and Run"
            }
        },

        BlitzAction.ChooseToUseSprintForBlocking to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Sprint?"
                false -> "Waiting for opponent to use Sprint"
            }
        },
    )

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

