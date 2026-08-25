package com.jervisffb.engine.bb2020.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.bb2020.modifiers.boneHead
import com.jervisffb.engine.commands.AddPlayerStatusEffect
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.common.commands.SetHasTackleZones
import com.jervisffb.engine.common.context.ActivatePlayerContext
import com.jervisffb.engine.common.context.BoneHeadRollContext
import com.jervisffb.engine.common.procedures.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.common.procedures.dicerolls.RerollData
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure for rolling for Bone Head in BB2020.
 */
object BoneHeadRoll : D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.BONE_HEAD
    override val initialNode: Node get() = RollDie

    override fun onEnterRollProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer ?: INVALID_GAME_STATE("Missing active player")
        return AddContext(BoneHeadRollContext(player = player))
    }

    override fun onExitRollProcedure(state: Game, rules: Rules): Command {
        val activateContext = state.getContext<ActivatePlayerContext>()
        val context = state.getContext<BoneHeadRollContext>()
        return buildCompositeCommand {
            add(RemoveContext<BoneHeadRollContext>())
            if (!context.isSuccess) {
                add(AddPlayerStatusEffect(context.player, PlayerStatusEffect.boneHead()))
                addAll(
                    SetHasTackleZones(context.player, false),
                    UpdateContext(
                        activateContext.copy(
                            rolledForNegaTrait = true,
                            activationEndsImmediately = true,
                            markActionAsUsed = true,
                        ),
                    ),
                )
            }
        }
    }

    override fun getActionOwner(state: Game): Player = state.getContext<ActivatePlayerContext>().player

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<BoneHeadRollContext>()
            return context.copy(roll = D6DieRoll.create(d6), isSuccess = calculateSuccess(d6))
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<BoneHeadRollContext>()
            return RerollData(context.player, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<BoneHeadRollContext>()
            return context.copy(
                roll = context.roll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
                isSuccess = calculateSuccess(d6),
            )
        }
    }

    private fun calculateSuccess(d6: D6Result): Boolean = d6.value > 1
}
