package com.bosch.vaehiclefitness.model

import com.bosch.vaehiclefitness.util.SharedPreferenceUtil
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class VehicleFitness(
    @SerializedName("vehicleNumber")
    @Expose val vehicleNumber: String,
    @SerializedName("chassisNumber")
    @Expose val chassisNumber: String,
    @SerializedName("applicationNumber")
    @Expose val emailId: String? = null
) {
    @SerializedName("applicationNumber")
    @Expose
    var applicationNumber: String? = null

    @SerializedName("applicationDate")
    @Expose
    var applicationDate: Long? = null

    @SerializedName("ownerName")
    @Expose
    var ownerName: String? = null

    @SerializedName("color")
    @Expose
    var color: String? = null

    @SerializedName("bodyType")
    @Expose
    var bodyType: String? = null

    @SerializedName("isTransport")
    @Expose
    var isTransport: Boolean = false

    var testSteps: ArrayList<TestStep>? = null

    fun getApplicationDateString(): String? {
        return SharedPreferenceUtil.getCurrentDateTime()
    }
}