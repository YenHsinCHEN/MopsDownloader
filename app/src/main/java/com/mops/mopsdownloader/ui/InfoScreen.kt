package com.mops.mopsdownloader.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mops.mopsdownloader.R

// 使用說明的頁面
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("使用說明") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // 讓內容可以滾動
        ) {
            InfoText(
                title = "【基本操作】",
                content = """
                    1. (首次使用) 點擊「選擇」按鈕，授權一個儲存資料夾。此設定將會被記住。
                    2. 在頂部輸入框輸入單一股票代號。
                    3. 在下方左右兩個區塊，勾選您需要下載的財報季度與年報年份。
                    4. 點擊最下方的「開始下載」按鈕，啟動批次下載任務。
                """.trimIndent()
            )
            InfoText(
                title = "【批次下載】",
                content = """
                    - 財報區：點擊年份卡片可展開/收合該年份的季度選項。
                    - 年報區：直接勾選所需年份即可。
                    - 財報與年報的選擇完全獨立，互不干擾。
                """.trimIndent()
            )

            // 文字內容現在從 strings.xml 資源檔中讀取
            InfoText(
                title = stringResource(id = R.string.instruction_strategy_title),
                content = stringResource(id = R.string.instruction_strategy_content)
            )

            InfoText(
                title = "【任務控制與背景執行】",
                content = """
                    - 下載任務將依照「財報->年報」、「年份新->舊」的順序執行。
                    - 點擊「取消下載」並確認後，可隨時中止下載序列。
                    - 所有下載都在背景執行，您可以自由切換到其他 App，下載不會中斷。
                """.trimIndent()
            )
        }
    }
}

// 版本紀錄的頁面
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("版本紀錄") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 版本紀錄的文字也從 strings.xml 讀取
            InfoText(
                title = stringResource(id = R.string.version_history_v2_title),
                content = stringResource(id = R.string.version_history_v2_content)
            )
            InfoText(
                title = stringResource(id = R.string.version_history_v1_title),
                content = stringResource(id = R.string.version_history_v1_content)
            )
        }
    }
}

// 一個輔助 Composable，用於顯示標題和內容
@Composable
private fun InfoText(title: String, content: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
    Text(
        text = content,
        style = MaterialTheme.typography.bodyLarge
    )
}