package com.example.nativemicrosite.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private val retrofit = Retrofit.Builder()
//        .baseUrl("http://192.168.10.45:5000/") // must end with /
        .baseUrl("https://theclinicall.com/") // must end with /
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: ApiService = retrofit.create(ApiService::class.java)
    val url: String = retrofit.baseUrl().toString()
}
