package com.jervisffb.engine.rules.common.procedures.actions.move

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.HelpingHandsModifier
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.sum

/**
 * Procedure for handling a Standing Up Roll as described on page 44 in the rulebook.
 * It is only responsible for handling the actual dice roll. The result is stored
 * in [StandingUpRollContext] and it is up to the caller of the procedure to choose
 * the appropriate action depending on the outcome.
 */
object StandingUpRoll : D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.STANDING_UP
    override val initialNode: Node = UseTimmber
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<StandingUpRollContext>()
    override fun getActionOwner(state: Game): Player = state.getContext<StandingUpRollContext>().player

    // The only modifier for Standing Up currently comes from Timm-ber!
    // We will apply these automatically since doing it or not, has no
    // side effects, and if you do not want the player to stand up,
    // you will never attempt it in the first place.
    object UseTimmber: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<StandingUpRollContext>()
            val player = context.player
            val useSkill = player.isSkillAvailable(SkillType.TIMMMBER)
            val openPlayers = when (useSkill) {
                true -> {
                    player.coordinates.getSurroundingCoordinates(rules, 1)
                        .count { coordinate ->
                            val neighborPlayer = state.field[coordinate].player
                            val sameTeam = neighborPlayer?.team == player.team
                            val isOpen = neighborPlayer?.let { rules.isOpen(it) } ?: false
                            sameTeam && isOpen
                        }
                }
                false -> 0
            }
            return buildCompositeCommand {
                if (useSkill) {
                    addAll(
                        ReportSkillUsed(context.player, SkillType.TIMMMBER),
                        UpdateContext(context.copy(modifiers = context.modifiers.add(HelpingHandsModifier(openPlayers))))
                    )
                }
                add(GotoNode(RollDie))
            }
        }
    }

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val rollContext = state.getContext<StandingUpRollContext>()
            return rollContext.copy(
                roll = D6DieRoll.create(state, d6),
                isSuccess = isStandingUp(d6, rules.standingUpTarget, rollContext.modifiers)
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<StandingUpRollContext>()
            return RerollData(context.player, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val rollContext = state.getContext<StandingUpRollContext>()
            return rollContext.copy(
                roll = rollContext.roll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
                isSuccess = isStandingUp(d6, rules.standingUpTarget, rollContext.modifiers)
            )
        }
    }

    private fun isStandingUp(
        it: D6Result,
        target: Int,
        modifiers: List<DiceModifier>,
    ): Boolean {
        return it.value != 1 && (target <= it.value + modifiers.sum())
    }
}
