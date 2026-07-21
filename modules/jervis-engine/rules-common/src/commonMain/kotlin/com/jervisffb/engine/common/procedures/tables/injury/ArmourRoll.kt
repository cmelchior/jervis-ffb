package com.jervisffb.engine.common.procedures.tables.injury

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
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetSkillRerollUsed
import com.jervisffb.engine.commands.SetSkillUsed
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.common.context.BlockContext
import com.jervisffb.engine.common.context.ChainsawContext
import com.jervisffb.engine.common.context.FoulContext
import com.jervisffb.engine.common.modifiers.ArmourModifier
import com.jervisffb.engine.common.modifiers.MightyBlowArmourModifier
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.context.hasContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.common.reports.ReportDiceRoll
import com.jervisffb.engine.common.reports.ReportSkillUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.common.procedures.AnimalSavageryContext
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.common.procedures.actions.throwteammate.ThrowTeamMateContext
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.sum
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

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
 * Knocked Down (during Block Action):
 * 1. Claw (Knocked Down)
 * 2. Mighty Blow (Knocked Down)
 * 4. TBD: Chainsaw, Others?
 *
 * Foul:
 * 1. Chainsaw (Modifier)
 * 1. Dirty Player (Modifier)
 * 2. Lone Fouler (Reroll)
 *
 * Knocked Down (Throw Team-mate):
 * 1. Lethal Flight (Knocked Down)
 *
 * Fall Over:
 * 1. Arm Bar (If in the middle of a Dodge, Jump or Leap)
 */
