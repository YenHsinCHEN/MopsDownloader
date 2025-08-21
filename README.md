# MOPS 批次下載工具 (Android App)

這是一個基於 Kotlin 和 Jetpack Compose 開發的原生 Android 應用程式，旨在提供一個現代化、易於使用的介面，用於批次下載台灣公開資訊觀測站 (MOPS) 的財務報表與股東會年報。

此專案的靈感來源於一個功能強大的 Python 桌面應用程式，並旨在將其核心功能移植到行動裝置上，同時利用 Android 平台的優勢提供更佳的使用者體驗。

![應用程式截圖](https://raw.githubusercontent.com/Yen-learn-java/MopsDownloader/master/art/screenshot.png)
_（請替換為您自己的應用程式截圖 URL）_

---

## ✨ 核心功能

*   **獨立的報表選擇**: 財務報表與股東會年報擁有完全獨立的年份選擇區塊，解決了兩者發布時間不同的問題。
*   **批次任務處理**: 使用者可以一次性勾選多個年份、多個季度的報表，並透過單一按鈕啟動全部下載任務。
*   **智慧下載策略**:
    *   **下載限制**: 單次任務上限為 30 個檔案，避免對伺服器造成過大壓力。
    *   **動態延遲**: 當任務數量超過 16 個時，自動切換至 5-7 秒的慢速下載模式；否則使用 3-5 秒的常規模式。
    *   **有序執行**: 所有下載任務會自動排序，確保按「財報 -> 年報」、「年份新 -> 舊」、「季度小 -> 大」的順序執行。
*   **可靠的背景下載**:
    *   整合 **Android WorkManager**，所有下載任務都在背景執行。
    *   即使 App 切換到背景或被系統回收，下載任務依然能保證完成。
*   **友善的使用者體驗**:
    *   **持久化儲存位置**: 只需設定一次儲存資料夾，App 就會記住您的選擇。
    *   **完整的任務控制**: 提供「取消下載」功能，並有確認對話框防止誤觸。
    *   **即時進度與日誌**: 在介面上即時顯示下載進度條和詳細的執行日誌。
*   **多頁面導航**: 包含「使用說明」和「版本紀錄」頁面，提供完整的 App 資訊。

---

## 🛠️ 技術棧

本專案採用了 Google 推薦的現代 Android 開發技術棧 (Modern Android Development, MAD)：

*   **語言**: [Kotlin](https://kotlinlang.org/)
*   **UI 框架**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
*   **架構**: MVVM (Model-View-ViewModel)
*   **非同步處理**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
*   **背景任務**: [Android WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
*   **頁面導航**: [Navigation for Compose](https://developer.android.com/jetpack/compose/navigation)
*   **資料持久化**: [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (用於儲存設定)
*   **網路請求**: [Retrofit](https://square.github.io/retrofit/)
*   **HTML 解析**: [Jsoup](https://jsoup.org/)
*   **資料序列化**: [Gson](https://github.com/google/gson)

---

## 🚀 如何建置

1.  使用 Git Clone 此專案：
    ```bash
    git clone https://github.com/YenHsinCHEN/MopsDownloader.git
    ```
2.  使用最新版的 Android Studio 打開專案。
3.  等待 Gradle 同步完成所有依賴。
4.  點擊 "Run 'app'" 按鈕即可在本機模擬器或實體裝置上運行。

---

## 📝 開發歷程與致謝

這個專案是我從一個 Python GUI 應用程式完整遷移到原生 Android App 的學習紀錄。整個過程涵蓋了從 UI 設計、網路爬蟲邏輯重寫，到背景任務處理、資料持久化和現代 App 架構的實踐。

特別感謝 [Gemini](https://gemini.google.com/) 在整個開發過程中提供的指導與程式碼協助。

by **Yen**