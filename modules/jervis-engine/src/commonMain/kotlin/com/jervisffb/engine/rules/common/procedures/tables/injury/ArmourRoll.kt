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
import com.jervisffb.engine.commands.SetOldContext
import com.jervisffb.engine.commands.SetSkillRerollUsed
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
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.context.hasContext
import com.jervisffb.engine.model.getSkill
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.ArmourModifier
import com.jervisffb.engine.model.modifiers.MightyBlowArmourModifier
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.skills.LoneFouler
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowTeamMateContext
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.sum

/**
 * Implement the armour roll.
 *
 * See page 60 in the BB2020 rulebook.
 * See page 66 in the BB2025 rulebook.
 *
 * The result is stored in [RiskingInjuryContext] and it is up
 * to the caller to determine what to do with the result.
 *
 * Developer's Commentary:
 * There are quite a lot of skills that affect Armour Rolls, especially as they
 * can occur in different scenarios. This procedure is capturing all of them.
 *
 * For now, we just go through all relevant skills in an arbitrary order. Since
 * all of the skills can be applied after rolling the dice, this is where
 * we choose to apply them.
 *
 * This also allows us to skip skills like Claw (when rolling 7 or less) or
 * Mighty Blow (if they don't make a difference)
 *
 * Block:
 * 1. Claw (Knocked Down)
 * 2. Mighty Blow (Knocked Down)
 * 4. TBD: Chainsaw, Arm Bar, Others?
 *
 * Foul:
 * 1. Dirty Player (AV Roll - Modifier)
 * 2. Lone Fouler (AV Roll - Reroll)
 *
 * Throw Team-mate:
 * 1. Lethal Flight (Knocked Down)
 */
