package com.jervisffb.engine.rules.bb2020.procedures.actions.move

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
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetOldContext
import com.jervisffb.engine.commands.SetSkillUsed
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.DodgeRollContext
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.hasContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.BreakTackleModifier
import com.jervisffb.engine.model.modifiers.DodgeRollModifier
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportDodgeResult
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.D6DieRoll
import com.jervisffb.engine.rules.bb2020.skills.BreakTackle
import com.jervisffb.engine.rules.bb2020.skills.DivingTackle
import com.jervisffb.engine.rules.bb2020.skills.PrehensileTail
import com.jervisffb.engine.rules.bb2020.skills.Stunty
import com.jervisffb.engine.rules.bb2020.skills.Titchy
import com.jervisffb.engine.rules.bb2020.skills.TwoHeads
import com.jervisffb.engine.rules.bb2020.testAgainstAgility
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.calculateAvailableRerollsFor

/**
 * Handle a player making a dodge roll.
 * See page 45 in the rulebook.
 *
 * Dodge can be modified in a number of ways. In this implementation it is handled the following way:
 * Note, the order of skills is the order they are resolved in.
 *
 * 1. Roll D6.
 * 2. Calculate required modifiers. These apply to both roll and reroll.
 *      a. -1 for each marking player in target field
 *      b. Stunty* (Ignore all -1 marked modifiers in target field)
 *      c. Titchy* (+1)
 * 2. Choose optional modifiers. These apply to both roll and reroll.
 *      a. Two Heads (+1)
 *      b. Break Tackle (+1 with S4 or +2 with S5)
 *      c. Prehensile Tail (-1)
 *      d. Diving Tackle (-2, and user prone)
 * 4. Choose to Reroll or not.
 * 5. If Reroll. Choose optional modifiers with negative consequences for user.
 *      a. Diving Tackle
 * 6. Calculate the final result.
 *
 * Designer's Commentary:
 * It is possible to wait using Diving Tackle until the reroll has been made
 *
 * Developer's Commentary:
 * Given the Designer's Commentary, technically all optional skills not selected before the
 * first roll should go through another selection process, but I cannot find any reason why
 * you would want that. It makes sense for "Diving Tackle", since it brings a penalty, but
 * for others it doesn't.
 *
 * The only reason you would want to avoid using them is to intentionally fail the roll, in
 * which case, you wouldn't want to apply them after a reroll either.
 *
 * Also, it is unclear from the rules who choose to use skills first, e.g., Break Tackle and
 * Prehensile Tail. In this case, it doesn't matter since they are both "free", but in other
 * cases it might.
 */
