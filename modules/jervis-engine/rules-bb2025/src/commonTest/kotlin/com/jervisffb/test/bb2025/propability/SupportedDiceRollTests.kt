package com.jervisffb.test.bb2025.propability

import com.jervisffb.engine.bb2025.procedures.actions.block.BreatheFireRoll
import com.jervisffb.engine.bb2025.procedures.actions.block.ChainsawRoll
import com.jervisffb.engine.bb2025.procedures.actions.block.ChompRoll
import com.jervisffb.engine.bb2025.procedures.actions.block.DauntlessRoll
import com.jervisffb.engine.bb2025.procedures.actions.block.JumpUpRoll
import com.jervisffb.engine.bb2025.procedures.actions.block.singleblock.SingleStandardBlockRollDice
import com.jervisffb.engine.bb2025.procedures.actions.move.LeapRoll
import com.jervisffb.engine.bb2025.procedures.actions.move.PogoRoll
import com.jervisffb.engine.bb2025.procedures.actions.pass.InterceptionRoll
import com.jervisffb.engine.bb2025.procedures.actions.pass.PassAccuracyRoll
import com.jervisffb.engine.bb2025.procedures.actions.securetheball.SecureTheBallRoll
import com.jervisffb.engine.bb2025.procedures.actions.throwteammate.AlwaysHungryRoll
import com.jervisffb.engine.bb2025.procedures.actions.throwteammate.AlwaysHungrySquirmFreeRoll
import com.jervisffb.engine.bb2025.procedures.actions.throwteammate.SwoopDirectionRoll
import com.jervisffb.engine.bb2025.procedures.actions.throwteammate.SwoopDistanceRoll
import com.jervisffb.engine.bb2025.procedures.injury.SteadyFootingRoll
import com.jervisffb.engine.bb2025.procedures.rerolls.TeamCaptainRoll
import com.jervisffb.engine.bb2025.procedures.rerolls.TeamMascotRoll
import com.jervisffb.engine.bb2025.procedures.table.kickoff.BB2025CheeringFans
import com.jervisffb.engine.bb2025.skills.HypnoticGazeRoll
import com.jervisffb.engine.bb2025.skills.PuntDirectionRoll
import com.jervisffb.engine.bb2025.skills.PuntDistanceRoll
import com.jervisffb.engine.bb2025.skills.ShadowingRoll
import com.jervisffb.engine.bb2025.skills.TentaclesRoll
import com.jervisffb.engine.common.procedures.AnimalSavageryRoll
import com.jervisffb.engine.common.procedures.BloodLustRoll
import com.jervisffb.engine.common.procedures.BoneHeadRoll
import com.jervisffb.engine.common.procedures.Bounce
import com.jervisffb.engine.common.procedures.CatchRoll
import com.jervisffb.engine.common.procedures.DetermineKickingTeamStep
import com.jervisffb.engine.common.procedures.DeviateRoll
import com.jervisffb.engine.common.procedures.FanFactorRolls
import com.jervisffb.engine.common.procedures.PickupRoll
import com.jervisffb.engine.common.procedures.PrayersToNuffleRoll
import com.jervisffb.engine.common.procedures.ReallyStupidRoll
import com.jervisffb.engine.common.procedures.RecoverPlayerRoll
import com.jervisffb.engine.common.procedures.RegenerationRoll
import com.jervisffb.engine.common.procedures.ScatterRoll
import com.jervisffb.engine.common.procedures.SuddenDeathStep
import com.jervisffb.engine.common.procedures.TakeRootRoll
import com.jervisffb.engine.common.procedures.TheKickOffEvent
import com.jervisffb.engine.common.procedures.ThrowIn
import com.jervisffb.engine.common.procedures.UnchannelledFuryRoll
import com.jervisffb.engine.common.procedures.WeatherRoll
import com.jervisffb.engine.common.procedures.actions.block.FoulAppearanceRoll
import com.jervisffb.engine.common.procedures.actions.block.ProjectileVomitRoll
import com.jervisffb.engine.common.procedures.actions.foul.ArgueTheCallRoll
import com.jervisffb.engine.common.procedures.actions.foul.BribeRoll
import com.jervisffb.engine.common.procedures.actions.move.DodgeRoll
import com.jervisffb.engine.common.procedures.actions.move.JumpRoll
import com.jervisffb.engine.common.procedures.actions.move.MovePlayerIntoSquare
import com.jervisffb.engine.common.procedures.actions.move.RushRoll
import com.jervisffb.engine.common.procedures.actions.move.StandingUpRoll
import com.jervisffb.engine.common.procedures.actions.throwteammate.LandingRoll
import com.jervisffb.engine.common.procedures.rerolls.LonerRoll
import com.jervisffb.engine.common.procedures.rerolls.ProRoll
import com.jervisffb.engine.common.procedures.tables.injury.ArmourRoll
import com.jervisffb.engine.common.procedures.tables.injury.CasualtyRoll
import com.jervisffb.engine.common.procedures.tables.injury.InjuryRoll
import com.jervisffb.engine.common.procedures.tables.injury.LastingInjuryRoll
import com.jervisffb.engine.common.procedures.tables.injury.UseBB7Apothecary
import com.jervisffb.engine.common.procedures.tables.kickoff.Blitz
import com.jervisffb.engine.common.procedures.tables.kickoff.BrilliantCoaching
import com.jervisffb.engine.bb2025.procedures.table.kickoff.Charge
import com.jervisffb.engine.common.procedures.tables.kickoff.DodgySnack
import com.jervisffb.engine.common.procedures.tables.kickoff.OfficiousRef
import com.jervisffb.engine.common.procedures.tables.kickoff.PitchInvasion
import com.jervisffb.engine.common.procedures.tables.kickoff.QuickSnap
import com.jervisffb.engine.common.procedures.tables.kickoff.SolidDefense
import com.jervisffb.engine.common.procedures.tables.prayers.BadHabits
import com.jervisffb.engine.common.procedures.tables.prayers.ResolveThrowARock
import com.jervisffb.engine.common.procedures.tables.weather.SwelteringHeat
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationHandler
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * This test ensures that we remember to add support for new dice roll types
 * in the probability system.
 *
 * This is done by checking that all dice rolls have a procedure attached
 * that implements the [ChanceObservationHandler] interface. This is not
 * perfect, but should be enough to trigger test errors for new roll types.
 */
