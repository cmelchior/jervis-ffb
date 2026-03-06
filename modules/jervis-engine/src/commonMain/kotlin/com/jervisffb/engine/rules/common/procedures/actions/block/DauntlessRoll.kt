package com.jervisffb.engine.rules.common.procedures.actions.block

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
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetOldContext
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.DauntlessStrengthModifier
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.reports.ReportDauntlessResult
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportRerollUsed
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.calculateAvailableRerollsFor

data class DauntlessRollContext(
    val attacker: Player,
    val defender: Player,
    val roll: D6DieRoll? = null,
    val modifier: StatModifier? = null
): ProcedureContext {
    val isSuccess = (modifier != null)
}

/**
 * Implement the Dauntless Roll as described on page 127 in the BB2025 rulebook.
 *
 * The result is stored in [DauntlessRollContext] and it is up to the caller to
 * determine what to do with the result.
 */
object DauntlessRoll: Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<DauntlessRollContext>()
        return ReportSkillUsed(context.attacker, SkillType.DAUNTLESS)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<DauntlessRollContext>()
        return ReportDauntlessResult(context)
    }
    override fun isValid(state: Game, rules: Rules) = state.assertContext<DauntlessRollContext>()

    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<DauntlessRollContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<DauntlessRollContext>()
                val updatedContext = context.copy(
                    roll = D6DieRoll.create(state, d6),
                    modifier = calculateModifier(context.attacker, context.defender, d6),
                )
                return compositeCommandOf(
                    SetContext(updatedContext),
                    ReportDiceRoll(DiceRollType.DAUNTLESS, d6),
                    GotoNode(ChooseReRollSource),
                )
            }
        }
    }

    object ChooseReRollSource : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<DauntlessRollContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<DauntlessRollContext>()
            val player = context.attacker
            val availableRerolls = calculateAvailableRerollsFor(
                rules,
                player,
                DiceRollType.DAUNTLESS,
                context.roll!!,
                context.isSuccess
            )
            return if (availableRerolls == null) {
                listOf(ContinueWhenReady)
            } else {
                listOf(SelectNoReroll(null)) + availableRerolls
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue -> ExitProcedure()
                is NoRerollSelected -> ExitProcedure()
                is RerollOptionSelected -> {
                    val rerollContext = UseRerollContext(DiceRollType.DAUNTLESS, action.getRerollSource(state))
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
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = state.rerollContext!!.source.rerollProcedure
        override fun onExitNode(state: Game, rules: Rules): Command {
            return if (state.rerollContext!!.rerollAllowed) {
                GotoNode(ReRollDie)
            } else {
                ExitProcedure()
            }
        }
    }

    object ReRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<DauntlessRollContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<DauntlessRollContext>()
                val updatedContext = context.copy(
                    roll = context.roll!!.copyReroll(
                        rerollSource = state.rerollContext!!.source,
                        rerolledResult = d6,
                    ),
                    modifier = calculateModifier(context.attacker, context.defender, d6),
                )
                compositeCommandOf(
                    SetContext(updatedContext),
                    ReportDiceRoll(DiceRollType.DAUNTLESS, d6),
                    ExitProcedure(),
                )
            }
        }
    }

    // -- HELPER FUNCTIONS --
    fun calculateModifier(attacker: Player, defender: Player, d6: D6Result): StatModifier? {
        // Dauntless is based on "unmodified strength", which we interpret as strength without
        // modifiers that does not have a PERMANENT duration.
        val defenderStrength = defender.strengthModifiers.filter { it.expiresAt == Duration.PERMANENT }.sumOf { it.modifier } + defender.baseStrength
        val attackerStrength = attacker.strengthModifiers.filter { it.expiresAt == Duration.PERMANENT }.sumOf { it.modifier } + attacker.baseStrength
        val isDefenderWeaker = attackerStrength >= defenderStrength
        // We should never encounter this, but better safe than sorry
        if (isDefenderWeaker) {
            return null
        }
        val diff = defenderStrength - attackerStrength
        return if (d6.value > diff) {
            DauntlessStrengthModifier(modifier = diff)
        } else {
            null
        }
    }
}
