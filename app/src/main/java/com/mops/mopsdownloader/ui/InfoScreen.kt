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
                    1. (首次使用) 點擊「選擇」按鈕，授權一個儲存資料夾。此設定將會被記住。
                    2. 在頂部輸入框輸入單一股票代號。
                    3. 在下方左右兩個區塊，勾選您需要下載的財報季度與年報年份。
                    4. 點擊最下方的「開始下載」按鈕，啟動批次下載任務。
                """.trimIndent()
            )
            InfoText(
                title = "【批次下載】",
                content = """
                    - 財務報表區：點擊年份卡片可展開/收合該年份的季度選項。
                    - 年報區：直接勾選所需年份即可。
                    - 財報與年報的選擇完全獨立，互不干擾。
                """.trimIndent()
            )
            InfoText(
                title = "【智慧下載策略】",
                content = """
                    為保護網站伺服器及避免 IP 被封鎖，App 內建智慧下載策略：
                    - 任務數 1-5: 快速模式 (1-2秒/個)
                    - 任務數 6-15: 常規模式 (3-5秒/個)
                    - 任務數 16-25: 慢速模式 (4-7秒/個)
                    - 任務數 > 25: 禁止下載，並提示使用者減少項目。
                """.trimIndent()
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
            InfoText(
                title = "v2.0.0 (批次處理與 UI 升級)",
                content = """
                    - **[新功能]** 全新批次處理介面：
                        - 財報與年報可獨立選擇不同年份。
                        - 財報年份可展開選擇季度。
                    - **[新功能]** 智慧下載策略：
                        - 根據任務數量自動調整下載延遲 (1-7秒)。
                        - 新增單次 25 個檔案的下載上限。
                    - **[新功能]** 任務排序：下載佇列會自動依序排列。
                    - **[新功能]** 儲存位置記憶：App 會記住使用者選擇的資料夾。
                    - **[新功能]** 新增「使用說明」與「版本紀錄」頁面。
                    - **[優化]** UI 頂部標題列加入作者署名。
                    - **[優化]** 鍵盤最佳化：輸入股票代號時自動切換為數字鍵盤。
                """.trimIndent()
            )
            InfoText(
                title = "v1.0.0 (初始版本)",
                content = """
                    - 實現財報與年報的單檔案下載核心功能。
                    - 採用 WorkManager 實現可靠的背景下載。
                    - 支援取消下載與基本的 UI 介面。
                    - 專案從 Python 桌面應用成功移植到 Android 原生平台。
                """.trimIndent()
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