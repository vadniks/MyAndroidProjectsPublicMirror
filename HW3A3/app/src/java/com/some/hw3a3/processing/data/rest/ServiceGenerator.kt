package com.some.hw3a3.processing.data.rest

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * @author Vad Nik.
 * @version dated Sep 14, 2018.
 * @link http://github.com/vadniks
 */
class ServiceGenerator {
    private val gson = GsonBuilder().create()

    fun <T> createService(clazz: Class<T>): T = Retrofit.Builder().apply {
        baseUrl("https://api.github.com")
        addConverterFactory(GsonConverterFactory.create(gson))
        addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        client(getClient())
    }.build().create(clazz)

    private fun getClient(): OkHttpClient {
        val i = HttpLoggingInterceptor()
        i.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder().addInterceptor(i).build()
    }
}
