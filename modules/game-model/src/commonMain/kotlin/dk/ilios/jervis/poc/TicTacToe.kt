package dk.ilios.jervis.poc

//package dk.ilios.bowlbot.controller
//
//import dk.ilios.bowlbot.controller.Game.PlayGame.getChildProcedure
//import kotlin.math.log
//import kotlin.random.Random
//
////data class Action(val x: UInt, val y: UInt, val mark: Mark)
//data class Player(val name: String, val type: Mark)
//enum class Mark { X, O, BLANK }
//
//class Board {
//    val boardSize = 3u
//    private val board: Array<Array<Mark>> = Array(boardSize.toInt()) { Array(boardSize.toInt()) { Mark.BLANK } }
//
//    fun isBlank(x: UInt, y: UInt): Boolean {
//        return board[x.toInt()][y.toInt()] == Mark.BLANK
//    }
//
//    fun add(x: UInt, y: UInt, mark: Mark) {
//        board[x.toInt()][y.toInt()] = mark
//    }
//
//    fun isFull(): Boolean {
//        board.forEach {
//            it.forEach { cell ->
//                if (cell == Mark.BLANK) {
//                    return false
//                }
//            }
//        }
//        return true
//    }
//
//    fun printBoard(): String {
//        val sb = StringBuilder()
//        for (y in 0u until boardSize) {
//            for (x in 0u until boardSize) {
//                when(x) {
//                    0u -> sb.append("|")
//                    else -> sb.append(" ")
//                }
//                sb.append(when(board[x.toInt()][y.toInt()]) {
//                    Mark.X -> "X"
//                    Mark.O -> "O"
//                    Mark.BLANK -> "-"
//                })
//                if (x == boardSize - 1u) {
//                    sb.append("|")
//                }
//            }
//            if (y < boardSize - 1u) {
//                sb.append("\n")
//            }
//        }
//        return sb.toString()
//    }
//
//    fun iterateBoard(action: (x: UInt, y: UInt, content: Mark) -> Unit) {
//        for (y in 0u until boardSize) {
//            for (x in 0u until boardSize) {
//                action(x, y, board[x.toInt()][y.toInt()])
//            }
//        }
//    }
//
//    fun get(x: UInt, y: UInt): Mark {
//        return board[x.toInt()][y.toInt()]
//    }
//
//    fun isWinner(): Boolean {
//        // Check rows
//        for (row in board) {
//            if (row[0] != Mark.BLANK && row[0] == row[1] && row[1] == row[2]) {
//                return true
//            }
//        }
//
//        // Check columns
//        for (col in 0 until 3) {
//            if (board[0][col] != Mark.BLANK && board[0][col] == board[1][col] && board[1][col] == board[2][col]) {
//                return true
//            }
//        }
//
//        // Check diagonals
//        if (board[0][0] != Mark.BLANK && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
//            return true
//        }
//        if (board[0][2] != Mark.BLANK && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
//            return true
//        }
//
//        return false // No winner
//    }
//
//    fun isDraw(): Boolean {
//        return if (!isFull()) {
//            false
//        } else {
//            !isWinner()
//        }
//    }
//}
//
//class GameState(val p1: Player, val p2: Player, val board: Board) {
//    var currentPlayer: Player = p1
//}
//
//interface Procedure {
//    fun name(): String = this::class.simpleName!!
//    val initialState: ActionNode
//}
//
//
//interface Node {
//    fun name(): String = this::class.simpleName!!
//}
//abstract class ActionNode: Node {
//    abstract fun getAvailableActions(state: GameState): List<ActionDescriptor>
//    abstract fun applyAction(action: Action, state: GameState): Command
//    inline fun <reified T: Action> checkType(action: Action): T {
//        if (action is T) {
//            return action
//        } else {
//            throw IllegalArgumentException("Action (${action::class}) is not of the expected type: ${T::class}")
//        }
//    }
//    inline fun <reified T: Action> checkType(action: Action, function: (T) -> Command): Command {
//        if (action is T) {
//            return function(action)
//        } else {
//            throw IllegalArgumentException("Action (${action::class}) is not of the expected type: ${T::class}")
//        }
//    }
//}
//
//abstract class ComputationNode: ActionNode() {
//    abstract fun apply(state: GameState): Command
//    override fun getAvailableActions(state: GameState): List<ActionDescriptor> = listOf(ContinueWhenReady)
//    override fun applyAction(action: Action, state: GameState): Command {
//        return checkType<Continue>(action) {
//            apply(state)
//        }
//    }
//}
//
///**
// * Calling a sub procedure is its own state-machine with enter and exit events.
// */
//abstract class ParentNode: Procedure, Node {
//    override fun name(): String = this::class.simpleName!!
//    abstract fun onEnter(state: GameState): Command
//    abstract fun getChildProcedure(): Procedure
//    abstract fun onExit(state: GameState): Command
//
//    override val initialState: ActionNode = OnEnter()
//
//    private inner class OnEnter: ComputationNode() {
//        override fun apply(state: GameState): Command {
//            return compositeActionOf(
//                onEnter(state),
//                GotoNode(LoadSubProcedure())
//            )
//        }
//    }
//
//    private inner class LoadSubProcedure: ComputationNode() {
//        override fun apply(state: GameState): Command {
//            return compositeActionOf(
//                GotoNode(OnExit()), // Manipulate the stack by putting the exit event on before loading the next procedure
//                EnterProcedure(getChildProcedure())
//            )
//        }
//    }
//
//    private inner class OnExit: ComputationNode() {
//        override fun apply(state: GameState): Command {
//            return compositeActionOf(
//                ExitProcedure(),
//                onExit(state),
//            )
//         }
//    }
//}
//
//interface Command {
//    fun execute(state: GameState, controller: GameController)
//    fun undo(state: GameState, controller: GameController)
//}
//
//class CompositeCommand private constructor(private val commands: List<Command>) : Command {
//    class Builder {
//        private val commands = mutableListOf<Command>()
//        fun add(command: Command) = commands.add(command)
//        fun build(): CompositeCommand {
//            return CompositeCommand(commands)
//        }
//    }
//
//    override fun undo(state: GameState, controller: GameController) {
//        for (i in commands.size-1 downTo 0) {
//            commands[i].undo(state, controller)
//        }
//    }
//
//    override fun execute(state: GameState, controller: GameController) {
//        commands.forEach { it.execute(state, controller) }
//    }
//
//    companion object {
//        fun create(function: Builder.() -> Unit): Command {
//            val builder = Builder()
//            function(builder)
//            return builder.build()
//        }
//    }
//}
//
//fun compositeActionOf(vararg commands: Command): Command {
//    return CompositeCommand.create {
//        commands.forEach {
//            add(it)
//        }
//    }
//}
//
//
//class SetCurrentPlayer(val currentPlayer: Player): Command {
//    lateinit var originalPlayer: Player
//    override fun execute(state: GameState, controller: GameController) {
//        originalPlayer = state.currentPlayer
//        state.currentPlayer = currentPlayer
//    }
//    override fun undo(state: GameState, controller: GameController) {
//        state.currentPlayer = originalPlayer
//    }
//}
//
//class GotoNode(private val nextNode: Node): Command {
//    private lateinit var logEntry1: LogEntry
//    private var logEntry2: LogEntry? = null
//    override fun execute(state: GameState, controller: GameController) {
//        logEntry1 = SimpleLogEntry(LogCategory.STATE_MACHINE, "Transition to: ${controller.currentProcedure()!!.name()}[${nextNode.name()}]")
//        controller.addLog(logEntry1)
//        controller.addNode(nextNode)
//        if (nextNode is Procedure) {
//            logEntry2 = SimpleLogEntry(LogCategory.STATE_MACHINE, "Load procedure: ${nextNode.name()}[${nextNode.initialState.name()}]")
//            controller.addLog(logEntry2!!)
//            controller.addProcedure(nextNode)
//        }
//    }
//
//    override fun undo(state: GameState, controller: GameController) {
//        if (nextNode is Procedure) {
//            controller.removeProcedure()
//            controller.removeLog(logEntry2!!)
//        }
//        controller.removeNode()
//        controller.removeLog(logEntry1)
//    }
//}
//
//class EnterProcedure(private val procedure: Procedure): Command {
//    private val enterProcedureEntry = SimpleLogEntry(message = "Load procedure: ${procedure.name()}[${procedure.initialState.name()}]")
//    override fun execute(state: GameState, controller: GameController) {
//        controller.addLog(enterProcedureEntry)
//        controller.addProcedure(procedure)
//    }
//    override fun undo(state: GameState, controller: GameController) {
//        controller.removeProcedure()
//        controller.removeLog(enterProcedureEntry)
//    }
//}
//
//class ExitProcedure: Command {
//    private lateinit var entry2: SimpleLogEntry
//    private lateinit var entry1: SimpleLogEntry
//    private lateinit var currentProcedure: ProcedureState
//
//    override fun execute(state: GameState, controller: GameController) {
//        val removed: ProcedureState = controller.removeProcedure()
//        currentProcedure = removed.copy()
//        val current: ProcedureState? = controller.currentProcedure()
//        entry1 = SimpleLogEntry(LogCategory.STATE_MACHINE, "${removed.name()}[${removed.currentNode().name()}] removed.")
//        if (current != null) {
//            entry2 = SimpleLogEntry(LogCategory.STATE_MACHINE, "Current state: ${current.name()}[${current.currentNode().name()}]")
//        } else {
//            entry2 = SimpleLogEntry(LogCategory.STATE_MACHINE, "Current state: <Empty>")
//        }
//        controller.addLog(entry1)
//        controller.addLog(entry2)
//    }
//
//    override fun undo(state: GameState, controller: GameController) {
//        controller.removeLog(entry2)
//        controller.removeLog(entry1)
//        controller.addProcedure(currentProcedure)
//    }
//}
//
//data object NoOpCommand: Command {
//    override fun execute(state: GameState, controller: GameController) {
//        /* Do nothing */
//    }
//    override fun undo(state: GameState, controller: GameController) {
//        /* Do nothing */
//    }
//}
//
//class AddTokenToBoard(val x: UInt, val y: UInt, val mark: Mark): Command {
//    lateinit var initialToken: Mark
//    override fun execute(state: GameState, controller: GameController) {
//        initialToken = state.board.get(x, y)
//        state.board.add(x, y, mark)
//    }
//    override fun undo(state: GameState, controller: GameController) {
//        state.board.add(x, y, initialToken)
//    }
//}
//
//sealed interface ActionDescriptor
//data object ContinueWhenReady: ActionDescriptor
//data object RollD2: ActionDescriptor
//data class SelectAvailableSpace(val x: UInt, val y: UInt): ActionDescriptor
//
//
//abstract class DieResult(val result: Int, val min: Short, val max: Short): Number(), Action {
//    init {
//        if (result < min || result > max) {
//            throw IllegalArgumentException("Result outside range: $min <= $result <= $max")
//        }
//    }
//    override fun toByte(): Byte = result.toByte()
//    override fun toDouble(): Double = result.toDouble()
//    override fun toFloat(): Float = result.toFloat()
//    override fun toInt(): Int = result.toInt()
//    override fun toLong(): Long = result.toLong()
//    override fun toShort(): Short = result.toShort()
//    override fun toString(): String {
//        return "${this::class.simpleName}[$result]"
//    }
//}
//
//sealed interface Action
//data object Continue: Action
//class D2Result(result: Int = Random.nextInt(1, 3)): DieResult(result, 1, 2)
//class SpaceSelected(val x: UInt, val y: UInt): Action {
//    override fun toString(): String {
//        return "${this::class.simpleName}[$x, $y]"
//    }
//}
//
//object Game: Procedure {
//
//    override val initialState: ActionNode = SelectStartingPlayer
//
//    object SelectStartingPlayer: ActionNode() {
//        override fun getAvailableActions(state: GameState): List<ActionDescriptor> = listOf(RollD2)
//        override fun applyAction(action: Action, state: GameState): Command {
//            val dieRoll: D2Result = checkType<D2Result>(action)
//            val setCurrentPlayer = when (dieRoll.result) {
//                1 -> SetCurrentPlayer(state.p1)
//                2 -> SetCurrentPlayer(state.p2)
//                else -> TODO()
//            }
//            return compositeActionOf(
//                setCurrentPlayer,
//                GotoNode(PlayGame)
//            )
//        }
//    }
//
//    object PlayGame: ParentNode() {
//        override fun onEnter(state: GameState): Command = NoOpCommand
//        override fun getChildProcedure(): Procedure = PlayerTurn
//        override fun onExit(state: GameState): Command {
//            // Check for winner?
//            if (state.board.isDraw() || state.board.isWinner()) {
//                return compositeActionOf(
//                    GotoNode(RunPostGame)
//                )
//            } else {
//                // If game is not ended yet, continue game
//                val nextCurrentPlayer = if (state.currentPlayer == state.p1) {
//                    state.p2
//                } else {
//                    state.p1
//                }
//                return compositeActionOf(
//                    SetCurrentPlayer(nextCurrentPlayer),
//                    GotoNode(PlayGame)
//                )
//            }
//        }
//    }
//
//    object RunPostGame: ComputationNode() {
//        override fun apply(state: GameState): Command {
//            return compositeActionOf(
//                ExitProcedure()
//            )
//        }
//    }
//}
//
//class ReportLog(private val logEntry: LogEntry) : Command {
//    override fun execute(state: GameState, controller: GameController) {
//        controller.addLog(logEntry)
//    }
//    override fun undo(state: GameState, controller: GameController) {
//        controller.removeLog(logEntry)
//    }
//}
//
//object PlayerTurn: Procedure {
//    override val initialState: ActionNode = PlaceToken
//
//    object PlaceToken: ActionNode() {
//        override fun getAvailableActions(state: GameState): List<ActionDescriptor> {
//            val availableSpaces = mutableListOf<SelectAvailableSpace>()
//            state.board.iterateBoard { x, y, _ ->
//                if (state.board.isBlank(x, y)) {
//                    availableSpaces.add(SelectAvailableSpace(x, y))
//                }
//            }
//            if (availableSpaces.isEmpty()) {
//                TODO()
//            }
//            return availableSpaces
//        }
//
//        override fun applyAction(action: Action, state: GameState): Command {
//            return checkType<SpaceSelected>(action) {
//                compositeActionOf(
//                    AddTokenToBoard(it.x, it.y, state.currentPlayer.type),
//                    ReportLog(SimpleLogEntry(LogCategory.BOARD, "Added ${state.currentPlayer.type} to [${it.x}, ${it.y}]")),
//                    ExitProcedure()
//                )
//            }
//        }
//    }
//}
//
//enum class LogCategory {
//   ACTIONS,
//   STATE_MACHINE,
//   BOARD
//}
//
//interface LogEntry {
//    val category: LogCategory
//    fun render(state: GameState): String
//}
//
//class SimpleLogEntry(
//    override val category: LogCategory = LogCategory.STATE_MACHINE,
//    private val message: String
//): LogEntry {
//    override fun render(state: GameState): String = message
//}
//
//class ProcedureState(val procedure: Procedure) {
//    private val nodes: MutableList<Node> = mutableListOf()
//    constructor(procedure: Procedure, initialNode: Node): this(procedure) {
//        nodes.add(initialNode)
//    }
//    private constructor(procedure: Procedure, history: List<Node>): this(procedure) {
//        nodes.addAll(history)
//    }
//    fun currentNode(): Node = nodes.last()
//    fun addNode(node: Node) {
//        nodes.add(node)
//    }
//    fun copy(): ProcedureState {
//        return ProcedureState(procedure, nodes.map { it })
//    }
//    fun name(): String = procedure.name()
//
//    override fun toString(): String {
//        return "${name()}[${nodes.lastOrNull()?.name()}]"
//    }
//    fun removeLast() {
//        nodes.removeLast()
//    }
//}
//
//class ProcedureStack {
//    private val history: ArrayDeque<ProcedureState> = ArrayDeque()
//    fun isEmpty(): Boolean = history.isEmpty()
//    fun currentNode(): Node = history.first().currentNode()
//    fun pushProcedure(procedure: ProcedureState) {
//        history.addFirst(procedure)
//    }
//    fun pushProcedure(procedure: Procedure) {
//        history.addFirst(ProcedureState(procedure, procedure.initialState))
//    }
//    fun popProcedure(): ProcedureState = history.removeFirst()
//    fun firstOrNull(): ProcedureState? = history.firstOrNull()
//    fun addNode(nextNode: Node) = history.first().addNode(nextNode)
//    fun removeNode() = history.first().removeLast()
//}
//
//
//
//class GameController(
//    p1: Player,
//    p2: Player,
//    val actionProvider: (state: GameState, availableActions: List<ActionDescriptor>) -> Action,
//) {
//    val fsmHistory: ProcedureStack = ProcedureStack()
//    val logs: MutableList<LogEntry> = mutableListOf()
//    val commands: MutableList<Command> = mutableListOf()
//    val state: GameState = GameState(p1, p2, Board())
//    private var replayMode: Boolean = false
//    private var replayIndex: Int = -1
//    private val isStopped = false
//
//    private fun processNode(currentNode: Node) {
//        when(currentNode) {
//            is ComputationNode -> {
//                // Reduce noise from Continue events
//                val command = currentNode.applyAction(Continue, state)
//                commands.add(command)
//                command.execute(state, this)
//            }
//            is ActionNode -> {
//                val actions = currentNode.getAvailableActions(state)
//                val reportAvailableActions = ReportLog(SimpleLogEntry(LogCategory.STATE_MACHINE, "Available actions: ${actions.joinToString()}"))
//                commands.add(reportAvailableActions)
//                reportAvailableActions.execute(state, this)
//                val selectedAction = actionProvider(state, actions)
//                val reportSelectedAction = ReportLog(SimpleLogEntry(LogCategory.STATE_MACHINE, "Selected action: $selectedAction"))
//                commands.add(reportSelectedAction)
//                reportSelectedAction.execute(state, this)
//                val command = currentNode.applyAction(selectedAction, state)
//                commands.add(command)
//                command.execute(state, this)
//            }
//            else -> {
//                throw IllegalStateException("Unsupported type: ${currentNode::class.simpleName}")
//            }
//        }
//    }
//
//    fun start() {
//        if (replayMode) {
//            throw IllegalStateException("Replay mode is enabled")
//        }
//        setInitialProcedure(Game)
//        while (!fsmHistory.isEmpty() && !isStopped) {
//            val currentState: Node = fsmHistory.currentNode()
//            processNode(currentState)
//        }
//    }
//
//    private fun setInitialProcedure(procedure: Procedure) {
//        val command = compositeActionOf(
//            ReportLog(SimpleLogEntry(LogCategory.STATE_MACHINE, "Set initial procedure: ${procedure.name()}[${procedure.initialState.name()}]")),
//            EnterProcedure(procedure)
//        )
//        commands.add(command)
//        command.execute(state, this)
//    }
//
//    fun addLog(entry: LogEntry) {
//        println(entry.render(state))
//        logs.add(entry)
//    }
//
//    fun removeLog(entry: LogEntry) {
//        if (logs.lastOrNull() == entry) {
//            logs.removeLast()
//        } else {
//            throw IllegalStateException("Log could not be removed: ${entry.render(state)}")
//        }
//    }
//
//    fun addProcedure(procedure: Procedure) {
//        fsmHistory.pushProcedure(procedure)
//    }
//
//    fun addProcedure(procedure: ProcedureState) {
//        fsmHistory.pushProcedure(procedure)
//    }
//
//    fun removeProcedure(): ProcedureState {
//        return fsmHistory.popProcedure()
//    }
//
//    fun currentProcedure(): ProcedureState? = fsmHistory.firstOrNull()
//
//    fun addNode(nextState: Node) {
//        fsmHistory.addNode(nextState)
//    }
//
//    fun removeNode() {
//        fsmHistory.removeNode()
//    }
//
//    fun enableReplayMode() {
//        this.replayMode = true
//        this.replayIndex = commands.size
//    }
//
//    fun disableReplayMode() {
//        checkReplayMode()
//        while(forward()) { }
//        this.replayMode = false
//        this.replayIndex = -1
//    }
//
//    fun back(): Boolean {
//        checkReplayMode()
//        if (replayIndex == 0) {
//            return false
//        }
//        replayIndex -= 1
//        val undoCommand = commands[replayIndex]
//        undoCommand.undo(state, this)
//        return true
//    }
//
//    private inline fun checkReplayMode() {
//        if (!replayMode) {
//            throw IllegalStateException("Controller is not in replay mode.")
//        }
//    }
//
//    fun forward(): Boolean {
//        checkReplayMode()
//        if (replayIndex == commands.size) {
//            return false
//        }
//        commands[replayIndex].execute(state, this)
//        replayIndex += 1
//        return true
//    }
//}
//
//fun main() {
//    val p1 = Player(name = "Alice", Mark.O)
//    val p2 = Player(name = "Bob", Mark.X)
//    val controller = GameController(p1, p2) { state: GameState, actions: List<ActionDescriptor> ->
//        println(state.board.printBoard())
//        if (actions.isEmpty()) {
//            TODO()
//        }
//        when(val action = actions.random()) {
//            RollD2 -> D2Result(1)
//            is SelectAvailableSpace -> SpaceSelected(action.x, action.y)
//            ContinueWhenReady -> Continue
//        }
//    }
//    controller.start()
//    controller.enableReplayMode()
//    while(controller.back()) {
//        println("---------")
//        println(controller.state.board.printBoard())
//    }
//    while(controller.forward()) {
//        println("---------")
//        println(controller.state.board.printBoard())
//    }
//}