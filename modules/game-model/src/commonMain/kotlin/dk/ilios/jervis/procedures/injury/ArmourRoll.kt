package dk.ilios.jervis.procedures.injury

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.utils.sum

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
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> = listOf(RollDice(Dice.D6, Dice.D6))

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
                val result = roll.sum()
                val modifiers = emptyList<DiceModifier>() // Any skills that modify the result
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
