@file:OptIn(ExperimentalTime::class)

package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.bb2020.procedures.actions.pass.AccuracyRoll
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.CatchContext
import com.jervisffb.engine.model.context.ChainsawContext
import com.jervisffb.engine.model.context.DodgeRollContext
import com.jervisffb.engine.model.context.HypnoticGazeContext
import com.jervisffb.engine.model.context.InterceptionContext
import com.jervisffb.engine.model.context.InterceptionRollContext
import com.jervisffb.engine.model.context.JumpRollContext
import com.jervisffb.engine.model.context.LandingRollContext
import com.jervisffb.engine.model.context.LeapRollContext
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.PickupRollContext
import com.jervisffb.engine.model.context.PogoRollContext
import com.jervisffb.engine.model.context.PuntContext
import com.jervisffb.engine.model.context.RushRollContext
import com.jervisffb.engine.model.context.ShadowingRollContext
import com.jervisffb.engine.model.context.SteadyFootingRollContext
import com.jervisffb.engine.model.context.TentaclesRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.bb2025.model.context.SecureTheBallRollContext
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.JumpUpRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.JumpUpRollContext
import com.jervisffb.engine.rules.bb2025.procedures.actions.move.LeapRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.move.PogoRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.pass.InterceptionRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.securetheball.SecureTheBallRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate.AlwaysHungryContext
import com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate.AlwaysHungryRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate.AlwaysHungrySquirmFreeRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate.SwoopContext
import com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate.SwoopDistanceRoll
import com.jervisffb.engine.rules.bb2025.procedures.skills.HypnoticGazeRoll
import com.jervisffb.engine.rules.bb2025.procedures.skills.PuntDistanceRoll
import com.jervisffb.engine.rules.bb2025.procedures.skills.ShadowingRoll
import com.jervisffb.engine.rules.bb2025.procedures.skills.TentaclesRoll
import com.jervisffb.engine.rules.common.procedures.AnimalSavageryContext
import com.jervisffb.engine.rules.common.procedures.AnimalSavageryRoll
import com.jervisffb.engine.rules.common.procedures.BoneHeadRoll
import com.jervisffb.engine.rules.common.procedures.BoneHeadRollContext
import com.jervisffb.engine.rules.common.procedures.CatchRoll
import com.jervisffb.engine.rules.common.procedures.PickupRoll
import com.jervisffb.engine.rules.common.procedures.ReallyStupidRoll
import com.jervisffb.engine.rules.common.procedures.ReallyStupidRollContext
import com.jervisffb.engine.rules.common.procedures.RegenerationRoll
import com.jervisffb.engine.rules.common.procedures.SteadyFootingRoll
import com.jervisffb.engine.rules.common.procedures.TakeRootRoll
import com.jervisffb.engine.rules.common.procedures.TakeRootRollContext
import com.jervisffb.engine.rules.common.procedures.UnchannelledFuryRoll
import com.jervisffb.engine.rules.common.procedures.UnchannelledFuryRollContext
import com.jervisffb.engine.rules.common.procedures.actions.block.BreatheFireContext
import com.jervisffb.engine.rules.common.procedures.actions.block.BreatheFireRoll
import com.jervisffb.engine.rules.common.procedures.actions.block.ChainsawRoll
import com.jervisffb.engine.rules.common.procedures.actions.block.ChompContext
import com.jervisffb.engine.rules.common.procedures.actions.block.ChompRoll
import com.jervisffb.engine.rules.common.procedures.actions.block.DauntlessRoll
import com.jervisffb.engine.rules.common.procedures.actions.block.DauntlessRollContext
import com.jervisffb.engine.rules.common.procedures.actions.block.FoulAppearanceContext
import com.jervisffb.engine.rules.common.procedures.actions.block.FoulAppearanceRoll
import com.jervisffb.engine.rules.common.procedures.actions.block.ProjectileVomitContext
import com.jervisffb.engine.rules.common.procedures.actions.block.ProjectileVomitRoll
import com.jervisffb.engine.rules.common.procedures.actions.move.DodgeRoll
import com.jervisffb.engine.rules.common.procedures.actions.move.JumpRoll
import com.jervisffb.engine.rules.common.procedures.actions.move.RushRoll
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.LandingRoll
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowTeamMateContext
import com.jervisffb.engine.rules.common.procedures.rerolls.LonerRoll
import com.jervisffb.engine.rules.common.procedures.rerolls.LonerRollContext
import com.jervisffb.engine.rules.common.procedures.rerolls.MascotRollContext
import com.jervisffb.engine.rules.common.procedures.rerolls.ProRoll
import com.jervisffb.engine.rules.common.procedures.rerolls.ProRollContext
import com.jervisffb.engine.rules.common.procedures.rerolls.TeamCaptainRoll
import com.jervisffb.engine.rules.common.procedures.rerolls.TeamCaptainRollContext
import com.jervisffb.engine.rules.common.procedures.rerolls.TeamMascotRoll
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import kotlin.time.ExperimentalTime


