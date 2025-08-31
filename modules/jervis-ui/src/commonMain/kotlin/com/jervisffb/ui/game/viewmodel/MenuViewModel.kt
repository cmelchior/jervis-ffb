package com.jervisffb.ui.game.viewmodel

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.procedures.GameDrive
import com.jervisffb.engine.rules.bb2020.procedures.SetupTeam
import com.jervisffb.engine.rules.bb2020.procedures.SetupTeamContext
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.engine.serialize.JervisSetupFile
import com.jervisffb.ui.CacheManager
import com.jervisffb.ui.IssueTracker
import com.jervisffb.ui.SoundEffect
import com.jervisffb.ui.SoundManager
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.menu.BackNavigationHandler
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.ui.menu.intro.CreditData
import com.jervisffb.ui.utils.saveFile
import com.jervisffb.utils.canBeHost
import com.jervisffb.utils.jervisLogger
import com.jervisffb.utils.multiThreadDispatcher
import com.jervisffb.utils.singleThreadDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class Feature {
    DO_NOT_REROLL_SUCCESSFUL_ACTIONS,
    SELECT_KICKING_PLAYER,
    END_PLAYER_ACTION_IF_ONLY_OPTION,
    SELECT_BLOCK_TYPE_IF_ONLY_OPTION,
    PUSH_PLAYER_INTO_CROWD,
}

data class ErrorDialog(
    val visible: Boolean,
    val title: String,
    val error: Throwable? = null,
)

data class ReportIssueDialogData(
    val visible: Boolean,
    val title: String,
    val body: String,
    val error: Throwable? = null,
    val gameState: GameEngineController? = null
) {
    companion object {
        val HIDDEN = ReportIssueDialogData(false, "", "")
    }
}

class MenuViewModel {
    companion object {
        val LOG = jervisLogger()
    }

    // Some place to store the last exception thrown by the Game Engine (if any)
    var lastActionException: Throwable? = null

    // Expose a flow
    val setupAvailable: MutableStateFlow<GameType?> = MutableStateFlow(null)

    var controller: GameEngineController? = null
    lateinit var uiState: UiGameController

    val p2pHostAvaiable: Boolean = canBeHost()

    private val _showSettingsDialog = MutableStateFlow(false)
    private val _showDialogDialog = MutableStateFlow(false)
    private val _showErrorDialog = MutableStateFlow(ErrorDialog(false, "",))
    private val _showReportIssueDialog = MutableStateFlow(ReportIssueDialogData.HIDDEN)
    val isAboutDialogVisible: StateFlow<Boolean> = _showDialogDialog
    val isErrorDialogVisible: StateFlow<ErrorDialog> = _showErrorDialog
    val isReportIssueDialogVisible: StateFlow<ReportIssueDialogData> = _showReportIssueDialog
    val creditData: CreditData

    // Scope for lauching tasks directly related to navigating the UI
    val uiScope = CoroutineScope(CoroutineName("UI") + Dispatchers.Default)
    val navigatorContext = CoroutineScope(CoroutineName("ScreenNavigator") + singleThreadDispatcher("menuThread"))
    // Scope for launching background tasks for Menu actions
    val backgroundContext = CoroutineScope(SupervisorJob() + CoroutineName("ScreenBackground") + multiThreadDispatcher("menuBackgroundThread"))

    init {
        // Customize the create issue link, so it contains some basic information about the client
        // Formatting is weird because `getPlatformDescription` returns a multiline text that doesn't
        // follow the same indentation as the rest of the text.
        val body = """
                <Describe the issue>
                <Attach Game Dump if applicable. Available under the in-game menu>
        """.trimIndent()
        val issueUrl = IssueTracker.createIssueUrl(title = null, body = body, IssueTracker.Label.USER)
        creditData = CreditData(
            newIssueUrl = issueUrl
        )
    }

    fun showAboutDialog(visible: Boolean) {
        _showDialogDialog.value = visible
    }

    fun showErrorDialog(message: String, error: Throwable? = null) {
        _showErrorDialog.value = ErrorDialog(
            visible = true,
            title = message,
            error = error,
        )
    }

    fun hideErrorDialog() {
        _showErrorDialog.value = ErrorDialog(visible = false, title = "",)
    }

    fun showReportIssueDialog(title: String, body: String, error: Throwable? = null, gameState: GameEngineController? = null) {
        _showReportIssueDialog.value = ReportIssueDialogData(
            visible = true,
            title = title,
            body = body,
            error = error,
            gameState = gameState
        )
    }

    fun hideReportIssueDialog() {
        _showReportIssueDialog.value = ReportIssueDialogData.HIDDEN
    }

    // Default values .. figure out a way to persist these
    private var features: MutableMap<Feature, Boolean> = mutableMapOf(
        Feature.DO_NOT_REROLL_SUCCESSFUL_ACTIONS to true,
        Feature.SELECT_KICKING_PLAYER to true,
        Feature.END_PLAYER_ACTION_IF_ONLY_OPTION to true,
        Feature.SELECT_BLOCK_TYPE_IF_ONLY_OPTION to true,
        Feature.PUSH_PLAYER_INTO_CROWD to true,
    )

    fun backToLastScreen() {
        BackNavigationHandler.execute()
    }

    fun openSettings(bool: Boolean = true) {
        _showSettingsDialog.value = bool
    }

    fun showSettingsDialog(): StateFlow<Boolean> = _showSettingsDialog

    fun serializeGameState(includeDebugState: Boolean): String {
        return JervisSerialization.serializeGameStateToJson(controller!!, includeDebugState)
    }

