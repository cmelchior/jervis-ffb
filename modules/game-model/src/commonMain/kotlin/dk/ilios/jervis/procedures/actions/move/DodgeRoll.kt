package dk.ilios.jervis.procedures.actions.move

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CancelWhenReady
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectNoReroll
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.actions.SelectRerollOption
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetOldContext
import dk.ilios.jervis.commands.SetSkillUsed
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.DodgeRollContext
import dk.ilios.jervis.model.context.UseRerollContext
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.context.hasContext
import dk.ilios.jervis.model.hasSkill
import dk.ilios.jervis.model.isSkillAvailable
import dk.ilios.jervis.model.modifiers.BreakTackleModifier
import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.model.modifiers.DodgeRollModifier
import dk.ilios.jervis.model.modifiers.MarkedModifier
import dk.ilios.jervis.procedures.D6DieRoll
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.reports.ReportDodgeResult
import dk.ilios.jervis.reports.ReportSkillUsed
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.BreakTackle
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.skills.DivingTackle
import dk.ilios.jervis.rules.skills.PrehensileTail
import dk.ilios.jervis.rules.skills.Stunty
import dk.ilios.jervis.rules.skills.Titchy
import dk.ilios.jervis.rules.skills.TwoHeads
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.calculateAvailableRerollsFor
import dk.ilios.jervis.utils.sum

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

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<DodgeRollContext>()
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.DODGE, d6),
                    SetContext(context.copy(roll = D6DieRoll(d6))),
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
     *  2. Stunty* (Ignore all -1 marked modifiers in target field)
     *  3. Titchy* (+1)
     */
    object CalculateMandatoryModifiers : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<DodgeRollContext>()
            val player = context.player
            val modifiers = buildList {
                // Add marking modifiers if moving player doesn't have Stunty.
                if (!player.hasSkill<Stunty>()) {
                    val marks = rules.calculateMarks(state, player.team, context.targetSquare)
                    if (marks > 0) add(MarkedModifier(marks * -1))
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

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
            val success = isDodgeSuccess(context.player, context.roll!!.result, context.rollModifiers)
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
        ): List<ActionDescriptor> {
            val context = state.getContext<DodgeRollContext>()
            val dodgingPlayer = context.player
            val availableReRolls: List<SelectRerollOption> = calculateAvailableRerollsFor(
                rules,
                dodgingPlayer,
                DiceRollType.DODGE,
                context.roll!!,
                context.isSuccess
            )
            return if (availableReRolls.isEmpty()) {
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
                NoRerollSelected -> ExitProcedure()
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
        ): List<ActionDescriptor> = listOf(RollDice(Dice.D6))

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val dodgeContext = state.getContext<DodgeRollContext>()
                val rerollContext = state.rerollContext!!
                val rerolledDodgeRoll = dodgeContext.copy(
                    roll = dodgeContext.roll!!.copy(
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

    private fun isDodgeSuccess(player: Player, d6: D6Result, modifiers: List<DiceModifier>): Boolean {
        val target = player.agility
        return when (d6.value) {
            1 -> false
            in 2..5 -> d6.value + modifiers.sum() >= target && d6.value != 1
            6 -> true
            else -> error("Invalid value: ${d6.value}")
        }
    }
}