/**
 * Abstract class for handling all single D6 with a potential reroll like:
 *
 * - Accuracy
 * - Always Hungry
 * - Always Hungry (Squirm Free)
 * - Animal Savagery
 * - BoneHead
 * - Breathe Fire
 * - Catch
 * - Chainsaw
 * - Chomped
 * - Dauntless
 * - Dodge
 * - Foul Appearance
 * - Hypnotic Gaze
 * - Interception
 * - Jump
 * - Jump Up
 * - Landing
 * - Leap
 * - Loner
 * - Pickup
 * - Pogo
 * - Projectile Vomit
 * - Really Stupid
 * - Rush
 * - Shadowing
 * - Steady Footing
 * - Swoop (Distance)
 * - Take Root
 * - Team Captain
 * - Team Mascot
 * - Unchannelled Fury
 */
abstract class D6WithRerollWheelController : SingleDieWithRerollWheelController<D6Result>() {
    override val allOptions: List<D6Result> = D6Result.allOptions()
}

/**
 * Define the Action Wheel layout when rolling for Accuracy (passing) in BB2025.
 */
object AccuracyBB2020WheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "accuracy"
    override val diceRollType: DiceRollType = DiceRollType.ACCURACY
    override val rollDiceNode: Node = AccuracyRoll.RollDie
    override val chooseRerollSourceNode: Node = AccuracyRoll.ChooseReRollSource
    override val rerollDiceNode: Node = AccuracyRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<PassContext>().thrower.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<PassContext>()
        return context.passingRoll!!.originalRoll
    }
}

/**
 * Define the Action Wheel layout when rolling for Accuracy (passing) in BB2025.
 */
object AccuracyBB2025PassWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "accuracy"
    override val diceRollType: DiceRollType = DiceRollType.ACCURACY
    override val rollDiceNode: Node = com.jervisffb.engine.rules.bb2025.procedures.actions.pass.PassAccuracyRoll.RollDie
    override val chooseRerollSourceNode: Node = com.jervisffb.engine.rules.bb2025.procedures.actions.pass.PassAccuracyRoll.ChooseReRollSource
    override val rerollDiceNode: Node = com.jervisffb.engine.rules.bb2025.procedures.actions.pass.PassAccuracyRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<PassContext>().thrower.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<PassContext>()
        return context.passingRoll!!.originalRoll
    }
}

object AccuracyBB2025ThrowTeamMateWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "quality"
    override val diceRollType: DiceRollType = DiceRollType.ACCURACY
    override val rollDiceNode: Node = com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate.ThrowTeammateAccuracyRoll.RollDie
    override val chooseRerollSourceNode: Node = com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate.ThrowTeammateAccuracyRoll.ChooseReRollSource
    override val rerollDiceNode: Node = com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate.ThrowTeammateAccuracyRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<ThrowTeamMateContext>().thrower.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<ThrowTeamMateContext>()
        return context.qualityRoll!!.originalRoll
    }
}

