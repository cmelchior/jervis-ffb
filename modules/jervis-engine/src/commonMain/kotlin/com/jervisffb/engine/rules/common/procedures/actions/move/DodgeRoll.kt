package com.jervisffb.engine.rules.common.procedures.actions.move

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetSkillUsed
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.DodgeRollContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.BreakTackleModifier
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.DodgeRollModifier
import com.jervisffb.engine.model.modifiers.MarkedModifier
import com.jervisffb.engine.reports.ReportDodgeResult
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.testAgainstAgility
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.collections.immutable.toPersistentList

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
object DodgeRoll: D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.DODGE
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<DodgeRollContext>()
        return ReportDodgeResult(context)
    }
    override fun isValid(state: Game, rules: Rules) = state.assertContext<DodgeRollContext>()
    override fun getActionOwner(state: Game): Player = state.getContext<DodgeRollContext>().player

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<DodgeRollContext>()
            return context.copy(roll = D6DieRoll.create(state, d6))
        }
        override val nextNode: Node = CalculateMandatoryModifiers
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
                UpdateContext(context.copy(rollModifiers = modifiers.toPersistentList())),
                GotoNode(ChooseToUseTwoHeads)
            )
        }
    }

    /**
     * Choose whether the dodging player should use Two Heads (if applicable).
     */
    object ChooseToUseTwoHeads: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state).team

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
                        UpdateContext(context.copyAndAddModifier(DodgeRollModifier.TWO_HEADS))
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
        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state).team

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
                        UpdateContext(context.copyAndAddModifier(modifier)),
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
        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state).team.otherTeam()

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
                        UpdateContext(context.copyAndAddModifier(DodgeRollModifier.PREHENSILE_TAIL)),
                        GotoNode(ChooseToUseDivingTackleBeforeRoll)
                    )
                }
                is Cancel,
                is Continue -> {
                    GotoNode(ChooseToUseDivingTackleBeforeRoll)
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    /**
     * Choose whether to use Diving Tackle (if applicable).
     * If multiple players have it, only 1 can use it.
     * It is only BB2020 that allows you to use it before rolling, in BB2025,
     * this choice is after.
     */
    object ChooseToUseDivingTackleBeforeRoll: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state).team.otherTeam()

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
            val canUseBeforeRoll = (rules.baseVersion == GameVersion.BB2020)

            return if (!canUseBeforeRoll || eligiblePlayers.isEmpty()) {
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
                    val updatedModifiers = context.rollModifiers.add(DodgeRollModifier.DIVING_TACKLE)
                    compositeCommandOf(
                        ReportSkillUsed(player, skill),
                        UpdateContext(context.copy(
                            rollModifiers = updatedModifiers,
                        )),
                        GotoNode(ChooseToUseTackle)
                    )
                }
                is Cancel,
                is Continue -> GotoNode(ChooseToUseTackle)
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ChooseToUseTackle: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state).team.otherTeam()
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
                        UpdateContext(context.copy(useTackle = tacklePlayer)),
                        GotoNode(CalculateSuccessBeforeReroll)
                    )
                }
                Cancel, Continue -> {
                    GotoNode(CalculateSuccessBeforeReroll)
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object CalculateSuccessBeforeReroll: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<DodgeRollContext>()
            val success = isSuccess(context)
            return compositeCommandOf(
                UpdateContext(context.copy(isSuccess = success)),
                GotoNode(ChooseReRollSource)
            )
        }
    }

    /**
     * Choose where a reroll should come from (if any). This can be skills, team rerolls, special cards
     * or other sources.
     */
    override val ChooseReRollSource = object : AbstractChooseRerollSource(
        exitWithoutRerollCommand = { GotoNode(ChooseToUseDivingTackleAfterReRoll) }
    ) {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<DodgeRollContext>()
            return RerollData(context.player, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val dodgeContext = state.getContext<DodgeRollContext>()
            val rerollContext = state.getRerollContext()
            return dodgeContext.copy(
                roll = dodgeContext.roll!!.copyReroll(
                    rerollSource = rerollContext.source,
                    rerolledResult = d6,
                ),
                isSuccess = isSuccess(dodgeContext, overrideD6 = d6)
            )
        }
        override fun nextNodeCommand(): Command = GotoNode(ChooseToUseDivingTackleAfterReRoll)
    }

    // Needs to be below ReRollDie due to initialization order issues.
    override val UseRerollSource = CommonUseRerollSource(
        rerollDiceNode = ReRollDie,
        noRerollCommand = { GotoNode(ChooseToUseDivingTackleAfterReRoll) }
    )

    object ChooseToUseDivingTackleAfterReRoll: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state).team.otherTeam()
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

            return if (eligiblePlayers.isNotEmpty()) {
                listOf(SelectPlayer.fromPlayers(eligiblePlayers), CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<DodgeRollContext>()
            return when (action) {
                is PlayerSelected -> {
                    val player = action.getPlayer(state)
                    val skill = player.getSkill(SkillType.DIVING_TACKLE)
                    val updatedModifiers = context.rollModifiers.add(DodgeRollModifier.DIVING_TACKLE)
                    val success = isSuccess(context, overrideModifiers = updatedModifiers)
                    // If a player use Fumblerooski, they might have left a ball in the square the Diving Tackle
                    // player ends up prone in. In that case, the ball will always bounce.
                    val ballInSquare = state.field[context.startingSquare].balls.isNotEmpty()
                    compositeCommandOf(
                        ReportSkillUsed(player, skill),
                        UpdateContext(context.copy(
                            rollModifiers = updatedModifiers,
                            isSuccess = success
                        )),
                        SetPlayerState(player, PlayerState.PRONE, hasTackleZones = false),
                        SetPlayerLocation(player, context.startingSquare),
                        if (ballInSquare) GotoNode(BounceBallInStartingSquare) else ExitProcedure(),
                    )
                }
                Cancel,
                Continue -> ExitProcedure()
                else -> INVALID_ACTION(action)
            }
        }
    }

    object BounceBallInStartingSquare: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<DodgeRollContext>()
            val ball = state.field[context.startingSquare].balls.singleOrNull() ?: INVALID_GAME_STATE("Too many balls in square: ${context.startingSquare}")
            return compositeCommandOf(
                SetBallState.bouncing(ball),
                SetCurrentBall(ball),
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetCurrentBall(null),
                ExitProcedure()
            )
        }
    }

    // -- HELPER METHODS --
    private fun isSuccess(
        context: DodgeRollContext,
        overrideD6: D6Result? = null,
        overrideModifiers: List<DiceModifier>? = null
    ): Boolean {
        val player = context.player
        val d6 = overrideD6 ?: context.roll!!.result
        val modifiers = overrideModifiers ?: context.rollModifiers
        return testAgainstAgility(player, d6, modifiers)
    }
}
