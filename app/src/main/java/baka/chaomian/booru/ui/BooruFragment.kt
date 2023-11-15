package baka.chaomian.booru.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MenuItem.OnActionExpandListener
import android.view.View
import androidx.appcompat.widget.SearchView
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
import androidx.recyclerview.widget.RecyclerView
import baka.chaomian.booru.R
import baka.chaomian.booru.databinding.FragmentBooruBinding
import baka.chaomian.booru.databinding.ToolbarSearchViewBinding
import baka.chaomian.booru.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BooruFragment : Fragment(R.layout.fragment_booru) {

    private lateinit var binding : FragmentBooruBinding
    private lateinit var searchQuery: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var postsAdapter: PostsAdapter

    private val viewModel: MainViewModel by viewModels()

    companion object {
        private const val KEY_FRAGMENT = "switch_fragment"
        private const val KEY_POST = "post"
        private const val STATE_SEARCH_QUERY = "search"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBooruBinding.bind(view)
        searchQuery = savedInstanceState?.getString(STATE_SEARCH_QUERY).orEmpty()
        val progressBar = binding.progressBar
        val errorView = binding.errorView
        val posts = viewModel.pagingDataFlow
        recyclerView = binding.picsRecycler
        postsAdapter = PostsAdapter(PostsAdapter.OnPictureClickListener { post ->
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

        val searchView = ToolbarSearchViewBinding.inflate(LayoutInflater.from(requireContext())).root

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_toolbar, menu)
                menu.findItem(R.id.search).apply {
                    actionView = searchView
                    setOnActionExpandListener(object : OnActionExpandListener {
                        override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                            if (searchQuery.isNotEmpty()) {
                                searchView.post {
                                    searchView.setQuery(searchQuery, false)
                                }
                            }
                            return true
                        }

                        override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                            submitSearchQuery("")
                            return true
                        }

                    })
                }
                searchView.apply {
                    queryHint = getString(R.string.search)
                    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String): Boolean {
                            searchView.clearFocus()
                            submitSearchQuery(query)
                            return true
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            return true
                        }
                    })
                }
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_SEARCH_QUERY, searchQuery)
    }

    private fun submitSearchQuery(query: String) {
        recyclerView.scrollToPosition(0)
        searchQuery = query
        viewModel.onSearchQueryChanged(searchQuery)
        postsAdapter.refresh()
    }
}
