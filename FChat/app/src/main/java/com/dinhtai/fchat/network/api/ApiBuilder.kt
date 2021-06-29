package com.dinhtai.fchat.network.api

import com.dinhtai.fchat.utils.config.ConfigAPI.URL
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ApiBuilder {
    val retrofit: Retrofit
        get() = getRetrofit(URL)

    private fun getRetrofit(URL: String): Retrofit {
        return Retrofit.Builder().baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }
}


