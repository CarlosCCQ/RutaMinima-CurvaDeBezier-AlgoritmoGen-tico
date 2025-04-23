package com.estudying.mykpc01application

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitRuta {
    private const val BASE_URL = "https://carlosccq-rutamascortaybezier.hf.space/"
    val api: RutaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RutaApi::class.java)
    }
}