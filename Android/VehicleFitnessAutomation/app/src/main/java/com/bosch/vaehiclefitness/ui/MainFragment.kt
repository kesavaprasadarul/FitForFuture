package com.bosch.vaehiclefitness.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bosch.vaehiclefitness.R
import com.bosch.vaehiclefitness.util.Constants.Companion.KEY_APP_LOCALE
import kotlinx.android.synthetic.main.fragment_main.*


class MainFragment : BaseFragment() {

    companion object {
        fun newInstance(): MainFragment = MainFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnStartOfficialTest?.setOnClickListener {
            getMainActivity()?.addFragment(LoginFragment(), true)
        }
        btnApplicantLogin?.setOnClickListener {
            getMainActivity()?.addFragment(VehicleDetailsFragment(), true)
        }
        btnChangeLanguage?.setOnClickListener {
            AlertDialog.Builder(activity)
                .setTitle(R.string.change_language)
                .setSingleChoiceItems(
                    resources.getStringArray(R.array.array_languages),
                    getLocaleIndex()
                ) { _, index ->
                    if (index == 0) {
                        setLocale(activity, "en")
                    } else {
                        setLocale(activity, "hi")
                    }
                }
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
        getMainActivity()?.setupToolbar(R.string.app_name)
    }

    private fun setLocale(context: Context?, locale: String?) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(KEY_APP_LOCALE, locale)
            .apply()
        val refresh = Intent(context, SplashActivity::class.java)
        startActivity(refresh)
        activity?.finish()
    }

    private fun getLocaleIndex(): Int {
        val locale: String? = PreferenceManager.getDefaultSharedPreferences(activity)
            .getString(KEY_APP_LOCALE, null)
        return if (locale != null && locale == "hi") 1 else 0
    }

}