class SupportedDiceRollTests {

    @Test
    fun allDiceRollsAreObservable() {
        DiceRollType.entries.forEach { type ->
            val procedure: Procedure? = getProcedure(type)
            val isSupported = procedure is ChanceObservationHandler
            val supportExpected = getExpectedSupport(type)
            assertEquals(supportExpected, isSupported, "Mismatch in expected chance behavior support for: $type")
        }
    }

    private fun getExpectedSupport(type: DiceRollType): Boolean {
        return when (type) {
            DiceRollType.ACCURACY,
            DiceRollType.ALWAYS_HUNGRY,
            DiceRollType.ALWAYS_HUNGRY_EAT_ATTEMPT,
            DiceRollType.ANIMAL_SAVAGERY,
            DiceRollType.ARGUE_THE_CALL,
            DiceRollType.ARMOUR,
            DiceRollType.BAD_HABITS,
            DiceRollType.BB7_APOTHECARY,
            DiceRollType.BLOCK,
            DiceRollType.BONE_HEAD,
            DiceRollType.BOUNCE,
            DiceRollType.BREATHE_FIRE,
            DiceRollType.BRIBE,
            DiceRollType.BRILLIANT_COACHING,
            DiceRollType.CASUALTY,
            DiceRollType.CATCH,
            DiceRollType.CHAINSAW,
            DiceRollType.CHARGE,
            DiceRollType.CHEERING_FANS,
            DiceRollType.CHOMP,
            DiceRollType.COIN_TOSS,
            DiceRollType.DAUNTLESS,
            DiceRollType.DEVIATE,
            DiceRollType.DODGE,
            DiceRollType.DODGY_SNACK_EFFECT,
            DiceRollType.DODGY_SNACK_ROLL_OFF,
            DiceRollType.FAN_FACTOR,
            DiceRollType.FOUL_APPEARANCE,
            DiceRollType.HYPNOTIC_GAZE,
            DiceRollType.INJURY,
            DiceRollType.INTERCEPTION,
            DiceRollType.JUMP,
            DiceRollType.JUMP_UP,
            DiceRollType.KICK_OFF_TABLE,
            DiceRollType.LANDING,
            DiceRollType.LASTING_INJURY,
            DiceRollType.LEAP,
            DiceRollType.LONER,
            DiceRollType.OFFICIOUS_REF_FAN_FACTOR,
            DiceRollType.OFFICIOUS_REF_REFEREE,
            DiceRollType.PASS,
            DiceRollType.PICKUP,
            DiceRollType.PITCH_INVASION_FAN_FACTOR,
            DiceRollType.PITCH_INVASION_PLAYERS_AFFECTED,
            DiceRollType.POGO,
            DiceRollType.PRAYERS_TO_NUFFLE,
            DiceRollType.PRO,
            DiceRollType.PROJECTILE_VOMIT,
            DiceRollType.PUNT_DIRECTION,
            DiceRollType.PUNT_DISTANCE,
            DiceRollType.QUICK_SNAP,
            DiceRollType.REALLY_STUPID,
            DiceRollType.RECOVER_PLAYER,
            DiceRollType.REGENERATION,
            DiceRollType.RUSH,
            DiceRollType.SCATTER,
            DiceRollType.SECURE_THE_BALL,
            DiceRollType.SHADOWING,
            DiceRollType.SOLID_DEFENSE,
            DiceRollType.STANDING_UP,
            DiceRollType.STEADY_FOOTING,
            DiceRollType.SUDDEN_DEATH,
            DiceRollType.SWELTERING_HEAT,
            DiceRollType.SWOOP_DIRECTION,
            DiceRollType.SWOOP_DISTANCE,
            DiceRollType.TAKE_ROOT,
            DiceRollType.TEAM_CAPTAIN,
            DiceRollType.TEAM_MASCOT,
            DiceRollType.TENTACLES,
            DiceRollType.THROWIN_DIRECTION,
            DiceRollType.THROWIN_DISTANCE,
            DiceRollType.THROW_A_ROCK,
            DiceRollType.TREACHEROUS_TRAPDOOR,
            DiceRollType.UNCHANNELLED_FURY,
            DiceRollType.WEATHER -> true

            DiceRollType.BLITZ,
            DiceRollType.BLOODLUST,
            DiceRollType.CROWD_TAKES_ACTION,
            DiceRollType.PASSING_INTERFERENCE,
            DiceRollType.QUALITY -> false
        }
    }

