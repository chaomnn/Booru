package baka.chaomian.booru.network

import baka.chaomian.booru.utils.LoginManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface DanbooruService {

    @GET("posts.json")
    suspend fun getPosts(@Query("page") page: Int): List<DanbooruPost>

    @GET("posts.json")
    suspend fun getPostsByTags(@Query("page") page: Int, @Query("tags") query: String): List<DanbooruPost>

    @GET("autocomplete.json")
    suspend fun getTags(@Query("search[query]") name: String,
                            @Query("search[type]") type: String = "tag_query",
                            @Query("limit") limit: Int = 10): List<DanbooruTag>

    companion object {
        private const val DANBOORU_URL = "https://danbooru.donmai.us/"

        private val client: OkHttpClient = OkHttpClient()
            .newBuilder()
            .addInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    chain.request().let { originalRequest ->
                        if (!LoginManager.isUserLoggedIn) {
                            return chain.proceed(originalRequest)
                        }
                        val url = originalRequest.url()
                            .newBuilder()
                            .addQueryParameter("api_key", LoginManager.apiKey)
                            .addQueryParameter("login", LoginManager.username)
                            .build()
                        val request = originalRequest
                            .newBuilder()
                            .url(url)
                            .build()
                        return chain.proceed(request)
                    }
                }
            })
            .build()

        private val retrofit =
            Retrofit.Builder()
                .baseUrl(DANBOORU_URL)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create())
                .build()

        val service: DanbooruService = retrofit.create(DanbooruService::class.java)
    }
}
