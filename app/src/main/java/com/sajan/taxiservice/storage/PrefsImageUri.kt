package com.sajan.bktguide.storage

import android.content.Context

class PrefsImageUri(context: Context) {

    private var prefsManager = context.getSharedPreferences("PREFS_URI", Context.MODE_PRIVATE)

    private val keyUri = "KEY_IMAGE_URI"
    private val keyFileName = "KEY_FILE_NAME"
    var imgUri: String?
        set(value) {
            prefsManager.edit().putString(keyUri, value).apply()
        }
        get() {
            return prefsManager.getString(keyUri, null)
        }

    var fileName: String?
        set(value) {
            prefsManager.edit().putString(keyFileName, value).apply()
        }
        get() {
            return prefsManager.getString(keyFileName, null)
        }

    fun resetImageData() {
        prefsManager.edit().clear().apply()
    }
}