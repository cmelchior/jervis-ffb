package dk.ilios.jervis.procedures.actions.move

import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.MoveType
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.actions.SelectMoveType
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.procedures.Pickup
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Returns all the reachable squares. player can go to using a specific type of move.
 *
 * Will throw if the player is not able to do the request move type, ie. is prone,
 * does not have the skill etc.
 */
fun calculateOptionsForMoveType(state: Game, rules: Rules, player: Player, type: MoveType): List<ActionDescriptor> {
    return when (type) {
        MoveType.JUMP -> TODO()
        MoveType.LEAP -> TODO()
        MoveType.STANDARD -> {
            val eligibleEmptySquares: List<ActionDescriptor> =
                if (player.movesLeft + player.rushesLeft > 0) {
                    player.location.coordinate.getSurroundingCoordinates(rules)
                        .filter { state.field[it].isUnoccupied() }
                        .map { SelectFieldLocation.move(it, player.movesLeft <= 0) }
                } else {
                    emptyList()
                }
            eligibleEmptySquares
        }
        MoveType.STAND_UP -> TODO()
    }
}

/**
 * Returns a list of all possible move actions for a given player.
 * This should take into account normal moves, rushing, jump and all
 * skills like Leap and Ball & Chain
 *
 * TODO Maybe not ball an chain? :thinking:
 */
fun calculateMoveTypesAvailable(player: Player, rules: Rules): List<ActionDescriptor> {

    val options = mutableListOf<MoveType>()

    // Normal move (with a potential rush)
    if (player.movesLeft + player.rushesLeft >= 1 && player.isStanding(rules)) {
        options.add(MoveType.STANDARD)
    }

    // Jump/Leap (with potential rushes)
    if (player.movesLeft + player.rushesLeft >= 2 && player.isStanding(rules)) {
        options.add(MoveType.JUMP)
        options.add(MoveType.LEAP)
    }

    // Standup
    if (player.location.isOnField(rules) && player.state == PlayerState.PRONE) {
        options.add(MoveType.STAND_UP)
    }

    return options.map { SelectMoveType(it) }
}

/**
 * Procedure for handling a player moving "one step". "One step" is categorized as
 * any of the following moves:
 *
 *  - Normal move
 *  - Standing Up,
 *  - Rush
 *  - Jump
 *  - Leap
 *
 *  Turnovers are handled in procedures calling this one.
 *
 *  --------------
 *  Design Notes:
 *
 *  To make it easier to handle each type, it is required to select
 *  the type before choosing the target.
 *
 *  This means a normal move is represented as [NormalMove, (x,y), NormalMove, (x1, y1), ...].
 *
 * The other option would have been to calculate all possible targets and
 * then enhance the field location with type data. This was considered, but
 * rejected, because it would lead to a lot of calculations on each move.
 *
 * Mixing the two would fundamentally mean that UI logic bleeds into this
 * layer, which is also not desirable.
 *
 * The FUMBBL Client mixes the two options as e.g. rushes are shown on the field,
 * but you have to choose Jump as an action. The Jervis UI can choose which
 * route to go by using automated actions instead. Which allows us to change the
 * UI without touching this layer.
 *
 * Jumping are handled in [JumpStep]
 * Standing up are handled in [StandingUp]
 */
object MoveTypeSelectorStep : Procedure() {
    override val initialNode: Node = ResolveMove

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command? {
        if (state.moveContext == null) {
            INVALID_GAME_STATE("Move context is null")
        }
        return null
    }

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    object ResolveMove : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return when(val moveType = state.moveContext!!.moveType) {
                MoveType.STANDARD -> StandardMoveStep
                MoveType.JUMP,
                MoveType.LEAP,
                MoveType.STAND_UP -> TODO("Not supported yet: $moveType")
            }
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            val player = state.moveContext!!.player
            val pickupBall = player.isStanding(rules) && state.field[player.location as FieldCoordinate].ball != null
            return if (pickupBall) {
                GotoNode(PickUpBall)
            } else {
                ExitProcedure()
            }
        }
    }

    // If a player moved into the ball as part of the action, they must pick it up.
    object PickUpBall : ParentNode() {
        override fun getChildProcedure(
            state: Game,
            rules: Rules,
        ): Procedure = Pickup

        override fun onExitNode(
            state: Game,
            rules: Rules,
        ): Command {
            return ExitProcedure()
        }
    }
}
