package com.bosch.vaehiclefitness.network

import com.bosch.vaehiclefitness.VehTestApplication
import com.bosch.vaehiclefitness.util.SharedPreferenceUtil
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class OAuth2Interceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        val user = SharedPreferenceUtil.getUser(VehTestApplication.getAppContext())

        if (user != null) {
            requestBuilder.addHeader(
                "Authorization",
                Credentials.basic(user.userName, user.password)
            )
        }
        val request = requestBuilder.build()
        return chain.proceed(request)
    }

}