/**
 * Define the Action Wheel layout when rolling for Catch.
 */
object CatchWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "catch"
    override val diceRollType: DiceRollType = DiceRollType.CATCH
    override val rollDiceNode: Node = CatchRoll.RollDie
    override val chooseRerollSourceNode: Node = CatchRoll.ChooseReRollSource
    override val rerollDiceNode: Node = CatchRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<CatchContext>().catchingPlayer.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<CatchContext>()
        return context.roll!!.originalRoll
    }
}

/**
 * Define the Action Wheel layout when rolling for Chainsaw.
 */
object ChainsawWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "chainsaw"
    override val diceRollType: DiceRollType = DiceRollType.CHAINSAW
    override val rollDiceNode: Node = ChainsawRoll.RollDie
    override val chooseRerollSourceNode: Node = ChainsawRoll.ChooseReRollSource
    override val rerollDiceNode: Node = ChainsawRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<ChainsawContext>().attacker.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<ChainsawContext>()
        return context.kickbackRoll!!.originalRoll
    }
}

/**
 * Define the Action Wheel layout when rolling for Chomp.
 */
object ChompWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "chomped"
    override val diceRollType: DiceRollType = DiceRollType.CHOMP
    override val rollDiceNode: Node = ChompRoll.RollDie
    override val chooseRerollSourceNode: Node = ChompRoll.ChooseReRollSource
    override val rerollDiceNode: Node = ChompRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<ChompContext>().attacker.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<ChompContext>()
        return context.chompRoll!!.originalRoll
    }
}


/**
 * Define the Action Wheel layout when rolling for Dodge.
 */
object DodgeWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "dodge"
    override val diceRollType: DiceRollType = DiceRollType.DODGE
    override val rollDiceNode: Node = DodgeRoll.RollDie
    override val chooseRerollSourceNode: Node = DodgeRoll.ChooseReRollSource
    override val rerollDiceNode: Node = DodgeRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.activePlayer?.coordinates ?: error("Missing active player: $state")
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<DodgeRollContext>()
        return context.roll!!.originalRoll
    }
}

object DauntlessWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "dauntless"
    override val diceRollType: DiceRollType = DiceRollType.DAUNTLESS
    override val rollDiceNode: Node = DauntlessRoll.RollDie
    override val chooseRerollSourceNode: Node = DauntlessRoll.ChooseReRollSource
    override val rerollDiceNode: Node = DauntlessRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        val context = state.getContext<DauntlessRollContext>()
        return context.attacker.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<DauntlessRollContext>()
        return context.roll!!.originalRoll
    }
}

object FoulAppearanceWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "foul-appearance"
    override val diceRollType: DiceRollType = DiceRollType.FOUL_APPEARANCE
    override val rollDiceNode: Node = FoulAppearanceRoll.RollDie
    override val chooseRerollSourceNode: Node = FoulAppearanceRoll.ChooseReRollSource
    override val rerollDiceNode: Node = FoulAppearanceRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        val context = state.getContext<FoulAppearanceContext>()
        return context.defender.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<FoulAppearanceContext>()
        return context.roll!!.originalRoll
    }
}

/**
 * Define the Action Wheel layout when rolling for Interception.
 */
object InterceptionWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "interception"
    override val diceRollType: DiceRollType = DiceRollType.INTERCEPTION
    override val rollDiceNode: Node = InterceptionRoll.RollDie
    override val chooseRerollSourceNode: Node = InterceptionRoll.ChooseReRollSource
    override val rerollDiceNode: Node = InterceptionRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        val context = state.getContext<InterceptionContext>()
        return context.interceptingPlayer?.coordinates ?: error("Missing intercepting player: $state")
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<InterceptionRollContext>()
        return context.roll!!.originalRoll
    }
}

