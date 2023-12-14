package baka.chaomian.booru.network

import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface DanbooruService {

    @GET("posts.json")
    suspend fun getPosts(@Query("page") page: Int): List<DanbooruPost>

    @GET("posts.json")
    suspend fun getPostsByTags(@Query("page") page: Int, @Query("tags") query: String): List<DanbooruPost>

//    @GET("tags.json")
//    suspend fun getTags(@Query("search[name_or_alias_matches]") name: String,
//                        @Query("search[hide_empty]") hideEmpty: String = "yes",
//                        @Query("search[order]") order: String = "count"):
//            List<DanbooruTag>

    @GET("autocomplete.json")
    suspend fun getTags(@Query("search[query]") name: String,
                            @Query("search[type]") type: String = "tag_query",
                            @Query("limit") limit: Int = 10): List<DanbooruTag>

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
