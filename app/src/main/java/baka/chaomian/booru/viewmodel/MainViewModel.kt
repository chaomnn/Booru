package baka.chaomian.booru.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import baka.chaomian.booru.data.Post
import baka.chaomian.booru.data.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class MainViewModel : ViewModel() {

    val pagingDataFlow: Flow<PagingData<Post>>
    private val repository = PostRepository()
    private val queryFlow = MutableStateFlow<String?>(null)

    init {
        pagingDataFlow = queryFlow
            .flatMapLatest { getPosts(query = it) }
            .cachedIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        queryFlow.value = query
    }

    private fun getPosts(query: String?): Flow<PagingData<Post>> = repository
        .getPosts(query)
}