/**
 * Define the Action Wheel layout when rolling for Pickup.
 */
object PickupWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "pickup"
    override val diceRollType: DiceRollType = DiceRollType.PICKUP
    override val rollDiceNode: Node = PickupRoll.RollDie
    override val chooseRerollSourceNode: Node = PickupRoll.ChooseReRollSource
    override val rerollDiceNode: Node = PickupRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): PitchCoordinate? {
        return state.activePlayer?.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<PickupRollContext>()
        return context.roll!!.originalRoll
    }
}

/**
 * Define the Action Wheel layout when rolling for Rush.
 */
object RushWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "rush"
    override val diceRollType: DiceRollType = DiceRollType.RUSH
    override val rollDiceNode: Node = RushRoll.RollDie
    override val chooseRerollSourceNode: Node = RushRoll.ChooseReRollSource
    override val rerollDiceNode: Node = RushRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.activePlayer?.coordinates ?: error("Missing active player: $state")
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<RushRollContext>()
        return context.roll!!.originalRoll
    }
}

/**
 * Define the Action Wheel layout when rolling for Securing the Ball.
 */
object SecureTheBallWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "secure"
    override val diceRollType: DiceRollType = DiceRollType.SECURE_THE_BALL
    override val rollDiceNode: Node = SecureTheBallRoll.RollDie
    override val chooseRerollSourceNode: Node = SecureTheBallRoll.ChooseReRollSource
    override val rerollDiceNode: Node = SecureTheBallRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): PitchCoordinate? {
        return state.activePlayer?.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<SecureTheBallRollContext>()
        return context.roll!!.originalRoll
    }
}


/**
 * Define the Action Wheel layout when rolling for Shadowing.
 */
object ShadowingWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "shadowing"
    override val diceRollType: DiceRollType = DiceRollType.SHADOWING
    override val rollDiceNode: Node = ShadowingRoll.RollDie
    override val chooseRerollSourceNode: Node = ShadowingRoll.ChooseReRollSource
    override val rerollDiceNode: Node = ShadowingRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<ShadowingRollContext>().player.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<ShadowingRollContext>()
        return context.roll!!.originalRoll
    }
}

object SteadyFootingWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "steady-footing"
    override val diceRollType: DiceRollType = DiceRollType.STEADY_FOOTING
    override val rollDiceNode: Node = SteadyFootingRoll.RollDie
    override val chooseRerollSourceNode: Node = SteadyFootingRoll.ChooseReRollSource
    override val rerollDiceNode: Node = SteadyFootingRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<SteadyFootingRollContext>().player.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<SteadyFootingRollContext>()
        return context.roll!!.originalRoll
    }
}

object TakeRootWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "take-root"
    override val diceRollType: DiceRollType = DiceRollType.TAKE_ROOT
    override val rollDiceNode: Node = TakeRootRoll.RollDie
    override val chooseRerollSourceNode: Node = TakeRootRoll.ChooseReRollSource
    override val rerollDiceNode: Node = TakeRootRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<ActivatePlayerContext>().player.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<TakeRootRollContext>()
        return context.roll?.originalRoll ?: error("No roll found in context")
    }
}

/**
 * Define the Action-Wheel layout when rolling to Jump.
 */
object JumpWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "jump"
    override val diceRollType: DiceRollType = DiceRollType.JUMP
    override val rollDiceNode: Node = JumpRoll.RollDie
    override val chooseRerollSourceNode: Node = JumpRoll.ChooseReRollSource
    override val rerollDiceNode: Node = JumpRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<MoveContext>().player.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<JumpRollContext>()
        return context.roll!!.originalRoll
    }
}

object JumpUpWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "jump-up"
    override val diceRollType: DiceRollType = DiceRollType.JUMP_UP
    override val rollDiceNode: Node = JumpUpRoll.RollDie
    override val chooseRerollSourceNode: Node = JumpUpRoll.ChooseReRollSource
    override val rerollDiceNode: Node = JumpUpRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<JumpUpRollContext>().player.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<JumpUpRollContext>()
        return context.roll!!.originalRoll
    }
}

