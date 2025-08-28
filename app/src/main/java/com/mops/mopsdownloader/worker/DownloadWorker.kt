package com.mops.mopsdownloader.worker

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mops.mopsdownloader.data.DownloadResult
import com.mops.mopsdownloader.data.MopsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.IOException



object Progress {
    const val KEY = "KEY_PROGRESS"
    const val LOG = "KEY_LOG"
}

data class DownloadTask(
    val coId: String,
    val year: String,
    val season: String?,
    val type: ReportType,
    val fileName: String
)
enum class ReportType {
    FINANCIAL, ANNUAL
}

class DownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repository = MopsRepository()

    override suspend fun doWork(): Result {
        val tasksJson = inputData.getString("tasks") ?: return Result.failure()
        val directoryUriString = inputData.getString("directoryUri") ?: return Result.failure()
        // 【【【新增點 1: 讀取延遲策略】】】
        val delayMin = inputData.getLong("delayMin", 3000L)
        val delayMax = inputData.getLong("delayMax", 5000L)

        val directoryUri = Uri.parse(directoryUriString)
        val taskType = object : TypeToken<List<DownloadTask>>() {}.type
        val tasks: List<DownloadTask> = Gson().fromJson(tasksJson, taskType)

        val totalTasks = tasks.size
        reportLog("準備下載 $totalTasks 個檔案...")
        delay(1000)

        tasks.forEachIndexed { index, task ->
            if (isStopped) {
                reportLog("下載已由使用者取消。")
                reportProgress("已取消")
                return Result.success()
            }

            val progress = "正在下載 (${index + 1}/$totalTasks): ${task.fileName}"
            reportProgress(progress)
            reportLog(progress)

            val downloadResult = if (task.type == ReportType.FINANCIAL) {
                repository.downloadFinancialReport(task.coId, task.year, task.season!!)
            } else {
                repository.downloadAnnualReport(task.coId, task.year)
            }

            when (downloadResult) {
                is DownloadResult.Success -> {
                    val isSaved = savePdfToFile(directoryUri, task.fileName, downloadResult.body)
                    if (isSaved) {
                        reportLog("✓ ${task.fileName} 儲存成功!")
                    } else {
                        reportLog("✗ ${task.fileName} 儲存失敗 (無法建立檔案)。")
                    }
                }
                is DownloadResult.NotFound -> reportLog("✗ ${task.fileName} - ${downloadResult.message}")
                is DownloadResult.Error -> reportLog("✗ ${task.fileName} - ${downloadResult.message}")
            }

            // 【【【新增點 2: 實現動態延遲】】】
            // 如果不是最後一個任務，就執行延遲
            if (index < tasks.size - 1) {
                val randomDelay = (delayMin..delayMax).random()
                val delayInSeconds = randomDelay / 1000.0
                reportLog("...等待 ${String.format("%.1f", delayInSeconds)} 秒後繼續...")
                delay(randomDelay)
            }
        }

        reportProgress("下載完成")
        reportLog("全部下載任務完成！")
        return Result.success()
    }

    private suspend fun reportProgress(progress: String) {
        setProgress(workDataOf(Progress.KEY to progress))
    }

    private suspend fun reportLog(log: String) {
        setProgress(workDataOf(Progress.LOG to log))
    }

    private suspend fun savePdfToFile(directoryUri: Uri, fileName: String, body: ResponseBody): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val dir = DocumentFile.fromTreeUri(context, directoryUri)
                dir?.findFile(fileName)?.delete()
                val newFile = dir?.createFile("application/pdf", fileName)

                if (newFile != null) {
                    context.contentResolver.openOutputStream(newFile.uri)?.use { outputStream ->
                        body.byteStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    true
                } else {
                    reportLog("錯誤: 無法在指定資料夾建立檔案，請檢查權限。")
                    false
                }
            } catch (e: Exception) {
                Log.e("DownloadWorker", "Failed to save file", e)
                reportLog("錯誤: 儲存檔案時發生異常 - ${e.message}")
                false
            } finally {
                body.close()
            }
        }
    }
}