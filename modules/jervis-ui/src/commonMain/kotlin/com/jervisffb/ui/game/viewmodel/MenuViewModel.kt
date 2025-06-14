package com.jervisffb.ui.game.viewmodel

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.procedures.GameDrive
import com.jervisffb.engine.rules.bb2020.procedures.SetupTeamContext
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.engine.serialize.JervisSetupFile
import com.jervisffb.ui.BuildConfig
import com.jervisffb.ui.CacheManager
import com.jervisffb.ui.SoundEffect
import com.jervisffb.ui.SoundManager
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.menu.BackNavigationHandler
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.ui.menu.intro.CreditData
import com.jervisffb.utils.canBeHost
import com.jervisffb.utils.getBuildType
import com.jervisffb.utils.getPlatformDescription
import com.jervisffb.utils.jervisLogger
import com.jervisffb.utils.multiThreadDispatcher
import com.jervisffb.utils.singleThreadDispatcher
import io.ktor.http.encodeURLParameter
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okio.Path

enum class Feature {
    DO_NOT_REROLL_SUCCESSFUL_ACTIONS,
    SELECT_KICKING_PLAYER,
    END_PLAYER_ACTION_IF_ONLY_OPTION,
    SELECT_BLOCK_TYPE_IF_ONLY_OPTION,
}

class MenuViewModel {
    companion object {
        val LOG = jervisLogger()
    }

    // Expose a flow
    val setupAvailable: MutableStateFlow<GameType?> = MutableStateFlow(null)

    var controller: GameEngineController? = null
    lateinit var uiState: UiGameController

    val p2pHostAvaiable: Boolean = canBeHost()

    private val _showSettingsDialog = MutableStateFlow(false)
    private val _showDialogDialog = MutableStateFlow(false)
    val isAboutDialogVisible: StateFlow<Boolean> = _showDialogDialog
    val creditData: CreditData

    // Scope for lauching tasks directly related to navigating the UI
    val navigatorContext = CoroutineScope(CoroutineName("ScreenNavigator") + singleThreadDispatcher("menuThread"))
    // Scope for launching background tasks for Menu actions
    val backgroundContext = CoroutineScope(CoroutineName("ScreenBackground") + multiThreadDispatcher("menuBackgroundThread"))

    init {
        // Customize the create issue link, so it contains some basic information about the client
        // Formatting is weird because `getPlatformDescription` returns a multiline text that doesn't
        // follow the same indentation as the rest of the text.
        val body = buildString {
            appendLine("""
                <Describe the issue>

                -----
                **Client Information (${getBuildType()})**
                Jervis Client Version: ${BuildConfig.releaseVersion}
                Git Commit: ${BuildConfig.gitHash}
            """.trimIndent())
            appendLine(getPlatformDescription())
        }.encodeURLParameter()
        creditData = CreditData(
            newIssueUrl = "https://github.com/cmelchior/jervis-ffb/issues/new?body=$body&labels=user"
        )
    }

    fun showAboutDialog(visible: Boolean) {
        _showDialogDialog.value = visible
    }

    // Default values .. figure out a way to persist these
    private var features: MutableMap<Feature, Boolean> = mutableMapOf(
        Feature.DO_NOT_REROLL_SUCCESSFUL_ACTIONS to true,
        Feature.SELECT_KICKING_PLAYER to true,
        Feature.END_PLAYER_ACTION_IF_ONLY_OPTION to true,
        Feature.SELECT_BLOCK_TYPE_IF_ONLY_OPTION to true
    )

    fun backToLastScreen() {
        BackNavigationHandler.execute()
    }

    fun openSettings(bool: Boolean = true) {
        _showSettingsDialog.value = bool
    }

    fun showSettingsDialog(): StateFlow<Boolean> = _showSettingsDialog

    fun serializeGameState(): String {
        return JervisSerialization.serializeGameState(controller!!)
    }

    fun saveGameState(destination: Path) {
        JervisSerialization.saveToFile(controller!!, destination)
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



