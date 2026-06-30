import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * LABEL: Form UI State
 * Holds the reactive parameters of the user input fields before database commitment.
 */
data class LoggingUiState(
    val selectedSymptom: String? = null,
    val severityLevel: Int = 1,
    val isSavingSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * LABEL: Input Form ViewModel
 * Dispatches form entries directly into the underlying repository thread framework.
 */
class LoggingViewModel(private val repository: SymptomRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoggingUiState())
    val uiState: StateFlow<LoggingUiState> = _uiState.asStateFlow()

    fun selectSymptom(symptom: String) {
        _uiState.value = _uiState.value.copy(selectedSymptom = symptom)
    }

    fun updateSeverity(severity: Int) {
        _uiState.value = _uiState.value.copy(severityLevel = severity)
    }

    /**
     * Executes fully offline. Converts form parameters into a persistent
     * database entity and requests immediate storage write-out.
     */
    fun saveSymptomLog() {
        val currentSymptom = _uiState.value.selectedSymptom
        val currentSeverity = _uiState.value.severityLevel

        if (currentSymptom == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please select a symptom icon first.")
            return
        }

        viewModelScope.launch {
            try {
                // Instantiate the structural database schema class
                val newLog = SymptomLogEntity(
                    timestamp = System.currentTimeMillis(), // Record current millisecond snapshot
                    symptomName = currentSymptom,
                    severityLevel = currentSeverity,
                    bmiSnapshot = 22.4, // Example hardcoded value; baseline metrics computed on-device
                    isSynced = false    // Flag explicitly marks it for downstream background sync pipelines
                )

                // Dispatch to Local Storage
                repository.addSymptomLog(newLog)

                // Alert UI layer to fire the immediate completion dialog
                _uiState.value = _uiState.value.copy(isSavingSuccess = true, errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Database write failure: ${e.localizedMessage}")
            }
        }
    }

    fun resetForm() {
        _uiState.value = LoggingUiState()
    }
}