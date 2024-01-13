package dk.ilios.jervis.fumbbl

import dk.ilios.jervis.fumbbl.model.Game
import dk.ilios.jervis.fumbbl.model.InducementSet
import dk.ilios.jervis.fumbbl.model.PlayerResult
import dk.ilios.jervis.fumbbl.model.TeamResult
import dk.ilios.jervis.fumbbl.model.TurnData
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerMarkSkillUnused
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerMarkSkillUsed
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetCurrentMove
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetDodging
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetGoingForIt
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetHasBlocked
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetHasFed
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetHasFouled
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetHasJumped
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetHasMoved
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetHasPassed
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetLeaping
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetOldPlayerState
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetPlayerAction
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetPlayerId
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetStandingUp
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetStrength
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetSufferingAnimosity
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetSufferingBloodLust
import dk.ilios.jervis.fumbbl.model.change.FieldModelAddBloodSpot
import dk.ilios.jervis.fumbbl.model.change.FieldModelAddCard
import dk.ilios.jervis.fumbbl.model.change.FieldModelAddCardEffect
import dk.ilios.jervis.fumbbl.model.change.FieldModelAddDiceDecoration
import dk.ilios.jervis.fumbbl.model.change.FieldModelAddMoveSquare
import dk.ilios.jervis.fumbbl.model.change.FieldModelAddPlayerMarker
import dk.ilios.jervis.fumbbl.model.change.FieldModelAddPrayer
import dk.ilios.jervis.fumbbl.model.change.FieldModelAddPushbackSquare
import dk.ilios.jervis.fumbbl.model.change.FieldModelAddTrackNumber
import dk.ilios.jervis.fumbbl.model.change.FieldModelAddTrapDoor
import dk.ilios.jervis.fumbbl.model.change.FieldModelAddWisdom
import dk.ilios.jervis.fumbbl.model.change.FieldModelRemoveCard
import dk.ilios.jervis.fumbbl.model.change.FieldModelRemoveCardEffect
import dk.ilios.jervis.fumbbl.model.change.FieldModelRemoveDiceDecoration
import dk.ilios.jervis.fumbbl.model.change.FieldModelRemoveFieldMarker
import dk.ilios.jervis.fumbbl.model.change.FieldModelRemoveMoveSquare
import dk.ilios.jervis.fumbbl.model.change.FieldModelRemovePlayer
import dk.ilios.jervis.fumbbl.model.change.FieldModelRemovePlayerMarker
import dk.ilios.jervis.fumbbl.model.change.FieldModelRemovePrayer
import dk.ilios.jervis.fumbbl.model.change.FieldModelRemovePushbackSquare
import dk.ilios.jervis.fumbbl.model.change.FieldModelRemoveSkillEnhancements
import dk.ilios.jervis.fumbbl.model.change.FieldModelRemoveTrackNumber
import dk.ilios.jervis.fumbbl.model.change.FieldModelRemoveTrapDoor
import dk.ilios.jervis.fumbbl.model.change.FieldModelSetBallCoordinate
import dk.ilios.jervis.fumbbl.model.change.FieldModelSetBallInPlay
import dk.ilios.jervis.fumbbl.model.change.FieldModelSetBallMoving
import dk.ilios.jervis.fumbbl.model.change.FieldModelSetBlitzState
import dk.ilios.jervis.fumbbl.model.change.FieldModelSetBombCoordinate
import dk.ilios.jervis.fumbbl.model.change.FieldModelSetBombMoving
import dk.ilios.jervis.fumbbl.model.change.FieldModelSetPlayerCoordinate
import dk.ilios.jervis.fumbbl.model.change.FieldModelSetPlayerState
import dk.ilios.jervis.fumbbl.model.change.FieldModelSetRangeRuler
import dk.ilios.jervis.fumbbl.model.change.FieldModelSetTargetSelectionState
import dk.ilios.jervis.fumbbl.model.change.FieldModelSetWeather
import dk.ilios.jervis.fumbbl.model.change.GameSetAdminMode
import dk.ilios.jervis.fumbbl.model.change.GameSetConcededLegally
import dk.ilios.jervis.fumbbl.model.change.GameSetConcessionPossible
import dk.ilios.jervis.fumbbl.model.change.GameSetDefenderAction
import dk.ilios.jervis.fumbbl.model.change.GameSetDefenderId
import dk.ilios.jervis.fumbbl.model.change.GameSetDialogParameter
import dk.ilios.jervis.fumbbl.model.change.GameSetFinished
import dk.ilios.jervis.fumbbl.model.change.GameSetHalf
import dk.ilios.jervis.fumbbl.model.change.GameSetHomeFirstOffense
import dk.ilios.jervis.fumbbl.model.change.GameSetHomePlaying
import dk.ilios.jervis.fumbbl.model.change.GameSetId
import dk.ilios.jervis.fumbbl.model.change.GameSetLastDefenderId
import dk.ilios.jervis.fumbbl.model.change.GameSetLastTurnMode
import dk.ilios.jervis.fumbbl.model.change.GameSetPassCoordinate
import dk.ilios.jervis.fumbbl.model.change.GameSetScheduled
import dk.ilios.jervis.fumbbl.model.change.GameSetSetupOffense
import dk.ilios.jervis.fumbbl.model.change.GameSetStarted
import dk.ilios.jervis.fumbbl.model.change.GameSetTesting
import dk.ilios.jervis.fumbbl.model.change.GameSetThrowerAction
import dk.ilios.jervis.fumbbl.model.change.GameSetThrowerId
import dk.ilios.jervis.fumbbl.model.change.GameSetTimeoutEnforced
import dk.ilios.jervis.fumbbl.model.change.GameSetTimeoutPossible
import dk.ilios.jervis.fumbbl.model.change.GameSetTurnMode
import dk.ilios.jervis.fumbbl.model.change.GameSetWaitingForOpponent
import dk.ilios.jervis.fumbbl.model.change.InducementSetAddPrayer
import dk.ilios.jervis.fumbbl.model.change.InducementSetRemovePrayer
import dk.ilios.jervis.fumbbl.model.change.ModelChange
import dk.ilios.jervis.fumbbl.model.change.PlayerMarkSkillUnused
import dk.ilios.jervis.fumbbl.model.change.PlayerMarkSkillUsed
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetBlocks
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetCasualties
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetCasualtiesWithAdditionalSpp
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetCompletions
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetCompletionsWithAdditionalSpp
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetCurrentSpps
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetDefecting
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetDeflections
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetFouls
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetHasUsedSecretWeapon
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetInterceptions
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetPassing
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetPlayerAwards
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetRushing
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetSendToBoxByPlayerId
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetSendToBoxHalf
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetSendToBoxReason
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetSendToBoxTurn
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetSeriousInjury
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetSeriousInjuryDecay
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetTouchdowns
import dk.ilios.jervis.fumbbl.model.change.PlayerResultSetTurnsPlayed
import dk.ilios.jervis.fumbbl.model.change.TeamResultDedicatedFansModifier
import dk.ilios.jervis.fumbbl.model.change.TeamResultSetBadlyHurtSuffered
import dk.ilios.jervis.fumbbl.model.change.TeamResultSetConceded
import dk.ilios.jervis.fumbbl.model.change.TeamResultSetFame
import dk.ilios.jervis.fumbbl.model.change.TeamResultSetFanFactor
import dk.ilios.jervis.fumbbl.model.change.TeamResultSetFanFactorModifier
import dk.ilios.jervis.fumbbl.model.change.TeamResultSetPenaltyScore
import dk.ilios.jervis.fumbbl.model.change.TeamResultSetPettyCashTransferred
import dk.ilios.jervis.fumbbl.model.change.TeamResultSetPettyCashUsed
import dk.ilios.jervis.fumbbl.model.change.TeamResultSetRaisedDead
import dk.ilios.jervis.fumbbl.model.change.TeamResultSetRipSuffered
import dk.ilios.jervis.fumbbl.model.change.TeamResultSetScore
import dk.ilios.jervis.fumbbl.model.change.TeamResultSetSeriousInjurySuffered
import dk.ilios.jervis.fumbbl.model.change.TeamResultSetSpectators
import dk.ilios.jervis.fumbbl.model.change.TeamResultSetSpirallingExpenses
import dk.ilios.jervis.fumbbl.model.change.TeamResultSetTeamValue
import dk.ilios.jervis.fumbbl.model.change.TeamResultSetWinnings
import dk.ilios.jervis.fumbbl.model.change.TurnDataSetApothecaries
import dk.ilios.jervis.fumbbl.model.change.TurnDataSetBlitzUsed
import dk.ilios.jervis.fumbbl.model.change.TurnDataSetBombUsed
import dk.ilios.jervis.fumbbl.model.change.TurnDataSetCoachBanned
import dk.ilios.jervis.fumbbl.model.change.TurnDataSetFirstTurnAfterKickoff
import dk.ilios.jervis.fumbbl.model.change.TurnDataSetFoulUsed
import dk.ilios.jervis.fumbbl.model.change.TurnDataSetHandOverUsed
import dk.ilios.jervis.fumbbl.model.change.TurnDataSetKtmUsed
import dk.ilios.jervis.fumbbl.model.change.TurnDataSetLeaderState
import dk.ilios.jervis.fumbbl.model.change.TurnDataSetPassUsed
import dk.ilios.jervis.fumbbl.model.change.TurnDataSetPlagueDoctors
import dk.ilios.jervis.fumbbl.model.change.TurnDataSetReRollUsed
import dk.ilios.jervis.fumbbl.model.change.TurnDataSetReRolls
import dk.ilios.jervis.fumbbl.model.change.TurnDataSetReRollsBrilliantCoachingOneDrive
import dk.ilios.jervis.fumbbl.model.change.TurnDataSetReRollsPumpUpTheCrowdOneDrive
import dk.ilios.jervis.fumbbl.model.change.TurnDataSetReRollsSingleUse
import dk.ilios.jervis.fumbbl.model.change.TurnDataSetTurnNr
import dk.ilios.jervis.fumbbl.model.change.TurnDataSetTurnStarted
import dk.ilios.jervis.fumbbl.model.change.TurnDataSetWanderingApothecaries
import dk.ilios.jervis.fumbbl.model.change.isHomeData

