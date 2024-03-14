package baka.chaomian.booru.utils

import android.content.SharedPreferences

object LoginManager {
    lateinit var sharedPreferences: SharedPreferences
    private const val USERNAME_KEY = "Username"
    private const val API_KEY = "ApiKey"
    private const val STATE_KEY = "State"

    var username
        get() = sharedPreferences.getString(USERNAME_KEY, "")!!
        set(value) = sharedPreferences
            .edit()
            .putString(USERNAME_KEY, value)
            .apply()

    var apiKey
        get() = sharedPreferences.getString(API_KEY, "")!!
        set(value) = sharedPreferences
            .edit()
            .putString(API_KEY, value)
            .apply()

    var isUserLoggedIn
        get() = sharedPreferences.getBoolean(STATE_KEY, false)
        set(value) = sharedPreferences
            .edit()
            .putBoolean(STATE_KEY, value)
            .apply()

    fun logout() {
        sharedPreferences.edit()
            .remove(USERNAME_KEY)
            .remove(API_KEY)
            .putBoolean(STATE_KEY, false)
            .apply()
    }
}
