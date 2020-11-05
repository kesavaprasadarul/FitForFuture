package com.bosch.vaehiclefitness.network

import com.bosch.vaehiclefitness.model.VehicleFitness
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface APIService {

    @FormUrlEncoded
    @POST("/api/users")
    fun login(
        @Field("username") username: String?,
        @Field("password") password: String?,
        @Field("displayname") grant_type: String?,
    ): Call<HashMap<String, String>>

    @Multipart
    @POST
    fun verifyNumberPlate(
        @Url url: String,
        @Part data: MultipartBody.Part
    ): Call<HashMap<String, String>>

    @Multipart
    @POST
    fun verifyImage(
        @Url url: String,
        @Part data: MultipartBody.Part
    ): Call<HashMap<String, String>>

    @Multipart
    @POST
    fun verifyVideo(
        @Url url: String,
        @Part data: ArrayList<MultipartBody.Part>
    ): Call<HashMap<String, String>>

    @POST("/api/applicationDetails")
    fun getApplicationDetails(
        @Body data: HashMap<String, String>
    ): Call<VehicleFitness>
}