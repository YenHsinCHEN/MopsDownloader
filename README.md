# MOPS 批次下載工具 (Android App)

這是一個基於 Kotlin 和 Jetpack Compose 開發的原生 Android 應用程式，旨在提供一個現代化、易於使用的介面，用於批次下載台灣公開資訊觀測站 (MOPS) 的財務報表與股東會年報。

此專案的靈感來源於一個功能強大的 Python 桌面應用程式，並旨在將其核心功能移植到行動裝置上，同時利用 Android 平台的優勢提供更佳的使用者體驗。

<p align="center">
  <img src="https://raw.githubusercontent.com/YenHsinCHEN/MopsDownloader/master/Screenshot_20250821_230400.png" width="300">
</p>
---

## ✨ 核心功能

*   **獨立批次選擇**: 財務報表與股東會年報擁有完全獨立的年份選擇區塊，財報年份可展開勾選季度，實現精準的批次任務設定。
*   **智慧下載策略**:
    *   **多層級延遲**: 根據任務數量（1-5, 6-15, 16-25）自動採用 1-7 秒不等的隨機延遲策略，有效避免觸發反爬蟲機制。
    *   **下載上限**: 公開發佈版本的單次任務上限為 25 個檔案，超過時會提示使用者，保護網站伺服器與使用者 IP 安全。
*   **有序的任務佇列**: 所有下載任務會自動排序，確保按「財報 -> 年報」、「年份新 -> 舊」、「季度小 -> 大」的順序執行，方便管理。
*   **健壯的背景下載**:
    *   深度整合 **Android WorkManager** 並採用**前台服務 (Foreground Service)** 模式，確保下載任務擁有高優先級，即使 App 切換到背景或處於省電模式，下載也不會中斷。
    *   經過多輪嚴格的偵錯與測試，解決了 `release` 版本中的程式碼壓縮 (R8 Shrinking) 與底層函式庫的相容性衝突，確保 App 在各種客製化 Android 系統（如 MIUI）上的功能穩定性。
*   **極致的使用者體驗**:
    *   **持久化儲存位置**: 只需設定一次儲存資料夾，App 就會透過 `DataStore` 記住您的選擇。
    *   **完整的任務控制**: 提供帶有確認對話框的「取消下載」功能，防止誤觸。
    *   **即時進度與日誌**: 在介面上即時顯示下載進度條和詳細的執行日誌。
*   **完善的資訊架構**:
    *   整合 **Navigation for Compose**，提供「使用說明」和「版本紀錄」頁面。
    *   清晰的作者署名與專案識別。

---

## 🛠️ 技術棧

本專案採用了 Google 推薦的現代 Android 開發技術棧 (Modern Android Development, MAD)：

*   **語言**: [Kotlin](https://kotlinlang.org/)
*   **UI 框架**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
*   **架構**: MVVM (Model-View-ViewModel)
*   **非同步處理**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
*   **背景任務**: [Android WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) (with Foreground Service)
*   **頁面導航**: [Navigation for Compose](https://developer.android.com/jetpack/compose/navigation)
*   **資料持久化**: [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
*   **網路請求**: [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/)
*   **HTML 解析**: [Jsoup](https://jsoup.org/)
*   **資料序列化**: [Gson](https://github.com/google/gson)
*   **建置與發佈**: [Gradle Build Variants](https://developer.android.com/studio/build/build-variants), Release Build Signing & ProGuard/R8 (`-dontshrink`)

---

## 🚀 如何建置

1.  使用 Git Clone 此專案：
    ```bash
    git clone https://github.com/YenHsinCHEN/MopsDownloader.git
    ```
2.  使用最新版的 Android Studio 打開專案。
3.  等待 Gradle 同步完成所有依賴。
4.  在 **Build Variants** 面板選擇 `storeDebug` 或 `internalDebug`。
5.  點擊 "Run 'app'" 按鈕即可在本機模擬器或實體裝置上運行。

---

## 📝 開發歷程與致謝

這個專案是我從一個 Python GUI 應用程式完整遷移到原生 Android App 的學習紀錄。整個過程涵蓋了從 UI 設計、網路爬蟲邏輯重寫，到背景任務處理、資料持久化和現代 App 架構的實踐。

開發過程中解決了多個深層次的 Android `release` 版本建置問題，包括但不限於：
-   **前台服務相容性**：針對 Android 14+ 的前台服務權限 (`FOREGROUND_SERVICE_DATA_SYNC`) 與服務類型進行了正確的宣告。
-   **R8 相容性衝突**：透過系統性的控制變數測試，最終定位到 R8 的**程式碼壓縮 (Shrinking)** 步驟是導致 `release` 版本靜默失敗的根源。最終透過在 ProGuard 規則中加入 `-dontshrink` 指令，在保留程式碼混淆的同時，確保了 App 在所有裝置上的核心功能穩定性。

特別感謝 [Gemini](https://gemini.google.com/) 在整個開發過程中提供的即時指導與偵錯協助。

by **Yen**

---
### 隱私權政策
本應用程式為單機工具，不會收集或分享任何使用者個人資料。
