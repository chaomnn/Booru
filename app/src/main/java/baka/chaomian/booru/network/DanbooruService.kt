package baka.chaomian.booru.network

import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET

interface DanbooruService {

    @GET("posts?format=json")
    suspend fun getPosts(): List<DanbooruPost>

    companion object {
        private const val DANBOORU_URL = "https://danbooru.donmai.us/"

        private val retrofit =
            Retrofit.Builder()
                .baseUrl(DANBOORU_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build()

        val service: DanbooruService = retrofit.create(DanbooruService::class.java)
    }
}
