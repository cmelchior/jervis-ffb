package com.jervisffb.engine.rules.bb2025.procedures.actions.pass

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
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
import com.jervisffb.engine.actions.SelectRerollOption
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
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.AccuracyModifier
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassingType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.Range
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.calculateAvailableRerollsFor
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
object PassAccuracyRoll: Procedure() {
    override val initialNode: Node = ChooseToUseNervesOfSteel
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val context = setInitialModifiers(state, rules)
        return SetContext(context)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<PassContext>()
        val updatedContext = updatePassContextWithResult(context)
        return SetContext(updatedContext)
    }
    override fun isValid(state: Game, rules: Rules) = state.assertContext<PassContext>()

    object ChooseToUseNervesOfSteel: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<PassContext>().thrower.team
        }
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
                SetContext(context.copy(useNervesOfSteel = useNervesOfSteel, passingModifiers = modifiers.toPersistentList())),
                GotoNode(RollDie)
            )
        }
    }

    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PassContext>().thrower.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<PassContext>()
                return compositeCommandOf(
                    ReportDiceRoll(DiceRollType.ACCURACY, d6),
                    SetContext(context.copy(passingRoll = D6DieRoll.create(state, d6))),
                    GotoNode(ChooseToUseAccurate),
                )
            }
        }
    }

    object ChooseToUseAccurate: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PassContext>().thrower.team
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
                        SetContext(context.copyAndAdd(passingModifier = AccuracyModifier.ACCURATE))
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
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PassContext>().thrower.team
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
                        SetContext(context.copyAndAdd(passingModifier = AccuracyModifier.CANNONEER))
                    }

                    Cancel,
                    Continue -> null

                    else -> INVALID_ACTION(action)
                },
                GotoNode(ChooseReRollSource)
            )
        }
    }


    // Team Reroll, Pro, Catch (only if failed), other skills
    object ChooseReRollSource : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<PassContext>().thrower.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PassContext>()
            val availableRerolls: SelectRerollOption? = calculateAvailableRerollsFor(
                rules,
                context.thrower,
                DiceRollType.ACCURACY,
                context.passingRoll!!,
                null
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
                    val rerollContext = UseRerollContext(DiceRollType.ACCURACY, action.getRerollSource(state))
                    compositeCommandOf(
                        SetOldContext(Game::rerollContext, rerollContext),
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
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<PassContext>().thrower.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<PassContext>()
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.ACCURACY, d6),
                    SetContext(
                        context.copy(
                            passingRoll = context.passingRoll!!.copyReroll(
                                rerollSource = state.rerollContext!!.source,
                                rerolledResult = d6
                            )
                        )
                    ),
                    ExitProcedure(),
                )
            }
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
        val result = when {
            // The value can be modified below 1. Players with PA 1+ will fumble
            // on a modified 0 and below.
            context.thrower.passing == null -> PassingType.FUMBLED
            d6.value == 6 -> PassingType.ACCURATE
            d6.value == 1 && passingStat != 1 -> PassingType.FUMBLED
            d6.value + modifierTotal <= 1 && passingStat > 1 -> PassingType.FUMBLED
            d6.value + modifierTotal <= 0 && passingStat == 1 -> PassingType.FUMBLED
            d6.value + modifierTotal >= passingStat -> PassingType.ACCURATE
            d6.value + modifierTotal < passingStat -> PassingType.INACCURATE
            else -> INVALID_GAME_STATE("Unsupported result: ${d6.value}, target: $passingStat, modifierTotal: $modifierTotal")
        }
        return context.copy(
            passingResult = result
        )
    }
}
