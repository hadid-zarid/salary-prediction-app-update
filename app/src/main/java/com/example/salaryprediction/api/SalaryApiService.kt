package com.example.salaryprediction.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SalaryApiService {

    @Headers("Content-Type: application/json")
    @POST("/predict")
    fun predictSalary(
        @Body request: SalaryRequest
    ): Call<SalaryResponse>
}