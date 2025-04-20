package ca.qc.bdeb.c5gm.tp1.API

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query


data class Question(
    val category: String,
    val question: String,
    val answer: String
)

// Interface des méthodes qui intéragie avec l'API
interface APIService {
    @GET("trivia")
    suspend fun getUneQuestion(
        @Header("X-Api-Key") apiKey: String,
        @Query("category") category: String) : Response<List<Question>>
}

// Code fournie par les notes de cours du professeur Mathieu Brodeur-Béliveau
object API {

    private const val BASE_URL = "https://api.api-ninjas.com/v1/"

    private val gson: Gson by lazy {
        GsonBuilder().setLenient().create()
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiService: APIService by lazy {
        retrofit.create(APIService::class.java)
    }
}