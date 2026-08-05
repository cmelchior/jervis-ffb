package com.jervisffb.ui.menu.challenges

import androidx.compose.ui.unit.Density
import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.engine.challenge.Challenge
import com.jervisffb.engine.model.ChallengeId
import com.jervisffb.ui.game.view.SidebarEntryState
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.JervisScreenModel
import com.jervisffb.ui.menu.challenges.data.ChallengeDetails
import com.jervisffb.ui.menu.challenges.data.ChallengeRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChallengeDetailScreenModel(
    currentChallenge: ChallengeDetails,
    private val challengesList: Flow<List<ChallengeRow>>,
    private val repository: ChallengeRepository,
    private val uiScope: CoroutineScope,
    private val repositoryScope: CoroutineScope
) : JervisScreenModel {

    /**
     * The game backing the pitch preview, or `null` while one is being built.
     *
     * A preview is a real (if idle) game, so exactly one is kept alive at a
     * time: switching challenge stops the previous one before starting the
     * next, and [stopPreview] runs when the screen goes away.
     */
    val preview: StateFlow<GameScreenModel?>
        field = MutableStateFlow<GameScreenModel?>(null)
    private var previewJob: Job? = null

    private val activeId = MutableStateFlow(currentChallenge.id)

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeChallenge = activeId.flatMapLatest {
        repository.challengeDetails(it)
    }.stateIn(
        scope = uiScope,
        started = SharingStarted.Eagerly,
        initialValue = currentChallenge
    )

    val allChallenges: Flow<List<ChallengeSidebarEntry>> = combine(
        activeId, challengesList
    ) { activeId: ChallengeId, allChallenges: List<ChallengeRow> ->
        allChallenges.mapIndexed { _, row ->
            ChallengeSidebarEntry(
                name = row.data.name,
                state = when (activeId == row.data.id) {
                    true -> SidebarEntryState.ACTIVE
                    false -> SidebarEntryState.DONE_AVAILABLE
                },
                isSolved = row.userState.isSolved(),
                alternativeBackground = false,
                challenge = row,
                onClick = { setActiveChallenge(row.data.id) }
            )
        }
    }

    fun toggleFavorite() {
        activeChallenge.value.let { challenge ->
            repositoryScope.launch {
                repository.markAsFavorite(challenge.data.id, !challenge.userState.favorite)
            }
        }
    }

    fun setVote(voted: Boolean) {
        activeChallenge.value.let {challenge ->
            repositoryScope.launch {
                repository.vote(challenge.data.id, voted)
            }
        }
    }

    /**
     * Builds the preview for [challenge], replacing any earlier one if it
     * exists.
     */
    fun loadPreview(menuViewModel: MenuViewModel, challenge: Challenge, density: Density) {
        val previous = previewJob
        stopPreview()
        previewJob = menuViewModel.backgroundContext.launch {
            // Cancelling is asynchronous, so without waiting here the previous
            // game would still be starting up while this one is built. The join
            // runs on the background dispatcher, so nothing blocks the UI.
            previous?.cancelAndJoin()
            var model: GameScreenModel? = null
            try {
                // Only the game state is built off the main thread; that is the
                // part that can hit the disk. Starting the engine has to happen
                // on the main thread like it does for a real game, because the
                // game state holds Compose state that cannot be touched from
                // anywhere else.
                model = ChallengeGameFactory.createPreviewScreenModel(menuViewModel, challenge)
                withContext(Dispatchers.Main) {
                    model.initialize(density, attachToMenu = false)
                }
                ensureActive()
                preview.value = model
                // Published, so the flow owns it now, and `stopPreview` is what
                // releases it. Anything still held here was never handed over.
                model = null
            } finally {
                // A build that gets canceled has still started a real game, and
                // that game keeps its threads for the rest of the session unless
                // it is stopped.
                withContext(NonCancellable) { model?.onDispose() }
            }
        }
    }

    /** Releases the running preview game. Safe to call when there is none. */
    fun stopPreview() {
        preview.value?.onDispose()
        preview.value = null
    }

    /**
     * Releases the preview and abandons a build still in progress. For leaving
     * the page, where [stopPreview] alone would let an unfinished build go on to
     * start a game nothing is left to stop.
     */
    fun releasePreview() {
        previewJob?.cancel()
        previewJob = null
        stopPreview()
    }

    fun playChallenge(navigator: Navigator, menuViewModel: MenuViewModel) {
        val challenge = activeChallenge.value.data
        menuViewModel.navigatorContext.launch {
            ChallengeGameFactory.start(navigator, menuViewModel, challenge)
        }
    }

    fun setActiveChallenge(id: ChallengeId) {
        activeId.value = id
    }
}
