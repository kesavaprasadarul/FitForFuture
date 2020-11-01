package com.bosch.vaehiclefitness.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.bosch.vaehiclefitness.model.*


class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val mainRepository = MainRepository()
    lateinit var vehicleFitness: VehicleFitness
    lateinit var user: User
    var isOfficialLogIn = false

    fun login(userName: String, password: String, displayName: String) =
        mainRepository.login(userName, password, displayName)

    fun setVehicleDetails(vehicleNumber: String, chassisNumber: String, emailId: String) {
        vehicleFitness = VehicleFitness(vehicleNumber, chassisNumber, emailId)
    }

    fun verifyNumberPlate(testStep: ImageTestStep): LiveData<String?> =
        mainRepository.verifyDataFromImage(getApplication(), testStep)

    fun verifyChassisNumber(testStep: ImageTestStep): LiveData<String?> =
        mainRepository.verifyDataFromImage(getApplication(), testStep)

    fun verifyPollCert(testStep: ImageTestStep): LiveData<String?> =
        mainRepository.verifyDataFromImage(getApplication(), testStep)

    fun verifyImage(testStep: ImageTestStep): LiveData<Boolean?> =
        mainRepository.verifyImage(getApplication(), testStep)

    fun verifyVideo(testStep: VideoTestStep): LiveData<Boolean?> =
        mainRepository.verifyVideo(testStep)

    fun verifyAudio(audioTestStep: AudioTestStep): LiveData<Boolean?> =
        mainRepository.verifyAudio(audioTestStep)

    fun getApplicationDetails(applicationId: String): LiveData<VehicleFitness?> =
        mainRepository.getApplicationDetails(applicationId)
}