    fun undoAction() {
        val team = when (uiState.uiMode) {
            TeamActionMode.HOME_TEAM -> uiState.state.homeTeam.id
            TeamActionMode.AWAY_TEAM -> uiState.state.awayTeam.id
            TeamActionMode.ALL_TEAMS -> null // No team restrictions when undoing on a Client controlling both teams
        }
        if (uiState.gameController.isUndoAvailable(team = team)) {
            uiState.userSelectedAction(Undo)
        } else {
            SoundManager.play(SoundEffect.ERROR)
        }
    }

    fun toggleFeature(feature: Feature, enabled: Boolean) {
        features[feature] = enabled
    }

    fun isFeatureEnabled(feature: Feature): Boolean {
        val isUndoing = controller?.lastActionWasUndo() ?: false
        return !isUndoing && (features[feature] ?: false)
    }

    fun loadSetup(setup: JervisSetupFile) {
        if (setup.gameType == GameType.DUNGEON_BOWL) error("Dungeon Bowl Setups not supported yet")
        if (controller == null) {
            // It shouldn't be possible to call "Load Setup" with out a game controller,
            // but just in case it happens.
            LOG.w { "Load Setup called before game was started." }
            SoundManager.play(SoundEffect.ERROR)
            return
        }
        val allowedTeam = when (uiState.uiMode) {
            TeamActionMode.HOME_TEAM -> uiState.state.homeTeam.id
            TeamActionMode.AWAY_TEAM -> uiState.state.awayTeam.id
            TeamActionMode.ALL_TEAMS -> null // No team restrictions when undoing on a Client controlling both teams
        }
        val game = controller ?: error("No game controller was found")
        val team = game.state.getContext<SetupTeamContext>().team
        if (allowedTeam != null && allowedTeam != team.id) {
            // This client is not considered the "active" client, which means it isn't allowed
            // to load setup formations
            SoundManager.play(SoundEffect.ERROR)
            return
        }

        val rules = game.rules
        val setupActions = setup.formation.flatMap { (playerNo, relativeCoordinate) ->
            // Ignore player setup if either player or coordinate is not valid
            val playerAvailable = (
                team.noToPlayer.contains(playerNo)
                    && ((team[playerNo].state == PlayerState.RESERVE) || (team[playerNo].state == PlayerState.STANDING))
            )

            // Map to field coordinate
            val fieldCoordinate = when (team.isHomeTeam()) {
                true -> {
                    val x = rules.lineOfScrimmageHome - relativeCoordinate.dist
                    val y = relativeCoordinate.y
                    FieldCoordinate(x, y)
                }
                false -> {
                    val x = rules.lineOfScrimmageAway + relativeCoordinate.dist
                    val y = relativeCoordinate.y
                    FieldCoordinate(x, y)
                }
            }
            val isValidCoordinate = rules.isInSetupArea(team, fieldCoordinate)

            if (playerAvailable && isValidCoordinate) {
                listOf(
                    PlayerSelected(team[playerNo].id),
                    FieldSquareSelected(fieldCoordinate)
                )
            } else {
                emptyList()
            }
        }.let { setupActions ->
            // In some cases, a player was already selected when selecting a Setup. This will disrupt the above logic
            // So in that case, deselect the player first.
            if (controller?.currentNode() == SetupTeam.PlacePlayer) {
                val deselectAction = PlayerDeselected(game.state.getContext<SetupTeamContext>().currentPlayer!!)
                listOf(deselectAction) + setupActions
            } else {
                setupActions
            }
        }

        uiState.userSelectedMultipleActions(setupActions, delayEvent = false)
    }

    // Called by the UiGameController whenever a new snapshot is created. This can be used to determine
    // which menu actions should be enabled/disabled.
    fun updateUiState(uiSnapshot: UiGameSnapshot) {
        // Enable/Disable Setup options
        val setupKickingTeam = uiSnapshot.stack.containsNode(GameDrive.SetupKickingTeam)
        val setupReceivingTeam = uiSnapshot.stack.containsNode(GameDrive.SetupReceivingTeam)
        val teamControlledByClient = when (uiState.uiMode) {
            TeamActionMode.HOME_TEAM -> setupKickingTeam && uiSnapshot.game.kickingTeam.isHomeTeam()
            TeamActionMode.AWAY_TEAM -> setupReceivingTeam && uiSnapshot.game.receivingTeam.isHomeTeam()
            TeamActionMode.ALL_TEAMS -> true
        }
        if ((setupReceivingTeam || setupKickingTeam) && teamControlledByClient) {
            setupAvailable.value = uiState.rules.gameType
        } else {
            setupAvailable.value = null
        }
    }

    fun showSaveGameDialog(includeDebugState: Boolean = false) {
        saveFile(
            dialogTitle = "Save Game File",
            fileName = JervisSerialization.getGameFileName(uiState.gameController, includeDebugState),
            fileContent = serializeGameState(includeDebugState),
        )
    }
}

/**
 * Object responsible for managing team setups.
 * For now, we only load Setup files once. You need to restart the application
 * to re-fill the cache. This approach should probably be refactored at some
 * point, but having setups in-memory makes them a lot faster to access
 */
object Setups {

    private val setups = mutableSetOf<JervisSetupFile>()

    suspend fun initialize() {
        val fileSetups = CacheManager.loadSetups()
        this.setups.addAll(fileSetups)
    }

    fun getSetups(type: GameType): List<JervisSetupFile> {
        return setups
            .filter { it.gameType == type }
            .sortedBy { it.name }
    }
}



