package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.AddPlayerStatusEffect
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetHasTackleZones
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_GAME_STATE

data class ReallyStupidRollContext(
    val player: Player,
    // A Team-mate is adjacent and can offer a +2 modifier to the dice roll
    val helpAvailable: Boolean = false,
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false,
) : ProcedureContext {
    val rerolled: Boolean = roll?.rerollSource != null && roll.rerolledResult != null
}

/**
 * Procedure for rolling for Really Stupid
 *
 * See page 86 in the BB2020 rulebook.
 * See page 135 in the BB2025 rulebook.
 *
 * This procedure will update [ActivatePlayerContext] with the result of the roll.
 * It is up to the caller of this method to react to it.
 */
object ReallyStupidRoll: D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.REALLY_STUPID
    override val initialNode: Node get() = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer ?: INVALID_GAME_STATE("Missing active player")
        return AddContext(ReallyStupidRollContext(player))
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val activateContext = state.getContext<ActivatePlayerContext>()
        val context = state.getContext<ReallyStupidRollContext>()
        return buildCompositeCommand {
            add(RemoveContext<ReallyStupidRollContext>())
            if (!context.isSuccess) {
                add(AddPlayerStatusEffect(context.player, PlayerStatusEffect.reallyStupid()))
                add(SetHasTackleZones(context.player, false))
                add(
                    UpdateContext(
                        activateContext.copy(
                            rolledForNegaTrait = true,
                            activationEndsImmediately = true,
                            markActionAsUsed = true
                        )
                    )
                )
            }
        }
    }
    override fun getActionOwner(state: Game): Team = state.getContext<ActivatePlayerContext>().player.team

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<ReallyStupidRollContext>()
            val activateContext = state.getContext<ActivatePlayerContext>()
            val player = activateContext.player
            val helpAvailable = player.coordinates.getSurroundingCoordinates(rules)
                .mapNotNull { state.field[it].player }
                .filter { it.team == player.team }
                .any { helper -> !helper.hasSkill(SkillType.REALLY_STUPID) && !rules.isDistracted(helper) }
            val isSuccess = calculateSuccess(d6, helpAvailable)
            return context.copy(
                helpAvailable = helpAvailable,
                roll = D6DieRoll.create(state, d6),
                isSuccess = isSuccess
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<ReallyStupidRollContext>()
            return RerollData(context.player, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val rollResultContext = state.getContext<ReallyStupidRollContext>()
            return rollResultContext.copy(
                roll = rollResultContext.roll!!.copyReroll(
                    rerollSource = state.rerollContext!!.source,
                    rerolledResult = d6,
                ),
                isSuccess = calculateSuccess(d6, rollResultContext.helpAvailable)
            )
        }
    }

    private fun calculateSuccess(d6: D6Result, hasHelp: Boolean): Boolean {
        val modifier = if (hasHelp) 2 else 0
        val isSuccess = d6.value + modifier >= 4
        return isSuccess
    }
}
