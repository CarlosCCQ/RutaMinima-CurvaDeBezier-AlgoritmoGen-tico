package com.estudying.mykpc01application

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RutaApi {
    @POST("/predict/")
    fun predict(@Body request: RequestData): Call<ResponseData>
}