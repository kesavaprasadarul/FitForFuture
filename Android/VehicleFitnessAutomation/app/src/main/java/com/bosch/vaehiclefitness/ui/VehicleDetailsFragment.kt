package com.bosch.vaehiclefitness.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.bosch.vaehiclefitness.R
import kotlinx.android.synthetic.main.fragment_vehicle_details.*

class VehicleDetailsFragment : BaseFragment() {

    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        mainViewModel.isOfficialLogIn = false
        return inflater.inflate(R.layout.fragment_vehicle_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnContinue?.setOnClickListener {
            validateData(etVehicleNumber?.text, etChassisNumber?.text, etEmailId?.text)
        }
        getMainActivity()?.setupToolbar(R.string.title_vehicle_details)
    }

    private fun validateData(
        vehicleNumber: CharSequence?,
        chassisNumber: CharSequence?,
        emailId: CharSequence?
    ) {
        if (vehicleNumber.isNullOrBlank() || chassisNumber.isNullOrBlank() || emailId.isNullOrBlank()) {
            showToast(R.string.error_invalid_input)
        } else {
            mainViewModel.setVehicleDetails(
                formatText(vehicleNumber),
                formatText(chassisNumber),
                emailId.toString()
            )
            getMainActivity()?.addFragment(VehicleFitnessTestFragment(), clearBackStack = true)
        }
    }

    private fun formatText(text: CharSequence): String {
        return text.toString().toUpperCase().replace("\\s".toRegex(), "")
    }

}