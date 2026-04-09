package com.jervisffb.engine.rules.bb2025.procedures.actions.pass

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
import com.jervisffb.engine.rules.common.actions.PassType
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
 * Implement the Accuracy Roll for Pass actions as described on page 71 in the
 * BB2025 rulebook.
 *
 * The result is stored in [PassContext] and it is up to the caller to determine
 * what to do with the result.
 *
 * Designer's Commentary:
 * Rules as written, make PA 1+ worthless, which is probably not intended.
 * They recommend the following interpretation
 *
 * If the Passing Ability Test is a 1 or lower after modifiers, or a natural 1,
 * the Pass is Fumbled. PA1+ players pass the test on a modified 1.
 *
 * FUMBBL has adopted a similar interpretation, so Jervis does the same.
 *
 * Developer's Commentary:
 * RAW rules would allow you to change your mind about applying the modifier
 * if you reroll the die. But I do not see a use case where that would be
 * relevant (outside misclicking).
 *
 * So for this roll, we only ask once after rolling the first die.
 */
object PassAccuracyRoll: D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.ACCURACY
    override val initialNode: Node get() = ChooseToUseNervesOfSteel
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val updatedContext = setInitialModifiers(state, rules)
        return UpdateContext(updatedContext)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<PassContext>()
        val updatedContext = updatePassContextWithResult(context)
        return UpdateContext(updatedContext)
    }
    override fun isValid(state: Game, rules: Rules) = state.assertContext<PassContext>()
    override fun getActionOwner(state: Game): Team = state.getContext<PassContext>().thrower.team

    object ChooseToUseNervesOfSteel: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state)
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PassContext>()
            val player = context.thrower
            val hasExtraArms = player.isSkillAvailable(SkillType.NERVES_OF_STEEL)
            return when (hasExtraArms) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<PassContext>()
            val player = context.thrower
            val useNervesOfSteel = (action == Confirm)
            val modifiers = context.passingModifiers.toMutableList()
            if (!useNervesOfSteel) {
                rules.addMarkedModifiers(
                    state,
                    context.thrower.team,
                    context.thrower.coordinates,
                    modifiers,
                    AccuracyModifier.MARKED
                )
            }
            return compositeCommandOf(
                if (useNervesOfSteel) {
                    ReportSkillUsed(player, SkillType.NERVES_OF_STEEL)
                } else {
                    null
                },
                UpdateContext(context.copy(useNervesOfSteel = useNervesOfSteel, passingModifiers = modifiers.toPersistentList())),
                GotoNode(RollDie)
            )
        }
    }

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
            return RerollData(context.thrower, context.passingRoll!!, null)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<PassContext>()
            return context.copy(
                passingRoll = context.passingRoll!!.copyReroll(
                    rerollSource = state.rerollContext!!.source,
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
        val isUsingHailMary = (context.type == PassType.HAIL_MARY_PASS)
        val result = when {
            // The value can be modified below 1. Players with PA 1+ will fumble
            // on a modified 0 and below.
            context.thrower.passing == null -> PassingType.FUMBLED
            d6.value == 6 -> if (isUsingHailMary) PassingType.INACCURATE else PassingType.ACCURATE
            d6.value == 1 && passingStat != 1 -> PassingType.FUMBLED
            d6.value + modifierTotal <= 1 && passingStat > 1 -> PassingType.FUMBLED
            d6.value + modifierTotal <= 0 && passingStat == 1 -> PassingType.FUMBLED
            d6.value + modifierTotal >= passingStat -> if (isUsingHailMary) PassingType.INACCURATE else PassingType.ACCURATE
            d6.value + modifierTotal < passingStat -> PassingType.INACCURATE
            else -> INVALID_GAME_STATE("Unsupported result: ${d6.value}, target: $passingStat, modifierTotal: $modifierTotal")
        }
        return context.copy(
            passingResult = result
        )
    }
}
