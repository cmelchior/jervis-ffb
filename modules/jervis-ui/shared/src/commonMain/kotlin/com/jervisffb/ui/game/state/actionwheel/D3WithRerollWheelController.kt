package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.bb2025.context.SwoopContext
import com.jervisffb.engine.bb2025.procedures.actions.throwteammate.SwoopDirectionRoll
import com.jervisffb.engine.bb2025.skills.PuntDirectionRoll
import com.jervisffb.engine.common.context.PuntContext
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.DiceRollType


/**
 * Abstract class for handling all single D3 with a potential reroll like:
 *
 * - Swoop (Direction)
 */
abstract class D3WithRerollWheelController : SingleDieWithRerollWheelController<D3Result>() {
    override val allOptions: List<D3Result> = D3Result.allOptions()
}

object SwoopDirectionWheelController : D3WithRerollWheelController() {
    override val buttonIdPrefix: String = "swoop-direction"
    override val diceRollType: DiceRollType = DiceRollType.SWOOP_DIRECTION
    override val rollDiceNode: Node = SwoopDirectionRoll.RollDie
    override val chooseRerollSourceNode: Node = SwoopDirectionRoll.ChooseReRollSource
    override val rerollDiceNode: Node = SwoopDirectionRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<SwoopContext>().player.coordinates
    }

    override fun getOriginalRoll(state: Game): D3Result {
        val context = state.getContext<SwoopContext>()
        return context.directionRoll!!.originalRoll
    }
}

object PuntDirectionWheelController : D3WithRerollWheelController() {
    override val buttonIdPrefix: String = "punt-direction"
    override val diceRollType: DiceRollType = DiceRollType.PUNT_DIRECTION
    override val rollDiceNode: Node = PuntDirectionRoll.RollDie
    override val chooseRerollSourceNode: Node = PuntDirectionRoll.ChooseReRollSource
    override val rerollDiceNode: Node = PuntDirectionRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<PuntContext>().punter.coordinates
    }

    override fun getOriginalRoll(state: Game): D3Result {
        val context = state.getContext<PuntContext>()
        return context.directionRoll!!.originalRoll
    }
}
