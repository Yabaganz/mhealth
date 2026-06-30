import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * LABEL: UI ViewState Class
 * Represents the clean state of the Dashboard UI.
 * This guarantees that the screen only reacts to defined state changes.
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val healthHistory: List<SymptomLogEntity> = emptyList(),
    val errorMessage: String? = null
)

/**
 * LABEL: Presentation Layer (ViewModel)
 * Manages the state communication between the SymptomRepository and Screen 1.
 */
class DashboardViewModel(private val repository: SymptomRepository) : ViewModel() {

    // Internal mutable state flow that handles system updates privately
    private val _uiState = MutableStateFlow(DashboardUiState())

    // Publicly exposed immutable state flow that the UI Composable listens to safely
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Automatically fetch the offline data entries as soon as the screen initializes
        loadSymptomHistory()
    }

    /**
     * Pulls persistent symptom logs from the localized database.
     * Uses viewModelScope so that if the user exits the screen,
     * the coroutine database operation cancels cleanly to prevent memory leaks.
     */
    fun loadSymptomHistory() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Directly fetch via the repository abstraction layer
                val history = repository.getHealthHistory()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    healthHistory = history,
                    errorMessage = null
                )
            } catch (e: Exception) {
                // Graceful error handling for data retrieval exceptions
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load local health records: ${e.localizedMessage}"
                )
            }
        }
    }
}