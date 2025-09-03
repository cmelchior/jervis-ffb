package com.jervisffb.ui.game.viewmodel

import com.jervis.generated.SettingsKeys
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.DogoutSelected
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
import com.jervisffb.ui.SETTINGS_MANAGER
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

// Map between "Automated Actions" and their corresponding settings keys.
enum class Feature(val settingsKey: String) {
    DO_NOT_REROLL_SUCCESSFUL_ACTIONS(SettingsKeys.JERVIS_AUTO_ACTION_DO_NOT_REROLL_SUCCESSFUL_ACTIONS_VALUE),
    SELECT_KICKING_PLAYER(SettingsKeys.JERVIS_AUTO_ACTION_SELECT_KICKING_PLAYER_VALUE),
    END_PLAYER_ACTION_IF_ONLY_OPTION(SettingsKeys.JERVIS_AUTO_ACTION_END_PLAYER_ACTION_IF_ONLY_OPTION_VALUE),
    SELECT_BLOCK_TYPE_IF_ONLY_OPTION(SettingsKeys.JERVIS_AUTO_ACTION_SELECT_BLOCK_TYPE_IF_ONE_OPTION_VALUE),
    PUSH_PLAYER_INTO_CROWD(SettingsKeys.JERVIS_AUTO_ACTION_PUSH_PLAYER_INTO_CROWD_VALUE),
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

/**
 * View Model controlling the full Jervis Menu System. This probably has too much responsibility right now,
 * but we do need something that cuts across a lot of concerns since the menu can impact all parts of the UI.
 */
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

    fun isFeatureEnabled(feature: Feature): Boolean {
        val isUndoing = controller?.lastActionWasUndo() ?: false
        return !isUndoing && SETTINGS_MANAGER.getBoolean(feature.settingsKey, false)
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
            // Applying a setup to a random game state is tricky, so we attempt
            // to start from a known state by resetting the state. This is done
            // by first moving all players back to the dogout.
            // This is a best effort attempt, but invalid setups should not crash the game,
            // Instead they should show up as errors in the Developer Console in the in-game
            // menu.
            val resetStateActions = buildList {
                val context = game.state.getContext<SetupTeamContext>()

                // In some cases, a player was already selected when starting a setup.
                // So we need to take that into account as well.
                if (controller?.currentNode() == SetupTeam.PlacePlayer) {
                    val deselectAction = PlayerDeselected(context.currentPlayer!!)
                    add(deselectAction)
                }

                // Move any players on the field back to the dogout.
                context.team.forEach { player ->
                    if (player.location.isOnField(rules)) {
                        add(PlayerSelected(player.id))
                        add(DogoutSelected)
                    }
                }
            }
            resetStateActions + setupActions
        }

        // Treat the entire setup as one action. Which makes it easy to undo again
        uiState.userSelectedAction(CompositeGameAction(setupActions))
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



