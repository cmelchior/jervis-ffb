package com.jervisffb.ui.menu.challenges

import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.engine.challenge.ChallengeCategory
import com.jervisffb.engine.model.ChallengeId
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.JervisScreenModel
import com.jervisffb.ui.menu.challenges.data.ChallengeDetails
import com.jervisffb.ui.menu.challenges.data.ChallengeRow
import com.jervisffb.ui.menu.challenges.data.ChallengeUserState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChallengesListScreenModel(private val menuViewModel: MenuViewModel) : JervisScreenModel {

    private val repository = menuViewModel.challengesRepository

    init {
        // Loading reads the challenge positions off the disk. It should be
        // cached, so only the first hit is expensive.
        menuViewModel.backgroundContext.launch {
            repository.initialize()
        }
    }

    val activeCategories = MutableStateFlow(ChallengeCategory.entries.toSet())
    val hideSolved = MutableStateFlow(false)
    val showOnlyFavorites = MutableStateFlow(false)

    /** The filtered list of challenges, kept in sync with the filters and the shared [ChallengeRepository]. */
    val visibleChallenges: StateFlow<List<ChallengeRow>> =
        combine(activeCategories, hideSolved, showOnlyFavorites, repository.challengesList) { categories, hideSolved, showOnlyFavorites, challenges ->
            challenges
                .asSequence()
                .filter { it.data.category in categories }
                .filter { !(hideSolved && it.userState.solved != ChallengeUserState.SolvedState.UNSOLVED) }
                .filter { !showOnlyFavorites || it.userState.favorite }
                .sortedByDescending { it.votes }
                .toList()
        }.stateIn(screenModelScope, SharingStarted.Eagerly, emptyList())

    fun toggleCategory(category: ChallengeCategory) {
        val updated = activeCategories.value.toMutableSet()
        if (!updated.add(category)) updated.remove(category)
        activeCategories.value = updated
    }

    fun setHideSolved(value: Boolean) {
        hideSolved.value = value
    }

    fun setShowOnlyFavorites(value: Boolean) {
        showOnlyFavorites.value = value
    }

    fun toggleFavorite(id: ChallengeId, marked: Boolean) {
        menuViewModel.backgroundContext.launch {
            repository.markAsFavorite(id, marked)
        }
    }

    fun openChallenge(navigator: Navigator, challenge: ChallengeRow) {
        menuViewModel.navigatorContext.launch {
            val viewModel = ChallengeDetailScreenModel(
                currentChallenge = ChallengeDetails(
                    data = challenge.data,
                    userState = challenge.userState,
                    votes = challenge.votes,
                    scoreboard = emptyList()
                ),
                challengesList = visibleChallenges,
                repository = repository,
                uiScope = menuViewModel.uiScope,
                repositoryScope = menuViewModel.backgroundContext
            )
            navigator.push(ChallengeDetailScreen(menuViewModel, viewModel))
        }
    }
}
