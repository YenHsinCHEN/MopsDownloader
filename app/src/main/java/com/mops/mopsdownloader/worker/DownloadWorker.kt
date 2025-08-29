package com.mops.mopsdownloader.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mops.mopsdownloader.R
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

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "MopsDownloaderChannel"
        const val NOTIFICATION_ID = 1
    }

    override suspend fun doWork(): Result {
        val tasksJson = inputData.getString("tasks") ?: return Result.failure()
        val directoryUriString = inputData.getString("directoryUri") ?: return Result.failure()
        val delayMin = inputData.getLong("delayMin", 3000L)
        val delayMax = inputData.getLong("delayMax", 5000L)

        val directoryUri = Uri.parse(directoryUriString)
        val taskType = object : TypeToken<List<DownloadTask>>() {}.type
        val tasks: List<DownloadTask> = Gson().fromJson(tasksJson, taskType)

        val totalTasks = tasks.size

        val initialProgress = "準備下載 $totalTasks 個檔案..."
        setForeground(createForegroundInfo(initialProgress))
        reportLog(initialProgress)
        delay(1000)

        tasks.forEachIndexed { index, task ->
            if (isStopped) {
                reportLog("下載已由使用者取消。")
                reportProgress("已取消")
                notificationManager.cancel(NOTIFICATION_ID)
                return Result.success()
            }

            val progress = "正在下載 (${index + 1}/$totalTasks): ${task.fileName}"

            setForeground(createForegroundInfo(progress))
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

            if (index < tasks.size - 1) {
                val randomDelay = (delayMin..delayMax).random()
                val delayInSeconds = randomDelay / 1000.0
                val waitMessage = "...等待 ${String.format("%.1f", delayInSeconds)} 秒後繼續..."

                setForeground(createForegroundInfo(waitMessage))
                reportLog(waitMessage)
                delay(randomDelay)
            }
        }

        notificationManager.cancel(NOTIFICATION_ID)
        reportProgress("下載完成")
        reportLog("全部下載任務完成！")
        return Result.success()
    }

    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val channelId = NOTIFICATION_CHANNEL_ID
        val title = "MOPS 下載任務進行中"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "下載通知",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(progress)
            .setTicker(progress)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        // 【【【【【 以下是唯一的、最關鍵的修改點 】】】】】
        // 根據 Android 版本，使用不同的 ForegroundInfo 建構函式
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 對於 Android 10 (API 29) 及以上版本，我們必須在 ForegroundInfo 中明確指定服務類型
            // 這正是解決 InvalidForegroundServiceTypeException 崩潰問題的核心
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            // 對於較舊版本，使用不含服務類型的舊版建構函式
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
        // 【【【【【 修改結束 】】】】】
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