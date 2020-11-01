package com.bosch.vaehiclefitness.network

import com.bosch.vaehiclefitness.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/*

SRS: 2.3.1.1-c
DESIGN ID: FR_VFT_001 (FRS-2.3.1.1)
Feature Name: Application and Deployement

 */

class RetrofitApiClient private constructor() {
    companion object {
        private const val CONNECTION_TIME_OUT =  5 * 60L
        private const val READ_TIME_OUT = 5 * 60L
        private const val WRITE_TIME_OUT = 5 * 60L

        const val BASE_URL = "http://152.67.176.97:5000"

        private var instance: RetrofitApiClient? = null

        fun getInstance(): RetrofitApiClient {
            if (instance == null) {
                instance = RetrofitApiClient()
            }
            return instance!!
        }
    }

    fun getAPIService(): APIService {
        val okHttpClientAuth: OkHttpClient.Builder = getOkHttpBuilder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .connectTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS)
            .addInterceptor(OAuth2Interceptor())
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            okHttpClientAuth.addInterceptor(logging)
        }
        val retrofit: Retrofit =
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClientAuth.build())
                .build()
        return retrofit.create(APIService::class.java)
    }

    private fun getOkHttpBuilder(): OkHttpClient.Builder {
        return try {
            val okHttpBuilder = OkHttpClient().newBuilder()
                .connectTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS)
            if (BuildConfig.DEBUG) {
                val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.BODY
                okHttpBuilder.addInterceptor(logging)
            }
            okHttpBuilder
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

}