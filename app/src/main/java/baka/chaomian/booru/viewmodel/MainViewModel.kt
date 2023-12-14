package baka.chaomian.booru.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import baka.chaomian.booru.data.Post
import baka.chaomian.booru.data.PostRepository
import baka.chaomian.booru.data.Tag
import baka.chaomian.booru.network.DanbooruService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    val pagingDataFlow: Flow<PagingData<Post>>
    private val repository = PostRepository()
    private val queryFlow = MutableStateFlow<String?>(null)

    private val mutableTags = MutableLiveData<List<Tag>>()
    val tags: LiveData<List<Tag>> = mutableTags

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

    fun getTags(query: String) {
        viewModelScope.launch {
            try {
                mutableTags.value = DanbooruService.service.getTags(query).map {
                    Tag(it.label, it.name, it.category, it.postCount, it.antecedent)
                }
            } catch (e: Exception) {
                println(e)
                mutableTags.value = listOf()
            }
        }
    }
}