    private fun getProcedure(type: DiceRollType): Procedure? {
        return when (type) {
            DiceRollType.ACCURACY -> PassAccuracyRoll
            DiceRollType.ALWAYS_HUNGRY -> AlwaysHungryRoll
            DiceRollType.ALWAYS_HUNGRY_EAT_ATTEMPT -> AlwaysHungrySquirmFreeRoll
            DiceRollType.ANIMAL_SAVAGERY -> AnimalSavageryRoll
            DiceRollType.ARGUE_THE_CALL -> ArgueTheCallRoll
            DiceRollType.ARMOUR -> ArmourRoll
            DiceRollType.BAD_HABITS -> BadHabits
            DiceRollType.BB7_APOTHECARY -> UseBB7Apothecary
            DiceRollType.BLITZ -> Blitz
            DiceRollType.BLOCK -> SingleStandardBlockRollDice // TODO This doesn't cover all the procedures. We probably need to handle block specifically
            DiceRollType.BLOODLUST -> BloodLustRoll
            DiceRollType.BONE_HEAD -> BoneHeadRoll
            DiceRollType.BOUNCE -> Bounce
            DiceRollType.BREATHE_FIRE -> BreatheFireRoll
            DiceRollType.BRIBE -> BribeRoll
            DiceRollType.BRILLIANT_COACHING -> BrilliantCoaching
            DiceRollType.CASUALTY -> CasualtyRoll
            DiceRollType.CATCH -> CatchRoll
            DiceRollType.CHAINSAW -> ChainsawRoll
            DiceRollType.CHARGE -> Charge
            DiceRollType.CHEERING_FANS -> BB2025CheeringFans
            DiceRollType.CHOMP -> ChompRoll
            DiceRollType.COIN_TOSS -> DetermineKickingTeamStep
            DiceRollType.DAUNTLESS -> DauntlessRoll
            DiceRollType.DEVIATE -> DeviateRoll
            DiceRollType.DODGE -> DodgeRoll
            DiceRollType.DODGY_SNACK_EFFECT -> DodgySnack
            DiceRollType.DODGY_SNACK_ROLL_OFF -> DodgySnack
            DiceRollType.FAN_FACTOR -> FanFactorRolls
            DiceRollType.FOUL_APPEARANCE -> FoulAppearanceRoll
            DiceRollType.HYPNOTIC_GAZE -> HypnoticGazeRoll
            DiceRollType.INJURY -> InjuryRoll
            DiceRollType.INTERCEPTION -> InterceptionRoll
            DiceRollType.JUMP -> JumpRoll
            DiceRollType.JUMP_UP -> JumpUpRoll
            DiceRollType.KICK_OFF_TABLE -> TheKickOffEvent
            DiceRollType.LANDING -> LandingRoll
            DiceRollType.LASTING_INJURY -> LastingInjuryRoll
            DiceRollType.LEAP -> LeapRoll
            DiceRollType.LONER -> LonerRoll
            DiceRollType.OFFICIOUS_REF_FAN_FACTOR,
            DiceRollType.OFFICIOUS_REF_REFEREE -> OfficiousRef
            DiceRollType.PASS -> PassAccuracyRoll
            DiceRollType.PICKUP -> PickupRoll
            DiceRollType.PITCH_INVASION_FAN_FACTOR,
            DiceRollType.PITCH_INVASION_PLAYERS_AFFECTED -> PitchInvasion
            DiceRollType.POGO -> PogoRoll
            DiceRollType.PRAYERS_TO_NUFFLE -> PrayersToNuffleRoll
            DiceRollType.PRO -> ProRoll
            DiceRollType.PROJECTILE_VOMIT -> ProjectileVomitRoll
            DiceRollType.PUNT_DIRECTION -> PuntDirectionRoll
            DiceRollType.PUNT_DISTANCE -> PuntDistanceRoll
            DiceRollType.QUICK_SNAP -> QuickSnap
            DiceRollType.REALLY_STUPID -> ReallyStupidRoll
            DiceRollType.RECOVER_PLAYER -> RecoverPlayerRoll
            DiceRollType.REGENERATION -> RegenerationRoll
            DiceRollType.RUSH -> RushRoll
            DiceRollType.SCATTER -> ScatterRoll
            DiceRollType.SECURE_THE_BALL -> SecureTheBallRoll
            DiceRollType.SHADOWING -> ShadowingRoll
            DiceRollType.SOLID_DEFENSE -> SolidDefense
            DiceRollType.STANDING_UP -> StandingUpRoll
            DiceRollType.STEADY_FOOTING -> SteadyFootingRoll
            DiceRollType.SUDDEN_DEATH -> SuddenDeathStep
            DiceRollType.SWELTERING_HEAT -> SwelteringHeat
            DiceRollType.SWOOP_DIRECTION -> SwoopDirectionRoll
            DiceRollType.SWOOP_DISTANCE -> SwoopDistanceRoll
            DiceRollType.TAKE_ROOT -> TakeRootRoll
            DiceRollType.TEAM_CAPTAIN -> TeamCaptainRoll
            DiceRollType.TEAM_MASCOT -> TeamMascotRoll
            DiceRollType.TENTACLES -> TentaclesRoll
            DiceRollType.THROWIN_DIRECTION,
            DiceRollType.THROWIN_DISTANCE -> ThrowIn
            DiceRollType.THROW_A_ROCK -> ResolveThrowARock
            DiceRollType.TREACHEROUS_TRAPDOOR -> MovePlayerIntoSquare
            DiceRollType.UNCHANNELLED_FURY -> UnchannelledFuryRoll
            DiceRollType.WEATHER -> WeatherRoll

            DiceRollType.BLOODLUST,
            DiceRollType.CROWD_TAKES_ACTION,
            DiceRollType.PASSING_INTERFERENCE, // Not supported in BB2025
            DiceRollType.QUALITY -> null // Not supported in BB2025
        }
    }
}