object ModelChangeProcessor {
    fun apply(game: Game, change: ModelChange): Boolean {
//        val dialogParameter: IDialogParameter
//        val skillFactory: SkillFactory = pGame.getFactory(FactoryType.Factory.SKILL) as SkillFactory
        when(change) {
            is ActingPlayerMarkSkillUnused -> game.actingPlayer.markSkillUnused(change.modelChangeValue)
            is ActingPlayerMarkSkillUsed -> game.actingPlayer.markSkillUsed(change.modelChangeValue)
            is ActingPlayerSetCurrentMove -> game.actingPlayer.currentMove = change.modelChangeValue
            is ActingPlayerSetDodging -> game.actingPlayer.dodging = change.modelChangeValue
            is ActingPlayerSetGoingForIt -> game.actingPlayer.goingForIt = change.modelChangeValue
            is ActingPlayerSetHasBlocked -> game.actingPlayer.hasBlocked = change.modelChangeValue
            is ActingPlayerSetHasFed -> game.actingPlayer.hasFed = change.modelChangeValue
            is ActingPlayerSetHasFouled -> game.actingPlayer.hasFouled = change.modelChangeValue
            is ActingPlayerSetHasJumped -> game.actingPlayer.hasJumped = change.modelChangeValue
            is ActingPlayerSetHasMoved -> game.actingPlayer.hasMoved = change.modelChangeValue
            is ActingPlayerSetHasPassed -> game.actingPlayer.hasPassed = change.modelChangeValue
            is ActingPlayerSetLeaping -> game.actingPlayer.jumping = change.modelChangeValue
            is ActingPlayerSetOldPlayerState -> game.actingPlayer.playerStateOld = change.modelChangeValue
            is ActingPlayerSetPlayerAction -> game.actingPlayer.playerAction = change.modelChangeValue
            is ActingPlayerSetPlayerId -> game.actingPlayer.playerId = change.modelChangeValue
            is ActingPlayerSetStandingUp -> game.actingPlayer.standingUp = change.modelChangeValue
            is ActingPlayerSetStrength -> game.actingPlayer.strenght = change.modelChangeValue
            is ActingPlayerSetSufferingAnimosity -> game.actingPlayer.sufferingAnimosity = change.modelChangeValue
            is ActingPlayerSetSufferingBloodLust -> game.actingPlayer.sufferingBloodlust = change.modelChangeValue
            is FieldModelAddBloodSpot -> game.fieldModel.addBloodSpot(change.modelChangeValue)
            is FieldModelAddCard -> game.fieldModel.addCard(change.modelChangeKey, change.modelChangeValue)
            is FieldModelAddCardEffect -> game.fieldModel.addCardEffect(change.modelChangeKey, change.modelChangeValue)
            is FieldModelAddDiceDecoration -> game.fieldModel.addDiceDecoration(change.modelChangeValue)
//            is FieldModelAddFieldMarker -> TODO()
//            is FieldModelAddIntensiveTraining -> TODO()
            is FieldModelAddMoveSquare -> game.fieldModel.addMoveSquare(change.modelChangeValue)
            is FieldModelAddPlayerMarker -> game.fieldModel.addPlayerMarker(change.modelChangeValue)
            is FieldModelAddPrayer -> game.fieldModel.addPrayerEnhancements(change.modelChangeKey, change.modelChangeValue)
            is FieldModelAddPushbackSquare -> game.fieldModel.addPushBackSquare(change.modelChangeValue)
//            is FieldModelAddSkillEnhancements -> TODO()
            is FieldModelAddTrackNumber -> game.fieldModel.addTrackNumber(change.modelChangeValue)
            is FieldModelAddTrapDoor -> game.fieldModel.addTrapDoor(change.modelChangeValue)
//            is FieldModelAddWisdom -> TODO()
//            is FieldModelKeepDeactivatedCard -> TODO()
            is FieldModelRemoveCard -> game.fieldModel.removeCard(change.modelChangeKey, change.modelChangeValue)
            is FieldModelRemoveCardEffect -> game.fieldModel.removeCardEffect(change.modelChangeKey, change.modelChangeValue)
            is FieldModelRemoveDiceDecoration -> game.fieldModel.removeDiceDecoration(change.modelChangeValue)
            is FieldModelRemoveFieldMarker -> game.fieldModel.removeFieldMarker(change.modelChangeValue)
            is FieldModelRemoveMoveSquare -> game.fieldModel.removeMoveSquare(change.modelChangeValue)
            is FieldModelRemovePlayer -> game.fieldModel.removePlayer(change.modelChangeKey)
            is FieldModelRemovePlayerMarker -> game.fieldModel.removePlayerMarker(change.modelChangeValue)
            is FieldModelRemovePrayer -> game.fieldModel.removePrayerEnhancement(change.modelChangeKey, change.modelChangeValue)
            is FieldModelRemovePushbackSquare -> game.fieldModel.removePushbackSquare(change.modelChangeValue)
            is FieldModelRemoveSkillEnhancements -> game.fieldModel.removeSkillEnhancements(game.getPlayerById(change.modelChangeKey)!!, change.modelChangeValue)
            is FieldModelRemoveTrackNumber -> game.fieldModel.removeTrackNumber(change.modelChangeValue)
            is FieldModelRemoveTrapDoor -> game.fieldModel.removeTrapDoor(change.modelChangeValue)
            is FieldModelSetBallCoordinate -> game.fieldModel.ballCoordinate = change.modelChangeValue
            is FieldModelSetBallInPlay -> game.fieldModel.ballInPlay = change.modelChangeValue
            is FieldModelSetBallMoving -> game.fieldModel.ballMoving = change.modelChangeValue
            is FieldModelSetBlitzState -> game.fieldModel.targetSelectionState = change.modelChangeValue
            is FieldModelSetBombCoordinate -> game.fieldModel.bombCoordinate = change.modelChangeValue
            is FieldModelSetBombMoving -> game.fieldModel.bombMoving = change.modelChangeValue
            is FieldModelSetPlayerCoordinate -> game.fieldModel.setPlayerCoordinate(change.modelChangeKey, change.modelChangeValue!!)
            is FieldModelSetPlayerState -> game.fieldModel.setPlayerState(change.modelChangeKey, change.modelChangeValue)
            is FieldModelSetRangeRuler -> game.fieldModel.rangeRuler = change.modelChangeValue
            is FieldModelSetTargetSelectionState -> game.fieldModel.targetSelectionState = change.modelChangeValue
            is FieldModelSetWeather -> game.fieldModel.weather = change.modelChangeValue
//            is GameOptionsAddOption -> TODO()
            is GameSetAdminMode -> game.adminMode = change.modelChangeValue
            is GameSetConcededLegally -> game.concededLegally = change.modelChangeValue
            is GameSetConcessionPossible -> game.concessionPossible = change.modelChangeValue
            is GameSetDefenderAction -> game.defenderAction = change.modelChangeValue
            is GameSetDefenderId -> game.defenderId = change.modelChangeValue
            is GameSetDialogParameter -> game.dialogParameter = change.modelChangeValue
            is GameSetFinished -> game.finished = change.modelChangeValue
            is GameSetHalf -> game.half = change.modelChangeValue
            is GameSetHomeFirstOffense -> game.homeFirstOffense = change.modelChangeValue
            is GameSetHomePlaying -> game.homePlaying = change.modelChangeValue
            is GameSetId -> game.gameId = change.modelChangeValue
            is GameSetLastDefenderId -> game.lastDefenderId = change.modelChangeValue
            is GameSetLastTurnMode -> game.lastTurnMode = change.modelChangeValue
            is GameSetPassCoordinate -> game.passCoordinate = change.modelChangeValue
            is GameSetScheduled -> game.scheduled = change.modelChangeValue
            is GameSetSetupOffense -> game.setupOffense = change.modelChangeValue
            is GameSetStarted -> game.started = change.modelChangeValue
            is GameSetTesting -> game.testing = change.modelChangeValue
            is GameSetThrowerAction -> game.throwerAction = change.modelChangeValue
            is GameSetThrowerId -> game.throwerId = change.modelChangeValue
            is GameSetTimeoutEnforced -> game.timeoutEnforced = change.modelChangeValue
            is GameSetTimeoutPossible -> game.timeoutPossible = change.modelChangeValue
            is GameSetTurnMode -> game.turnMode = change.modelChangeValue
            is GameSetWaitingForOpponent -> game.waitingForOpponent = change.modelChangeValue
//            is InducementSetActivateCard -> TODO()
//            is InducementSetAddAvailableCard -> TODO()
//            is InducementSetAddInducement -> TODO()
            is InducementSetAddPrayer -> getInducementSet(game, change.isHomeData()).prayers.add(change.modelChangeValue)
//            is InducementSetCardChoices -> TODO()
//            is InducementSetDeactivateCard -> TODO()
//            is InducementSetRemoveAvailableCard -> TODO()
//            is InducementSetRemoveInducement -> TODO()
            is InducementSetRemovePrayer -> getInducementSet(game, change.isHomeData()).prayers.remove(change.modelChangeValue)
            is PlayerMarkSkillUnused -> game.getPlayerById(change.modelChangeKey)!!.markUnused(change.modelChangeValue, game)
            is PlayerMarkSkillUsed -> game.getPlayerById(change.modelChangeKey)!!.markUsed(change.modelChangeValue, game)
            is PlayerResultSetBlocks -> getPlayerResult(game, change.modelChangeKey).blocks = change.modelChangeValue
            is PlayerResultSetCasualties -> getPlayerResult(game, change.modelChangeKey).casualties = change.modelChangeValue
            is PlayerResultSetCasualtiesWithAdditionalSpp -> getPlayerResult(game, change.modelChangeKey).casualtiesWithAdditionalSpp = change.modelChangeValue
            is PlayerResultSetCompletions -> getPlayerResult(game, change.modelChangeKey).completions = change.modelChangeValue
            is PlayerResultSetCompletionsWithAdditionalSpp -> getPlayerResult(game, change.modelChangeKey).completionsWithAdditionalSpp = change.modelChangeValue
            is PlayerResultSetCurrentSpps -> getPlayerResult(game, change.modelChangeKey).currentSpps = change.modelChangeValue
            is PlayerResultSetDefecting -> getPlayerResult(game, change.modelChangeKey).defecting = change.modelChangeValue
            is PlayerResultSetDeflections -> getPlayerResult(game, change.modelChangeKey).deflections = change.modelChangeValue
            is PlayerResultSetFouls -> getPlayerResult(game, change.modelChangeKey).fouls = change.modelChangeValue
            is PlayerResultSetHasUsedSecretWeapon -> getPlayerResult(game, change.modelChangeKey).hasUsedSecretWeapon = change.modelChangeValue
            is PlayerResultSetInterceptions -> getPlayerResult(game, change.modelChangeKey).interceptions = change.modelChangeValue
            is PlayerResultSetPassing -> getPlayerResult(game, change.modelChangeKey).passing = change.modelChangeValue
            is PlayerResultSetPlayerAwards -> getPlayerResult(game, change.modelChangeKey).playerAwards = change.modelChangeValue
            is PlayerResultSetRushing -> getPlayerResult(game, change.modelChangeKey).rushing = change.modelChangeValue
            is PlayerResultSetSendToBoxByPlayerId -> getPlayerResult(game, change.modelChangeKey).sendToBoxByPlayerId = change.modelChangeValue
            is PlayerResultSetSendToBoxHalf -> getPlayerResult(game, change.modelChangeKey).sendToBoxHalf = change.modelChangeValue
            is PlayerResultSetSendToBoxReason -> getPlayerResult(game, change.modelChangeKey).sendToBoxReason = change.modelChangeValue
            is PlayerResultSetSendToBoxTurn -> getPlayerResult(game, change.modelChangeKey).sendToBoxTurn = change.modelChangeValue
            is PlayerResultSetSeriousInjury -> getPlayerResult(game, change.modelChangeKey).seriousInjury = change.modelChangeValue
            is PlayerResultSetSeriousInjuryDecay -> getPlayerResult(game, change.modelChangeKey).seriousInjuryDecay = change.modelChangeValue
            is PlayerResultSetTouchdowns -> getPlayerResult(game, change.modelChangeKey).touchdowns = change.modelChangeValue
            is PlayerResultSetTurnsPlayed -> getPlayerResult(game, change.modelChangeKey).turnsPlayed = change.modelChangeValue
            is TeamResultDedicatedFansModifier -> getTeamResult(game, change.isHomeData()).dedicatedFans = change.modelChangeValue
            is TeamResultSetBadlyHurtSuffered -> getTeamResult(game, change.isHomeData()).badlyHurtSuffered = change.modelChangeValue
            is TeamResultSetConceded -> getTeamResult(game, change.isHomeData()).conceded = change.modelChangeValue
            is TeamResultSetFame -> getTeamResult(game, change.isHomeData()).fame = change.modelChangeValue
            is TeamResultSetFanFactor -> getTeamResult(game, change.isHomeData()).fanFactorModifier = change.modelChangeValue
            is TeamResultSetFanFactorModifier -> getTeamResult(game, change.isHomeData()).fanFactorModifier = change.modelChangeValue
            is TeamResultSetPenaltyScore -> getTeamResult(game, change.isHomeData()).penaltyScore = change.modelChangeValue
            is TeamResultSetPettyCashTransferred -> getTeamResult(game, change.isHomeData()).pettyCashTransferred = change.modelChangeValue
            is TeamResultSetPettyCashUsed -> getTeamResult(game, change.isHomeData()).pettyCashUsed = change.modelChangeValue
            is TeamResultSetRaisedDead -> getTeamResult(game, change.isHomeData()).raisedDead = change.modelChangeValue
            is TeamResultSetRipSuffered -> getTeamResult(game, change.isHomeData()).ripSuffered = change.modelChangeValue
            is TeamResultSetScore -> getTeamResult(game, change.isHomeData()).score = change.modelChangeValue
            is TeamResultSetSeriousInjurySuffered -> getTeamResult(game, change.isHomeData()).seriousInjurySuffered = change.modelChangeValue
            is TeamResultSetSpectators -> getTeamResult(game, change.isHomeData()).spectators = change.modelChangeValue
            is TeamResultSetSpirallingExpenses -> getTeamResult(game, change.isHomeData()).spirallingExpenses = change.modelChangeValue
            is TeamResultSetTeamValue -> getTeamResult(game, change.isHomeData()).teamValue = change.modelChangeValue
            is TeamResultSetWinnings -> getTeamResult(game, change.isHomeData()).winnings = change.modelChangeValue
            is TurnDataSetApothecaries -> getTurnData(game, change.isHomeData()).apothecaries = change.modelChangeValue
            is TurnDataSetBlitzUsed -> getTurnData(game, change.isHomeData()).blitzUsed = change.modelChangeValue
            is TurnDataSetBombUsed -> getTurnData(game, change.isHomeData()).bombUsed = change.modelChangeValue
            is TurnDataSetCoachBanned -> getTurnData(game, change.isHomeData()).coachBanned = change.modelChangeValue
            is TurnDataSetFirstTurnAfterKickoff -> getTurnData(game, change.isHomeData()).firstTurnAfterKickoff = change.modelChangeValue
            is TurnDataSetFoulUsed -> getTurnData(game, change.isHomeData()).foulUsed = change.modelChangeValue
            is TurnDataSetHandOverUsed -> getTurnData(game, change.isHomeData()).handOverUsed = change.modelChangeValue
            is TurnDataSetKtmUsed -> getTurnData(game, change.isHomeData()).ktmUsed = change.modelChangeValue
            is TurnDataSetLeaderState -> getTurnData(game, change.isHomeData()).leaderState = change.modelChangeValue
            is TurnDataSetPassUsed -> getTurnData(game, change.isHomeData()).passUsed = change.modelChangeValue
            is TurnDataSetPlagueDoctors -> getTurnData(game, change.isHomeData()).plagueDoctors = change.modelChangeValue
            is TurnDataSetReRollUsed -> getTurnData(game, change.isHomeData()).reRollUsed = change.modelChangeValue
            is TurnDataSetReRolls -> getTurnData(game, change.isHomeData()).reRolls = change.modelChangeValue
            is TurnDataSetReRollsBrilliantCoachingOneDrive -> getTurnData(game, change.isHomeData()).rerollBrilliantCoachingOneDrive = change.modelChangeValue
            is TurnDataSetReRollsPumpUpTheCrowdOneDrive -> getTurnData(game, change.isHomeData()).rerollPumpUpTheCrowdOneDrive = change.modelChangeValue
            is TurnDataSetReRollsSingleUse -> getTurnData(game, change.isHomeData()).singleUseReRolls = change.modelChangeValue
            is TurnDataSetTurnNr -> getTurnData(game, change.isHomeData()).turnNr = change.modelChangeValue
            is TurnDataSetTurnStarted -> getTurnData(game, change.isHomeData()).turnStarted = change.modelChangeValue
            is TurnDataSetWanderingApothecaries -> getTurnData(game, change.isHomeData()).wanderingApothecaries = change.modelChangeValue
            else -> {
                println("Ignore $change")
                return false
            }
        }
        return true

//        when (pModelChange.modelChangeId) {
//            FIELD_MODEL_ADD_CARD -> {
//                pGame.fieldModel.addCard(pGame.getPlayerById(pModelChange.getKey()), pModelChange.getValue() as Card?)
//                return true
//            }
//
//            FIELD_MODEL_ADD_CARD_EFFECT -> {
//                pGame.fieldModel.addCardEffect(
//                    pGame.getPlayerById(pModelChange.getKey()), pModelChange
//                        .getValue() as CardEffect?
//                )
//                return true
//            }
//
//            FIELD_MODEL_ADD_DICE_DECORATION -> {
//                pGame.fieldModel.add(pModelChange.getValue() as DiceDecoration?)
//                return true
//            }
//
//            FIELD_MODEL_ADD_INTENSIVE_TRAINING -> {
//                pGame.fieldModel.addIntensiveTrainingSkill(pModelChange.getKey(), pModelChange.getValue() as Skill?)
//                return true
//            }
//
//            FIELD_MODEL_ADD_FIELD_MARKER -> {
//                pGame.fieldModel.add(pModelChange.getValue() as FieldMarker?)
//                return true
//            }
//
//            FIELD_MODEL_ADD_MOVE_SQUARE -> {
//                pGame.fieldModel.add(pModelChange.getValue() as MoveSquare?)
//                return true
//            }
//
//            FIELD_MODEL_ADD_PLAYER_MARKER -> {
//                pGame.fieldModel.add(pModelChange.getValue() as PlayerMarker?)
//                return true
//            }
//
//            FIELD_MODEL_ADD_PRAYER -> {
//                pGame.fieldModel.addPrayerEnhancements(
//                    pGame.getPlayerById(pModelChange.getKey()),
//                    Prayer.valueOf(pModelChange.getValue() as String?)
//                )
//                return true
//            }
//
//            FIELD_MODEL_ADD_PUSHBACK_SQUARE -> {
//                pGame.fieldModel.add(pModelChange.getValue() as PushbackSquare?)
//                return true
//            }
//
//            FIELD_MODEL_ADD_SKILL_ENHANCEMENTS -> {
//                pGame.fieldModel.addSkillEnhancements(
//                    pGame.getPlayerById(pModelChange.getKey()),
//                    skillFactory.forName(pModelChange.getValue() as String?)
//                )
//                return true
//            }
//
//            FIELD_MODEL_ADD_TRACK_NUMBER -> {
//                pGame.fieldModel.add(pModelChange.getValue() as TrackNumber?)
//                return true
//            }
//
//            FIELD_MODEL_ADD_TRAP_DOOR -> {
//                pGame.fieldModel.add(pModelChange.getValue() as TrapDoor?)
//                return true
//            }
//
//            FIELD_MODEL_ADD_WISDOM -> {
//                Constant.getGrantAbleSkills(skillFactory).stream()
//                    .filter { swv -> swv.getSkill().equals(pModelChange.getValue()) }
//                    .findFirst().ifPresent { swv -> pGame.fieldModel.addWisdomSkill(pModelChange.getKey(), swv) }
//
//
//                return true
//            }
//
//            FIELD_MODEL_KEEP_DEACTIVATED_CARD -> {
//                pGame.fieldModel.keepDeactivatedCard(
//                    pGame.getPlayerById(pModelChange.getKey()),
//                    pModelChange.getValue() as Card?
//                )
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_CARD -> {
//                pGame.fieldModel.removeCard(
//                    pGame.getPlayerById(pModelChange.getKey()),
//                    pModelChange.getValue() as Card?
//                )
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_CARD_EFFECT -> {
//                pGame.fieldModel.removeCardEffect(
//                    pGame.getPlayerById(pModelChange.getKey()), pModelChange
//                        .getValue() as CardEffect?
//                )
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_DICE_DECORATION -> {
//                pGame.fieldModel.remove(pModelChange.getValue() as DiceDecoration?)
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_FIELD_MARKER -> {
//                pGame.fieldModel.remove(pModelChange.getValue() as FieldMarker?)
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_MOVE_SQUARE -> {
//                pGame.fieldModel.remove(pModelChange.getValue() as MoveSquare?)
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_PLAYER -> {
//                pGame.fieldModel.remove(pGame.getPlayerById(pModelChange.getKey()))
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_PLAYER_MARKER -> {
//                pGame.fieldModel.remove(pModelChange.getValue() as PlayerMarker?)
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_PRAYER -> {
//                pGame.fieldModel.removePrayerEnhancements(
//                    pGame.getPlayerById(pModelChange.getKey()),
//                    Prayer.valueOf(pModelChange.getValue() as String?)
//                )
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_PUSHBACK_SQUARE -> {
//                pGame.fieldModel.remove(pModelChange.getValue() as PushbackSquare?)
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_TRACK_NUMBER -> {
//                pGame.fieldModel.remove(pModelChange.getValue() as TrackNumber?)
//                return true
//            }
//
//            FIELD_MODEL_REMOVE_TRAP_DOOR -> {
//                pGame.fieldModel.remove(pModelChange.getValue() as TrapDoor?)
//                return true
//            }
//
//            FIELD_MODEL_SET_BALL_COORDINATE -> {
//                pGame.fieldModel.setBallCoordinate(pModelChange.getValue() as FieldCoordinate?)
//                return true
//            }
//
//            FIELD_MODEL_SET_BALL_IN_PLAY -> {
//                pGame.fieldModel.setBallInPlay((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            FIELD_MODEL_SET_BALL_MOVING -> {
//                pGame.fieldModel.setBallMoving((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            FIELD_MODEL_SET_BLITZ_STATE, FIELD_MODEL_SET_TARGET_SELECTION_STATE -> {
//                pGame.fieldModel.setTargetSelectionState(pModelChange.getValue() as TargetSelectionState?)
//                return true
//            }
//
//            FIELD_MODEL_SET_BOMB_COORDINATE -> {
//                pGame.fieldModel.setBombCoordinate(pModelChange.getValue() as FieldCoordinate?)
//                return true
//            }
//
//            FIELD_MODEL_SET_BOMB_MOVING -> {
//                pGame.fieldModel.setBombMoving((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            FIELD_MODEL_SET_RANGE_RULER -> {
//                pGame.fieldModel.setRangeRuler(pModelChange.getValue() as RangeRuler?)
//                return true
//            }
//
//            FIELD_MODEL_SET_WEATHER -> {
//                pGame.fieldModel.setWeather(pModelChange.getValue() as Weather?)
//                return true
//            }
//
//            GAME_SET_CONCEDED_LEGALLY -> {
//                pGame.setConcededLegally((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            GAME_SET_DEFENDER_ACTION -> {
//                pGame.setDefenderAction(pModelChange.getValue() as PlayerAction?)
//                return true
//            }
//
//            GAME_SET_DEFENDER_ID -> {
//                pGame.setDefenderId(pModelChange.getKey())
//                return true
//            }
//
//            GAME_SET_FINISHED -> {
//                pGame.setFinished(pModelChange.getValue() as Date?)
//                return true
//            }
//
//            GAME_SET_HALF -> {
//                pGame.setHalf((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            GAME_SET_HOME_FIRST_OFFENSE -> {
//                pGame.setHomeFirstOffense((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            GAME_SET_HOME_PLAYING -> {
//                pGame.setHomePlaying((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            GAME_SET_ID -> {
//                pGame.setId((pModelChange.getValue() as Long?))
//                return true
//            }
//
//            GAME_SET_LAST_DEFENDER_ID -> {
//                pGame.setLastDefenderId(pModelChange.getKey())
//                return true
//            }
//
//            GAME_SET_PASS_COORDINATE -> {
//                pGame.setPassCoordinate(pModelChange.getValue() as FieldCoordinate?)
//                return true
//            }
//
//            GAME_SET_SCHEDULED -> {
//                pGame.setScheduled(pModelChange.getValue() as Date?)
//                return true
//            }
//
//            GAME_SET_TESTING -> {
//                pGame.setTesting((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            GAME_SET_ADMIN_MODE -> {
//                pGame.setAdminMode((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            GAME_SET_THROWER_ID -> {
//                pGame.setThrowerId(pModelChange.getKey())
//                return true
//            }
//
//            GAME_SET_THROWER_ACTION -> {
//                pGame.setThrowerAction(pModelChange.getValue() as PlayerAction?)
//                return true
//            }
//
//            GAME_SET_TIMEOUT_ENFORCED -> {
//                pGame.setTimeoutEnforced((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            GAME_SET_TIMEOUT_POSSIBLE -> {
//                pGame.setTimeoutPossible((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            GAME_SET_TURN_MODE -> {
//                pGame.setTurnMode(pModelChange.getValue() as TurnMode?)
//                return true
//            }
//
//            GAME_SET_WAITING_FOR_OPPONENT -> {
//                pGame.setWaitingForOpponent((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            GAME_OPTIONS_ADD_OPTION -> {
//                pGame.getOptions().addOption(pModelChange.getValue() as IGameOption?)
//                return true
//            }
//
//            INDUCEMENT_SET_ACTIVATE_CARD -> {
//                getInducementSet(pGame, isHomeData(pModelChange)).activateCard(pModelChange.getValue() as Card?)
//                return true
//            }
//
//            INDUCEMENT_SET_ADD_AVAILABLE_CARD -> {
//                getInducementSet(pGame, isHomeData(pModelChange)).addAvailableCard(pModelChange.getValue() as Card?)
//                return true
//            }
//
//            INDUCEMENT_SET_ADD_INDUCEMENT -> {
//                getInducementSet(pGame, isHomeData(pModelChange)).addInducement(pModelChange.getValue() as Inducement?)
//                return true
//            }
//
//            INDUCEMENT_SET_ADD_PRAYER -> {
//                getInducementSet(pGame, isHomeData(pModelChange)).addPrayer(pModelChange.getValue() as Prayer?)
//                return true
//            }
//
//            INDUCEMENT_SET_CARD_CHOICES -> {
//                dialogParameter = pGame.dialogParameter
//                if (dialogParameter is DialogBuyCardsAndInducementsParameter) {
//                    (dialogParameter as DialogBuyCardsAndInducementsParameter).setCardChoices(pModelChange.getValue() as CardChoices?)
//                }
//                return true
//            }
//
//            INDUCEMENT_SET_DEACTIVATE_CARD -> {
//                getInducementSet(pGame, isHomeData(pModelChange)).deactivateCard(pModelChange.getValue() as Card?)
//                return true
//            }
//
//            INDUCEMENT_SET_REMOVE_AVAILABLE_CARD -> {
//                getInducementSet(pGame, isHomeData(pModelChange)).removeAvailableCard(pModelChange.getValue() as Card?)
//                return true
//            }
//
//            INDUCEMENT_SET_REMOVE_INDUCEMENT -> {
//                getInducementSet(
//                    pGame,
//                    isHomeData(pModelChange)
//                ).removeInducement(pModelChange.getValue() as Inducement?)
//                return true
//            }
//
//            INDUCEMENT_SET_REMOVE_PRAYER -> {
//                getInducementSet(pGame, isHomeData(pModelChange)).removePrayer(pModelChange.getValue() as Prayer?)
//                return true
//            }
//
//            PLAYER_MARK_SKILL_USED -> {
//                pGame.getPlayerById(pModelChange.getKey()).markUsed(pModelChange.getValue() as Skill?, pGame)
//                return true
//            }
//
//            PLAYER_MARK_SKILL_UNUSED -> {
//                pGame.getPlayerById(pModelChange.getKey()).markUnused(pModelChange.getValue() as Skill?, pGame)
//                return true
//            }
//
//            PLAYER_RESULT_SET_BLOCKS -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setBlocks((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_CASUALTIES -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setCasualties((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_CASUALTIES_WITH_ADDITIONAL_SPP -> {
//                getPlayerResult(
//                    pGame,
//                    pModelChange.getKey()
//                ).setCasualtiesWithAdditionalSpp((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_COMPLETIONS -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setCompletions((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_COMPLETIONS_WITH_ADDITIONAL_SPP -> {
//                getPlayerResult(
//                    pGame,
//                    pModelChange.getKey()
//                ).setCompletionsWithAdditionalSpp((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_CURRENT_SPPS -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setCurrentSpps((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_DEFECTING -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setDefecting((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_FOULS -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setFouls((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_HAS_USED_SECRET_WEAPON -> {
//                getPlayerResult(
//                    pGame,
//                    pModelChange.getKey()
//                ).setHasUsedSecretWeapon((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_INTERCEPTIONS -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setInterceptions((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_DEFLECTIONS -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setDeflections((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_PASSING -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setPassing((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_PLAYER_AWARDS -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setPlayerAwards((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_RUSHING -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setRushing((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_SEND_TO_BOX_BY_PLAYER_ID -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setSendToBoxByPlayerId(pModelChange.getValue() as String?)
//                return true
//            }
//
//            PLAYER_RESULT_SET_SEND_TO_BOX_HALF -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setSendToBoxHalf((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_SEND_TO_BOX_REASON -> {
//                getPlayerResult(
//                    pGame,
//                    pModelChange.getKey()
//                ).setSendToBoxReason(pModelChange.getValue() as SendToBoxReason?)
//                return true
//            }
//
//            PLAYER_RESULT_SET_SEND_TO_BOX_TURN -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setSendToBoxTurn((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_SERIOUS_INJURY -> {
//                getPlayerResult(
//                    pGame,
//                    pModelChange.getKey()
//                ).setSeriousInjury(pModelChange.getValue() as SeriousInjury?)
//                return true
//            }
//
//            PLAYER_RESULT_SET_SERIOUS_INJURY_DECAY -> {
//                getPlayerResult(
//                    pGame,
//                    pModelChange.getKey()
//                ).setSeriousInjuryDecay(pModelChange.getValue() as SeriousInjury?)
//                return true
//            }
//
//            PLAYER_RESULT_SET_TOUCHDOWNS -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setTouchdowns((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            PLAYER_RESULT_SET_TURNS_PLAYED -> {
//                getPlayerResult(pGame, pModelChange.getKey()).setTurnsPlayed((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_CONCEDED -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setConceded((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TEAM_RESULT_SET_DEDICATED_FANS_MODIFIER -> {
//                getTeamResult(
//                    pGame,
//                    isHomeData(pModelChange)
//                ).setDedicatedFansModifier((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_FAME -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setFame((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_BADLY_HURT_SUFFERED -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setBadlyHurtSuffered((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_PENALTY_SCORE -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setPenaltyScore((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_PETTY_CASH_TRANSFERRED -> {
//                getTeamResult(
//                    pGame,
//                    isHomeData(pModelChange)
//                ).setPettyCashTransferred((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_PETTY_CASH_USED -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setPettyCashUsed((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_RAISED_DEAD -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setRaisedDead((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_RIP_SUFFERED -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setRipSuffered((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_SCORE -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setScore((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_SERIOUS_INJURY_SUFFERED -> {
//                getTeamResult(
//                    pGame,
//                    isHomeData(pModelChange)
//                ).setSeriousInjurySuffered((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_SPECTATORS -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setSpectators((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_SPIRALLING_EXPENSES -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setSpirallingExpenses((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_TEAM_VALUE -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setTeamValue((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_FAN_FACTOR -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setFanFactor((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TEAM_RESULT_SET_WINNINGS -> {
//                getTeamResult(pGame, isHomeData(pModelChange)).setWinnings((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TURN_DATA_SET_APOTHECARIES -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setApothecaries((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TURN_DATA_SET_BLITZ_USED -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setBlitzUsed((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_BOMB_USED -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setBombUsed((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_FIRST_TURN_AFTER_KICKOFF -> {
//                getTurnData(
//                    pGame,
//                    isHomeData(pModelChange)
//                ).setFirstTurnAfterKickoff((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_FOUL_USED -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setFoulUsed((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_HAND_OVER_USED -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setHandOverUsed((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_LEADER_STATE -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setLeaderState(pModelChange.getValue() as LeaderState?)
//                return true
//            }
//
//            TURN_DATA_SET_PASS_USED -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setPassUsed((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_KTM_USED -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setKtmUsed((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_RE_ROLLS_BRILLIANT_COACHING_ONE_DRIVE -> {
//                getTurnData(
//                    pGame,
//                    isHomeData(pModelChange)
//                ).setReRollsBrilliantCoachingOneDrive((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TURN_DATA_SET_RE_ROLLS_PUMP_UP_THE_CROWD_ONE_DRIVE -> {
//                getTurnData(
//                    pGame,
//                    isHomeData(pModelChange)
//                ).setReRollsPumpUpTheCrowdOneDrive((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TURN_DATA_SET_RE_ROLLS_SINGLE_USE -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setSingleUseReRolls((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TURN_DATA_SET_RE_ROLL_USED -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setReRollUsed((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_TURN_NR -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setTurnNr((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TURN_DATA_SET_TURN_STARTED -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setTurnStarted((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_COACH_BANNED -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setCoachBanned((pModelChange.getValue() as Boolean?))
//                return true
//            }
//
//            TURN_DATA_SET_WANDERING_APOTHECARIES -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setWanderingApothecaries((pModelChange.getValue() as Int?))
//                return true
//            }
//
//            TURN_DATA_SET_PLAGUE_DOCTORS -> {
//                getTurnData(pGame, isHomeData(pModelChange)).setPlagueDoctors((pModelChange.getValue() as Int?))
//                return true
//            }
//        }
        return false
    }

//    fun transform(pModelChange: ModelChange?): ModelChange? {
//        val bloodSpot: BloodSpot?
//        val diceDecoration: DiceDecoration?
//        val fieldMarker: FieldMarker?
//        val moveSquare: MoveSquare?
//        val playerMarker: PlayerMarker?
//        val pushbackSquare: PushbackSquare?
//        val trackNumber: TrackNumber?
//        val trapDoor: TrapDoor
//        val ballCoordinate: FieldCoordinate?
//        val bombCoordinate: FieldCoordinate?
//        val playerCoordinate: FieldCoordinate?
//        val rangeRuler: RangeRuler?
//        val dialogParameter: IDialogParameter?
//        val passCoordinate: FieldCoordinate?
//        if (pModelChange == null || pModelChange.getChangeId() == null) {
//            return null
//        }
//
//        when (pModelChange.getChangeId()) {
//            FIELD_MODEL_ADD_BLOOD_SPOT -> {
//                bloodSpot = pModelChange.getValue() as BloodSpot?
//                return dk.ilios.jervis.fumbbl.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((bloodSpot != null)) bloodSpot
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_ADD_DICE_DECORATION, FIELD_MODEL_REMOVE_DICE_DECORATION -> {
//                diceDecoration = pModelChange.getValue() as DiceDecoration?
//                return dk.ilios.jervis.fumbbl.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((diceDecoration != null)) diceDecoration
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_ADD_FIELD_MARKER, FIELD_MODEL_REMOVE_FIELD_MARKER -> {
//                fieldMarker = pModelChange.getValue() as FieldMarker?
//                return dk.ilios.jervis.fumbbl.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((fieldMarker != null)) fieldMarker
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_ADD_MOVE_SQUARE, FIELD_MODEL_REMOVE_MOVE_SQUARE -> {
//                moveSquare = pModelChange.getValue() as MoveSquare?
//                return dk.ilios.jervis.fumbbl.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((moveSquare != null)) moveSquare
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_ADD_PLAYER_MARKER, FIELD_MODEL_REMOVE_PLAYER_MARKER -> {
//                playerMarker = pModelChange.getValue() as PlayerMarker?
//                return dk.ilios.jervis.fumbbl.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((playerMarker != null)) playerMarker
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_ADD_PUSHBACK_SQUARE, FIELD_MODEL_REMOVE_PUSHBACK_SQUARE -> {
//                pushbackSquare = pModelChange.getValue() as PushbackSquare?
//                return dk.ilios.jervis.fumbbl.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((pushbackSquare != null)) pushbackSquare
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_ADD_TRACK_NUMBER, FIELD_MODEL_REMOVE_TRACK_NUMBER -> {
//                trackNumber = pModelChange.getValue() as TrackNumber?
//                return dk.ilios.jervis.fumbbl.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((trackNumber != null)) trackNumber
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_ADD_TRAP_DOOR, FIELD_MODEL_REMOVE_TRAP_DOOR -> {
//                trapDoor = pModelChange.getValue() as TrapDoor
//                return dk.ilios.jervis.fumbbl.model.change.ModelChange(
//                    pModelChange.getChangeId(),
//                    pModelChange.getKey(),
//                    trapDoor.transform()
//                )
//            }
//
//            FIELD_MODEL_SET_BALL_COORDINATE -> {
//                ballCoordinate = pModelChange.getValue() as FieldCoordinate?
//                return dk.ilios.jervis.fumbbl.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((ballCoordinate != null)) ballCoordinate
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_SET_BOMB_COORDINATE -> {
//                bombCoordinate = pModelChange.getValue() as FieldCoordinate?
//                return dk.ilios.jervis.fumbbl.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((bombCoordinate != null)) bombCoordinate
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_SET_PLAYER_COORDINATE -> {
//                playerCoordinate = pModelChange.getValue() as FieldCoordinate?
//                return dk.ilios.jervis.fumbbl.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((playerCoordinate != null)) playerCoordinate
//                        .transform() else null
//                )
//            }
//
//            FIELD_MODEL_SET_RANGE_RULER -> {
//                rangeRuler = pModelChange.getValue() as RangeRuler?
//                return dk.ilios.jervis.fumbbl.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((rangeRuler != null)) rangeRuler
//                        .transform() else null
//                )
//            }
//
//            GAME_SET_DIALOG_PARAMETER -> {
//                dialogParameter = pModelChange.getValue() as IDialogParameter?
//                return dk.ilios.jervis.fumbbl.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((dialogParameter != null)) dialogParameter
//                        .transform() else null
//                )
//            }
//
//            GAME_SET_HOME_FIRST_OFFENSE, GAME_SET_HOME_PLAYING -> return dk.ilios.jervis.fumbbl.model.change.ModelChange(
//                pModelChange.getChangeId(), pModelChange.getKey(),
//                !(pModelChange.getValue() as Boolean?)!!
//            )
//
//            GAME_SET_PASS_COORDINATE -> {
//                passCoordinate = pModelChange.getValue() as FieldCoordinate?
//                return dk.ilios.jervis.fumbbl.model.change.ModelChange(
//                    pModelChange.getChangeId(), pModelChange.getKey(), if ((passCoordinate != null)) passCoordinate
//                        .transform() else null
//                )
//            }
//
//            INDUCEMENT_SET_ACTIVATE_CARD, INDUCEMENT_SET_ADD_AVAILABLE_CARD, INDUCEMENT_SET_ADD_INDUCEMENT, INDUCEMENT_SET_ADD_PRAYER, INDUCEMENT_SET_DEACTIVATE_CARD, INDUCEMENT_SET_REMOVE_AVAILABLE_CARD, INDUCEMENT_SET_REMOVE_INDUCEMENT, INDUCEMENT_SET_REMOVE_PRAYER -> return dk.ilios.jervis.fumbbl.model.change.ModelChange(
//                pModelChange.getChangeId(), if (isHomeData(pModelChange)) "away" else "home", pModelChange
//                    .getValue()
//            )
//
//            TEAM_RESULT_SET_CONCEDED, TEAM_RESULT_SET_DEDICATED_FANS_MODIFIER, TEAM_RESULT_SET_FAME, TEAM_RESULT_SET_BADLY_HURT_SUFFERED, TEAM_RESULT_SET_FAN_FACTOR_MODIFIER, TEAM_RESULT_SET_PENALTY_SCORE, TEAM_RESULT_SET_PETTY_CASH_TRANSFERRED, TEAM_RESULT_SET_PETTY_CASH_USED, TEAM_RESULT_SET_RAISED_DEAD, TEAM_RESULT_SET_RIP_SUFFERED, TEAM_RESULT_SET_SCORE, TEAM_RESULT_SET_SERIOUS_INJURY_SUFFERED, TEAM_RESULT_SET_SPECTATORS, TEAM_RESULT_SET_SPIRALLING_EXPENSES, TEAM_RESULT_SET_TEAM_VALUE, TEAM_RESULT_SET_FAN_FACTOR, TEAM_RESULT_SET_WINNINGS, TURN_DATA_SET_APOTHECARIES, TURN_DATA_SET_BLITZ_USED, TURN_DATA_SET_BOMB_USED, TURN_DATA_SET_FIRST_TURN_AFTER_KICKOFF, TURN_DATA_SET_FOUL_USED, TURN_DATA_SET_HAND_OVER_USED, TURN_DATA_SET_LEADER_STATE, TURN_DATA_SET_PASS_USED, TURN_DATA_SET_KTM_USED, TURN_DATA_SET_RE_ROLLS, TURN_DATA_SET_RE_ROLLS_BRILLIANT_COACHING_ONE_DRIVE, TURN_DATA_SET_RE_ROLLS_PUMP_UP_THE_CROWD_ONE_DRIVE, TURN_DATA_SET_RE_ROLLS_SINGLE_USE, TURN_DATA_SET_RE_ROLL_USED, TURN_DATA_SET_TURN_NR, TURN_DATA_SET_TURN_STARTED, TURN_DATA_SET_COACH_BANNED, TURN_DATA_SET_WANDERING_APOTHECARIES, TURN_DATA_SET_PLAGUE_DOCTORS -> {
//            }
//            else -> return dk.ilios.jervis.fumbbl.model.change.ModelChange(
//                pModelChange.getChangeId(),
//                pModelChange.getKey(),
//                pModelChange.getValue()
//            )
//        }
//        return dk.ilios.jervis.fumbbl.model.change.ModelChange(
//            pModelChange.getChangeId(), if (isHomeData(pModelChange)) "away" else "home", pModelChange
//                .getValue()
//        )
//    }


    private fun isHomeData(pChange: ModelChange): Boolean {
        return "home" == pChange.modelChangeKey
    }

    private fun getTeamResult(game: Game, homeData: Boolean): TeamResult {
        return if (homeData) game.gameResult.teamResultHome else game.gameResult.teamResultAway
    }

    private fun getTurnData(game: Game, homeData: Boolean): TurnData {
        return if (homeData) game.turnDataHome else game.turnDataAway
    }

    private fun getInducementSet(pGame: Game, pHomeData: Boolean): InducementSet {
        return if (pHomeData) pGame.turnDataHome.inducementSet else pGame.turnDataAway.inducementSet
    }

    private fun getPlayerResult(game: Game, playerId: String): PlayerResult {
        return game.gameResult.getPlayerResult(game.getPlayerById(playerId)!!)
    }
}