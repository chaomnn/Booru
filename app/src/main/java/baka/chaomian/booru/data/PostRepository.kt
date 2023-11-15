package baka.chaomian.booru.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

class PostRepository {

    companion object {
        private const val PAGE_SIZE = 10
    }

    fun getPosts(query: String?): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PostPagingSource(query) }
        ).flow
    }
}
