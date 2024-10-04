//package dk.ilios.jervis.procedures.actions.block
//
//import compositeCommandOf
//import dk.ilios.jervis.actions.ActionDescriptor
//import dk.ilios.jervis.actions.Continue
//import dk.ilios.jervis.actions.ContinueWhenReady
//import dk.ilios.jervis.actions.DBlockResult
//import dk.ilios.jervis.actions.Dice
//import dk.ilios.jervis.actions.DiceResults
//import dk.ilios.jervis.actions.GameAction
//import dk.ilios.jervis.actions.NoRerollSelected
//import dk.ilios.jervis.actions.RerollOptionSelected
//import dk.ilios.jervis.actions.RollDice
//import dk.ilios.jervis.actions.SelectDiceResult
//import dk.ilios.jervis.actions.SelectNoReroll
//import dk.ilios.jervis.actions.SelectRerollOption
//import dk.ilios.jervis.commands.Command
//import dk.ilios.jervis.commands.SetContext
//import dk.ilios.jervis.commands.SetOldContext
//import dk.ilios.jervis.commands.fsm.ExitProcedure
//import dk.ilios.jervis.commands.fsm.GotoNode
//import dk.ilios.jervis.fsm.ActionNode
//import dk.ilios.jervis.fsm.Node
//import dk.ilios.jervis.fsm.ParentNode
//import dk.ilios.jervis.fsm.Procedure
//import dk.ilios.jervis.model.Game
//import dk.ilios.jervis.model.Team
//import dk.ilios.jervis.model.context.UseRerollContext
//import dk.ilios.jervis.model.context.assertContext
//import dk.ilios.jervis.model.context.getContext
//import dk.ilios.jervis.procedures.BlockDieRoll
//import dk.ilios.jervis.reports.ReportDiceRoll
//import dk.ilios.jervis.rules.Rules
//import dk.ilios.jervis.rules.skills.DiceRerollOption
//import dk.ilios.jervis.rules.skills.DiceRollType
//import dk.ilios.jervis.rules.skills.RerollSource
//import dk.ilios.jervis.rules.skills.Skill
//import dk.ilios.jervis.utils.INVALID_ACTION
//
///**
// * Procedure for handling a Block dice roll. It is only responsible for handling the actual dice roll.
// * The result is stored in [BlockRollContext] and it is up to the caller of the procedure to
// * choose the appropriate action depending on the outcome. This also includes deleting the [BlockContext]
// * again.
// */
//object BlockRoll : Procedure() {
//    override val initialNode: Node = RollDice
//    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
//    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
//    override fun isValid(state: Game, rules: Rules) = state.assertContext<BlockContext>()
//
//    object RollDice : ActionNode() {
//        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlockContext>().attacker.team
//        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
//            val noOfDice = calculateNoOfBlockDice(state)
//            return listOf(RollDice(List(noOfDice) { Dice.BLOCK }))
//        }
//
//        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
//            return checkDiceRollList<DBlockResult>(action) { it: List<DBlockResult> ->
//                val roll =
//                    it.map { diceRoll: DBlockResult ->
//                        BlockDieRoll(originalRoll = diceRoll)
//                    }
//
//                val context = state.getContext<BlockContext>()
//                val result = BlockRollContext(
//                    context.attacker,
//                    context.defender,
//                    context.isBlitzing,
//                    context.isUsingMultiBlock,
//                    roll,
//                    hasAcceptedResult = false,
//                )
//
//                return compositeCommandOf(
//                    ReportDiceRoll(roll),
//                    SetContext(result),
//                    GotoNode(ChooseResultOrReRollSource),
//                )
//            }
//        }
//    }
//
//    object ChooseResultOrReRollSource : ActionNode() {
//        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlockContext>().attacker.team
//        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
//            val context = state.getContext<BlockContext>()
//            val rollContext = state.getContext<BlockRollContext>()
//            val attackingPlayer = context.attacker
//
//            // Re-rolling block dice can be pretty complex,
//            // Brawler: Can reroll a single "Both Down"
//            // Pro: Can reroll any single die
//            // Team reroll: Can reroll all of them
//            val availableSkills: List<SelectRerollOption> =
//                attackingPlayer.skills
//                    .filter { skill: Skill -> skill is RerollSource }
//                    .map { it as RerollSource }
//                    .filter { it.canReroll(DiceRollType.BLOCK, rollContext.roll) }
//                    .flatMap { it.calculateRerollOptions(DiceRollType.BLOCK, rollContext.roll) }
//                    .map { rerollOption: DiceRerollOption -> SelectRerollOption(rerollOption) }
//
//            val team = attackingPlayer.team
//            val hasTeamRerolls = team.availableRerollCount > 0
//            val allowedToUseTeamReroll =
//                when (team.usedRerollThisTurn) {
//                    true -> rules.allowMultipleTeamRerollsPrTurn
//                    false -> true
//                }
//            return if (availableSkills.isEmpty() && (!hasTeamRerolls || !allowedToUseTeamReroll)) {
//                listOf(ContinueWhenReady)
//            } else {
//                val teamRerolls =
//                    if (hasTeamRerolls && allowedToUseTeamReroll) {
//                        listOf(SelectRerollOption(DiceRerollOption(team.availableRerolls.last(), rollContext.roll)))
//                    } else {
//                        emptyList()
//                    }
//                listOf(SelectNoReroll(null)) + availableSkills + teamRerolls
//            }
//        }
//
//        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
//            return when (action) {
//                // TODO What is the difference between Continue and NoRerollSelected
//                Continue,
//                NoRerollSelected -> GotoNode(SelectBlockResult)
//                is RerollOptionSelected -> {
//                    val rerollContext = UseRerollContext(DiceRollType.BLOCK, action.getRerollSource(state))
//                    compositeCommandOf(
//                        SetOldContext(Game::rerollContext, rerollContext),
//                        GotoNode(UseRerollSource),
//                    )
//                }
//                else -> INVALID_ACTION(action)
//            }
//        }
//    }
//
//    object UseRerollSource : ParentNode() {
//        override fun getChildProcedure(state: Game, rules: Rules, ): Procedure = state.rerollContext!!.source.rerollProcedure
//        override fun onExitNode( state: Game, rules: Rules): Command {
//            // useRerollResult must be set by the procedure running which determines if a reroll is allowed
//            return if (state.rerollContext!!.rerollAllowed) {
//                GotoNode(ReRollDie)
//            } else {
//                ExitProcedure()
//            }
//        }
//    }
//
//    object ReRollDie : ActionNode() {
//        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlockContext>().attacker.team
//        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
//            val noOfDice = calculateNoOfBlockDice(state)
//            return listOf(RollDice(List(noOfDice) { Dice.BLOCK }))
//        }
//
//        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
//            return checkDiceRollList<DBlockResult>(action) { rolls: List<DBlockResult> ->
//                val roll =
//                    rolls.map { blockRoll: DBlockResult ->
//                        BlockDieRoll(originalRoll = blockRoll)
//                    }
//                compositeCommandOf(
//                    SetContext(state.getContext<BlockRollContext>().copy(roll = roll)),
//                    GotoNode(ChooseResultOrReRollSource),
//                )
//            }
//        }
//    }
//
//    object SelectBlockResult : ActionNode() {
//        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlockContext>().attacker.team
//        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
//            return listOf(
//                SelectDiceResult(state.getContext<BlockRollContext>().roll.map { it.result })
//            )
//        }
//
//        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
//            return checkDiceRoll<DBlockResult>(action) {
//                val selectedDie = when(action) {
//                    is DBlockResult -> action
//                    is DiceResults -> action.rolls.first() as DBlockResult
//                    else -> INVALID_ACTION(action)
//                }
//
//                val roll = state.getContext<BlockContext>()
//                val result = BlockRollContext(
//                    roll.attacker,
//                    roll.defender,
//                    roll.isBlitzing,
//                    roll.isUsingMultiBlock,
//                    roll.roll,
//                    true,
//                    selectedDie,
//                )
//
//                compositeCommandOf(
//                    SetContext(result),
//                    ExitProcedure()
//                )
//            }
//        }
//    }
//
//    // Helper method to share logic between roll and reroll
//    private fun calculateNoOfBlockDice(state: Game): Int {
//        val context = state.getContext<BlockContext>()
//        val attackStrength = context.attacker.strength + context.offensiveAssists
//        val defenderStrength = context.defender.strength + context.defensiveAssists
//        return when {
//            attackStrength == defenderStrength -> 1
//            attackStrength > defenderStrength * 2 -> 3
//            defenderStrength > attackStrength * 2 -> 3
//            else -> 2
//        }
//    }
//}
