package com.jervisffb.engine.rules.common.procedures.tables.injury

import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetSkillUsed
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.FoulContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.InjuryModifier
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.InjuryResult
import com.jervisffb.engine.utils.sum

/**
 * Implement the injury roll.
 *
 * See page 60 in the BB2020 rulebook.
 * See page 66 in the BB2025 rulebook.
 *
 * The result is stored in [RiskingInjuryContext] and it is up
 * to the caller to determine what to do with the result.
 *
 * TODO Note, Mighty Blow specifically say "When an opposition player is knocked
 *  down" (page 80) and "Pushed into the Crows" (page 58) says "A player that
 *  is pushed into the crowd is immediately removed from play". So this would
 *  mean that any effect that requires a "Knocked Down" player doesn't apply.
 */
object InjuryRoll: Procedure() {
    override val initialNode: Node = RollDice
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<RiskingInjuryContext>()

    object RollDice : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RiskingInjuryContext>().player.team.otherTeam()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6, Dice.D6))

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result, D6Result>(action) { die1, die2 ->
                val context = state.getContext<RiskingInjuryContext>()

                // Determine result of injury roll
                // TODO This logic needs to be expanded to support things like Mighty Blow and others.
                val roll = listOf(die1, die2)
                val result = rules.injuryTable.roll(die1, die2)
                val updatedContext = context.copy(
                    injuryRoll = roll,
                    injuryResult = result,
                )

                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.INJURY, roll),
                    SetContext(updatedContext),
                    GotoNode(ChooseToUseDirtyPlayer),
                )
            }
        }
    }

    object ChooseToUseDirtyPlayer: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<RiskingInjuryContext>().player.team.otherTeam()
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RiskingInjuryContext>()
            val isFoul = (context.mode == RiskingInjuryMode.FOUL)
            val hasDirtyPlayer = (state.getContextOrNull<FoulContext>()?.fouler?.isSkillAvailable(SkillType.DIRTY_PLAYER) == true)
            return if (isFoul && hasDirtyPlayer) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val usedDirtyPlayer = (action is Confirm)
            return buildCompositeCommand {
                if (usedDirtyPlayer) {
                    val fouler = state.getContext<FoulContext>().fouler
                    val updatedModifiers = context.injuryModifiers + InjuryModifier.DIRTY_PLAYER
                    val updatedContext = context.copy(
                        injuryModifiers = updatedModifiers,
                        injuryResult = rules.injuryTable.roll(context.injuryRoll[0], context.injuryRoll[1], updatedModifiers.sum())
                    )
                    add(SetContext(updatedContext))
                    add(SetSkillUsed(fouler, fouler.getSkill(SkillType.DIRTY_PLAYER), true))
                    add(ReportSkillUsed(fouler, SkillType.DIRTY_PLAYER))
                }
                add(GotoNode(ChooseToUseThickSkull))
            }
        }
    }

    // Choose to use Thick Skull, but only if it actually reduces the injury.
    object ChooseToUseThickSkull: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<RiskingInjuryContext>().player.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RiskingInjuryContext>()
            val hasThickSkull = (context.player.hasSkill(SkillType.THICK_SKULL))
            val isStunty = context.player.hasSkill(SkillType.STUNTY)
            val roll = context.injuryRollResult
            val reduceNormalInjury = (!isStunty && roll == 8)
            val reduceStuntyInjury = (isStunty && roll == 7)
            return if (hasThickSkull && (reduceStuntyInjury || reduceNormalInjury)) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val useThickSkull = (action == Confirm)
            val isStunty = context.player.hasSkill(SkillType.STUNTY)
            return buildCompositeCommand {
                if (useThickSkull) {
                    val roll = context.injuryRollResult
                    val reduceToStunned = ((isStunty && roll == 7) || (!isStunty && roll == 8))
                    val newInjury = if (reduceToStunned) InjuryResult.STUNNED else context.injuryResult!!
                    add(SetContext(context.copy(injuryResult = newInjury, useThickSkullOnInjuryRoll = true)))
                    add(ReportSkillUsed(context.player, SkillType.THICK_SKULL))
                }
                add(ExitProcedure())
            }
        }
    }
}