object ArmourRoll: Procedure() {
    override val initialNode: Node = RollDice
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<RiskingInjuryContext>()
    }

    object RollDice : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RiskingInjuryContext>().player.team.otherTeam()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6, Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result, D6Result>(action) { die1, die2 ->
                val context = state.getContext<RiskingInjuryContext>()

                // Determine result of armour roll
                // All skills that can modify an armour roll can be used both before and after the roll,
                // but since there is no advantage to using them before, we will only apply them after.
                val roll = listOf(die1, die2)
                val updatedContext = state.getContext<RiskingInjuryContext>().copy(
                    armourRoll = listOf(
                        D6DieRoll.create(state, die1),
                        D6DieRoll.create(state, die2),
                    ),
                )
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.ARMOUR, roll),
                    UpdateContext(updatedContext),
                    GotoNode(DecideOnModifierPath)
                )
            }
        }
    }

    object ReRollDice : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            val context = state.getContext<RiskingInjuryContext>()
            return context.causedBy?.team ?: error("Missing causedBy: $context")
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6, Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result, D6Result>(action) { die1, die2 ->
                val context = state.getContext<RiskingInjuryContext>()
                val reroll = state.rerollContext ?: error("Missing rerollContext")
                val originalRoll = context.armourRoll
                val updatedD1 = originalRoll.first().copyReroll(
                    reroll.source,
                    die1
                )
                val updatedD2 = originalRoll.last().copyReroll(
                    reroll.source,
                    die2
                )
                val updatedContext = context.copy(
                    armourRoll = listOf(updatedD1, updatedD2)
                )
                val roll = listOf(die1, die2)
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.ARMOUR, roll),
                    UpdateContext(updatedContext),
                    GotoNode(DecideOnModifierPath)
                )
            }
        }
    }

    object DecideOnModifierPath: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            return when (context.mode) {
                RiskingInjuryMode.KNOCKED_DOWN -> {
                    val throwPlayerContext = state.getContextOrNull<ThrowTeamMateContext>()
                    val thrownPlayer = throwPlayerContext?.thrownPlayer
                    if (thrownPlayer != null && thrownPlayer == context.causedBy && context.mode == RiskingInjuryMode.KNOCKED_DOWN) {
                        GotoNode(CheckIfLethalFlightIsApplicable)
                    } else {
                        GotoNode(CheckIfMightyBlowIsApplicable)
                    }
                }
                RiskingInjuryMode.FOUL -> GotoNode(ChooseToUseDirtyPlayer)
                // None of these have skills that can affect the armour roll
                RiskingInjuryMode.BAD_LANDING,
                RiskingInjuryMode.FALLING_OVER,
                RiskingInjuryMode.HIT_BY_ROCK,
                RiskingInjuryMode.PLACED_PRONE,
                RiskingInjuryMode.PROJECTILE_VOMIT,
                RiskingInjuryMode.PUSHED_INTO_CROWD,
                RiskingInjuryMode.STAB -> ExitProcedure()
            }
        }
    }

    // Mighty Blow only works on Knocked Down players during Blocks (Animal Savagery TBD)
    // Also, to clean up the action flow, we skip Mighty Blow if using it wouldn't matter.
    object CheckIfMightyBlowIsApplicable: ComputationNode() {
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

            // We only want to use Mighty Blow if it makes a difference, i.e. Armour Roll should be
            // Target AV - 1.
            val target = context.player.armorValue
            val roll = context.armourRoll.sum() + context.armourModifiers.sum()
            val mbModifier: Int = if (opponentHasMightyBlow) {
                val skill = context.causedBy.getSkill(SkillType.MIGHTY_BLOW)
                when (skill) {
                    is com.jervisffb.engine.rules.bb2025.skills.MightyBlow -> skill.value
                    is com.jervisffb.engine.rules.bb2020.skills.MightyBlow -> skill.value!!
                    else -> 0
                }
            } else {
                0
            }
            val mbWillMatter = roll < target
                && mbModifier > 0
                && (roll + mbModifier) >= target

            return if (
                isBlock
                && isKnockedDown
                && opponentCanUseSkills
                && opponentHasMightyBlow
                && mbWillMatter
            ) {
                GotoNode(ChooseToUseMightyBlow)
            } else {
                ExitProcedure()
            }
        }
    }

    // This procedure assumes that `CheckIfMultipleBlowIsApplicable` filtered out invalid scenarios.
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
                compositeCommandOf(
                    ReportSkillUsed(mbPlayer, SkillType.MIGHTY_BLOW),
                    SetSkillUsed(mbPlayer, mbSkill, true),
                    UpdateContext(
                        context.copy(
                            armourModifiers = context.armourModifiers + listOf(MightyBlowArmourModifier(mbSkill.value as Int))
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

    // Only on Fouls
    object ChooseToUseDirtyPlayer: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<RiskingInjuryContext>().player.team.otherTeam()
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RiskingInjuryContext>()
            val isFoul = (context.mode == RiskingInjuryMode.FOUL)
            val hasDirtyPlayer = (context.causedBy?.isSkillAvailable(SkillType.DIRTY_PLAYER) == true)
            val target = context.player.armorValue
            val roll = context.armourResult
            val dirtyPlayerWillHaveImpact = roll < target && (roll + ArmourModifier.DIRTY_PLAYER.modifier) >= target

            return if (isFoul && hasDirtyPlayer && dirtyPlayerWillHaveImpact) {
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
                    val updatedContext = context.copy(
                        armourModifiers = context.armourModifiers + ArmourModifier.DIRTY_PLAYER,
                    )
                    add(UpdateContext(updatedContext))
                    add(SetSkillUsed(fouler, fouler.getSkill(SkillType.DIRTY_PLAYER), true))
                    add(ReportSkillUsed(fouler, SkillType.DIRTY_PLAYER))
                }
                add(GotoNode(ChooseToUseLoneFouler))
            }
        }
    }

    object ChooseToUseLoneFouler: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            val context = state.getContext<RiskingInjuryContext>()
            return context.causedBy?.team ?: error("Missing causedBy: $context")
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RiskingInjuryContext>()
            val fouler = context.causedBy ?: error("Missing fouler: $context")
            val hasLoneFouler = fouler.isSkillAvailable(SkillType.LONE_FOULER)
            val isLoneFoulerRerollAvailable = when (hasLoneFouler) {
                true -> fouler.getSkill<LoneFouler>().canReroll(state, DiceRollType.ARMOUR, context.armourRoll, null)
                false -> false
            }
            return when (isLoneFoulerRerollAvailable) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val useLoneFouler = (action is Confirm)
            val fouler = context.causedBy ?: error("Missing fouler: $context")
            return when (useLoneFouler) {
                true -> {
                    val rerollContext = UseRerollContext(DiceRollType.ARMOUR, fouler.getSkill<LoneFouler>())
                    compositeCommandOf(
                        ReportSkillUsed(fouler, SkillType.LONE_FOULER),
                        SetSkillRerollUsed(fouler.getSkill<LoneFouler>(), true),
                        SetOldContext(Game::rerollContext, rerollContext),
                        UpdateContext(context.copy(armourModifiers = context.armourModifiers.filter { it != ArmourModifier.DIRTY_PLAYER })),
                        GotoNode(ReRollDice)
                    )
                }
                false -> ExitProcedure()
            }
        }
    }

    // Knocked Down by thrown player during Throw Team-mate Action.
    // To clean up the action flow, we skip Lethal Flight if using it wouldn't matter.
    object CheckIfLethalFlightIsApplicable: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            // Since the opponent using Mighty Blow, might already be prone, we cannot rely on
            // normal checks.
            val opponentCanUseSkills = context.canOpponentUseSkills
            val opponentHasLethalFlight = (context.causedBy?.isSkillAvailable(SkillType.LETHAL_FLIGHT) == true)

            // We only want to use Lethal Flight if it makes a difference, i.e. Armour Roll should be
            // Target AV - 1.
            val target = context.player.armorValue
            val roll = context.armourRoll.sum() + context.armourModifiers.sum()
            val avModifier: Int = when (opponentHasLethalFlight) {
                true -> 1
                false -> 0
            }
            val lethalFlightWillMatter = roll < target
                && avModifier > 0
                && (roll + avModifier) >= target

            return if (opponentCanUseSkills && opponentHasLethalFlight && lethalFlightWillMatter) {
                GotoNode(ChooseToUseLethalFlight)
            } else {
                ExitProcedure()
            }
        }
    }

    object ChooseToUseLethalFlight: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            val injuryContext = state.getContext<RiskingInjuryContext>()
            return injuryContext.causedBy?.team ?: error("Missing team: $injuryContext")
        }

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RiskingInjuryContext>()
            val hasLethalFlight = (context.causedBy?.isSkillAvailable(SkillType.LETHAL_FLIGHT) == true)
            return if (hasLethalFlight) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val thrownPlayer = context.causedBy!!
            val useLethalFlight = (action is Confirm)
            return if (useLethalFlight) {
                val skill = thrownPlayer.getSkill(SkillType.LETHAL_FLIGHT)
                compositeCommandOf(
                    ReportSkillUsed(thrownPlayer, SkillType.LETHAL_FLIGHT),
                    SetSkillUsed(thrownPlayer, skill, true),
                    UpdateContext(
                        context.copy(
                            armourModifiers = context.armourModifiers + listOf(ArmourModifier.LETHAL_FLIGHT)
                        )
                    ),
                    ExitProcedure()
                )
            } else {
                ExitProcedure()
            }
        }
    }
}
