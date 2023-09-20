package baka.chaomian.booru.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import baka.chaomian.booru.data.Post
import baka.chaomian.booru.data.PostRepository
import kotlinx.coroutines.flow.Flow

class MainViewModel : ViewModel() {

    val pagingDataFlow: Flow<PagingData<Post>>
    private val repository = PostRepository()

    init {
        pagingDataFlow = repository
            .getAllPosts()
            .cachedIn(viewModelScope)
    }
}
