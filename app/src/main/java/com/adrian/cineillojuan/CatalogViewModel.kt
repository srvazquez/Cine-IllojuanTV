package com.adrian.cineillojuan

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.adrian.cineillojuan.data.Catalog
import com.adrian.cineillojuan.data.CatalogSource
import com.adrian.cineillojuan.data.Library
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface UiState {
    data object Loading : UiState
    data class Ready(val catalog: Catalog) : UiState
    data class Error(val message: String) : UiState
}

class CatalogViewModel(app: Application) : AndroidViewModel(app) {

    private val source = CatalogSource(app)
    private val library = Library(app)

    var state by mutableStateOf<UiState>(UiState.Loading)
        private set
    var refreshing by mutableStateOf(false)
        private set
    var refreshError by mutableStateOf<String?>(null)
        private set

    var seen by mutableStateOf(library.seen())
        private set
    var favorites by mutableStateOf(library.favorites())
        private set
    var hiddenCategories by mutableStateOf(library.hiddenCategories())
        private set
    /** URL → cuándo apareció en la web. */
    var newVideos by mutableStateOf(library.newVideos())
        private set

    init {
        // Primero lo que haya guardado (instantáneo) y luego se actualiza desde la web.
        viewModelScope.launch {
            runCatching { source.loadOffline() }.onSuccess { show(it) }
            refresh()
        }
    }

    fun refresh() {
        if (refreshing) return
        viewModelScope.launch {
            refreshing = true
            runCatching { source.fetchFromWeb() }
                .onSuccess {
                    show(it)
                    refreshError = null
                }
                .onFailure { e ->
                    val msg = e.message ?: e.javaClass.simpleName
                    if (state is UiState.Ready) refreshError = msg else state = UiState.Error(msg)
                }
            refreshing = false
        }
    }

    fun markSeen(url: String) {
        seen = library.markSeen(url)
    }

    /** Devuelve true si el vídeo ha quedado en Mi lista. */
    fun toggleFavorite(url: String): Boolean {
        favorites = library.toggleFavorite(url)
        return url in favorites
    }

    fun setCategoryHidden(name: String, hidden: Boolean) {
        hiddenCategories = library.setCategoryHidden(name, hidden)
    }

    private suspend fun show(catalog: Catalog) {
        newVideos = withContext(Dispatchers.IO) { library.registerCatalog(catalog.categories) }
        state = UiState.Ready(catalog)
    }
}
