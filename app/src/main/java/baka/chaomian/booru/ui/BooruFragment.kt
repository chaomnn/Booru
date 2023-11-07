package baka.chaomian.booru.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
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
import baka.chaomian.booru.databinding.ToolbarSearchViewBinding
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
                    val prepend = states.source.prepend
                    val append = states.source.append
                    progressBar.isVisible = prepend is LoadState.Loading || append is LoadState.Loading
                    errorView.isVisible = prepend is LoadState.Error || append is LoadState.Error
                    binding.swipeRefresh.isRefreshing = states.source.refresh is LoadState.Loading &&
                            progressBar.isVisible == false
                }
            }
        }
        recyclerView.adapter = postsAdapter
        val layoutManager = GridLayoutManager(context, 2)
        recyclerView.layoutManager = layoutManager

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_toolbar, menu)
                val searchView = ToolbarSearchViewBinding.inflate(LayoutInflater.from(requireContext())).root
                menu.findItem(R.id.search).actionView = searchView
                searchView.queryHint = getString(R.string.search)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.refresh -> {
                        postsAdapter.refresh()
                    }
                }
                return true
            }
        }, viewLifecycleOwner)

        binding.swipeRefresh.setOnRefreshListener {
            postsAdapter.refresh()
        }
    }
}
