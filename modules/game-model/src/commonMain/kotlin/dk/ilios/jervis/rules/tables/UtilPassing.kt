//package dk.ilios.jervis.rules.tables
//
//import com.fumbbl.ffb.FieldCoordinate
//import com.fumbbl.ffb.FieldCoordinateBounds
//import com.fumbbl.ffb.PlayerAction
//import com.fumbbl.ffb.PlayerState
//import com.fumbbl.ffb.model.ActingPlayer
//import com.fumbbl.ffb.model.FieldModel
//import com.fumbbl.ffb.model.Game
//import com.fumbbl.ffb.model.Player
//import com.fumbbl.ffb.model.Team
//import com.fumbbl.ffb.model.property.NamedProperties
//import kotlin.math.abs
//import kotlin.math.min
//import kotlin.math.sqrt
//
//object UtilPassing {
//    var RULER_WIDTH: Double = 1.74 // This looks wrong ... it should be 58/34 or 58/33.3875
//
//    fun findInterceptors(pGame: Game, pThrower: Player<*>?, pTargetCoordinate: FieldCoordinate?): Array<Player<*>> {
//        val interceptors: MutableList<Player<*>> = java.util.ArrayList<Player<*>>()
//        if ((pTargetCoordinate != null) && (pThrower != null)) {
//            val throwerCoordinate: FieldCoordinate = pGame.getFieldModel().getPlayerCoordinate(pThrower)
//            val otherTeam: Team =
//                if (pGame.getTeamHome().hasPlayer(pThrower)) pGame.getTeamAway() else pGame.getTeamHome()
//            val otherPlayers: Array<Player<*>> = otherTeam.getPlayers()
//            for (otherPlayer in otherPlayers) {
//                val interceptorState: PlayerState = pGame.getFieldModel().getPlayerState(otherPlayer)
//                val interceptorCoordinate: FieldCoordinate = pGame.getFieldModel().getPlayerCoordinate(otherPlayer)
//                if ((interceptorCoordinate != null) && (interceptorState != null) && interceptorState.hasTacklezones()
//                    && !otherPlayer.hasSkillProperty(NamedProperties.preventCatch)
//                ) {
//                    if (canIntercept(throwerCoordinate, pTargetCoordinate, interceptorCoordinate)) {
//                        interceptors.add(otherPlayer)
//                    }
//                }
//            }
//        }
//        return interceptors.toTypedArray<Player>()
//    }
//
//    private fun canIntercept(
//        pThrowerCoordinate: FieldCoordinate, pTargetCoordinate: FieldCoordinate,
//        pIinterceptorCoordinate: FieldCoordinate
//    ): Boolean {
//        val receiverX: Int = pTargetCoordinate.getX() - pThrowerCoordinate.getX()
//        val receiverY: Int = pTargetCoordinate.getY() - pThrowerCoordinate.getY()
//        val interceptorX: Int = pIinterceptorCoordinate.getX() - pThrowerCoordinate.getX()
//        val interceptorY: Int = pIinterceptorCoordinate.getY() - pThrowerCoordinate.getY()
//        val a = (((receiverX - interceptorX) * (receiverX - interceptorX))
//                + ((receiverY - interceptorY) * (receiverY - interceptorY)))
//        val b = (interceptorX * interceptorX) + (interceptorY * interceptorY)
//        val c = (receiverX * receiverX) + (receiverY * receiverY)
//        val d1 = abs((receiverY * (interceptorX + 0.5)) - (receiverX * (interceptorY + 0.5)))
//        val d2 = abs((receiverY * (interceptorX + 0.5)) - (receiverX * (interceptorY - 0.5)))
//        val d3 = abs((receiverY * (interceptorX - 0.5)) - (receiverX * (interceptorY + 0.5)))
//        val d4 = abs((receiverY * (interceptorX - 0.5)) - (receiverX * (interceptorY - 0.5)))
//        return (c > a) && (c > b) && (RULER_WIDTH > (2 * min(min(min(d1, d2), d3), d4) / sqrt(c.toDouble())))
//    }
//
//    fun findValidPassBlockEndCoordinates(pGame: Game?): Set<FieldCoordinate> {
//        val validCoordinates: MutableSet<FieldCoordinate> = java.util.HashSet<FieldCoordinate>()
//
//        // Sanity checks
//        if ((pGame == null) || (pGame.getThrower() == null) || (pGame.getPassCoordinate() == null)) {
//            return validCoordinates
//        }
//
//        val actingPlayer: ActingPlayer = pGame.getActingPlayer()
//
//        // Add the thrower tacklezone
//        var neighbours: Array<FieldCoordinate?> = pGame.getFieldModel().findAdjacentCoordinates(
//            pGame.getFieldModel().getPlayerCoordinate(pGame.getThrower()), FieldCoordinateBounds.FIELD, 1, false
//        )
//        for (c in neighbours) {
//            val playerInTz: Player<*> = pGame.getFieldModel().getPlayer(c)
//            if ((playerInTz == null) || (playerInTz === actingPlayer.getPlayer())) {
//                validCoordinates.add(c)
//            }
//        }
//
//        val targetPlayer: Player<*> = pGame.getFieldModel().getPlayer(pGame.getPassCoordinate())
//
//        if (PlayerAction.HAIL_MARY_PASS === pGame.getThrowerAction()) {
//            if (targetPlayer != null) {
//                validCoordinates.add(pGame.getPassCoordinate())
//            }
//        } else {
//            validCoordinates.addAll(findInterceptCoordinates(pGame))
//
//            // If there's a target, add the target's tacklezones
//            if (targetPlayer != null) {
//                neighbours = pGame.getFieldModel().findAdjacentCoordinates(
//                    pGame.getPassCoordinate(),
//                    FieldCoordinateBounds.FIELD, 1, false
//                )
//                for (c in neighbours) {
//                    val playerInTz: Player<*> = pGame.getFieldModel().getPlayer(c)
//                    if ((playerInTz == null) || (playerInTz === actingPlayer.getPlayer())) {
//                        validCoordinates.add(c)
//                    }
//                }
//            } else {
//                validCoordinates.add(pGame.getPassCoordinate())
//            }
//        }
//
//        return validCoordinates
//    }
//
//    private fun findInterceptCoordinates(pGame: Game): Set<FieldCoordinate> {
//        val fieldModel: FieldModel = pGame.getFieldModel()
//        val eligibleCoordinates: MutableSet<FieldCoordinate> = java.util.HashSet<FieldCoordinate>()
//        val closedSet: MutableSet<FieldCoordinate> = java.util.HashSet<FieldCoordinate>()
//        val openSet: MutableList<FieldCoordinate> = java.util.ArrayList<FieldCoordinate>()
//        val throwerCoord: FieldCoordinate = fieldModel.getPlayerCoordinate(pGame.getThrower())
//
//        // Start with the thrower's location.
//        openSet.add(throwerCoord)
//
//        while (!openSet.isEmpty()) {
//            // Get an unprocessed coordinate
//            val currentCoordinate: FieldCoordinate = openSet.removeAt(0)
//
//            // Since coordinates may be added multiple times to the open set, let's check if
//            // we already processed this coordinate
//            if (closedSet.contains(currentCoordinate)) {
//                continue
//            }
//
//            if (currentCoordinate.equals(throwerCoord)
//                || canIntercept(throwerCoord, pGame.getPassCoordinate(), currentCoordinate)
//            ) {
//                // This coordinate is eligible to intercept, so we add it to the list...
//                eligibleCoordinates.add(currentCoordinate)
//
//                // ... and queue all adjacent non-processed squares for processing
//                val adjacentCoordinates: Array<FieldCoordinate> = fieldModel.findAdjacentCoordinates(
//                    currentCoordinate,
//                    FieldCoordinateBounds.FIELD, 1, false
//                )
//                for (c in adjacentCoordinates) if (!closedSet.contains(c)) {
//                    openSet.add(c)
//                }
//            }
//
//            // Mark the coordinate as processed
//            closedSet.add(currentCoordinate)
//        }
//
//        // Remove coordinates occupied by players in the list.
//        val actingPlayer: ActingPlayer = pGame.getActingPlayer()
//        val actingPlayerPosition: FieldCoordinate = fieldModel.getPlayerCoordinate(actingPlayer.getPlayer())
//        val playerCoordinates: Array<FieldCoordinate> = fieldModel.getPlayerCoordinates()
//        for (pCoord in playerCoordinates) {
//            if (!pCoord.equals(actingPlayerPosition)) {
//                eligibleCoordinates.remove(pCoord)
//            }
//        }
//
//        return eligibleCoordinates
//    }
//}
