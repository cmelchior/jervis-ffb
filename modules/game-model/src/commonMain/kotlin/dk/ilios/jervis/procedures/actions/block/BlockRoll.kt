package dk.ilios.jervis.procedures.actions.block

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectDiceResult
import dk.ilios.jervis.actions.SelectNoReroll
import dk.ilios.jervis.actions.SelectRerollOption
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.BlockDieRoll
import dk.ilios.jervis.procedures.RerollContext
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRerollOption
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.skills.RerollSource
import dk.ilios.jervis.rules.skills.Skill
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Procedure for handling a Catch Roll. It is only responsible for handling the actual dice roll.
 * The result is stored in [Game.catchRollResultContext] and it is up to the caller of the procedure to
 * choose the appropriate action depending on the outcome.
 */
object BlockRoll : Procedure() {
    override fun isValid(
        state: Game,
        rules: Rules,
    ) {
        if (state.blockContext == null) {
            INVALID_GAME_STATE("No catch roll context found")
        }
    }

    override val initialNode: Node = RollDice

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    // Helper method to share logic between roll and reroll
    private fun calculateNoOfBlockDice(state: Game): Int {
        val context = state.blockContext!!
        val attackStrength = context.attacker.strength + context.offensiveAssists
        val defenderStrength = context.defender.strength + context.defensiveAssists
        return when {
            attackStrength == defenderStrength -> 1
            attackStrength > defenderStrength * 2 -> 3
            defenderStrength > attackStrength * 2 -> 3
            else -> 2
        }
    }

    object RollDice : ActionNode() {
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> {
            val noOfDice = calculateNoOfBlockDice(state)
            return listOf(RollDice(List(noOfDice) { Dice.BLOCK }))
        }

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return checkDiceRollList<DBlockResult>(action) { it: List<DBlockResult> ->
                val roll =
                    it.map { diceRoll: DBlockResult ->
                        BlockDieRoll(originalRoll = diceRoll)
                    }
                return compositeCommandOf(
                    SetContext(Game::blockContext, state.blockContext!!.copy(roll = roll)),
                    GotoNode(ChooseResultOrReRollSource),
                )
            }
        }
    }

    object ChooseResultOrReRollSource : ActionNode() {
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> {
            val context = state.blockContext!!
            val attackingPlayer = context.attacker

            // Re-rolling block dice can be pretty complex,
            // Brawler: Can reroll a single "Both Down"
            // Pro: Can reroll any single die
            // Team reroll: Can reroll all of them
            val availableSkills: List<SelectRerollOption> =
                attackingPlayer.skills
                    .filter { skill: Skill -> skill is RerollSource }
                    .map { it as RerollSource }
                    .filter { it.canReroll(DiceRollType.BlockRoll, context.roll) }
                    .flatMap { it.calculateRerollOptions(DiceRollType.BlockRoll, context.roll) }
                    .map { rerollOption: DiceRerollOption -> SelectRerollOption(rerollOption) }

            val team = attackingPlayer.team
            val hasTeamRerolls = team.availableRerollCount > 0
            val allowedToUseTeamReroll =
                when (team.usedTeamRerollThisTurn) {
                    true -> rules.allowMultipleTeamRerollsPrTurn
                    false -> true
                }
            return if (availableSkills.isEmpty() && (!hasTeamRerolls || !allowedToUseTeamReroll)) {
                listOf(ContinueWhenReady)
            } else {
                val teamRerolls =
                    if (hasTeamRerolls && allowedToUseTeamReroll) {
                        listOf(SelectRerollOption(DiceRerollOption(team.availableRerolls.last(), context.roll)))
                    } else {
                        emptyList()
                    }
                listOf(SelectNoReroll) + availableSkills + teamRerolls
            }
        }

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return when (action) {
                // TODO What is the difference between Continue and NoRerollSelected
                NoRerollSelected -> GotoNode(SelectBlockResult)
                is RerollOptionSelected -> {
                    val rerollContext = RerollContext(DiceRollType.BlockRoll, action.option.source)
                    compositeCommandOf(
                        SetContext(Game::useRerollContext, rerollContext),
                        GotoNode(UseRerollSource),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object UseRerollSource : ParentNode() {
        override fun getChildProcedure(
            state: Game,
            rules: Rules,
        ): Procedure = state.useRerollContext!!.source.rerollProcedure

        override fun onExitNode(
            state: Game,
            rules: Rules,
        ): Command {
            // useRerollResult must be set by the procedure running determing if a reroll is allowed
            return if (state.useRerollResult!!.rerollAllowed) {
                GotoNode(ReRollDie)
            } else {
                ExitProcedure()
            }
        }
    }

    object ReRollDie : ActionNode() {
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> {
            val noOfDice = calculateNoOfBlockDice(state)
            return listOf(RollDice(List(noOfDice) { Dice.BLOCK }))
        }

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return checkDiceRollList<DBlockResult>(action) { rolls: List<DBlockResult> ->
                val roll =
                    rolls.map { blockRoll: DBlockResult ->
                        BlockDieRoll(originalRoll = blockRoll)
                    }
                compositeCommandOf(
                    SetContext(Game::blockContext, state.blockContext!!.copy(roll = roll)),
                    GotoNode(ChooseResultOrReRollSource),
                )
            }
        }
    }

    object SelectBlockResult : ActionNode() {
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> {
            return listOf(
                SelectDiceResult(state.blockContext!!.roll.map { it.result }, 1)
            )
        }

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            if (action !is DBlockResult && !(action is DiceResults && action.rolls.size == 1)) {
                INVALID_ACTION(action)
            }

            val selectedDie = when(action) {
                is DBlockResult -> action
                is DiceResults -> action.rolls.first() as DBlockResult
                else -> INVALID_ACTION(action)
            }

            val roll = state.blockContext!!
            val result = BlockResultContext(
                roll.attacker,
                roll.defender,
                roll.isBlitzing,
                roll.isUsingMultiBlock,
                roll.roll,
                selectedDie,
            )

            return compositeCommandOf(
                SetContext(Game::blockRollResultContext, result),
                ExitProcedure()
            )
        }
    }
}
