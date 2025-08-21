package com.mops.mopsdownloader.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 透過屬性委派建立一個全域的 DataStore 實例
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    // 建立一個 Key 來代表我們要儲存的資料
    companion object {
        val DIRECTORY_URI_KEY = stringPreferencesKey("directory_uri")
    }

    // 提供一個 Flow 來讓外部可以「持續觀察」儲存位置的變化
    val directoryUriFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[DIRECTORY_URI_KEY]
        }

    // 提供一個 suspend 函式來儲存（更新）下載位置
    suspend fun saveDirectoryUri(uri: String) {
        context.dataStore.edit { settings ->
            settings[DIRECTORY_URI_KEY] = uri
        }
    }
}