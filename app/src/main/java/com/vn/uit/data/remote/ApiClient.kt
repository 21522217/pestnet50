package com.vn.uit.data.remote

import android.content.Context
import com.vn.uit.datastore.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "http://10.45.18.128:8081/pestnet/api/"

    @Volatile
    private var token: String? = null

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }


    private val authInterceptor = Interceptor { chain ->
        val originalRequest: Request = chain.request()
        val builder = originalRequest.newBuilder()

        token?.let {
            builder.addHeader("Authorization", "Bearer $it")
        }

        val newRequest = builder.build()
        chain.proceed(newRequest)
    }


    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .build()

    private lateinit var retrofit: Retrofit


    lateinit var authApi: AuthApi
        private set
    lateinit var historyApi: HistoryApi
        private set
    lateinit var pestApi: PestApi
        private set
    lateinit var classificationApi: ClassificationApi
        private set


    private val scope = CoroutineScope(Dispatchers.IO + Job())


    fun init(context: Context) {
        val userPrefs = UserPreferences(context)


        userPrefs.getToken()
            .onEach { newToken ->
                token = newToken
            }
            .launchIn(scope)

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        authApi = retrofit.create(AuthApi::class.java)
        historyApi = retrofit.create(HistoryApi::class.java)
        pestApi = retrofit.create(PestApi::class.java)
        classificationApi = retrofit.create(ClassificationApi::class.java)
    }
}
