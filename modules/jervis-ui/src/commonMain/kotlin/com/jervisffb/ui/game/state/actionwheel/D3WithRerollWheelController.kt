package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate.SwoopContext
import com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate.SwoopDirectionRoll


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
