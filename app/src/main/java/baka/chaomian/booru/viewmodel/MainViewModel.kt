package baka.chaomian.booru.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import baka.chaomian.booru.data.Post
import baka.chaomian.booru.network.DanbooruService
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    enum class Status { LOADING, ERROR, DONE }

    private val mutableStatus = MutableLiveData<Status>()
    private val mutablePosts = MutableLiveData<List<Post>>()

    val status: LiveData<Status> = mutableStatus
    val posts: LiveData<List<Post>> = mutablePosts

    init {
        getRecentPosts()
    }

    private fun getRecentPosts() {
        viewModelScope.launch {
            mutableStatus.value = Status.LOADING
            try {
                val booruPosts = DanbooruService.service.getPosts()
                val posts: List<Post> = booruPosts
                    .filter {
                        it.previewUrl != null && it.originalUrl != null && it.largeUrl != null
                    }
                    .map {
                        Post(it.id, it.previewUrl!!, it.originalUrl!!, it.largeUrl!!)
                    }
                mutablePosts.value = posts
                mutableStatus.value = Status.DONE
                println("Fetched " + mutablePosts.value!!.size + " posts")
            } catch (e: Exception) {
                mutableStatus.value = Status.ERROR
                mutablePosts.value = listOf()
                println("Failed to get posts: \n $e")
            }
        }
    }
}
