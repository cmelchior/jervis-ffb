package com.jervisffb.ui.game.view

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.bb2020.procedures.actions.block.BB2020PushStepInitialMoveSequence
import com.jervisffb.engine.bb2020.procedures.actions.block.BB2020Stumble
import com.jervisffb.engine.bb2020.procedures.actions.pass.AccuracyRoll
import com.jervisffb.engine.bb2020.procedures.blitz.BlitzAction
import com.jervisffb.engine.bb2020.procedures.table.kickoff.BB2020CheeringFans
import com.jervisffb.engine.bb2025.procedures.actions.block.BB2025BothDown
import com.jervisffb.engine.bb2025.procedures.actions.block.BreatheFireRoll
import com.jervisffb.engine.bb2025.procedures.actions.block.ChainsawRoll
import com.jervisffb.engine.bb2025.procedures.actions.block.ChompRoll
import com.jervisffb.engine.bb2025.procedures.actions.block.DauntlessRoll
import com.jervisffb.engine.bb2025.procedures.actions.block.HitAndRunStep
import com.jervisffb.engine.bb2025.procedures.actions.block.JumpUpRoll
import com.jervisffb.engine.bb2025.procedures.actions.block.PileDriverStep
import com.jervisffb.engine.bb2025.procedures.actions.block.push.CreatePushChainStep
import com.jervisffb.engine.bb2025.procedures.actions.block.push.FollowUpStep
import com.jervisffb.engine.bb2025.procedures.actions.block.push.UseStripBallStep
import com.jervisffb.engine.bb2025.procedures.actions.block.singleblock.SingleStandardBlockChooseReroll
import com.jervisffb.engine.bb2025.procedures.actions.block.singleblock.SingleStandardBlockChooseResult
import com.jervisffb.engine.bb2025.procedures.actions.block.singleblock.SingleStandardBlockRerollDice
import com.jervisffb.engine.bb2025.procedures.actions.foul.ChainsawFoulStep
import com.jervisffb.engine.bb2025.procedures.actions.move.JumpStep
import com.jervisffb.engine.bb2025.procedures.actions.move.LeapRoll
import com.jervisffb.engine.bb2025.procedures.actions.move.LeapStep
import com.jervisffb.engine.bb2025.procedures.actions.move.PogoRoll
import com.jervisffb.engine.bb2025.procedures.actions.move.PogoStep
import com.jervisffb.engine.bb2025.procedures.actions.pass.PassAccuracyRoll
import com.jervisffb.engine.bb2025.procedures.actions.pass.PassStep
import com.jervisffb.engine.bb2025.procedures.actions.securetheball.SecureTheBallRoll
import com.jervisffb.engine.bb2025.procedures.actions.securetheball.SecureTheBallStep
import com.jervisffb.engine.bb2025.procedures.actions.throwteammate.AlwaysHungryRoll
import com.jervisffb.engine.bb2025.procedures.actions.throwteammate.AlwaysHungrySquirmFreeRoll
import com.jervisffb.engine.bb2025.procedures.actions.throwteammate.SwoopDirectionRoll
import com.jervisffb.engine.bb2025.procedures.actions.throwteammate.SwoopDistanceRoll
import com.jervisffb.engine.bb2025.procedures.actions.throwteammate.SwoopStep
import com.jervisffb.engine.bb2025.procedures.actions.throwteammate.ThrowPlayerStep
import com.jervisffb.engine.bb2025.procedures.actions.throwteammate.ThrowTeammateAccuracyRoll
import com.jervisffb.engine.bb2025.procedures.injury.SteadyFootingRoll
import com.jervisffb.engine.bb2025.procedures.rerolls.TeamCaptainRoll
import com.jervisffb.engine.bb2025.procedures.rerolls.TeamMascotRoll
import com.jervisffb.engine.bb2025.procedures.rerolls.TeamMascotStep
import com.jervisffb.engine.bb2025.procedures.rerolls.UseBrawlerReroll
import com.jervisffb.engine.bb2025.skills.HypnoticGazeRoll
import com.jervisffb.engine.bb2025.skills.PuntDirectionRoll
import com.jervisffb.engine.bb2025.skills.PuntDistanceRoll
import com.jervisffb.engine.bb2025.skills.PuntStep
import com.jervisffb.engine.bb2025.skills.SafePairOfHandsStep
import com.jervisffb.engine.bb2025.skills.ShadowingRoll
import com.jervisffb.engine.bb2025.skills.ShadowingStep
import com.jervisffb.engine.bb2025.skills.TentaclesRoll
import com.jervisffb.engine.bb2025.skills.TentaclesStep
import com.jervisffb.engine.common.context.BlockContext
import com.jervisffb.engine.common.context.ChargeContext
import com.jervisffb.engine.common.context.DeviateRollContext
import com.jervisffb.engine.common.procedures.AnimalSavageryRoll
import com.jervisffb.engine.common.procedures.BoneHeadRoll
import com.jervisffb.engine.common.procedures.Bounce
import com.jervisffb.engine.common.procedures.Catch
import com.jervisffb.engine.common.procedures.CatchRoll
import com.jervisffb.engine.common.procedures.DealWithSecretWeaponsStep
import com.jervisffb.engine.common.procedures.DetermineKickingTeamStep
import com.jervisffb.engine.common.procedures.DeviateRoll
import com.jervisffb.engine.common.procedures.FanFactorRolls
import com.jervisffb.engine.common.procedures.Pickup
import com.jervisffb.engine.common.procedures.PickupRoll
import com.jervisffb.engine.common.procedures.ReallyStupidRoll
import com.jervisffb.engine.common.procedures.RecoverKnockedOutPlayersStep
import com.jervisffb.engine.common.procedures.RecoverPlayerRoll
import com.jervisffb.engine.common.procedures.RegenerationRoll
import com.jervisffb.engine.common.procedures.ResolveBallLandingOnPitch
import com.jervisffb.engine.common.procedures.ScatterRoll
import com.jervisffb.engine.common.procedures.TakeRootRoll
import com.jervisffb.engine.common.procedures.TheKickOff
import com.jervisffb.engine.common.procedures.TheKickOffEvent
import com.jervisffb.engine.common.procedures.UnchannelledFuryRoll
import com.jervisffb.engine.common.procedures.WeatherRoll
import com.jervisffb.engine.common.procedures.actions.block.FoulAppearanceRoll
import com.jervisffb.engine.common.procedures.actions.block.ProjectileVomitRoll
import com.jervisffb.engine.common.procedures.actions.foul.ArgueTheCallRoll
import com.jervisffb.engine.common.procedures.actions.foul.BeingSentOff
import com.jervisffb.engine.common.procedures.actions.foul.BribeRoll
import com.jervisffb.engine.common.procedures.actions.foul.FoulStep
import com.jervisffb.engine.common.procedures.actions.move.DodgeRoll
import com.jervisffb.engine.common.procedures.actions.move.JumpRoll
import com.jervisffb.engine.common.procedures.actions.move.RushRoll
import com.jervisffb.engine.common.procedures.actions.move.StandardMoveStep
import com.jervisffb.engine.common.procedures.actions.throwteammate.LandingRoll
import com.jervisffb.engine.common.procedures.rerolls.LonerRoll
import com.jervisffb.engine.common.procedures.rerolls.ProRoll
import com.jervisffb.engine.common.procedures.rerolls.UseProReroll
import com.jervisffb.engine.common.procedures.tables.injury.ArmourRoll
import com.jervisffb.engine.common.procedures.tables.injury.InjuryRoll
import com.jervisffb.engine.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.common.procedures.tables.injury.UseBB11Apothecary
import com.jervisffb.engine.common.procedures.tables.injury.UseBB7Apothecary
import com.jervisffb.engine.common.procedures.tables.kickoff.BrilliantCoaching
import com.jervisffb.engine.common.procedures.tables.kickoff.Charge
import com.jervisffb.engine.common.procedures.tables.kickoff.DodgySnack
import com.jervisffb.engine.common.procedures.tables.kickoff.PitchInvasion
import com.jervisffb.engine.common.procedures.tables.kickoff.QuickSnap
import com.jervisffb.engine.common.procedures.tables.kickoff.SolidDefense
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.QuickSnapContext
import com.jervisffb.engine.model.context.SteadyFootingRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.LocalActionProvider
import com.jervisffb.ui.game.state.P2PActionProvider
import com.jervisffb.ui.game.state.ReplayController
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
            com.jervisffb.engine.bb2020.procedures.actions.move.JumpStep.ChooseToUseVeryLongLegs to SkillType.VERY_LONG_LEGS,
            JumpStep.ChooseToUseVeryLongLegs to SkillType.VERY_LONG_LEGS,
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
            PileDriverStep.ChooseToUsePileDriver to SkillType.PILE_DRIVER,
            ThrowPlayerStep.ChooseToUseBullseye to SkillType.BULLSEYE,
            SwoopStep.ChooseToUseSwoop to SkillType.SWOOP,
            ArmourRoll.ChooseToUseClaws to SkillType.CLAWS,
            ArmourRoll.ChooseToUseIronHardSkin to SkillType.IRON_HARD_SKIN,
            ChainsawFoulStep.ChooseToUseChainsaw to SkillType.CHAINSAW
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
            AccuracyRoll.ReRollDie to "Re-roll D6 to Pass the Ball",
            AccuracyRoll.RollDie to "Roll D6 to Pass the Ball",
            AlwaysHungryRoll.RollDie to "Roll D6 for Always Hungry",
            AlwaysHungryRoll.ReRollDie to "Re-roll D6 for Always Hungry",
            AlwaysHungrySquirmFreeRoll.RollDie to "Roll D6 to Squirm Free",
            AlwaysHungrySquirmFreeRoll.ReRollDie to "Re-roll D6 to Squirm Free",
            AnimalSavageryRoll.RollDie to "Roll D6 for Animal Savagery",
            AnimalSavageryRoll.ReRollDie to "Re-roll D6 for Animal Savagery",
            ArgueTheCallRoll.RollDie to "Roll D6 to Argue the Call",
            BB2020CheeringFans.KickingTeamRollDie to "Roll D6 for Cheering Fans",
            BB2020CheeringFans.ReceivingTeamRollDie to "Roll D6 for Cheering Fans",
            BribeRoll.RollDie to "Roll D6 to use Bribe",
            BoneHeadRoll.ReRollDie to "Re-roll D6 to avoid Bone Head",
            BoneHeadRoll.RollDie to "Roll D6 for Bone Head",
            Bounce.RollDirection to "Roll D8 to Bounce the Ball",
            BreatheFireRoll.ReRollDie to "Re-roll D6 to Breathe Fire",
            BreatheFireRoll.RollDie to "Roll D6 to Breathe Fire",
            BrilliantCoaching.KickingTeamRollDie to "Roll D6 for Brilliant Coaching",
            BrilliantCoaching.ReceivingTeamRollDie to "Roll D6 for Brilliant Coaching",
            CatchRoll.ReRollDie to "Re-roll D6 to Catch the Ball",
            CatchRoll.RollDie to "Roll D6 to Catch the Ball",
            ChainsawRoll.RollDie to "Roll D6 to use Chainsaw",
            ChainsawRoll.ReRollDie to "Re-roll D6 to use Chainsaw",
            ChompRoll.ReRollDie to "Re-roll D6 to Chomp the opponent",
            ChompRoll.RollDie to "Roll D6 to Chomp the opponent",
            Charge.RollForPlayers to "Roll D3 + 3 for Number of Players to be used during Charge",
            DauntlessRoll.ReRollDie to "Re-roll D6 for Dauntless",
            DauntlessRoll.RollDie to "Roll D6 for Dauntless",
            DeviateRoll.RollDice to "Roll D8 + D6 to Deviate the Ball",
            DodgeRoll.ReRollDie to "Re-roll D6 to Dodge",
            DodgeRoll.RollDie to "Roll D6 to Dodge",
            DodgySnack.KickingTeamRollDie to "Roll D6 for Dodgy Snack",
            DodgySnack.ReceivingTeamRollDie to "Roll D6 for Dodgy Snack",
            DodgySnack.RollForKickingTeamSelectedPlayer to "Roll D6 for Effect of Dodgy Snack",
            DodgySnack.RollForReceivingTemSelectedPlayer to "Roll D6 for Effect of Dodgy Snack",
            FanFactorRolls.SetFanFactorForAwayTeam to "Roll D3 for Fan Factor",
            FanFactorRolls.SetFanFactorForHomeTeam to "Roll D3 for Fan Factor",
            FoulAppearanceRoll.ReRollDie to "Re-roll D6 for Foul Appearance",
            FoulAppearanceRoll.RollDie to "Roll D6 for Foul Appearance",
            HypnoticGazeRoll.ReRollDie to "Re-roll D6 for Hypnotic Gaze",
            HypnoticGazeRoll.RollDie to "Roll D6 for Hypnotic Gaze",
            JumpRoll.ReRollDie to "Re-roll D6 to Jump",
            JumpRoll.RollDie to "Roll D6 to Jump",
            JumpUpRoll.ReRollDie to "Re-roll D6 to Jump Up",
            JumpUpRoll.RollDie to "Roll D6 to Jump Up",
            LandingRoll.ReRollDie to "Re-roll D6 to Land",
            LandingRoll.RollDie to "Roll D6 to Land",
            LeapRoll.ReRollDie to "Re-roll D6 to Leap",
            LeapRoll.RollDie to "Roll D6 to Leap",
            LonerRoll.ReRollDie to "Re-roll D6 for Loner",
            LonerRoll.RollDie to "Roll D6 for Loner",
            PickupRoll.ReRollDie to "Re-roll D6 to Pickup the Ball",
            PickupRoll.RollDie to "Roll D6 to Pickup the Ball",
            PitchInvasion.RollForKickingTeamFans to "Roll D6 for Pitch Invasion",
            PitchInvasion.RollForKickingTeamStuns to "Roll D3 for number of Players Affected by Pitch Invasion",
            PitchInvasion.RollForReceivingTeamFans to "Roll D6 for Pitch Invasion",
            PitchInvasion.RollForReceivingTeamStuns to "Roll D3 for number of Players Affected by Pitch Invasion",
            PogoRoll.ReRollDie to "Re-roll D6 to use Pogo-stick",
            PogoRoll.RollDie to "Roll D6 to use Pogo-stick",
            ProRoll.ReRollDie to "Re-roll D6 for Pro",
            ProRoll.RollDie to "Roll D6 for Pro",
            ProjectileVomitRoll.ReRollDie to "Re-roll D6 for Projectile Vomit",
            ProjectileVomitRoll.RollDie to "Roll D6 for Projectile Vomit",
            QuickSnap.RollDie to "Roll D3 + 3 for Number of Players to Move during Quick Snap",
            PuntDirectionRoll.RollDie to "Roll D6 for Punt Direction",
            PuntDirectionRoll.ReRollDie to "Re-roll D3 for Punt Direction",
            PuntDistanceRoll.RollDie to "Roll D3 for Punt Distance",
            PuntDistanceRoll.ReRollDie to "Re-roll D6 for Punt Distance",
            ReallyStupidRoll.ReRollDie to "Re-roll D6 to avoid Really Stupid",
            ReallyStupidRoll.RollDie to "Roll D6 for Really Stupid",
            RecoverPlayerRoll.RollDie to "Roll D6 to Recover Player",
            RegenerationRoll.RollDie to "Roll D6 for Regeneration",
            RegenerationRoll.ReRollDie to "Re-roll D6 for Regeneration",
            RegenerationRoll.RerollUsingInducement to "Re-roll D6 for Regeneration",
            RushRoll.ReRollDie to "Re-roll D6 to Rush",
            RushRoll.RollDie to "Roll D6 to Rush",
            SecureTheBallRoll.ReRollDie to "Re-roll D6 to Secure the Ball",
            SecureTheBallRoll.RollDie to "Roll D6 to Secure the Ball",
            ShadowingRoll.ReRollDie to "Re-roll D6 to shadow player",
            ShadowingRoll.RollDie to "Roll D6 to shadow player",
            SolidDefense.RollDie to "Roll D3 + 3 for Number of Players to Move during Solid Defense",
            SwoopDirectionRoll.ReRollDie to "Re-roll D3 to determine direction of Swoop",
            SwoopDirectionRoll.RollDie to "Roll D3 to determine direction of Swoop",
            SwoopDistanceRoll.ReRollDie to "Re-roll D6 to determine distance of Swoop",
            SwoopDistanceRoll.RollDie to "Roll D6 to determine distance of Swoop",
            TakeRootRoll.ReRollDie to "Re-roll D6 for Take Root",
            TakeRootRoll.RollDie to "Roll D6 for Take Root",
            TeamCaptainRoll.ReRollDie to "Re-roll D6 for Team Captain",
            TeamCaptainRoll.RollDie to "Roll D6 for Team Captain",
            TeamMascotRoll.ReRollDie to "Re-roll D6 for Team Mascot",
            TeamMascotRoll.RollDie to "Roll D6 for Team Mascot",
            TentaclesRoll.RollDie to "Roll D6 for Tentacles",
            TentaclesRoll.ReRollDie to "Re-roll D6 for Tentacles",
            TheKickOffEvent.RollForKickOffEvent to "Roll 2D6 for the Kick-off Event",
            ThrowPlayerStep.BouncePlayer to "Roll D8 to Bounce the Player",
            ThrowTeammateAccuracyRoll.ReRollDie to "Re-roll D6 to Throw Player",
            ThrowTeammateAccuracyRoll.RollDie to "Roll D6 to Throw Player",
            UnchannelledFuryRoll.ReRollDie to "Re-roll D6 to avoid Unchannelle Fury",
            UnchannelledFuryRoll.RollDie to "Roll D6 for Unchannelled Fury",
            WeatherRoll.RollWeatherDice to "Roll 2D6 for the Weather",
            com.jervisffb.engine.bb2020.procedures.actions.throwteammate.ThrowPlayerStep.BouncePlayer to "Roll D8 to Bounce the Player",
            PassAccuracyRoll.ReRollDie to "Re-roll D6 to Pass the Ball",
            PassAccuracyRoll.RollDie to "Roll D6 to Pass the Ball",
        )
        val askForRerollScenarios = listOf(
            AccuracyRoll.ChooseReRollSource to "Accept Pass Result or Reroll D6?",
            AlwaysHungryRoll.ChooseReRollSource to "Accept Always Hungry Result or Reroll D6?",
            AlwaysHungrySquirmFreeRoll.ChooseReRollSource to "Accept Always Hungry Squirm Free Result or Reroll D6?",
            AnimalSavageryRoll.ChooseReRollSource to "Accept Animal Savagery Result or Reroll D6?",
            BoneHeadRoll.ChooseReRollSource to "Accept Bone Head Result or Reroll D6?",
            BreatheFireRoll.ChooseReRollSource to "Accept Breathe Fire Result or Reroll D6?",
            CatchRoll.ChooseReRollSource to "Accept Catch Result or Reroll D6?",
            ChainsawRoll.ChooseReRollSource to "Accept Chainsaw Result or Reroll D6?",
            ChompRoll.ChooseReRollSource to "Accept Chomp Result or Reroll D6?",
            DauntlessRoll.ChooseReRollSource to "Accept Dauntless Result or Reroll D6?",
            DodgeRoll.ChooseReRollSource to "Accept Dodge Result or Reroll D6?",
            FoulAppearanceRoll.ChooseReRollSource to "Accept Foul Appearance Result or Reroll D6?",
            HypnoticGazeRoll.ChooseReRollSource to "Accept Hypnotic Gaze Result or Reroll D6?",
            JumpRoll.ChooseReRollSource to "Accept Jump Result or Reroll D6?",
            JumpUpRoll.ChooseReRollSource to "Accept Jump Up Result or Reroll D6?",
            LandingRoll.ChooseReRollSource to "Accept Landing Result or Reroll D6?",
            LeapRoll.ChooseReRollSource to "Accept Leap Result or Reroll D6?",
            LonerRoll.ChooseReRollSource to "Accept Loner Result or Re-roll D6?",
            PickupRoll.ChooseReRollSource to "Accept Pickup Result or Reroll D6?",
            PogoRoll.ChooseReRollSource to "Accept Pogo-stick Result or Reroll D6?",
            ProRoll.ChooseReRollSource to "Accept Pro Result or Re-roll D6?",
            ProjectileVomitRoll.ChooseReRollSource to "Accept Projectile Vomit Result or Reroll D6?",
            ReallyStupidRoll.ChooseReRollSource to "Accept Really Stupid Result or Reroll D6?",
            RegenerationRoll.ChooseReRollSource to "Accept Regeneration Result or Reroll D6?",
            RushRoll.ChooseReRollSource to "Accept Rush Result or Reroll D6?",
            SecureTheBallRoll.ChooseReRollSource to "Accept Secure the Ball Result or Reroll D6?",
            ShadowingRoll.ChooseReRollSource to "Accept Shadowing Result or Reroll D6?",
            SteadyFootingRoll.ChooseReRollSource to "Accept Steady Footing Result or Reroll D6?",
            SwoopDirectionRoll.ChooseReRollSource to "Accept Swoop Direction Result or Re-roll D3?",
            SwoopDistanceRoll.ChooseReRollSource to "Accept Swoop Distance Result or Re-roll D6?",
            TakeRootRoll.ChooseReRollSource to "Accept Take Root Result or Reroll D6?",
            TeamCaptainRoll.ChooseReRollSource to "Accept Team Captain Result or Re-roll D6?",
            TeamMascotRoll.ChooseReRollSource to "Accept Mascot Result or Re-roll D6?",
            TeamMascotStep.ChooseAnotherReroll to "Team Mascot Failed, Choose Another Team Re-roll?",
            TentaclesRoll.ChooseReRollSource to "Accept Tentacles Result or Re-roll D6?",
            ThrowTeammateAccuracyRoll.ChooseReRollSource to "Accept Accuracy Result or Reroll D6?",
            UnchannelledFuryRoll.ChooseReRollSource to "Accept Unchannelled Fury Result or Reroll D6?",
            PassAccuracyRoll.ChooseReRollSource to "Accept Pass Result or Reroll D6?",
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
            ShadowingStep.CheckIfShadowingIsAvailable to { isActiveClient, _, _ ->
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
            RegenerationRoll.ChooseToUseMortuaryAssistant to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Use Mortuary Assistant to re-roll Regeneration?"
                    false -> "Waiting for opponent to use Mortuary Assistant"
                }
            },
            RegenerationRoll.ChooseToUsePlagueDoctor to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Use Plague Doctor to re-roll Regeneration?"
                    false -> "Waiting for opponent to use Plague Doctor"
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
            ResolveBallLandingOnPitch.InactiveTeamChoosesDivingCatchPlayers to { isActiveClient, _, state ->
                when {
                    (isActiveClient) -> "Select Players wanting to use Diving Catch"
                    else -> "Waiting for opponent to choose players to use Diving Catch"
                }
            },
            ResolveBallLandingOnPitch.ActiveTeamChoosesDivingCatchPlayers to { isActiveClient, _, state ->
                when {
                    (isActiveClient) -> "Select Players wanting to use Diving Catch"
                    else -> "Waiting for opponent to choose players to use Diving Catch"
                }
            },
            ResolveBallLandingOnPitch.ChooseDivingCatchPlayer to { isActiveClient, _, state ->
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
            PogoRoll.ChooseToUseDivingTackleAfterReRoll to { isActiveClient, _, _ ->
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
            SingleStandardBlockChooseReroll.ChooseRerollSourceOrAcceptRoll to { isActiveClient, _, state ->
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
            },
            DetermineKickingTeamStep.SelectCoinSide to { isActiveClient, _, state ->
                when {
                    isActiveClient -> "Select Coin Side"
                    else -> "Waiting for opponent to Select Coin Side"
                }
            },
            DetermineKickingTeamStep.CoinToss to { isActiveClient, _, state ->
                when {
                    isActiveClient -> "Flip the Coin"
                    else -> "Waiting for opponent to Flip the Coin"
                }
            },
            DetermineKickingTeamStep.ChooseKickingTeam to { isActiveClient, _, state ->
                when {
                    isActiveClient -> "Choose How to Start the Game"
                    else -> "Waiting for opponent to choose Kicking and Receiving Team"
                }
            },
            Charge.SelectPlayersToActivate to { isActiveClient, _, state ->
                val context = state.getContext<ChargeContext>()
                when {
                    isActiveClient -> "Select Up to ${context.playersToSelect} Players to be used during Charge"
                    else -> "Waiting for opponent to select Players for Charge"
                }
            },
            QuickSnap.SelectPlayerOrEndSetup to { isActiveClient, _, state ->
                val context = state.getContext<QuickSnapContext>()
                when {
                    isActiveClient -> "Select Player to Move - ${context.playersLeft} Left"
                    else -> null
                }
            },
            BeingSentOff.DecideToArgueTheCall to { isActiveClient, _, _ ->
                when {
                    isActiveClient -> "Argue the Call to avoid being Sent-off?"
                    else -> "Waiting for opponent to Argue the Call or not"
                }
            },
            BeingSentOff.ChooseToUseBribe to { isActiveClient, _, _ ->
                when {
                    isActiveClient -> "Decide whether to use a Bribe"
                    else -> "Waiting for opponent to use a Bribe or not"
                }
            },
            DealWithSecretWeaponsStep.ReceivingTeamSelectPlayerToSendOff to { isActiveClient, _, _ ->
                when {
                    isActiveClient -> "Select Player to Send-off for using a Secret Weapon"
                    else -> "Waiting for opponent to select a player to send-off using a Secret Weapon"
                }
            },
            DealWithSecretWeaponsStep.KickingTeamSelectPlayerToSendOff to { isActiveClient, _, _ ->
                when {
                    isActiveClient -> "Select Player to Send-off for using a Secret Weapon"
                    else -> "Waiting for opponent to select a player to send-off using a Secret Weapon"
                }
            },
            RecoverKnockedOutPlayersStep.ReceivingTeamSelectPlayerToRecover to { isActiveClient, _, _ ->
                when {
                    isActiveClient -> "Select Player to Recover"
                    else -> "Waiting for opponent to select a player to Recover"
                }
            },
            RecoverKnockedOutPlayersStep.KickingTeamSelectPlayerToRecover to { isActiveClient, _, _ ->
                when {
                    isActiveClient -> "Select Player to Recover"
                    else -> "Waiting for opponent to select a player to Recover"
                }
            },
            UseProReroll.SelectDieToReroll to { isActiveClient, _, _ ->
                when {
                    isActiveClient -> "Select Die to Reroll using Pro"
                    else -> "Waiting for opponent to select die to Reroll using Pro"
                }
            },
            UseBrawlerReroll.SelectBothDownToReroll to { isActiveClient, _, _ ->
                when {
                    isActiveClient -> "Select Die to Reroll using Brawler"
                    else -> "Waiting for opponent to select die to Reroll using Brawler"
                }
            },
            SingleStandardBlockRerollDice.ReRollDie to { isActiveClient, _, _ ->
                when {
                    isActiveClient -> "Reroll Selected Block Dice"
                    else -> "Waiting for Opponent to Reroll Block Dice"
                }
            },
            FoulStep.SelectOffensiveAssists to { isActiveClient, _ , _ ->
                when {
                    isActiveClient -> "Select Offensive Assists to Foul"
                    else -> "Waiting for Opponent to select Offensive Assists"
                }
            },
            PuntStep.SelectTemplateOrientation to { isActiveClient, _, _ ->
                when {
                    isActiveClient -> "Select Direction to Punt"
                    else -> "Waiting for Opponent to select Punt Direction"
                }
            },
            ArmourRoll.ChooseToUseArmBar to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Select a player to use Arm Bar"
                    false -> "Waiting for opponent to use Arm Bar"
                }
            },
            InjuryRoll.ChooseToUseArmBar to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Select a player to use Arm Bar"
                    false -> "Waiting for opponent to use Arm Bar"
                }
            },
            TentaclesStep.ChooseToUseTentacles to { isActiveClient, _, _ ->
                when (isActiveClient) {
                    true -> "Select player to use Tentacles"
                    false -> "Waiting for player to use Tentacles"
                }
            },
        )
    }

    private fun isActiveStep(actionProvider: UiActionProvider): Boolean {
        return when (actionProvider) {
            is LocalActionProvider -> true
            is P2PActionProvider -> actionProvider.currentClientIsCreatingAction()
            // Replays are viewed like a local/hotseat game, so always show message there.
            is ReplayController -> true
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

