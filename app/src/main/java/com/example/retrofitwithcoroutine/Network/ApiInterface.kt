package com.example.retrofitwithcoroutine.Network

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET

interface ApiInterface {

    // Define the endpoint to fetch data
    @GET("quotes?page=1")
    fun getData(): Call<JsonObject>
}