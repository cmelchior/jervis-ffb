package com.jervisffb.engine.rules.common.procedures.actions.move

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
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.DodgeRollContext
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.BreakTackleModifier
import com.jervisffb.engine.model.modifiers.DodgeRollModifier
import com.jervisffb.engine.model.modifiers.MarkedModifier
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportDodgeResult
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.testAgainstAgility
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.calculateAvailableRerollsFor

/**
 * Handle a player making a dodge roll.
 *
 * See page 45 in the BB2020 rulebook.
 * See page 55 in the BB2025 rulebook.
 *
 * Dodge can be modified in a number of ways. In this implementation it is
 * handled the following way:
 *
 * Note; the order of skills is the order they are resolved in.
 *
 * 1. Roll D6.
 * 2. Calculate required modifiers. These apply to both roll and reroll.
 *      a. -1 for each marking player in the target field.
 *      b. Stunty* (Ignore all -1 marked modifiers in the target field).
 *      c. Titchy* (+1).
 * 3. Choose optional modifiers. These apply to both roll and reroll.
 *      a. Two Heads (+1).
 *      b. Break Tackle (+1/+2 in 2020, +1/+2/+3 in 2025).
 *      c. Prehensile Tail (-1).
 *      d. Diving Tackle (-2, and user prone).
 * 4. Choose to use Tackle or not.
 * 5. Choose to Reroll or not.
 * 6. If Reroll. Choose optional modifiers with negative consequences for the user.
 *      a. Diving Tackle
 * 7. Calculate the final result.
 *
 * Designer's Commentary (BB2020):
 * It is possible to wait using Diving Tackle until the reroll has been made.
 *
 * Developer's Commentary:
 * Given the Designer's Commentary, technically all optional skills not selected
 * before the first roll should go through another selection process, but I
 * cannot find any reason why you would want that. It makes sense for "Diving
 * Tackle", since it brings a penalty, but for others it doesn't.
 *
 * The only reason you would want to avoid using them is to intentionally fail
 * the roll, in which case, you wouldn't want to apply them after a reroll
 * either.
 *
 * Also, it is unclear from the rules who chooses to use skills first, e.g.,
 * Break Tackle and Prehensile Tail. In this case, it doesn't matter since they
 * are both "free", but in other cases it might.
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
            return castDiceRoll<D6Result>(action) { d6 ->
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
     *  1. -1 for each player marking the field being moved into, unless they
     *     have Titchy*.
     *  2. Stunty* (Ignore all - marked modifiers in the target field)
     *  3. Titchy* (+1)
     */
    object CalculateMandatoryModifiers : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<DodgeRollContext>()
            val player = context.player
            val modifiers = buildList {
                // Add marking modifiers if the moving player doesn't have Stunty.
                // Players with Titchy do not count when counting marks on the square
                // being dodged into.
                if (!player.hasSkill(SkillType.STUNTY)) {
                    val marks = rules.getMarkingPlayers(
                        game = state,
                        markedTeam = player.team,
                        square = context.targetSquare
                    ).count { !it.isSkillAvailable(SkillType.TITCHY) }
                    if (marks != 0) {
                        add(MarkedModifier(marks, DodgeRollModifier.MARKED))
                    }
                }
                if (player.hasSkill(SkillType.TITCHY)) {
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
     * Choose whether the dodging player should use Two Heads (if applicable).
     */
    object ChooseToUseTwoHeads: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<DodgeRollContext>().player.team

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<DodgeRollContext>()
            return if (context.player.isSkillAvailable(SkillType.TWO_HEADS)) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<DodgeRollContext>()
            return compositeCommandOf(
                when (action) {
                    Confirm -> {
                        ReportSkillUsed(context.player, SkillType.TWO_HEADS)
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
            return if (context.player.isSkillAvailable(SkillType.BREAK_TACKLE)) {
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
                    val modifier = BreakTackleModifier(player.strength, rules.baseVersion)
                    compositeCommandOf(
                        ReportSkillUsed(context.player, SkillType.BREAK_TACKLE),
                        SetContext(context.copyAndAddModifier(modifier)),
                        SetSkillUsed(player = player, skill = player.getSkill(SkillType.BREAK_TACKLE), used = true),
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
                .filter { it.isSkillAvailable(SkillType.PREHENSILE_TAIL) }
                .let { players ->
                    when (players.isNotEmpty()) {
                        true -> SelectPlayer.fromPlayers(players)
                        false -> null
                    }
                }
            return if (eligiblePlayers == null) {
                listOf(ContinueWhenReady)
            } else {
                listOf(eligiblePlayers, CancelWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<DodgeRollContext>()
            return when (action) {
                is PlayerSelected -> {
                    val player = action.getPlayer(state)
                    compositeCommandOf(
                        ReportSkillUsed(player, SkillType.PREHENSILE_TAIL),
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
     * If multiple players have it, only 1 can use it.
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
                .filter { it.isSkillAvailable(SkillType.DIVING_TACKLE) }
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
                    val skill = player.getSkill(SkillType.DIVING_TACKLE)
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
            val context = state.getContext<DodgeRollContext>()
            val afterReroll = (context.roll?.rerolledResult != null)
            val success = testAgainstAgility(context.player, context.roll!!.result, context.rollModifiers)
            return compositeCommandOf(
                SetContext(context.copy(isSuccess = success)),
                if (afterReroll) ExitProcedure() else GotoNode(ChooseToUseTackle)
            )
        }
    }

    object ChooseToUseTackle: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<DodgeRollContext>().player.team.otherTeam()
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<DodgeRollContext>()
            val dodgingPlayerHasDodge = context.player.isSkillAvailable(SkillType.DODGE)
            val playersWithTackle = context.startingSquare.getSurroundingCoordinates(rules, distance = 1, includeOutOfBounds = false).mapNotNull {
                val player = state.field[it].player
                if (player != null && player.isSkillAvailable(SkillType.TACKLE)) {
                    player
                } else {
                    null
                }
            }

            return if (dodgingPlayerHasDodge && playersWithTackle.isNotEmpty()) {
                listOf(SelectPlayer.fromPlayers(playersWithTackle), CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<DodgeRollContext>()
            return when (action) {
                is PlayerSelected -> {
                    val tacklePlayer = action.getPlayer(state)
                    compositeCommandOf(
                        ReportSkillUsed(tacklePlayer, SkillType.TACKLE),
                        SetContext(context.copy(useTackle = tacklePlayer)),
                        GotoNode(ChooseReRollSource)
                    )
                }
                Cancel, Continue -> {
                    GotoNode(ChooseReRollSource)
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    /**
     * Choose where a reroll should come from (if any). This can be skills, team rerolls, special cards
     * or other sources.
     */
    object ChooseReRollSource : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<DodgeRollContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
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
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
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
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = state.rerollContext!!.source.rerollProcedure
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
            return castDiceRoll<D6Result>(action) { d6 ->
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
