package com.mops.mopsdownloader.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkInfo
import com.mops.mopsdownloader.ui.theme.MopsDownloaderTheme
import com.mops.mopsdownloader.worker.Progress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions

// MainScreen 和所有 UI Composable 函式都完全不變
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val workInfo by viewModel.workInfo.observeAsState()

    LaunchedEffect(workInfo) {
        val progressData = workInfo?.progress
        if (progressData != null) {
            progressData.getString(Progress.LOG)?.let { log ->
                if (uiState.logMessages.lastOrNull() != log) {
                    viewModel.addLog(log)
                }
            }
            progressData.getString(Progress.KEY)?.let { progress ->
                viewModel.updateProgress(progress)
            }
        }
        if (workInfo?.state?.isFinished == true) {
            viewModel.onDownloadingStateChange(false)
        }
    }

    MainScreenContent(
        uiState = uiState,
        navController = navController,
        onStockCodeChange = viewModel::onStockCodeChange,
        onSaveDirectory = viewModel::saveDirectoryUri,
        onFinancialCheckedChange = viewModel::onFinancialCheckedChange,
        onAnnualCheckedChange = viewModel::onAnnualCheckedChange,
        onToggleFinancialSelection = viewModel::toggleFinancialSelection,
        onToggleAnnualSelection = viewModel::toggleAnnualSelection,
        onStartDownload = viewModel::startDownload,
        onShowCancelDialog = { viewModel.onShowCancelDialog(true) },
        onClearLogs = viewModel::clearLogs,
        onCancelDownloads = viewModel::cancelDownloads,
        onDismissCancelDialog = { viewModel.onShowCancelDialog(false) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    uiState: MainUiState,
    navController: NavController,
    onStockCodeChange: (String) -> Unit,
    onSaveDirectory: (Uri) -> Unit,
    onFinancialCheckedChange: (Boolean) -> Unit,
    onAnnualCheckedChange: (Boolean) -> Unit,
    onToggleFinancialSelection: (String, Int) -> Unit,
    onToggleAnnualSelection: (String) -> Unit,
    onStartDownload: () -> Unit,
    onShowCancelDialog: () -> Unit,
    onClearLogs: () -> Unit,
    onCancelDownloads: () -> Unit,
    onDismissCancelDialog: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("MOPS 批次下載工具")
                        Text(text = "by Yen", style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = {
                    var menuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多選項")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("使用說明") },
                            onClick = {
                                menuExpanded = false
                                navController.navigate("instructions")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("版本紀錄") },
                            onClick = {
                                menuExpanded = false
                                navController.navigate("versions")
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            GeneralSettings(
                uiState = uiState,
                onStockCodeChange = onStockCodeChange,
                onSaveDirectory = onSaveDirectory
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FinancialReportSettings(
                    modifier = Modifier.weight(1f),
                    isChecked = uiState.isFinancialChecked,
                    onCheckedChange = onFinancialCheckedChange,
                    selections = uiState.financialReportSelections,
                    onSelectionChange = onToggleFinancialSelection
                )
                AnnualReportSettings(
                    modifier = Modifier.weight(1f),
                    isChecked = uiState.isAnnualChecked,
                    onCheckedChange = onAnnualCheckedChange,
                    selections = uiState.annualReportSelections,
                    onSelectionChange = onToggleAnnualSelection
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            ExecutionControls(
                isDownloading = uiState.isDownloading,
                onStart = onStartDownload,
                onCancel = onShowCancelDialog
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.isDownloading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    uiState.currentProgress,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LogPanel(
                modifier = Modifier.weight(1f),
                logMessages = uiState.logMessages,
                onClearLogs = onClearLogs
            )
            if (uiState.showCancelConfirmDialog) {
                CancelConfirmDialog(
                    onConfirm = onCancelDownloads,
                    onDismiss = onDismissCancelDialog
                )
            }
        }
    }
}

@Composable
fun GeneralSettings(
    uiState: MainUiState,
    onStockCodeChange: (String) -> Unit,
    onSaveDirectory: (Uri) -> Unit
) {
    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri -> uri?.let { onSaveDirectory(it) } }
    )
    Column {
        OutlinedTextField(
            value = uiState.stockCode,
            onValueChange = onStockCodeChange,
            label = { Text("股票代號 (單一)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("儲存位置: ", fontWeight = FontWeight.Bold)
            val path = uiState.directoryUri?.let { uriString ->
                val context = LocalContext.current
                try {
                    val uri = Uri.parse(uriString)
                    if (uri.scheme == "content") {
                        DocumentFile.fromTreeUri(context, uri)?.name ?: uri.path
                    } else {
                        uri.path
                    }
                } catch (e: Exception) {
                    uriString
                }
            } ?: "尚未選擇"
            Text(
                path ?: "N/A",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { directoryPickerLauncher.launch(null) }) {
                Text(if (uiState.directoryUri == null) "選擇" else "更改")
            }
        }
    }
}

@Composable
fun FinancialReportSettings(
    modifier: Modifier,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    selections: Map<String, Set<Int>>,
    onSelectionChange: (String, Int) -> Unit
) {
    val years = (LocalDate.now().year downTo LocalDate.now().year - 9).map { it.toString() }
    Column(modifier = modifier.fillMaxHeight()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(checked = isChecked, onCheckedChange = onCheckedChange)
            Text("📈 財務報表", style = MaterialTheme.typography.titleMedium)
        }
        Divider()
        LazyColumn {
            items(years) { year ->
                YearItemWithQuarters(
                    year = year,
                    selectedQuarters = selections[year] ?: emptySet(),
                    onQuarterSelected = { season -> onSelectionChange(year, season) },
                    enabled = isChecked
                )
            }
        }
    }
}

@Composable
fun YearItemWithQuarters(
    year: String,
    selectedQuarters: Set<Int>,
    onQuarterSelected: (Int) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = { expanded = !expanded }
            )) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val textColor = if (enabled) LocalContentColor.current else Color.Gray
                Text(
                    year,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = textColor
                )
            }
            AnimatedVisibility(visible = expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    (1..4).forEach { quarter ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val textColor = if (enabled) LocalContentColor.current else Color.Gray
                            Text("Q$quarter", fontSize = 14.sp, color = textColor)
                            Checkbox(
                                checked = quarter in selectedQuarters,
                                onCheckedChange = { onQuarterSelected(quarter) },
                                enabled = enabled
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnnualReportSettings(
    modifier: Modifier,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    selections: Map<String, Boolean>,
    onSelectionChange: (String) -> Unit
) {
    val years = (LocalDate.now().year - 1 downTo LocalDate.now().year - 10).map { it.toString() }
    Column(modifier = modifier.fillMaxHeight()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(checked = isChecked, onCheckedChange = onCheckedChange)
            Text("📋 股東會年報", style = MaterialTheme.typography.titleMedium)
        }
        Divider()
        LazyColumn {
            items(years) { year ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth()
                        .clickable(
                            enabled = isChecked,
                            onClick = { onSelectionChange(year) }
                        )) {
                    Checkbox(
                        checked = selections[year] ?: false,
                        onCheckedChange = { _ -> onSelectionChange(year) },
                        enabled = isChecked
                    )
                    Text(year, color = if (isChecked) LocalContentColor.current else Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ExecutionControls(isDownloading: Boolean, onStart: () -> Unit, onCancel: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        if (isDownloading) {
            Button(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text("取消下載")
            }
        } else {
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text("開始下載")
            }
        }
    }
}

@Composable
fun LogPanel(modifier: Modifier, logMessages: List<String>, onClearLogs: () -> Unit) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(logMessages.size) {
        if (logMessages.isNotEmpty()) {
            coroutineScope.launch { listState.animateScrollToItem(logMessages.size - 1) }
        }
    }
    Column(modifier = modifier.padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("下載日誌", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onClearLogs) { Text("清除日誌") }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            items(logMessages) { message ->
                Text(message, modifier = Modifier.padding(vertical = 2.dp), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun CancelConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("確認取消？") },
        text = { Text("目前尚有未完成的下載任務，您確定要全部取消嗎？") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("確定取消")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("繼續下載")
            }
        }
    )
}

@Preview(showBackground = true, name = "Main Screen Preview")
@Composable
fun MainScreenPreview() {
    MopsDownloaderTheme {
        val fakeUiState = MainUiState(
            stockCode = "2330",
            directoryUri = "/storage/emulated/0/Download/MyReports",
            logMessages = listOf("✓ 預覽模式已就緒", "提示：請勾選要下載的項目"),
            financialReportSelections = mapOf("2025" to setOf(1, 2)),
            annualReportSelections = mapOf("2024" to true)
        )
        MainScreenContent(
            uiState = fakeUiState,
            navController = rememberNavController(),
            onStockCodeChange = {}, onSaveDirectory = {}, onFinancialCheckedChange = {},
            onAnnualCheckedChange = {}, onToggleFinancialSelection = { _, _ -> },
            onToggleAnnualSelection = {}, onStartDownload = {}, onShowCancelDialog = {},
            onClearLogs = {}, onCancelDownloads = {}, onDismissCancelDialog = {}
        )
    }
}