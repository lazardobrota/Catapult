package com.example.catapult.di

import com.example.catapult.BuildConfig
import com.example.catapult.cats.network.api.ICatListAPI
import com.example.catapult.cats.network.api.IResultsAPI
import com.example.catapult.cats.network.serialization.JsonAndClass
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Singleton
    @Provides
    fun providesHttpLoggingInterceptor() = HttpLoggingInterceptor()
        .apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Singleton
    @Provides
    fun providesOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor {
                val request = it.request().newBuilder()
                    .addHeader(
                        "x-api-key",
                        BuildConfig.CAT_API_KEY
                    )
                    .build()
                it.proceed(request)
            }
            .addInterceptor(
                httpLoggingInterceptor
            )
            .build()


    @Singleton
    @Provides
    @Named("CatApiRetrofit")
    fun provideCatApiRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.thecatapi.com/v1/")
        .client(okHttpClient)
        .addConverterFactory(JsonAndClass.asConverterFactory("application/json".toMediaType()))
        .build()


    @Singleton
    @Provides
    @Named("ResultRetrofit")
    fun provideResultRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(" https://rma.finlab.rs/")
        .addConverterFactory(JsonAndClass.asConverterFactory("application/json".toMediaType()))
        .build()

    @Singleton
    @Provides
    fun provideCatApi(@Named("CatApiRetrofit") retrofit: Retrofit): ICatListAPI = retrofit.create(ICatListAPI::class.java)
    @Singleton
    @Provides
    fun provideResultApi(@Named("ResultRetrofit") retrofit: Retrofit): IResultsAPI = retrofit.create(IResultsAPI::class.java)

}