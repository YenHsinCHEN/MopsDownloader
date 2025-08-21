package com.mops.mopsdownloader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost // 【【【新增 import】】】
import androidx.navigation.compose.composable // 【【【新增 import】】】
import androidx.navigation.compose.rememberNavController // 【【【新增 import】】】
import com.mops.mopsdownloader.ui.InstructionScreen // 【【【新增 import】】】
import com.mops.mopsdownloader.ui.MainScreen
import com.mops.mopsdownloader.ui.VersionScreen // 【【【新增 import】】】
import com.mops.mopsdownloader.ui.theme.MopsDownloaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MopsDownloaderTheme {
                // 【【【【【 以下是修改的核心 】】】】】
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "main") {
                    // 主畫面
                    composable("main") {
                        MainScreen(navController = navController)
                    }
                    // 使用說明頁面
                    composable("instructions") {
                        InstructionScreen(navController = navController)
                    }
                    // 版本紀錄頁面
                    composable("versions") {
                        VersionScreen(navController = navController)
                    }
                }
            }
        }
    }
}