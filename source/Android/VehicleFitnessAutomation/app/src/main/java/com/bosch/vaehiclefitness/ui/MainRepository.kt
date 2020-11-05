package com.bosch.vaehiclefitness.ui

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bosch.vaehiclefitness.model.AudioTestStep
import com.bosch.vaehiclefitness.model.ImageTestStep
import com.bosch.vaehiclefitness.model.VehicleFitness
import com.bosch.vaehiclefitness.model.VideoTestStep
import com.bosch.vaehiclefitness.network.RetrofitApiClient
import com.bosch.vaehiclefitness.util.Constants
import com.bosch.vaehiclefitness.util.Constants.Companion.TAG
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/*

DESIGN ID:
Feature Name:

 */



class MainRepository {

    fun login(userName: String, password: String, displayName: String): LiveData<Boolean?> {
        val livaData = MutableLiveData<Boolean?>()
        RetrofitApiClient.getInstance()
            .getAPIService()
            .login(userName, password, displayName)
            .enqueue(object : Callback<HashMap<String, String>> {
                override fun onResponse(
                    call: Call<HashMap<String, String>>,
                    response: Response<HashMap<String, String>>
                ) {
                    livaData.value =
                        (response.body() != null && response.body()!!["username"] != null)
                }

                override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                    t.printStackTrace()
                    livaData.value = null
                }
            })
        return livaData
    }

    fun getApplicationDetails(applicationId: String): LiveData<VehicleFitness?> {
        val livaData = MutableLiveData<VehicleFitness?>()
        if (true) {
            livaData.value = VehicleFitness("KL40E3956", "MA3FSEB1S00376978").apply {
                applicationNumber = "12345678ABCD"
                applicationDate = System.currentTimeMillis()
                ownerName = "BAL RAJ"
            }
            return livaData
        }
        /*RetrofitApiClient.getInstance()
            .getAPIService()
            .getApplicationDetails(
                hashMapOf(
                    "applicationId" to applicationId
                )
            )
            .enqueue(object : Callback<VehicleFitness> {
                override fun onResponse(
                    call: Call<VehicleFitness>,
                    response: Response<VehicleFitness>
                ) {
                    livaData.value = response.body()
                }

                override fun onFailure(call: Call<VehicleFitness>, t: Throwable) {
                    t.printStackTrace()
                    livaData.value = null
                }
            })*/
        return livaData
    }

    private fun generateUniqueFileName(baseName: String) =
        System.currentTimeMillis().toString() + baseName

    fun verifyDataFromImage(context: Context, imageTestStep: ImageTestStep): LiveData<String?> {
        val liveData = MutableLiveData<String?>()
        val body =
            createMultipartBody(
                context,
                toByteArray(imageTestStep.bitMap!!),
                "${imageTestStep.responseKey}.jpg"
            )
        RetrofitApiClient.getInstance()
            .getAPIService()
            .verifyNumberPlate(RetrofitApiClient.BASE_URL + imageTestStep.url, body)
            .enqueue(object : retrofit2.Callback<HashMap<String, String>> {
                override fun onResponse(
                    call: Call<HashMap<String, String>>,
                    response: Response<HashMap<String, String>>
                ) {
                    liveData.value = response.body()?.get(imageTestStep.responseKey)
                    Log.d(TAG, "Response : $response")
                }

                override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                    liveData.value = null
                    Log.e(TAG, "API Failed : ${t.message}")
                }
            })
        return liveData
    }

    fun verifyImage(context: Context, imageTestStep: ImageTestStep): LiveData<Boolean?> {
        val liveData = MutableLiveData<Boolean?>()
        val key = imageTestStep.responseKey
        val body = createMultipartBody(context, toByteArray(imageTestStep.bitMap!!), "$key.jpg")
        verifyImage(imageTestStep.url, body, key, liveData)
        return liveData
    }

    fun verifyVideo(videoTestStep: VideoTestStep): LiveData<Boolean?> {
        val liveData = MutableLiveData<Boolean?>()
        val key = videoTestStep.responseKey
        val data: ArrayList<MultipartBody.Part> = ArrayList()
        for (file in videoTestStep.videoFrames!!) {
            val requestFile: RequestBody =
                RequestBody.create("image/jpg".toMediaTypeOrNull(), file)
            data.add(
                MultipartBody.Part.createFormData(
                    "files[]",
                    generateUniqueFileName(file.name),
                    requestFile
                )
            )
        }
        RetrofitApiClient.getInstance()
            .getAPIService()
            .verifyVideo(RetrofitApiClient.BASE_URL + videoTestStep.url, data)
            .enqueue(object : retrofit2.Callback<HashMap<String, String>> {
                override fun onResponse(
                    call: Call<HashMap<String, String>>,
                    response: Response<HashMap<String, String>>
                ) {
                    liveData.value = response.body()?.get(key) == Constants.SUCCESS
                    Log.d(TAG, "Response : $response")
                }

                override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                    liveData.value = null
                    Log.e(TAG, "API Failed : ${t.message}")
                }
            })
        return liveData
    }

    fun verifyAudio(audioTestStep: AudioTestStep): LiveData<Boolean?> {
        val liveData = MutableLiveData<Boolean?>()
        val key = audioTestStep.responseKey
        val data: ArrayList<MultipartBody.Part> = ArrayList()
        val file = File(audioTestStep.audioFilePath)
        val extension: String? =
            audioTestStep.audioFilePath!!.substring(audioTestStep.audioFilePath!!.lastIndexOf("."))
        val requestFile: RequestBody =
            RequestBody.create("audio/$extension".toMediaTypeOrNull(), file)
        data.add(
            MultipartBody.Part.createFormData(
                "files[]",
                generateUniqueFileName(file.name),
                requestFile
            )
        )
        RetrofitApiClient.getInstance()
            .getAPIService()
            .verifyVideo(RetrofitApiClient.BASE_URL + audioTestStep.url, data)
            .enqueue(object : retrofit2.Callback<HashMap<String, String>> {
                override fun onResponse(
                    call: Call<HashMap<String, String>>,
                    response: Response<HashMap<String, String>>
                ) {
                    liveData.value = response.body()?.get(key) == Constants.SUCCESS
                    Log.d(TAG, "Response : $response")
                }

                override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                    liveData.value = null
                    Log.e(TAG, "API Failed : ${t.message}")
                }
            })
        return liveData
    }

    private fun verifyImage(
        url: String?,
        body: MultipartBody.Part,
        key: String?,
        liveData: MutableLiveData<Boolean?>
    ) {
        RetrofitApiClient.getInstance()
            .getAPIService()
            .verifyImage(RetrofitApiClient.BASE_URL + url, body)
            .enqueue(object : retrofit2.Callback<HashMap<String, String>> {
                override fun onResponse(
                    call: Call<HashMap<String, String>>,
                    response: Response<HashMap<String, String>>
                ) {
                    liveData.value = response.body()?.get(key) == Constants.SUCCESS
                    Log.d(TAG, "Response : $response")
                }

                override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                    liveData.value = null
                    Log.e(TAG, "API Failed : ${t.message}")
                }
            })
    }

    private fun toByteArray(bitmap: Bitmap): ByteArray {
        val bmp = bitmap.copy(bitmap.config, bitmap.isMutable)
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray: ByteArray = stream.toByteArray()
        bmp.recycle()
        return byteArray
    }

    private fun createMultipartBody(
        context: Context,
        imageBytes: ByteArray,
        fileName: String
    ): MultipartBody.Part {
        writeFileToDevice(context, imageBytes, fileName)
        val requestFile: RequestBody =
            RequestBody.create("image/jpg".toMediaTypeOrNull(), File(context.filesDir, fileName))
        return MultipartBody.Part.createFormData(
            "files[]",
            generateUniqueFileName(fileName),
            requestFile
        )
    }

    private fun writeFileToDevice(context: Context, data: ByteArray, filePath: String): Boolean {
        try {
            File(context.filesDir, filePath).delete()
            val outputStream = context.openFileOutput(filePath, Context.MODE_PRIVATE)
            outputStream.write(data)
            outputStream.close()
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "FileNotFoundException in writeFileToDevice. " + e.message)
            return false
        } catch (e: IOException) {
            Log.e(TAG, "IOException in writeFileToDevice. " + e.message)
            return false
        }

        return true
    }


}