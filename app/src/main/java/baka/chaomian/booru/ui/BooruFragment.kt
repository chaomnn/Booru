package baka.chaomian.booru.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import baka.chaomian.booru.R
import baka.chaomian.booru.databinding.FragmentBooruBinding
import baka.chaomian.booru.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BooruFragment : Fragment(R.layout.fragment_booru) {

    private lateinit var binding : FragmentBooruBinding

    private val viewModel: MainViewModel by viewModels()

    companion object {
        private const val KEY_FRAGMENT = "switch_fragment"
        private const val KEY_POST = "post"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBooruBinding.bind(view)
        val progressBar = binding.progressBar
        val errorView = binding.errorView
        val posts = viewModel.pagingDataFlow
        val recyclerView = binding.picsRecycler
        val postsAdapter = PostsAdapter(PostsAdapter.OnPictureClickListener { post ->
            setFragmentResult(KEY_FRAGMENT, bundleOf(Pair(KEY_POST, post)))
        })
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                posts.collectLatest { posts ->
                    postsAdapter.submitData(posts)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                postsAdapter.loadStateFlow.collect { states ->
                    progressBar.isVisible = states.source.prepend is LoadState.Loading ||
                            states.source.append is LoadState.Loading
                    errorView.isVisible = states.source.prepend is LoadState.Error ||
                            states.source.append is LoadState.Error
                }
            }
        }
        recyclerView.adapter = postsAdapter
        val layoutManager = GridLayoutManager(context, 2)
        recyclerView.layoutManager = layoutManager
    }
}
