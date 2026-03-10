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
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.FoulContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.context.hasContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.InjuryModifier
import com.jervisffb.engine.model.modifiers.MightyBlowInjuryModifier
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
 * Mighty Blow specifically say "Whenever this player Knocks Down an opposition
 * player during a Block Action..." (page 131 in BB2025 rulebook), but when you
 * are pushed into the crowd, the player is never Knocked Down. So Mighty Blow
 * does not apply there
 */
object InjuryRoll: Procedure() {
    override val initialNode: Node = RollDice
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        val context = state.getContext<RiskingInjuryContext>()
        return when (context.player.isSkillAvailable(SkillType.STUNTY)) {
            true -> UpdateContext(context.copy(injuryModifiers = context.injuryModifiers.add(InjuryModifier.STUNTY)))
            false -> null
        }
    }
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
                val result = rules.injuryTable.roll(die1, die2, context.injuryModifiers.sum())
                val updatedContext = context.copy(
                    injuryRoll = roll,
                    injuryResult = result,
                )

                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.INJURY, roll),
                    UpdateContext(updatedContext),
                    GotoNode(CheckIfMultipleBlowIsApplicable),
                )
            }
        }
    }

    // Mighty Blow only works on Knocked Down players during Blocks (Animal Savagery TBD)
    object CheckIfMultipleBlowIsApplicable: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            // This should be safe as there is no way a Player can be Knocked Down during a Block unless
            // they are either the attacker or the defender. All other injuries will be crowd-surfs.
            val isBlock = state.hasContext<BlockContext>()
            val isKnockedDown = (context.mode == RiskingInjuryMode.KNOCKED_DOWN)

            // Since the opponent using Mighty Blow, might already be prone, we cannot rely on
            // normal checks.
            val opponentCanUseSkills = context.canOpponentUseSkills
            val opponentHasMightyBlow = (context.causedBy?.hasSkill(SkillType.MIGHTY_BLOW) == true)
            val isMightyBlowAvailable = if (opponentHasMightyBlow) {
                val skill = context.causedBy.getSkill(SkillType.MIGHTY_BLOW)
                !skill.used
            } else {
                false
            }

            return if (
                isBlock
                && isKnockedDown
                && opponentCanUseSkills
                && isMightyBlowAvailable
            ) {
                GotoNode(ChooseToUseMightyBlow)
            } else {
                GotoNode(ChooseToUseDirtyPlayer)
            }
        }
    }

    object ChooseToUseMightyBlow: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            val injuryContext = state.getContext<RiskingInjuryContext>()
            return injuryContext.causedBy?.team ?: error("Missing team: $injuryContext")
        }

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RiskingInjuryContext>()
            val hasMightyBlow = (context.causedBy?.hasSkill(SkillType.MIGHTY_BLOW) == true)
            return if (hasMightyBlow) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val mbPlayer = context.causedBy!!
            val useMightyBlow = (action is Confirm)
            return if (useMightyBlow) {
                val mbSkill = mbPlayer.getSkill(SkillType.MIGHTY_BLOW)
                val updatedModifiers = context.injuryModifiers.add(MightyBlowInjuryModifier(mbSkill.value as Int))
                val modifiersTotal = updatedModifiers.sum()
                val newResult = rules.injuryTable.roll(context.injuryRoll[0], context.injuryRoll[1], modifiersTotal)
                compositeCommandOf(
                    ReportSkillUsed(mbPlayer, SkillType.MIGHTY_BLOW),
                    SetSkillUsed(mbPlayer, mbSkill, true),
                    UpdateContext(
                        context.copy(
                            injuryModifiers = updatedModifiers,
                            injuryResult = newResult
                        )
                    ),
                    ExitProcedure()
                )
            } else {
                // Dirty Player will never work in the same context as Mighty Blow, so we can exit here
                ExitProcedure()
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
                    val updatedModifiers = context.injuryModifiers.add(InjuryModifier.DIRTY_PLAYER)
                    val updatedContext = context.copy(
                        injuryModifiers = updatedModifiers,
                        injuryResult = rules.injuryTable.roll(context.injuryRoll[0], context.injuryRoll[1], updatedModifiers.sum())
                    )
                    add(UpdateContext(updatedContext))
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
                    add(UpdateContext(context.copy(injuryResult = newInjury, useThickSkullOnInjuryRoll = true)))
                    add(ReportSkillUsed(context.player, SkillType.THICK_SKULL))
                }
                add(ExitProcedure())
            }
        }
    }
}
