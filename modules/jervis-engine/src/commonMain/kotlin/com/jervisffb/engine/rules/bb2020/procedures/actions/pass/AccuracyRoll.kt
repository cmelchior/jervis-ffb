package com.jervisffb.engine.rules.bb2020.procedures.actions.pass

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.AccuracyModifier
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassingType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.Range
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.sum
import kotlinx.collections.immutable.toPersistentList

/**
 * Implement the Accuracy Roll as described on page 49 in the BB2020 rulebook.
 *
 * The result is stored in [com.jervisffb.engine.rules.common.procedures.actions.pass.PassContext] and it is up
 * to the caller to determine what to do with the result.
 *
 * Developer's Commentary:
 * RAW rules would allow you to change your mind about applying the modifier
 * if you reroll the die. But I do not see a use case where that would be
 * relevant (outside misclicking).
 *
 * So for this roll, we only ask once after rolling the first die.
 */
object AccuracyRoll: D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.ACCURACY
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command {
        val context = setInitialModifiers(state, rules)
        return UpdateContext(context)
    }
    override fun onExitRollProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<PassContext>()
        val updatedContext = updatePassContextWithResult(context)
        return UpdateContext(updatedContext)
    }
    override fun isValid(state: Game, rules: Rules) = state.assertContext<PassContext>()
    override fun getActionOwner(state: Game): Team = state.getContext<PassContext>().thrower.team

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<PassContext>()
            return context.copy(passingRoll = D6DieRoll.create(state, d6))
        }
        override val nextNode: Node = ChooseToUseAccurate
    }

    object ChooseToUseAccurate: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state)
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PassContext>()
            val isApplicable = when (context.range) {
                Range.QUICK_PASS,
                Range.SHORT_PASS, -> true
                else -> false
            }
            return if (isApplicable && context.thrower.isSkillAvailable(SkillType.ACCURATE)) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<PassContext>()
            return compositeCommandOf(
                when (action) {
                    Confirm -> {
                        ReportSkillUsed(context.thrower, SkillType.ACCURATE)
                        UpdateContext(context.copyAndAdd(passingModifier = AccuracyModifier.ACCURATE))
                    }

                    Cancel,
                    Continue -> null

                    else -> INVALID_ACTION(action)
                },
                GotoNode(ChooseToUseCannoneer)
            )
        }
    }

    object ChooseToUseCannoneer: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state)
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PassContext>()
            val isApplicable = when (context.range) {
                Range.LONG_PASS,
                Range.LONG_BOMB, -> true
                else -> false
            }
            return if (isApplicable && context.thrower.isSkillAvailable(SkillType.CANNONEER)) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<PassContext>()
            return compositeCommandOf(
                when (action) {
                    Confirm -> {
                        ReportSkillUsed(context.thrower, SkillType.CANNONEER)
                        UpdateContext(context.copyAndAdd(passingModifier = AccuracyModifier.CANNONEER))
                    }

                    Cancel,
                    Continue -> null

                    else -> INVALID_ACTION(action)
                },
                GotoNode(ChooseReRollSource)
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<PassContext>()
            val player = context.thrower
            return RerollData(
                player = player,
                roll = context.passingRoll!!,
                isSuccess = null,
            )
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<PassContext>()
            return context.copy(
                passingRoll = context.passingRoll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6
                )
            )
        }
    }

    // HELPER METHODS

    private fun setInitialModifiers(state: Game, rules: Rules): PassContext {
        val context = state.getContext<PassContext>()
        val modifiers = mutableListOf<DiceModifier>()

        // Range modifier
        when (context.range) {
            Range.QUICK_PASS -> null
            Range.SHORT_PASS -> AccuracyModifier.SHORT_PASS
            Range.LONG_PASS -> AccuracyModifier.LONG_PASS
            Range.LONG_BOMB -> AccuracyModifier.LONG_BOMB
            else -> INVALID_GAME_STATE("Unsupported range: ${context.range}")
        }?.let { modifiers.add(it) }

        // Marked modifiers for thrower
        rules.addMarkedModifiers(
            state,
            context.thrower.team,
            context.thrower.coordinates,
            modifiers,
            AccuracyModifier.MARKED
        )

        // Weather
        if (state.weather == Weather.VERY_SUNNY) {
            modifiers.add(AccuracyModifier.VERY_SUNNY)
        }

        // Are there other accuracy modifiers? (Like disturbing presence)
        // TODO

        return context.copy(
            passingModifiers = modifiers.toPersistentList()
        )
    }

    private fun updatePassContextWithResult(context: PassContext): PassContext {
        val d6 = context.passingRoll!!.result
        val passingStat = context.thrower.passing ?: Int.MAX_VALUE
        val modifierTotal = context.passingModifiers.sum()
        val result = when {
            context.thrower.passing == null -> PassingType.FUMBLED
            d6.value == 6 -> PassingType.ACCURATE
            d6.value == 1 -> PassingType.FUMBLED
            // Designers commentary: Rolling 1 after modifiers with PA 1+ is an accurate pass.
            // Designers commentary: Rolling 1 or less after modifiers is Wildly Inaccurate, not
            // just a result of 1.
            d6.value + modifierTotal <= 1 && passingStat != 1 -> PassingType.WILDLY_INACCURATE
            d6.value + modifierTotal >= passingStat -> PassingType.ACCURATE
            d6.value + modifierTotal < passingStat -> PassingType.INACCURATE
            else -> INVALID_GAME_STATE("Unsupported result: ${d6.value}, target: $passingStat, modifierTotal: $modifierTotal")
        }
        return context.copy(
            passingResult = result
        )
    }
}
