package com.jervisffb.test.bb2025.propability

import com.jervisffb.engine.bb2025.procedures.actions.block.BreatheFireRoll
import com.jervisffb.engine.bb2025.procedures.actions.block.ChainsawRoll
import com.jervisffb.engine.bb2025.procedures.actions.block.ChompRoll
import com.jervisffb.engine.bb2025.procedures.actions.block.DauntlessRoll
import com.jervisffb.engine.bb2025.procedures.actions.block.JumpUpRoll
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
import com.jervisffb.engine.common.procedures.SuddenDeath
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
import com.jervisffb.engine.common.procedures.tables.kickoff.Charge
import com.jervisffb.engine.common.procedures.tables.kickoff.DodgySnack
import com.jervisffb.engine.common.procedures.tables.kickoff.OfficiousRef
import com.jervisffb.engine.common.procedures.tables.kickoff.PitchInvasion
import com.jervisffb.engine.common.procedures.tables.kickoff.QuickSnap
import com.jervisffb.engine.common.procedures.tables.kickoff.SolidDefense
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
            DiceRollType.BONE_HEAD,
            DiceRollType.BREATHE_FIRE,
            DiceRollType.CATCH,
            DiceRollType.CHAINSAW,
            DiceRollType.CHARGE,
            DiceRollType.COIN_TOSS,
            DiceRollType.CHOMP,
            DiceRollType.DAUNTLESS,
            DiceRollType.DODGE,
            DiceRollType.FOUL_APPEARANCE,
            DiceRollType.FAN_FACTOR,
            DiceRollType.CHEERING_FANS,
            DiceRollType.BRILLIANT_COACHING,
            DiceRollType.DODGY_SNACK_ROLL_OFF,
            DiceRollType.DODGY_SNACK_EFFECT,
            DiceRollType.HYPNOTIC_GAZE,
            DiceRollType.INTERCEPTION,
            DiceRollType.JUMP,
            DiceRollType.JUMP_UP,
            DiceRollType.KICK_OFF_TABLE,
            DiceRollType.LANDING,
            DiceRollType.LEAP,
            DiceRollType.LONER,
            DiceRollType.OFFICIOUS_REF_FAN_FACTOR,
            DiceRollType.OFFICIOUS_REF_REFEREE,
            DiceRollType.PICKUP,
            DiceRollType.POGO,
            DiceRollType.PITCH_INVASION_FAN_FACTOR,
            DiceRollType.PITCH_INVASION_PLAYERS_AFFECTED,
            DiceRollType.PRO,
            DiceRollType.PROJECTILE_VOMIT,
            DiceRollType.PUNT_DISTANCE,
            DiceRollType.QUICK_SNAP,
            DiceRollType.REALLY_STUPID,
            DiceRollType.REGENERATION,
            DiceRollType.RUSH,
            DiceRollType.SECURE_THE_BALL,
            DiceRollType.SHADOWING,
            DiceRollType.STANDING_UP,
            DiceRollType.STEADY_FOOTING,
            DiceRollType.SWOOP_DISTANCE,
            DiceRollType.SOLID_DEFENSE,
            DiceRollType.TAKE_ROOT,
            DiceRollType.TEAM_CAPTAIN,
            DiceRollType.TENTACLES,
            DiceRollType.UNCHANNELLED_FURY,
            DiceRollType.WEATHER -> true

            DiceRollType.ARGUE_THE_CALL,
            DiceRollType.ARMOUR,
            DiceRollType.BAD_HABITS,
            DiceRollType.BB7_APOTHECARY,
            DiceRollType.BLOCK,
            DiceRollType.BLITZ,
            DiceRollType.BLOODLUST,
            DiceRollType.BOUNCE,
            DiceRollType.BRIBE,
            DiceRollType.CASUALTY,
            DiceRollType.CROWD_TAKES_ACTION,
            DiceRollType.FOUL_APPEARANCE,
            DiceRollType.DEVIATE,
            DiceRollType.INJURY,
            DiceRollType.INTERCEPTION,
            DiceRollType.JUMP_UP,
            DiceRollType.LANDING,
            DiceRollType.LASTING_INJURY,
            DiceRollType.LONER,
            DiceRollType.PASS,
            DiceRollType.PASSING_INTERFERENCE,
            DiceRollType.POGO,
            DiceRollType.PRO,
            DiceRollType.PRAYERS_TO_NUFFLE,
            DiceRollType.REALLY_STUPID,
            DiceRollType.REGENERATION,
            DiceRollType.QUALITY,
            DiceRollType.RECOVER_PLAYER,
            DiceRollType.SCATTER,
            DiceRollType.SECURE_THE_BALL,
            DiceRollType.SHADOWING,
            DiceRollType.SUDDEN_DEATH,
            DiceRollType.STANDING_UP,
            DiceRollType.STEADY_FOOTING,
            DiceRollType.SWELTERING_HEAT,
            DiceRollType.SWOOP_DIRECTION,
            DiceRollType.SWOOP_DISTANCE,
            DiceRollType.TAKE_ROOT,
            DiceRollType.TEAM_CAPTAIN,
            DiceRollType.TEAM_MASCOT,
            DiceRollType.TENTACLES,
            DiceRollType.THROW_A_ROCK,
            DiceRollType.THROWIN_DIRECTION,
            DiceRollType.THROWIN_DISTANCE,
            DiceRollType.TREACHEROUS_TRAPDOOR,
            DiceRollType.UNCHANNELLED_FURY,
            DiceRollType.PROJECTILE_VOMIT,
            DiceRollType.PUNT_DIRECTION,
            DiceRollType.PUNT_DISTANCE -> false
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
            DiceRollType.BONE_HEAD -> BoneHeadRoll
            DiceRollType.BREATHE_FIRE -> BreatheFireRoll
            DiceRollType.BB7_APOTHECARY -> UseBB7Apothecary
            DiceRollType.BLITZ -> Blitz
            DiceRollType.BLOODLUST -> BloodLustRoll
            DiceRollType.BOUNCE -> Bounce
            DiceRollType.BRIBE -> BribeRoll
            DiceRollType.BRILLIANT_COACHING -> BrilliantCoaching
            DiceRollType.CATCH -> CatchRoll
            DiceRollType.CHAINSAW -> ChainsawRoll
            DiceRollType.CHOMP -> ChompRoll
            DiceRollType.DAUNTLESS -> DauntlessRoll
            DiceRollType.DODGE -> DodgeRoll
            DiceRollType.DODGY_SNACK_ROLL_OFF,
            DiceRollType.DODGY_SNACK_EFFECT -> DodgySnack
            DiceRollType.DEVIATE -> DeviateRoll
            DiceRollType.FOUL_APPEARANCE -> FoulAppearanceRoll
            DiceRollType.HYPNOTIC_GAZE -> HypnoticGazeRoll
            DiceRollType.INTERCEPTION -> InterceptionRoll
            DiceRollType.INJURY -> InjuryRoll
            DiceRollType.JUMP -> JumpRoll
            DiceRollType.JUMP_UP -> JumpUpRoll
            DiceRollType.LEAP -> LeapRoll
            DiceRollType.LANDING -> LandingRoll
            DiceRollType.LONER -> LonerRoll
            DiceRollType.CASUALTY -> CasualtyRoll
            DiceRollType.CHEERING_FANS -> BB2025CheeringFans
            DiceRollType.CHARGE -> Charge
            DiceRollType.COIN_TOSS -> DetermineKickingTeamStep
            DiceRollType.KICK_OFF_TABLE -> TheKickOffEvent
            DiceRollType.FAN_FACTOR -> FanFactorRolls
            DiceRollType.LASTING_INJURY -> LastingInjuryRoll
            DiceRollType.PICKUP -> PickupRoll
            DiceRollType.POGO -> PogoRoll
            DiceRollType.PRO -> ProRoll
            DiceRollType.PROJECTILE_VOMIT -> ProjectileVomitRoll
            DiceRollType.PUNT_DIRECTION -> PuntDirectionRoll
            DiceRollType.PUNT_DISTANCE -> PuntDistanceRoll
            DiceRollType.REALLY_STUPID -> ReallyStupidRoll
            DiceRollType.REGENERATION -> RegenerationRoll
            DiceRollType.RUSH -> RushRoll
            DiceRollType.SECURE_THE_BALL -> SecureTheBallRoll
            DiceRollType.SHADOWING -> ShadowingRoll
            DiceRollType.PITCH_INVASION_FAN_FACTOR,
            DiceRollType.PITCH_INVASION_PLAYERS_AFFECTED -> PitchInvasion
            DiceRollType.OFFICIOUS_REF_FAN_FACTOR,
            DiceRollType.OFFICIOUS_REF_REFEREE -> OfficiousRef
            DiceRollType.STANDING_UP -> StandingUpRoll
            DiceRollType.STEADY_FOOTING -> SteadyFootingRoll
            DiceRollType.PRAYERS_TO_NUFFLE -> PrayersToNuffleRoll
            DiceRollType.QUICK_SNAP -> QuickSnap
            DiceRollType.RECOVER_PLAYER -> RecoverPlayerRoll
            DiceRollType.SCATTER -> ScatterRoll
            DiceRollType.SOLID_DEFENSE -> SolidDefense
            DiceRollType.SUDDEN_DEATH -> SuddenDeath
            DiceRollType.SWELTERING_HEAT -> SwelteringHeat
            DiceRollType.SWOOP_DIRECTION -> SwoopDirectionRoll
            DiceRollType.SWOOP_DISTANCE -> SwoopDistanceRoll
            DiceRollType.TAKE_ROOT -> TakeRootRoll
            DiceRollType.TEAM_CAPTAIN -> TeamCaptainRoll
            DiceRollType.TEAM_MASCOT -> TeamMascotRoll
            DiceRollType.TENTACLES -> TentaclesRoll
            DiceRollType.UNCHANNELLED_FURY -> UnchannelledFuryRoll
            DiceRollType.THROW_A_ROCK -> ResolveThrowARock
            DiceRollType.THROWIN_DIRECTION,
            DiceRollType.THROWIN_DISTANCE -> ThrowIn
            DiceRollType.TREACHEROUS_TRAPDOOR -> MovePlayerIntoSquare
            DiceRollType.WEATHER -> WeatherRoll

            DiceRollType.ARGUE_THE_CALL,
            DiceRollType.ARMOUR,
            DiceRollType.BAD_HABITS,
            DiceRollType.BB7_APOTHECARY,
            DiceRollType.BLOCK,
            DiceRollType.BLITZ,
            DiceRollType.BLOODLUST,
            DiceRollType.BOUNCE,
            DiceRollType.BRIBE,
            DiceRollType.BRILLIANT_COACHING,
            DiceRollType.CASUALTY,
            DiceRollType.CHARGE,
            DiceRollType.CHEERING_FANS,
            DiceRollType.CROWD_TAKES_ACTION,
            DiceRollType.FAN_FACTOR,
            DiceRollType.DAUNTLESS,
            DiceRollType.DODGY_SNACK_ROLL_OFF,
            DiceRollType.DODGY_SNACK_EFFECT,
            DiceRollType.DEVIATE,
            DiceRollType.INJURY,
            DiceRollType.INTERCEPTION,
            DiceRollType.KICK_OFF_TABLE,
            DiceRollType.LASTING_INJURY,
            DiceRollType.OFFICIOUS_REF_FAN_FACTOR,
            DiceRollType.OFFICIOUS_REF_REFEREE,
            DiceRollType.PASS,
            DiceRollType.PASSING_INTERFERENCE,
            DiceRollType.PITCH_INVASION_FAN_FACTOR,
            DiceRollType.PITCH_INVASION_PLAYERS_AFFECTED,
            DiceRollType.PRAYERS_TO_NUFFLE,
            DiceRollType.QUALITY,
            DiceRollType.QUICK_SNAP,
            DiceRollType.RECOVER_PLAYER,
            DiceRollType.SCATTER,
            DiceRollType.SOLID_DEFENSE,
            DiceRollType.SUDDEN_DEATH,
            DiceRollType.SWELTERING_HEAT,
            DiceRollType.TEAM_MASCOT,
            DiceRollType.THROW_A_ROCK,
            DiceRollType.THROWIN_DIRECTION,
            DiceRollType.THROWIN_DISTANCE,
            DiceRollType.TREACHEROUS_TRAPDOOR,
            DiceRollType.WEATHER -> null
        }
    }
}
