package dk.ilios.bowlbot.controller

// Board State
data class BoardState(var position: Int)

// Rules with state
data class RulesState(var someRuleState: Int)

class Rules(private val state: RulesState) {
    fun isValidMove(currentPosition: Int, steps: Int): Boolean {
        // Use the state in some way to determine if a move is valid.
        // Here, it's a simple example where the move is invalid if the `someRuleState` is 0.
        return state.someRuleState != 0
    }

    fun updateRuleStateAfterMove() {
        // Example: Decrease rule state by one after each move.
        state.someRuleState -= 1
    }
}

// Game Commands
interface GameCommand {
    fun execute()
    fun undo()
}

class MoveCommand(
    private val rules: Rules,
    private val boardState: BoardState,
    private val rulesState: RulesState,
    private val steps: Int
) : GameCommand {
    private val previousBoardPosition = boardState.position
    private val previousRulesState = rulesState.someRuleState

    override fun execute() {
        if (rules.isValidMove(boardState.position, steps)) {
            boardState.position += steps
            rules.updateRuleStateAfterMove()
        } else {
            println("Invalid move!")
        }
    }

    override fun undo() {
        boardState.position = previousBoardPosition
        rulesState.someRuleState = previousRulesState
    }
}

// Game Controller
class GameController(
    private val boardState: BoardState,
    private val rulesState: RulesState,
    private val rules: Rules
) {
    private val commandHistory = mutableListOf<GameCommand>()

    fun move(steps: Int) {
        val moveCommand = MoveCommand(rules, boardState, rulesState, steps)
        moveCommand.execute()
        commandHistory.add(moveCommand)
    }

    fun undo() {
        if (commandHistory.isNotEmpty()) {
            val lastCommand = commandHistory.removeAt(commandHistory.size - 1)
            lastCommand.undo()
        }
    }

    fun redo() {
        if (commandHistory.size > 0) {
            val lastCommand = commandHistory.last()
            lastCommand.execute()
        }
    }

    override fun toString(): String {
        return "GameController(boardState=$boardState, rulesState=$rulesState)"
    }
}

fun main() {
    val boardState = BoardState(0)
    val rulesState = RulesState(5)
    val rules = Rules(rulesState)
    val gameController = GameController(boardState, rulesState, rules)

    gameController.move(3)
    println(gameController)

    gameController.move(-1)
    println(gameController)

    gameController.undo()
    println(gameController)

    gameController.redo()
    println(gameController)
}