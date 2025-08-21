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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

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
                    1. (首次) 選擇儲存資料夾。
                    2. 輸入股票代號。
                    3. 在左右兩側勾選要下載的財報/年報項目。
                    4. 點擊「開始下載」按鈕。
                """.trimIndent()
            )
            InfoText(
                title = "【批次下載】",
                content = """
                    - 財報區：點擊年份可展開/收合季度選項。
                    - 年報區：直接勾選所需年份。
                    - 兩區選擇完全獨立，互不干擾。
                """.trimIndent()
            )
            InfoText(
                title = "【下載限制與策略】",
                content = """
                    - 單次下載上限為 30 個檔案。
                    - 任務數 > 16 個時，自動啟用慢速模式 (5-7秒/個)。
                    - 任務數 <= 16 個時，使用常規模式 (3-5秒/個)。
                """.trimIndent()
            )
            InfoText(
                title = "【背景下載】",
                content = "下載任務在背景執行，您可以切換到其他 App，下載不會中斷。"
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
            InfoText(
                title = "v1.0.0 (初始版本)",
                content = """
                    - 實現財報與年報下載核心功能。
                    - 採用 WorkManager 實現可靠的背景序列下載。
                    - 支援批次選擇、下載限制與動態延遲策略。
                    - 支援取消下載與儲存位置記憶功能。
                """.trimIndent()
            )
            // 未來可以在這裡增加更多版本資訊
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