object ArmourRoll: Procedure() {
    override val initialNode: Node = RollDice
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<RiskingInjuryContext>()
        // According to the rules on page 37 (BB2025), it is always the opposing
        // team that makes the Armour roll, regardless of how the injury
        // happened, so any team-specific rerolls (none currently exists) must
        // be on that team.
        val team = context.player.team.otherTeam()
        val player = context.causedBy
        val rerollContext = UseRerollContext(
            type = DiceRollType.ARMOUR,
            originalRoll = emptyList(),  // Not used here as Lone Fouler is the only reroll and handled independently
            team = team,
            player = player
        )
        return AddContext(rerollContext)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val rerollContext = state.getRerollContext()
        if (rerollContext.type != DiceRollType.ARMOUR) {
            INVALID_GAME_STATE("Invalid reroll context type: $rerollContext")
        }
        return RemoveContext(rerollContext)
    }
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<RiskingInjuryContext>()
    }

    object RollDice : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RiskingInjuryContext>().player.team.otherTeam()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6, Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result, D6Result>(action) { die1, die2 ->
                // Determine result of armour roll
                // All skills that can modify an armour roll can be used both before and after the roll,
                // but since there is no advantage to using them before, we will only apply them after.
                val roll = listOf(die1, die2)
                val updatedContext = state.getContext<RiskingInjuryContext>().copy(
                    armourRoll = persistentListOf(
                        D6DieRoll.create(state, die1),
                        D6DieRoll.create(state, die2),
                    ),
                )
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.ARMOUR, roll),
                    UpdateContext(updatedContext),
                    GotoNode(ChooseToUseIronHardSkin)
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
                val reroll = state.getRerollContext()
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
                    armourRoll = persistentListOf(updatedD1, updatedD2)
                )
                val roll = listOf(die1, die2)
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.ARMOUR, roll),
                    UpdateContext(updatedContext),
                    GotoNode(ChooseToUseIronHardSkin)
                )
            }
        }
    }

    object ChooseToUseIronHardSkin: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<RiskingInjuryContext>().player.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            // We should only ask for Iron Hard Skin if there is a chance that the opponent has a skill
            // that could affect it
            val context = state.getContext<RiskingInjuryContext>()

            val hasSkill = context.player.isSkillAvailable(SkillType.IRON_HARD_SKIN)
            val alreadyUsedIhs = context.usedIronHardSkin
            val attackedByOwnTeam = (context.causedBy?.team == context.player.team)
            val ihsIsUseful = when (context.mode) {
                RiskingInjuryMode.CHAINSAW -> true
                RiskingInjuryMode.KNOCKED_DOWN -> {
                    val throwPlayerContext = state.getContextOrNull<ThrowTeamMateContext>()
                    val thrownPlayer = throwPlayerContext?.thrownPlayer
                    if (thrownPlayer != null && thrownPlayer == context.causedBy && context.mode == RiskingInjuryMode.KNOCKED_DOWN) {
                        thrownPlayer.isSkillAvailable(SkillType.LETHAL_FLIGHT)
                    } else {
                        context.causedBy?.let { player ->
                            val skills = listOf(
                                SkillType.MIGHTY_BLOW,
                                SkillType.CLAWS
                            )
                            skills.any { player.isSkillAvailable(it) }
                        } ?: false
                    }
                }
                RiskingInjuryMode.FOUL -> {
                    foulModifiersCanHelpWithBreakingArmour(context)
                }
                RiskingInjuryMode.FALLING_OVER -> {
                    isArmBarAvailableAndUseful(state, context)
                }
                else -> false
            }

            return when (hasSkill && ihsIsUseful && !alreadyUsedIhs && !attackedByOwnTeam) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val useIronHardSkin = (action is Confirm)
            val context = state.getContext<RiskingInjuryContext>()
            return when (useIronHardSkin) {
                true -> compositeCommandOf(
                    ReportSkillUsed(context.player, SkillType.IRON_HARD_SKIN),
                    UpdateContext(context.copy(usedIronHardSkin = true)),
                    GotoNode(AddMandatoryModifiers)
                )
                false -> GotoNode(AddMandatoryModifiers)
            }
        }
    }

    object AddMandatoryModifiers: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val modifiers = mutableSetOf<DiceModifier>()

            // If the player being Knocked Down or Falls Over is holding a Chainsaw, the +3 modifier is always applied
            val playerHasChainsaw = (context.player.hasSkill(SkillType.CHAINSAW) && (context.mode in listOf(RiskingInjuryMode.FALLING_OVER, RiskingInjuryMode.KNOCKED_DOWN, RiskingInjuryMode.BAD_LANDING)))
            // If the opponent is holding a chainsaw (and is using it), add +3 to the armour roll
            val chainsawContext = state.getContextOrNull<ChainsawContext>()
            val attackerHasChainsaw= (chainsawContext != null && chainsawContext.attacker == context.causedBy && chainsawContext.isSuccess)
            if ((playerHasChainsaw || attackerHasChainsaw) && !context.usedIronHardSkin) {
                modifiers.add(ArmourModifier.CHAINSAW)
            }

            // If this is during an Animal Savagery Block, we must use Claws/Mighty blow
            // The exact decision isn't defined by the rulebook, but since it is optional
            // to use Mighty Blow on the Injury Roll, it seems fine to just use the rule
            // that any role above 8 will use Claw over Mighty Blow
            val animalSavageryContext = state.getContextOrNull<AnimalSavageryContext>()
            var forceUseClaws = false
            var forceUseMightyBlow = false
            if (animalSavageryContext != null) {
                if (
                    animalSavageryContext.player == context.causedBy
                    && animalSavageryContext.player.isSkillAvailable(SkillType.CLAWS)
                    && context.player.armorValue > 8
                    && !context.usedIronHardSkin
                ) {
                    forceUseClaws = true
                }
                if (animalSavageryContext.player.isSkillAvailable(SkillType.MIGHTY_BLOW)
                    && !forceUseClaws
                    && !context.usedIronHardSkin
                ) {
                    forceUseMightyBlow = true
                    modifiers.add(MightyBlowArmourModifier(modifier = 1))
                }
            }
            return compositeCommandOf(
                if (forceUseClaws) ReportSkillUsed(context.causedBy!!, SkillType.CLAWS) else null,
                if (forceUseMightyBlow) ReportSkillUsed(context.causedBy!!, SkillType.MIGHTY_BLOW) else null,
                UpdateContext(context.copy(
                    armourModifiers = context.armourModifiers.addAll(modifiers),
                    useClawsOnArmourRoll = forceUseClaws || context.useClawsOnArmourRoll
                )),
                GotoNode(DecideOnModifierPath)
            )
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
                        GotoNode(CheckIfClawsIsApplicable)
                    }
                }
                RiskingInjuryMode.FOUL -> GotoNode(ChooseTouseChainsaw)
                RiskingInjuryMode.FALLING_OVER -> GotoNode(CheckIfArmBarIsApplicable)
                // None of these have skills that can affect the armour roll
                RiskingInjuryMode.BAD_LANDING,
                RiskingInjuryMode.HIT_BY_ROCK,
                RiskingInjuryMode.PLACED_PRONE,
                RiskingInjuryMode.PROJECTILE_VOMIT,
                RiskingInjuryMode.PUSHED_INTO_CROWD,
                RiskingInjuryMode.CHAINSAW, // Attacked by the Chainsaw. Modifier was added in `AddMandatoryModifiers`
                RiskingInjuryMode.STAB -> ExitProcedure()
            }
        }
    }

    // Claws only works on Knocked Down players during Block Actions (Animal Savagery TBD)
    // Also, to clean up the action flow, we skip Claws if using it doesn't matter, i.e.
    // We only use it on AV 9+.
    object CheckIfClawsIsApplicable: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            // This should be safe as there is no way a Player can be Knocked Down during a Block unless
            // they are either the attacker or the defender. All other injuries will be crowd-surfs.
            val isBlock = state.hasContext<BlockContext>()
            val isKnockedDown = (context.mode == RiskingInjuryMode.KNOCKED_DOWN)
            val opponentHasClaws = (context.causedBy?.isSkillAvailable(SkillType.CLAWS) == true)

            // We only want to use Claws if it makes a difference, i.e., Armour Roll should be
            // 8+ and Armour Value should be 9+.
            val avTarget = context.player.armorValue
            val roll = context.armourRoll.sum() + context.armourModifiers.sum()
            val clawsWillMatter = avTarget >= 9
                && roll >= 8
                && !context.usedIronHardSkin

            return when (isBlock && isKnockedDown && opponentHasClaws && clawsWillMatter) {
                true -> GotoNode(ChooseToUseClaws)
                false -> GotoNode(CheckIfMightyBlowIsApplicable)
            }
        }
    }

    object ChooseToUseClaws: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<RiskingInjuryContext>().causedBy?.team ?: INVALID_GAME_STATE("Missing causedBy")
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RiskingInjuryContext>()
            val hasClaws = (context.causedBy?.isSkillAvailable(SkillType.CLAWS) == true)
            return when (hasClaws) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val useClaws = (action is Confirm)
            return when (useClaws) {
                true -> {
                    val context = state.getContext<RiskingInjuryContext>()
                    val player = context.causedBy ?: INVALID_GAME_STATE("Missing causedBy")
                    compositeCommandOf(
                        ReportSkillUsed(player, SkillType.CLAWS),
                        SetSkillUsed(player, player.getSkill(SkillType.CLAWS), used = true),
                        UpdateContext(context.copy(useClawsOnArmourRoll = true)),
                        GotoNode(CheckIfMightyBlowIsApplicable)
                    )
                }
                false -> GotoNode(CheckIfMightyBlowIsApplicable)
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

            val opponentHasMightyBlow = (context.causedBy?.isSkillAvailable(SkillType.MIGHTY_BLOW) == true)
            val opponentUsedClaws = context.useClawsOnArmourRoll

            // We only want to use Mighty Blow if it makes a difference, i.e. Armour Roll should be
            // Target AV - 1.
            val target = context.player.armorValue
            val roll = context.armourRoll.sum() + context.armourModifiers.sum()
            val mbModifier: Int = if (opponentHasMightyBlow) {
                @Suppress("UNCHECKED_CAST")
                val skill = context.causedBy.getSkill(SkillType.MIGHTY_BLOW) as? Skill<Int>
                skill?.value ?: 0
            } else {
                0
            }
            val mbWillMatter = roll < target
                && mbModifier > 0
                && (roll + mbModifier) >= target
                && !context.usedIronHardSkin

            return if (
                isBlock
                && isKnockedDown
                && opponentHasMightyBlow
                && !opponentUsedClaws
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
            val hasMightyBlow = (context.causedBy?.isSkillAvailable(SkillType.MIGHTY_BLOW) == true)
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
                            armourModifiers = context.armourModifiers.add(MightyBlowArmourModifier(mbSkill.value as Int))
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
    object ChooseTouseChainsaw: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<RiskingInjuryContext>()

            val hasChainsaw = (context.causedBy?.isSkillAvailable(SkillType.CHAINSAW) == true)
            val isUseful = foulModifiersCanHelpWithBreakingArmour(context)
            return when (hasChainsaw && isUseful) {
                true -> null
                false -> ChooseToUseDirtyPlayer
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = rules.chainsawFoulStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Turnover can happen if the Chainsaw has a Kickback, but even for a Kickback, the player might be
            // left standing (by using Steady Footing). These details, including adding armour modifiers, or not,
            // have already been handled, so here we just determine next steps.
            return when (state.isTurnOver()) {
                true -> ExitProcedure()
                false -> GotoNode(ChooseToUseDirtyPlayer)
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
            val hasDirtyPlayer = (context.causedBy?.isSkillAvailable(SkillType.DIRTY_PLAYER) == true)
            val target = context.player.armorValue
            val roll = context.armourResult
            val dirtyPlayerWillHaveImpact= hasDirtyPlayer
                && roll < target
                && (roll + context.armourModifiers.sum() + ArmourModifier.DIRTY_PLAYER.modifier) >= target
                && !context.usedIronHardSkin

            return when (dirtyPlayerWillHaveImpact) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val usedDirtyPlayer = (action is Confirm)
            return buildCompositeCommand {
                if (usedDirtyPlayer) {
                    val fouler = state.getContext<FoulContext>().fouler
                    val updatedContext = context.copy(
                        armourModifiers = context.armourModifiers.add(ArmourModifier.DIRTY_PLAYER),
                    )
                    add(UpdateContext(updatedContext))
                    add(SetSkillUsed(fouler, fouler.getSkill(SkillType.DIRTY_PLAYER), true))
                    add(ReportSkillUsed(fouler, SkillType.DIRTY_PLAYER))
                }
                add(GotoNode(ChooseToUseLoneFouler))
            }
        }
    }

    // Only on Fouls
    object ChooseToUseLoneFouler: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            val context = state.getContext<RiskingInjuryContext>()
            return context.causedBy?.team ?: error("Missing causedBy: $context")
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RiskingInjuryContext>()
            val fouler = context.causedBy ?: error("Missing fouler: $context")
            val loneFoulerRerollSource = rules.getLoneFoulerRerollSource(fouler)
            val isLoneFoulerRerollAvailable = loneFoulerRerollSource
                ?.canReroll(state, DiceRollType.ARMOUR, context.armourRoll, null) == true
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
                    val rerollSource = rules.getLoneFoulerRerollSource(fouler)
                        ?: error("Lone Fouler is not available in ${rules.name}")
                    val rerollOption = rerollSource.calculateRerollOptions(
                        DiceRollType.ARMOUR,
                        context.armourRoll,
                        wasSuccess = false // Lone Fouler can only be used on failures
                    ).singleOrNull() ?: error("Lone Fouler returned more than one re-roll option")
                    val rerollContext = state.getRerollContext().copy(
                        source = rerollSource,
                        rerollDice = rerollOption.dice,
                        rerollAllowed = true
                    )
                    compositeCommandOf(
                        ReportSkillUsed(fouler, SkillType.LONE_FOULER),
                        SetSkillRerollUsed(rerollSource, true),
                        UpdateContext(rerollContext),
                        UpdateContext(context.copy(armourModifiers = context.armourModifiers.filter { it != ArmourModifier.DIRTY_PLAYER }.toPersistentList())),
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
                && !context.usedIronHardSkin

            return if (opponentHasLethalFlight && lethalFlightWillMatter) {
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
                            armourModifiers = context.armourModifiers.add(ArmourModifier.LETHAL_FLIGHT)
                        )
                    ),
                    ExitProcedure()
                )
            } else {
                ExitProcedure()
            }
        }
    }

    object CheckIfArmBarIsApplicable: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            return when (isArmBarAvailableAndUseful(state, context)) {
                true -> GotoNode(ChooseToUseArmBar)
                false -> ExitProcedure()
            }
        }
    }

    object ChooseToUseArmBar: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            val context = state.getContext<RiskingInjuryContext>()
            return context.player.team.otherTeam()
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RiskingInjuryContext>()
            val team = context.player.team
            val coordinates = context.startingCoordinatesForArmBar ?: INVALID_GAME_STATE("Missing starting coordinates: $context")
            val players = rules
                .getMarkingPlayers(state, team, coordinates)
                .filter { it.isSkillAvailable(SkillType.ARM_BAR) }

            return when (players.isNotEmpty()) {
                true -> listOf(CancelWhenReady, SelectPlayer.fromPlayers(players))
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Cancel, Continue -> ExitProcedure()
                is PlayerSelected -> {
                    val context = state.getContext<RiskingInjuryContext>()
                    val player = action.getPlayer(state)
                    compositeCommandOf(
                        ReportSkillUsed(player, SkillType.ARM_BAR),
                        SetSkillUsed(player, player.getSkill(SkillType.ARM_BAR), used = true),
                        UpdateContext(context.copy(
                            armourModifiers = context.armourModifiers.add(ArmourModifier.ARM_BAR)
                        )),
                        ExitProcedure()
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    // --- HELPER METHODS --

    private fun isArmBarAvailableAndUseful(state: Game, context: RiskingInjuryContext): Boolean {
        val downedPlayer = context.player
        val modifiersAfterArmBar = context.armourModifiers.sum() + ArmourModifier.ARM_BAR.modifier
        val resultBeforeArmBar = context.armourResult
        val resultAfterArmBar = resultBeforeArmBar + modifiersAfterArmBar
        val doNotBreakBeforeArmBar = downedPlayer.armorValue > resultBeforeArmBar
        val breakAfterArmBar = downedPlayer.armorValue <= resultAfterArmBar
        val willBreakArmour = doNotBreakBeforeArmBar && breakAfterArmBar
        return if (context.startingCoordinatesForArmBar != null && willBreakArmour && !context.usedIronHardSkin) {
            val team = context.player.team
            val coordinates = context.startingCoordinatesForArmBar
            state.rules
                .getMarkingPlayers(state, team, coordinates)
                .count { it.isSkillAvailable(SkillType.ARM_BAR) } > 0
        } else {
            false
        }
    }

    // Returns `true` if an optimal use of Fouling modifiers will break the amour. I.e:
    // - If the armour roll is above target armour. Using modifiers does not make a difference
    // - If rolling too low, so even using all modifiers will not make a difference
    private fun foulModifiersCanHelpWithBreakingArmour(context: RiskingInjuryContext): Boolean {
        val potentialModifiers = buildList {
            add(if (context.causedBy?.isSkillAvailable(SkillType.DIRTY_PLAYER) != true) 0 else ArmourModifier.DIRTY_PLAYER.modifier)
            add(if (context.causedBy?.isSkillAvailable(SkillType.CHAINSAW) != true) 0 else ArmourModifier.CHAINSAW.modifier)
        }
        val target = context.player.armorValue
        val roll = context.armourResult
        return roll < target
            && (roll + potentialModifiers.sum() >= target)
            && !context.usedIronHardSkin
    }
}
