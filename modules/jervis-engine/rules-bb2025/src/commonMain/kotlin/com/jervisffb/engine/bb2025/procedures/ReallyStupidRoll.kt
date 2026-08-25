package com.jervisffb.engine.bb2025.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.bb2025.modifiers.distracted
import com.jervisffb.engine.bb2025.procedures.getResetChompedStateCommands
import com.jervisffb.engine.commands.AddPlayerStatusEffect
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.common.commands.SetHasTackleZones
import com.jervisffb.engine.common.context.ActivatePlayerContext
import com.jervisffb.engine.common.context.ReallyStupidRollContext
import com.jervisffb.engine.common.procedures.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.common.procedures.dicerolls.RerollData
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure for rolling for Really Stupid in BB2025.
 */
object ReallyStupidRoll : D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.REALLY_STUPID
    override val initialNode: Node get() = RollDie

    override fun onEnterRollProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer ?: INVALID_GAME_STATE("Missing active player")
        return AddContext(ReallyStupidRollContext(player))
    }

    override fun onExitRollProcedure(state: Game, rules: Rules): Command {
        val activateContext = state.getContext<ActivatePlayerContext>()
        val context = state.getContext<ReallyStupidRollContext>()
        return buildCompositeCommand {
            add(RemoveContext<ReallyStupidRollContext>())
            if (!context.isSuccess) {
                add(AddPlayerStatusEffect(context.player, PlayerStatusEffect.distracted()))
                getResetChompedStateCommands(context.player, context.player.location, forceRemoveChompedByChomper = true)
                    ?.let(::add)
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
            val context = state.getContext<ReallyStupidRollContext>()
            val player = state.getContext<ActivatePlayerContext>().player
            val helpAvailable = player.coordinates.getSurroundingCoordinates(rules)
                .mapNotNull { state.pitch[it].player }
                .filter { it.team == player.team }
                .any { helper -> !helper.hasSkill(SkillType.REALLY_STUPID) && !rules.isDistracted(helper) }
            return context.copy(
                helpAvailable = helpAvailable,
                roll = D6DieRoll.create(d6),
                isSuccess = calculateSuccess(d6, helpAvailable),
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
            val context = state.getContext<ReallyStupidRollContext>()
            return context.copy(
                roll = context.roll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
                isSuccess = calculateSuccess(d6, context.helpAvailable),
            )
        }
    }

    private fun calculateSuccess(d6: D6Result, hasHelp: Boolean): Boolean {
        val modifier = if (hasHelp) 2 else 0
        return d6.value + modifier >= 4
    }
}
