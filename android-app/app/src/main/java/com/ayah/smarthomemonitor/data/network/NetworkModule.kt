package com.ayah.smarthomemonitor.data.network

import com.ayah.smarthomemonitor.BuildConfig
import com.ayah.smarthomemonitor.data.remote.GitHubApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Builds the single Retrofit/OkHttp instance the app uses to talk to GitHub.
 *
 * The GitHub Personal Access Token is read from [BuildConfig.GITHUB_TOKEN],
 * which Gradle injected from local.properties at build time (see
 * app/build.gradle.kts). It is attached here, in one place, as an
 * Authorization header — it is never stored, logged, or referenced anywhere
 * else in the app.
 */
object NetworkModule {

    private const val GITHUB_API_BASE_URL = "https://api.github.com/"

    private val authInterceptor = Interceptor { chain: Interceptor.Chain ->
        val original = chain.request()
        val authorized = original.newBuilder()
            .header("Authorization", "token ${BuildConfig.GITHUB_TOKEN}")
            .header("Accept", "application/vnd.github+json")
            .build()
        chain.proceed(authorized)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            // Intentionally no logging interceptor at BODY/HEADERS level here —
            // that would risk printing the Authorization header into Logcat.
            .build()
    }

    val gitHubApiService: GitHubApiService by lazy {
        Retrofit.Builder()
            .baseUrl(GITHUB_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubApiService::class.java)
    }
}
