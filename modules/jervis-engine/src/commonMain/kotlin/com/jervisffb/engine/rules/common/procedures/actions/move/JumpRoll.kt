package com.jervisffb.engine.rules.common.procedures.actions.move

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.JumpRollContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.JumpModifier
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.testAgainstAgility
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION

/**
 * Procedure for handling a Jump Roll as described on page 45 in the rulebook.
 * It is only responsible for handling the actual dice roll. The result is stored
 * in [JumpRollContext] and it is up to the caller of the procedure to choose
 * the appropriate action depending on the outcome.
 */
object JumpRoll : D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.JUMP
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<JumpRollContext>()
    override fun getActionOwner(state: Game): Team = state.getContext<JumpRollContext>().player.team

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val rollContext = state.getContext<JumpRollContext>()
            return rollContext.copy(
                roll = D6DieRoll.create(state, d6),
                isSuccess = isSuccess(rollContext, overrideD6 = d6)
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource(
        exitWithoutRerollCommand = { GotoNode(ChooseToUseDivingTackleAfterReRoll) }
    ) {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<JumpRollContext>()
            return RerollData(context.player, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val rollContext = state.getContext<JumpRollContext>()
            return rollContext.copy(
                roll = rollContext.roll!!.copyReroll(
                    rerollSource = state.rerollContext!!.source,
                    rerolledResult = d6,
                ),
                isSuccess = isSuccess(rollContext, overrideD6 = d6)
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
        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state).otherTeam()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<JumpRollContext>()
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
            val context = state.getContext<JumpRollContext>()
            return when (action) {
                is PlayerSelected -> {
                    val player = action.getPlayer(state)
                    val skill = player.getSkill(SkillType.DIVING_TACKLE)
                    val updatedModifiers = context.modifiers.add(JumpModifier.DIVING_TACKLE)
                    val success = isSuccess(context, overrideModifiers = updatedModifiers)
                    compositeCommandOf(
                        ReportSkillUsed(player, skill),
                        UpdateContext(context.copy(
                            modifiers = updatedModifiers,
                            isSuccess = success
                        )),
                        SetPlayerState(player, PlayerState.PRONE, hasTackleZones = false),
                        SetPlayerLocation(player, context.startingSquare),
                        ExitProcedure()
                    )
                }
                Cancel,
                Continue -> ExitProcedure()
                else -> INVALID_ACTION(action)
            }
        }
    }

    // -- HELPER METHODS --

    private fun isSuccess(
        context: JumpRollContext,
        overrideD6: D6Result? = null,
        overrideModifiers: List<DiceModifier>? = null
    ): Boolean {
        val player = context.player
        val d6 = overrideD6 ?: context.roll!!.result
        val modifiers = overrideModifiers ?: context.modifiers
        return testAgainstAgility(player, d6, modifiers)
    }
}