object DodgeRoll: Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return ReportDodgeResult(state.getContext<DodgeRollContext>())
    }
    override fun isValid(state: Game, rules: Rules) = state.assertContext<DodgeRollContext>()

    object RollDie: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<DodgeRollContext>().player.team

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<DodgeRollContext>()
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.DODGE, d6),
                    SetContext(context.copy(roll = D6DieRoll.create(state, d6))),
                    GotoNode(CalculateMandatoryModifiers)
                )
            }
        }
    }

    /**
     * Set all mandatory dodge modifiers.
     *
     * This includes:
     *  1. -1 for each player marking the field being moved into.
     *  2. Stunty* (Ignore all - marked modifiers in the target field)
     *  3. Titchy* (+1)
     */
    object CalculateMandatoryModifiers : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<DodgeRollContext>()
            val player = context.player
            val modifiers = buildList {
                // Add marking modifiers if the moving player doesn't have Stunty.
                if (!player.hasSkill<Stunty>()) {
                    rules.addMarkedModifiers(
                        state,
                        player.team,
                        context.targetSquare,
                        this,
                        DodgeRollModifier.MARKED
                    )
                }
                if (player.hasSkill<Titchy>()) {
                    add(DodgeRollModifier.TITCHY)
                }
            }
            return compositeCommandOf(
                SetContext(context.copy(rollModifiers = modifiers)),
                GotoNode(ChooseToUseTwoHeads)
            )
        }
    }

    /**
     * Choose whether dodging player should use Two Heads (if applicable).
     */
    object ChooseToUseTwoHeads: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<DodgeRollContext>().player.team

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<DodgeRollContext>()
            return if (context.player.hasSkill<TwoHeads>()) {
                return listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<DodgeRollContext>()
            return compositeCommandOf(
                when (action) {
                    Confirm -> {
                        ReportSkillUsed(context.player, context.player.getSkill<TwoHeads>())
                        SetContext(context.copyAndAddModifier(DodgeRollModifier.TWO_HEADS))
                    }
                    Cancel,
                    Continue -> null
                    else -> INVALID_ACTION(action)
                },
                GotoNode(ChooseToUseBreakTackle)
            )
        }
    }

    /**
     * Choose whether dodging player should use Break Tackle (if applicable).
     */
    object ChooseToUseBreakTackle: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<DodgeRollContext>().player.team

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<DodgeRollContext>()
            return if (context.player.isSkillAvailable<BreakTackle>()) {
                return listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<DodgeRollContext>()
            val player = context.player
            return when (action) {
                Confirm -> {
                    val modifier = BreakTackleModifier(player.strength)
                    compositeCommandOf(
                        ReportSkillUsed(context.player, context.player.getSkill<BreakTackle>()),
                        SetContext(context.copyAndAddModifier(modifier)),
                        SetSkillUsed(player = player, skill = player.getSkill<BreakTackle>(), used = true),
                        GotoNode(ChooseToUsePrehensileTail)
                    )
                }
                Cancel,
                Continue -> GotoNode(ChooseToUsePrehensileTail)
                else -> INVALID_ACTION(action)
            }
        }
    }

    /**
     * Choose whether to use Prehensile Tail (if applicable).
     * If multiple players have it, only 1 can use it.
     */
    object ChooseToUsePrehensileTail: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<DodgeRollContext>().player.team.otherTeam()

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<DodgeRollContext>()
            val eligiblePlayers = context.startingSquare.getSurroundingCoordinates(rules)
                .filter { coord ->
                    state.field[coord].player
                        ?.let { player -> player.team != context.player.team}
                        ?: false
                }
                .mapNotNull { state.field[it].player }
                .filter { it.isSkillAvailable<PrehensileTail>() }
                .map { SelectPlayer(it) }

            return if (eligiblePlayers.isEmpty()) {
                listOf(ContinueWhenReady)
            } else {
                eligiblePlayers + CancelWhenReady
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<DodgeRollContext>()
            return when (action) {
                is PlayerSelected -> {
                    val player = action.getPlayer(state)
                    compositeCommandOf(
                        ReportSkillUsed(player, player.getSkill<PrehensileTail>()),
                        SetContext(context.copyAndAddModifier(DodgeRollModifier.PREHENSILE_TAIL)),
                        GotoNode(ChooseToUseDivingTackle)
                    )
                }
                is Cancel,
                is Continue -> {
                    GotoNode(ChooseToUseDivingTackle)
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    /**
     * Choose whether to use Diving Tackle (if applicable).
     * If multiple players has it, only 1 can use it.
     */
    object ChooseToUseDivingTackle: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<DodgeRollContext>().player.team.otherTeam()

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<DodgeRollContext>()
            val eligiblePlayers = context.startingSquare.getSurroundingCoordinates(rules)
                .filter { coord ->
                    state.field[coord].player?.let { player ->
                        player.team != context.player.team
                    } ?: false
                }
                .mapNotNull { state.field[it].player }
                .filter { it.isSkillAvailable<DivingTackle>() }
                .map { SelectPlayer(it) }

            return if (eligiblePlayers.isEmpty()) {
                listOf(ContinueWhenReady)
            } else {
                eligiblePlayers + CancelWhenReady
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<DodgeRollContext>()
            return when (action) {
                is PlayerSelected -> {
                    val player = action.getPlayer(state)
                    val skill = player.getSkill<DivingTackle>()
                    compositeCommandOf(
                        ReportSkillUsed(player, skill),
                        SetContext(context.copyAndAddModifier(DodgeRollModifier.DIVING_TACKLE)),
                        GotoNode(CalculateSuccess)
                    )
                }
                is Cancel,
                is Continue -> {
                    GotoNode(CalculateSuccess)
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    /**
     * After selecting all modifiers. Calculate if the roll was successful or not.
     */
    object CalculateSuccess: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val afterReroll = state.hasContext<UseRerollContext>()
            val context = state.getContext<DodgeRollContext>()
            val success = testAgainstAgility(context.player, context.roll!!.result, context.rollModifiers)
            return compositeCommandOf(
                SetContext(context.copy(isSuccess = success)),
                if (afterReroll) ExitProcedure() else GotoNode(ChooseReRollSource)
            )
        }
    }

    /**
     * Choose where a reroll should come from (if any). This can be skills, team rerolls, special cards
     * or other sources.
     */
    object ChooseReRollSource : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<DodgeRollContext>().player.team

        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<GameActionDescriptor> {
            val context = state.getContext<DodgeRollContext>()
            val dodgingPlayer = context.player
            val availableReRolls: SelectRerollOption? = calculateAvailableRerollsFor(
                rules,
                dodgingPlayer,
                DiceRollType.DODGE,
                context.roll!!,
                context.isSuccess
            )
            return if (availableReRolls == null) {
                listOf(ContinueWhenReady)
            } else {
                listOf(SelectNoReroll(context.isSuccess)) + availableReRolls
            }
        }

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return when (action) {
                Continue -> ExitProcedure()
                is NoRerollSelected -> ExitProcedure()
                is RerollOptionSelected -> {
                    val rerollContext = UseRerollContext(DiceRollType.DODGE, action.getRerollSource(state))
                    compositeCommandOf(
                        SetOldContext(Game::rerollContext, rerollContext),
                        GotoNode(UseRerollSource),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    /**
     * Use the selected reroll source.
     */
    object UseRerollSource : ParentNode() {
        override fun getChildProcedure(
            state: Game,
            rules: Rules,
        ): Procedure = state.rerollContext!!.source.rerollProcedure

        override fun onExitNode(
            state: Game,
            rules: Rules,
        ): Command {
            val context = state.rerollContext!!
            return if (context.rerollAllowed) {
                GotoNode(ReRollDie)
            } else {
                ExitProcedure()
            }
        }
    }

    object ReRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<DodgeRollContext>().player.team

        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val dodgeContext = state.getContext<DodgeRollContext>()
                val rerollContext = state.rerollContext!!
                val rerolledDodgeRoll = dodgeContext.copy(
                    roll = dodgeContext.roll!!.copyReroll(
                        rerollSource = rerollContext.source,
                        rerolledResult = d6,
                    )
                )
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.DODGE, d6),
                    SetContext(rerolledDodgeRoll),
                    GotoNode(CalculateSuccess),
                )
            }
        }
    }
}
