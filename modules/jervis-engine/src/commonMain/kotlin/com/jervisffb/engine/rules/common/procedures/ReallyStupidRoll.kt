package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.commands.AddPlayerStatusEffect
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetHasTackleZones
import com.jervisffb.engine.commands.SetOldContext
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportRerollUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.calculateAvailableRerollsFor

data class ReallyStupidRollContext(
    val player: Player,
    // A Team-mate is adjacent and can offer a +2 modifier to the dice roll
    val helpAvailable: Boolean,
    val roll: D6DieRoll,
    val isSuccess: Boolean
) : ProcedureContext {
    val rerolled: Boolean = roll.rerollSource != null && roll.rerolledResult != null
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
object ReallyStupidRoll: Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
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

    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<ActivatePlayerContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val activateContext = state.getContext<ActivatePlayerContext>()
                val player = activateContext.player
                val helpAvailable = player.coordinates.getSurroundingCoordinates(rules)
                    .mapNotNull { state.field[it].player }
                    .filter { it.team == player.team }
                    .any { helper -> !helper.hasSkill(SkillType.REALLY_STUPID) && !rules.isDistracted(helper) }
                val isSuccess = calculateSuccess(d6, helpAvailable)
                val rollContext = ReallyStupidRollContext(
                    state.activePlayer!!,
                    helpAvailable,
                    D6DieRoll.create(state, d6),
                    isSuccess
                )
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.REALLY_STUPID, d6),
                    AddContext(rollContext),
                    GotoNode(ChooseReRollSource),
                )
            }
        }
    }

    object ChooseReRollSource : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<ReallyStupidRollContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<ReallyStupidRollContext>()
            val availableRerolls = calculateAvailableRerollsFor(
                rules = rules,
                player = context.player,
                type = DiceRollType.REALLY_STUPID,
                roll = context.roll,
                firstRollWasSuccess = context.isSuccess
            )
            return if (availableRerolls == null) {
                listOf(ContinueWhenReady)
            } else {
                listOf(SelectNoReroll(context.isSuccess)) + availableRerolls
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue -> ExitProcedure()
                is NoRerollSelected -> ExitProcedure()
                is RerollOptionSelected -> {
                    val rerollContext = UseRerollContext(DiceRollType.REALLY_STUPID, action.getRerollSource(state))
                    compositeCommandOf(
                        SetOldContext(Game::rerollContext, rerollContext),
                        ReportRerollUsed(action.getRerollSource(state)),
                        GotoNode(UseRerollSource),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object UseRerollSource : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return state.rerollContext!!.source.rerollProcedure
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.rerollContext!!
            return if (context.rerollAllowed) {
                GotoNode(ReRollDie)
            } else {
                ExitProcedure()
            }
        }
    }

    object ReRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<ReallyStupidRollContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val rollResultContext = state.getContext<ReallyStupidRollContext>()
                val isSuccess = calculateSuccess(d6, rollResultContext.helpAvailable)
                val rollContext = rollResultContext.copy(
                    roll = rollResultContext.roll.copyReroll(
                        rerollSource = state.rerollContext!!.source,
                        rerolledResult = d6,
                    ),
                    isSuccess = isSuccess
                )
                compositeCommandOf(
                    AddContext(rollContext),
                    ExitProcedure(),
                )
            }
        }
    }

    private fun calculateSuccess(d6: D6Result, hasHelp: Boolean): Boolean {
        val modifier = if (hasHelp) 2 else 0
        val isSuccess = d6.value + modifier >= 4
        return isSuccess
    }
}
