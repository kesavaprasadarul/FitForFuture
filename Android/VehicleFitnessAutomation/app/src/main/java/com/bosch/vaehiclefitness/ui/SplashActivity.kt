package com.bosch.vaehiclefitness.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import com.bosch.vaehiclefitness.R
import com.bosch.vaehiclefitness.util.Constants
import java.util.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onStart() {
        super.onStart()
        Handler().postDelayed({
            setLocale()
            goToMainActivity()
        }, 2000)
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java));
        finish()
    }

    private fun setLocale() {
        val locale: String? = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(Constants.KEY_APP_LOCALE, null)
        locale ?: return
        val myLocale = Locale(locale)
        val res: Resources = getResources()
        val dm: DisplayMetrics = res.getDisplayMetrics()
        val conf: Configuration = res.getConfiguration()
        conf.locale = myLocale
        res.updateConfiguration(conf, dm)

    }

}