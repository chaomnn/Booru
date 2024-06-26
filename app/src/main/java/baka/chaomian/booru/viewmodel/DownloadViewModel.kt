package baka.chaomian.booru.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.Okio

class DownloadViewModel : ViewModel() {

    private val client = OkHttpClient()
    private val mutableFile = MutableLiveData<File>()

    val file: LiveData<File> = mutableFile

    private suspend fun Call.await() = suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    continuation.resume(response)
                } else {
                    continuation.resumeWithException(Exception("Error: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }
        })
        continuation.invokeOnCancellation {
            runCatching { cancel() }
        }
    }

    fun downloadFile(url: String, filename: String, cacheDir: File) {
        viewModelScope.launch {
            val file: File = withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url(url)
                    .build()
                client.newCall(request).await().use { response ->
                    if (response.isSuccessful) {
                        val downloadedFile = File(cacheDir, filename)
                        try {
                            Okio.buffer(Okio.sink(downloadedFile)).use { sink ->
                                val buffer = sink.buffer()
                                response.body()!!.source().use { source ->
                                    while (true) {
                                        ensureActive()
                                        if (source.read(buffer, 8 * 1024) < 0) {
                                            break
                                        }
                                    }
                                }
                            }
                            ensureActive()
                            downloadedFile
                        } catch (e: Exception) {
                            println("failed to create a file")
                            throw e
                        }
                    } else {
                        throw Exception("Failed response")
                    }
                }
            }
            mutableFile.value = file
        }
    }
}
