package com.jervisffb.utils

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.minusAssign
import com.russhwolf.settings.observable.makeObservable
import kotlinx.coroutines.flow.Flow

/**
 * Low-level api for storing and retrieving key/value properties.
 *
 * It is using Multiplatform Settings as an internal implementation, but that
 * fact should not leave this class.
 *
 * All client settings MUST flow through this class to propagate correctly.
 *
 * Keys for setting entries are auto-generated from a `.ini` file by a Gradle
 * task and should be used exclusively when referencing setting keys.
 *
 * See `com.jervisffb.generated.SettingsKeys`
 *
 * - JVM: Stored in a jervis.properties file in the application folder
 * - Wasm: Stored in LocalStorage in the browser.
 * - iOS: Stored in NSUserDefaultsSettings
 */
@OptIn(ExperimentalSettingsApi::class)
class SettingsManager() {

    private val settings = Settings().makeObservable()

    fun clear() {
        settings.clear()
    }

    // Probably not the best place for this, but oh well...
    fun getSystemEnv(key: String): String? {
        return getSystemEnvironmentVariable(key)
    }

    fun getStringOrNull(key: String): String? {
        return settings.getStringOrNull(key)
    }
    fun getString(key: String, defaultValue: String): String {
        return settings.getString(key, defaultValue)
    }

    fun getBooleanOrNull(key: String): Boolean? {
        return settings.getBooleanOrNull(key)
    }
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return settings.getBoolean(key, defaultValue)
    }

    fun getIntOrNull(key: String): Int? {
        return settings.getIntOrNull(key)
    }
    fun getInt(key: String, defaultValue: Int): Int {
        return settings.getInt(key, defaultValue)
    }

    fun observeBooleanKey(key: String, defaultValue: Boolean): Flow<Boolean> {
        return settings.getBooleanFlow(key, defaultValue)
    }

    fun put(key: String, value: Any?) {
        if (value == null) {
            settings -= key
        } else {
            when (value::class) {
                Int::class -> settings.putInt(key, value as Int)
                String::class -> settings.putString(key, value as String)
                Boolean::class -> settings.putBoolean(key, value as Boolean)
                else -> throw IllegalArgumentException("Invalid type: ${value::class}")
            }
        }
    }

    operator fun set(key: String, value: Any?) = put(key, value)
}

