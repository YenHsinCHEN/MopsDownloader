package com.mops.mopsdownloader.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.gson.Gson
import com.mops.mopsdownloader.BuildConfig // <--- 請確保這一行是正確的
import com.mops.mopsdownloader.data.SettingsManager
import com.mops.mopsdownloader.worker.DownloadTask
import com.mops.mopsdownloader.worker.DownloadWorker
import com.mops.mopsdownloader.worker.Progress
import com.mops.mopsdownloader.worker.ReportType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

data class MainUiState(
    val stockCode: String = "",
    val isFinancialChecked: Boolean = true,
    val isAnnualChecked: Boolean = true,
    val financialReportSelections: Map<String, Set<Int>> = emptyMap(),
    val annualReportSelections: Map<String, Boolean> = emptyMap(),
    val directoryUri: String? = null,
    val isDownloading: Boolean = false,
    val logMessages: List<String> = emptyList(),
    val currentProgress: String = "準備就緒",
    val showCancelConfirmDialog: Boolean = false,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)
    private val settingsManager = SettingsManager(application)

    private val workId = MutableLiveData<UUID>()
    val workInfo: LiveData<WorkInfo> = workId.switchMap { id ->
        workManager.getWorkInfoByIdLiveData(id)
    }

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        settingsManager.directoryUriFlow
            .onEach { uri ->
                _uiState.update { it.copy(directoryUri = uri) }
            }
            .launchIn(viewModelScope)
    }

    fun onStockCodeChange(newCode: String) { _uiState.update { it.copy(stockCode = newCode) } }
    fun onFinancialCheckedChange(isChecked: Boolean) { _uiState.update { it.copy(isFinancialChecked = isChecked) } }
    fun onAnnualCheckedChange(isChecked: Boolean) { _uiState.update { it.copy(isAnnualChecked = isChecked) } }
    fun onShowCancelDialog(show: Boolean) { _uiState.update { it.copy(showCancelConfirmDialog = show) } }
    fun onDownloadingStateChange(isDownloading: Boolean) { _uiState.update { it.copy(isDownloading = isDownloading) } }

    fun toggleFinancialSelection(year: String, season: Int) {
        _uiState.update { currentState ->
            val newSelections: MutableMap<String, Set<Int>> = currentState.financialReportSelections.toMutableMap()
            val currentSeasons: MutableSet<Int> = if (newSelections.containsKey(year)) {
                newSelections.getValue(year).toMutableSet()
            } else {
                mutableSetOf()
            }
            if (season in currentSeasons) { currentSeasons.remove(season) } else { currentSeasons.add(season) }
            if (currentSeasons.isEmpty()) { newSelections.remove(year) } else { newSelections[year] = currentSeasons }
            currentState.copy(financialReportSelections = newSelections)
        }
    }

    fun toggleAnnualSelection(year: String) {
        _uiState.update { currentState ->
            val newSelections: MutableMap<String, Boolean> = currentState.annualReportSelections.toMutableMap()
            val isSelected = newSelections.getOrPut(year) { false }
            newSelections[year] = !isSelected
            currentState.copy(annualReportSelections = newSelections)
        }
    }

    fun saveDirectoryUri(uri: Uri) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            context.contentResolver.takePersistableUriPermission(uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            settingsManager.saveDirectoryUri(uri.toString())
        }
    }

    fun startDownload() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.directoryUri == null) { addLog("錯誤：請先選擇儲存資料夾"); return@launch }
            if (currentState.stockCode.isBlank()) { addLog("錯誤：請輸入股票代號"); return@launch }

            val tasks = buildTasks(currentState)
            if (tasks.isEmpty()) { addLog("提示：沒有選擇任何要下載的檔案"); return@launch }

            val totalTasks = tasks.size

            val downloadLimit = BuildConfig.MAX_DOWNLOAD_LIMIT

            if (totalTasks > downloadLimit) {
                addLog("錯誤：單次下載上限為 $downloadLimit 個檔案，您已選擇 $totalTasks 個。")
                addLog("請減少勾選項目後再試。")
                return@launch
            }

            val (delayMin, delayMax) = getDelayStrategy(totalTasks)
            addLogFromStrategy(totalTasks, delayMin, delayMax)

            tasks.sortWith(
                compareBy<DownloadTask> { it.type }
                    .thenByDescending { it.year.toInt() }
                    .thenBy { it.season?.toIntOrNull() ?: 5 }
            )

            val tasksJson = Gson().toJson(tasks)
            val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(workDataOf(
                    "tasks" to tasksJson,
                    "directoryUri" to currentState.directoryUri,
                    "delayMin" to delayMin,
                    "delayMax" to delayMax
                ))
                .build()
            workManager.enqueueUniqueWork("mopsDownload", ExistingWorkPolicy.REPLACE, workRequest)
            workId.value = workRequest.id
            _uiState.update { it.copy(isDownloading = true, logMessages = emptyList(), currentProgress = "已提交任務...") }
        }
    }

    fun cancelDownloads() {
        workId.value?.let {
            workManager.cancelWorkById(it)
        }
        onShowCancelDialog(false)
        _uiState.update { it.copy(isDownloading = false, currentProgress = "已取消") }
    }

    fun updateProgress(progress: String) { _uiState.update { it.copy(currentProgress = progress) } }
    fun addLog(log: String) { _uiState.update { currentState ->
        currentState.copy(logMessages = currentState.logMessages + log)
    }}
    fun clearLogs() {
        if (!_uiState.value.isDownloading) {
            _uiState.update { it.copy(logMessages = emptyList(), currentProgress = "準備就緒") }
        }
    }

    private fun buildTasks(currentState: MainUiState): MutableList<DownloadTask> {
        val tasks = mutableListOf<DownloadTask>()
        if (currentState.isFinancialChecked) {
            currentState.financialReportSelections.forEach { (year, seasons) ->
                seasons.forEach { season ->
                    tasks.add(DownloadTask(coId = currentState.stockCode, year = year, season = season.toString(), type = ReportType.FINANCIAL, fileName = "${currentState.stockCode}_${year}_Q${season}_財報.pdf"))
                }
            }
        }
        if (currentState.isAnnualChecked) {
            currentState.annualReportSelections.forEach { (year, isSelected) ->
                if (isSelected) {
                    tasks.add(DownloadTask(coId = currentState.stockCode, year = year, season = null, type = ReportType.ANNUAL, fileName = "${currentState.stockCode}_${year}_年報.pdf"))
                }
            }
        }
        return tasks
    }

    private fun getDelayStrategy(totalTasks: Int): Pair<Long, Long> {
        return when {
            totalTasks in 1..5 -> 1000L to 2000L
            totalTasks in 6..15 -> 3000L to 5000L
            else -> 4000L to 7000L
        }
    }

    private fun addLogFromStrategy(totalTasks: Int, delayMin: Long, delayMax: Long) {
        val min = delayMin / 1000
        val max = delayMax / 1000
        val mode = when {
            totalTasks in 1..5 -> "快速"
            totalTasks in 6..15 -> "常規"
            else -> "慢速"
        }
        addLog("提示：啟用${mode}下載模式 (${min}-${max}秒/個)。")
    }
}