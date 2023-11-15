package baka.chaomian.booru.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import baka.chaomian.booru.network.DanbooruService

class PostPagingSource(private val query: String?) : PagingSource<Int, Post>() {

    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
        println("anchorPosition ${state.anchorPosition}")
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        val page = params.key ?: 1
        return try {
            val booruPosts = if (query.isNullOrEmpty()) DanbooruService.service.getPosts(page) else
                DanbooruService.service.getPostsByTags(page, query)
            val posts: List<Post> = booruPosts
                .filter {
                    it.previewUrl != null && it.originalUrl != null && it.largeUrl != null
                }
                .map {
                    Post(it.id, it.previewUrl!!, it.originalUrl!!, it.largeUrl!!)
                }
            println("Fetched " + posts.size + " posts")
            val nextPage = if (posts.isEmpty()) null else page + 1
            LoadResult.Page(data = posts,
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextPage)
        } catch (e: Exception) {
            println("load error: $e")
            return LoadResult.Error(e)
        }
    }
}