/**
 * Define the Action-Wheel layout when rolling for Bone Head.
 */
object BoneHeadWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "bonehead"
    override val diceRollType: DiceRollType = DiceRollType.BONE_HEAD
    override val rollDiceNode: Node = BoneHeadRoll.RollDie
    override val chooseRerollSourceNode: Node = BoneHeadRoll.ChooseReRollSource
    override val rerollDiceNode: Node = BoneHeadRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<ActivatePlayerContext>().player.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<BoneHeadRollContext>()
        return context.roll?.originalRoll ?: error("No roll found in context")
    }
}

object AlwaysHungryWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "always-hungry"
    override val diceRollType: DiceRollType = DiceRollType.ALWAYS_HUNGRY
    override val rollDiceNode: Node = AlwaysHungryRoll.RollDie
    override val chooseRerollSourceNode: Node = AlwaysHungryRoll.ChooseReRollSource
    override val rerollDiceNode: Node = AlwaysHungryRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<AlwaysHungryContext>().thrower.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<AlwaysHungryContext>()
        return context.isHungryRoll?.originalRoll ?: error("No roll found in context")
    }
}

object AlwaysHungrySquirmFreeWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "squirm-free"
    override val diceRollType: DiceRollType = DiceRollType.ALWAYS_HUNGRY_EAT_ATTEMPT
    override val rollDiceNode: Node = AlwaysHungrySquirmFreeRoll.RollDie
    override val chooseRerollSourceNode: Node = AlwaysHungrySquirmFreeRoll.ChooseReRollSource
    override val rerollDiceNode: Node = AlwaysHungrySquirmFreeRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<AlwaysHungryContext>().thrownPlayer.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<AlwaysHungryContext>()
        return context.squirmFreeRoll?.originalRoll ?: error("No roll found in context")
    }
}

/**
 * Define the Action-Wheel layout when rolling for Animal Savagery.
 */
object AnimalSavageryWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "animal-savagery"
    override val diceRollType: DiceRollType = DiceRollType.ANIMAL_SAVAGERY
    override val rollDiceNode: Node = AnimalSavageryRoll.RollDie
    override val chooseRerollSourceNode: Node = AnimalSavageryRoll.ChooseReRollSource
    override val rerollDiceNode: Node = AnimalSavageryRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<AnimalSavageryContext>().player.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<AnimalSavageryContext>()
        return context.roll?.originalRoll ?: error("No roll found in context")
    }
}

/**
 * Define the Action-Wheel layout when rolling for Bone Head.
 */
object ReallyStupidWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "really-stupid"
    override val diceRollType: DiceRollType = DiceRollType.REALLY_STUPID
    override val rollDiceNode: Node = ReallyStupidRoll.RollDie
    override val chooseRerollSourceNode: Node = ReallyStupidRoll.ChooseReRollSource
    override val rerollDiceNode: Node = ReallyStupidRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<ActivatePlayerContext>().player.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<ReallyStupidRollContext>()
        return context.roll?.originalRoll ?: error("No roll found in context")
    }
}

/**
 * Define the Action-Wheel layout when rolling for Regeneration.
 *
 * Note that [RegenerationRoll.RerollUsingInducement] handles a reroll triggered
 * by a Mortuary Assistant or Plague Doctor and goes through the same dice
 * surface as the initial roll.
 */
object RegenerationWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "regeneration"
    override val diceRollType: DiceRollType = DiceRollType.REGENERATION
    override val rollDiceNode: Node = RegenerationRoll.RollDie
    override val chooseRerollSourceNode: Node = RegenerationRoll.ChooseReRollSource
    override val rerollDiceNode: Node = RegenerationRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate? {
        val player = state.getContext<RiskingInjuryContext>().player
        return when {
            player.location.isOnPitch(state.rules) -> player.coordinates
            player.team.isHomeTeam() -> getHomeCenterCoordinates(state)
            else -> getAwayCenterCoordinates(state)
        }
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<RiskingInjuryContext>()
        return context.regenerationRoll?.originalRoll ?: error("No roll found in context")
    }
}

/**
 * Define the Action-Wheel layout when rolling for Unchannelled Fury.
 */
object UnchannelledFuryWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "unchannelled-fury"
    override val diceRollType: DiceRollType = DiceRollType.UNCHANNELLED_FURY
    override val rollDiceNode: Node = UnchannelledFuryRoll.RollDie
    override val chooseRerollSourceNode: Node = UnchannelledFuryRoll.ChooseReRollSource
    override val rerollDiceNode: Node = UnchannelledFuryRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<ActivatePlayerContext>().player.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<UnchannelledFuryRollContext>()
        return context.roll?.originalRoll ?: error("No roll found in context")
    }
}

/**
 * Define the Action-Wheel layout when rolling for Leap.
 */
object LeapWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "leap"
    override val diceRollType: DiceRollType = DiceRollType.LEAP
    override val rollDiceNode: Node = LeapRoll.RollDie
    override val chooseRerollSourceNode: Node = LeapRoll.ChooseReRollSource
    override val rerollDiceNode: Node = LeapRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<LeapRollContext>().player.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<LeapRollContext>()
        return context.roll?.originalRoll!!
    }
}

object LonerWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "loner"
    override val diceRollType: DiceRollType = DiceRollType.LONER
    override val rollDiceNode: Node = LonerRoll.RollDie
    override val chooseRerollSourceNode: Node = LonerRoll.ChooseReRollSource
    override val rerollDiceNode: Node = LonerRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<LonerRollContext>().player.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<LonerRollContext>()
        return context.roll?.originalRoll!!
    }
}

/**
 * Define the Action-Wheel layout when rolling for Pogo.
 */
object PogoWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "pogo"
    override val diceRollType: DiceRollType = DiceRollType.POGO
    override val rollDiceNode: Node = PogoRoll.RollDie
    override val chooseRerollSourceNode: Node = PogoRoll.ChooseReRollSource
    override val rerollDiceNode: Node = PogoRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<PogoRollContext>().player.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<PogoRollContext>()
        return context.roll?.originalRoll!!
    }
}

/**
 * Define the Action-Wheel layout when rolling for Landing after being thrown.
 */
object LandingWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "landing"
    override val diceRollType: DiceRollType = DiceRollType.LANDING
    override val rollDiceNode: Node = LandingRoll.RollDie
    override val chooseRerollSourceNode: Node = LandingRoll.ChooseReRollSource
    override val rerollDiceNode: Node = LandingRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<ThrowTeamMateContext>().thrownPlayer!!.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<LandingRollContext>()
        return context.roll?.originalRoll!!
    }
}

object ProWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "pro"
    override val diceRollType: DiceRollType = DiceRollType.PRO
    override val rollDiceNode: Node = ProRoll.RollDie
    override val chooseRerollSourceNode: Node = ProRoll.ChooseReRollSource
    override val rerollDiceNode: Node = ProRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<ProRollContext>().player.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<ProRollContext>()
        return context.roll?.originalRoll!!
    }
}

object ProjectileVomitWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "projectile-vomit"
    override val diceRollType: DiceRollType = DiceRollType.PROJECTILE_VOMIT
    override val rollDiceNode: Node = ProjectileVomitRoll.RollDie
    override val chooseRerollSourceNode: Node = ProjectileVomitRoll.ChooseReRollSource
    override val rerollDiceNode: Node = ProjectileVomitRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<ProjectileVomitContext>().attacker.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<ProjectileVomitContext>()
        return context.vomitRoll?.originalRoll!!
    }
}

object HypnoticGazeWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "hypnotic-gaze"
    override val diceRollType: DiceRollType = DiceRollType.HYPNOTIC_GAZE
    override val rollDiceNode: Node = HypnoticGazeRoll.RollDie
    override val chooseRerollSourceNode: Node = HypnoticGazeRoll.ChooseReRollSource
    override val rerollDiceNode: Node = HypnoticGazeRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<HypnoticGazeContext>().gazer.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<HypnoticGazeContext>()
        return context.roll?.originalRoll!!
    }
}

object BreatheFireWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "breathe-fire"
    override val diceRollType: DiceRollType = DiceRollType.BREATHE_FIRE
    override val rollDiceNode: Node = BreatheFireRoll.RollDie
    override val chooseRerollSourceNode: Node = BreatheFireRoll.ChooseReRollSource
    override val rerollDiceNode: Node = BreatheFireRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<BreatheFireContext>().attacker.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<BreatheFireContext>()
        return context.breatheRoll?.originalRoll!!
    }
}

object SwoopDistanceWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "swoop-distance"
    override val diceRollType: DiceRollType = DiceRollType.SWOOP_DISTANCE
    override val rollDiceNode: Node = SwoopDistanceRoll.RollDie
    override val chooseRerollSourceNode: Node = SwoopDistanceRoll.ChooseReRollSource
    override val rerollDiceNode: Node = SwoopDistanceRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<SwoopContext>().player.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<SwoopContext>()
        return context.distanceRoll?.originalRoll!!
    }
}

object TeamCaptainWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "team-captain"
    override val diceRollType: DiceRollType = DiceRollType.TEAM_CAPTAIN
    override val rollDiceNode: Node = TeamCaptainRoll.RollDie
    override val chooseRerollSourceNode: Node = TeamCaptainRoll.ChooseReRollSource
    override val rerollDiceNode: Node = TeamCaptainRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<TeamCaptainRollContext>().player.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<TeamCaptainRollContext>()
        return context.roll?.originalRoll!!
    }
}

object TeamMascotWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "team-mascot"
    override val diceRollType: DiceRollType = DiceRollType.TEAM_MASCOT
    override val rollDiceNode: Node = TeamMascotRoll.RollDie
    override val chooseRerollSourceNode: Node = TeamMascotRoll.ChooseReRollSource
    override val rerollDiceNode: Node = TeamMascotRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate? {
        val context = state.getRerollContextOrNull()
        val team = context?.team
        val player = context?.player
        return when {
            (player != null && player.location.isOnPitch(state.rules)) -> player.coordinates
            (team != null) -> when (team.isHomeTeam()) {
                true -> getHomeCenterCoordinates(state)
                false -> getAwayCenterCoordinates(state)
            }
            else -> null
        }
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<MascotRollContext>()
        return context.roll?.originalRoll!!
    }
}

object TentaclesWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "tentacles"
    override val diceRollType: DiceRollType = DiceRollType.TENTACLES
    override val rollDiceNode: Node = TentaclesRoll.RollDie
    override val chooseRerollSourceNode: Node = TentaclesRoll.ChooseReRollSource
    override val rerollDiceNode: Node = TentaclesRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate? {
        return state.getContext<TentaclesRollContext>().tentaclePlayer!!.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<TentaclesRollContext>()
        return context.roll?.originalRoll!!
    }
}

object PuntDistanceWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "punt-distance"
    override val diceRollType: DiceRollType = DiceRollType.PUNT_DISTANCE
    override val rollDiceNode: Node = PuntDistanceRoll.RollDie
    override val chooseRerollSourceNode: Node = PuntDistanceRoll.ChooseReRollSource
    override val rerollDiceNode: Node = PuntDistanceRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): PitchCoordinate? {
        val context = state.getContext<PuntContext>()
        return context.punter.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<PuntContext>()
        return context.distanceRoll?.originalRoll!!
    }
}





