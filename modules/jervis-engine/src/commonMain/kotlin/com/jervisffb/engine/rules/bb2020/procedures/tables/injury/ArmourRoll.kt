package com.jervisffb.engine.rules.bb2020.procedures.tables.injury

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.sum

/**
 * Implement the armour roll as described on page 60 in the rulebook.
 *
 * The result is stored in [Game.armourRollResultContext] and it is up
 * to the caller to determine what to do with the result.
 *
 * Regarding Claws and Mighty Blow:
 *  - There would be multiple ways to implement the flow
 *      a. Select all skills that apply at once
 *      b. Select them one at a time
*   - It isn't clear what approach is the best (can also be deferred a bit)
 *    Although Claws would realistically always be the best option, if MB(4+)
 *    exists, then a 7 + MB would break Armour 10, where 7 + Claw would not.
 *
 *  - No matter what the UI can choose to do it differently, but it would
 *    be nice to not encode rules logic there.
 *
 *  - Roll -> Apply Claw -> Apply MB -> ... others?
 *  - Roll -> Apply Skills (find all skills that apply)
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
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<GameActionDescriptor> = listOf(RollDice(Dice.D6, Dice.D6))

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return checkDiceRoll<D6Result, D6Result>(action) { die1, die2 ->
                val context = state.getContext<RiskingInjuryContext>()

                // Determine result of armour roll
                // TODO This logic needs to be expanded to support things like Mighty Blow, Claw, Chainsaw and others.
                val roll = listOf(die1, die2)
                val modifiers = context.armourModifiers // Any skills that modify the result
                val result = roll.sum() + modifiers.sum()
                val broken = (context.player.armorValue <= result)

                val updatedContext = state.getContext<RiskingInjuryContext>().copy(
                    armourRoll = roll,
                    armourResult = result,
                    armourModifiers = modifiers,
                    armourBroken = broken
                )
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.ARMOUR, roll),
                    SetContext(updatedContext),
                    ExitProcedure()
                )
            }
        }
    }
}
