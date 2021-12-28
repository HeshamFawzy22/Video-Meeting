package com.example.videomeeting.network

import com.example.videomeeting.utilities.BASE_URL
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(ScalarsConverterFactory.create())
    .build()

interface ClientApiService {

    @POST("send")
    fun sendRemoteMessage(
        @HeaderMap headers: HashMap<String, String>,
        @Body remoteBody: String
    ): Call<String>

}

object ClientApi {
    val retrofitService: ClientApiService by lazy { retrofit.create(ClientApiService::class.java) }
}