package com.bosch.vaehiclefitness.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bosch.vaehiclefitness.BuildConfig
import com.bosch.vaehiclefitness.R
import com.bosch.vaehiclefitness.model.User
import com.bosch.vaehiclefitness.util.Constants.Companion.DEMO_EMAIL_ID
import com.bosch.vaehiclefitness.util.Constants.Companion.DEMO_PASSWORD
import com.bosch.vaehiclefitness.util.Constants.Companion.DEMO_UNIQUE_ID
import com.bosch.vaehiclefitness.util.Constants.Companion.DEMO_USER_ID
import com.bosch.vaehiclefitness.util.SharedPreferenceUtil
import kotlinx.android.synthetic.main.fragment_login.*

/*

SRS: 2.3.1.1-c
DESIGN ID: FR_VFT_001 (FRS-2.3.1.1)
Feature Name: Application and Deployement

 */


class LoginFragment : BaseFragment() {


    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        mainViewModel.isOfficialLogIn = true
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (BuildConfig.DEBUG) {
            etUniqueId.setText(DEMO_UNIQUE_ID)
            etUserId.setText(DEMO_USER_ID)
            etpassword.setText(DEMO_PASSWORD)
        }
        btnLogIn?.setOnClickListener {
            validateCredentials(etUniqueId?.text, etUserId?.text, etpassword?.text)
        }
        btnGetData?.setOnClickListener {
            showVehicleDetails()
        }
        getMainActivity()?.setupToolbar(R.string.title_log_in)
        val user = SharedPreferenceUtil.getUser(activity)
        if (user != null) {
            mainViewModel.user = user
            showApplicationDataUI()
        }
    }

    private fun showApplicationDataUI() {
        tiUniqueId.visibility = VISIBLE
        etUniqueId.visibility = VISIBLE
        btnGetData.visibility = VISIBLE
        etUserId.visibility = GONE
        etpassword.visibility = GONE
        tiUserId.visibility = GONE
        tipassword.visibility = GONE
        btnLogIn.visibility = GONE
    }

    private fun validateCredentials(
        displayName: CharSequence?,
        userId: CharSequence?,
        password: CharSequence?
    ) {
        showProgress()
        mainViewModel.login(userId.toString(), password.toString(), displayName.toString())
            .observe(viewLifecycleOwner, Observer {
                if (it == true) {
                    mainViewModel.user = SharedPreferenceUtil.saveUser(
                        activity,
                        userId.toString(),
                        password.toString(),
                        userId.toString()
                    )
                    showToast("Log in success")
                    showApplicationDataUI()
                } else {
                    showToast(R.string.something_wrong)
                }
                hideProgress()
            })
    }

    private fun showVehicleDetails() {
        if (etUniqueId?.text.isNullOrBlank()) {
            showToast(R.string.error_invalid_input)
            return
        }
        mainViewModel.getApplicationDetails(etUniqueId?.text.toString())
            .observe(viewLifecycleOwner, Observer {
                if (it == null) {
                    showToast(R.string.something_wrong)
                    return@Observer
                }
                mainViewModel.vehicleFitness = it
                AlertDialog.Builder(activity ?: return@Observer)
                    .setMessage(
                        getString(
                            R.string.info_application_details,
                            it.applicationNumber,
                            it.getApplicationDateString(),
                            it.vehicleNumber,
                            it.ownerName,
                            it.chassisNumber
                        )
                    )
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.btn_text_continue) { _, _ ->
                        mainViewModel.vehicleFitness.applicationNumber = etUniqueId?.text.toString()
                        getMainActivity()?.addFragment(VehicleFitnessTestFragment(), true)
                    }
                    .setCancelable(false)
                    .show()
            })
    }

    companion object {
        private val demoVehicleNumber = arrayListOf("KL13AJ6562", "KL55L2323")
        private val demoChassisNumber = arrayListOf("MA3FSEB1S00376978", "MJ4LIOG1M00984785")
    }


}