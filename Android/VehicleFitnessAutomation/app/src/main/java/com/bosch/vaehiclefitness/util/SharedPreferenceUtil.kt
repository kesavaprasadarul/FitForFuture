package com.bosch.vaehiclefitness.util

import android.content.Context
import android.preference.PreferenceManager
import com.bosch.vaehiclefitness.model.User
import java.text.SimpleDateFormat
import java.util.*

class SharedPreferenceUtil {
    companion object {
        fun saveUser(
            context: Context?,
            userName: String,
            password: String,
            displayName: String
        ): User {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(Constants.KEY_USER_NAME, userName)
                .putString(Constants.KEY_PASSWORD, password)
                .putString(Constants.KEY_DISPLAY_NAME, displayName)
                .apply()
            return User(userName, password, displayName)
        }

        fun getUser(context: Context?): User? {
            val preference = PreferenceManager.getDefaultSharedPreferences(context)
            val user = preference.getString(Constants.KEY_USER_NAME, null)
            val pass = preference.getString(Constants.KEY_PASSWORD, null)
            val displayName = preference.getString(Constants.KEY_DISPLAY_NAME, null)
            return if (user != null && pass != null && displayName != null) User(
                user,
                pass,
                displayName
            ) else null
        }

        fun getCurrentDateTime(): String? {
            val sdf = SimpleDateFormat("dd-MMM-YYYY")
            return sdf.format(Date())
        }

    }
}