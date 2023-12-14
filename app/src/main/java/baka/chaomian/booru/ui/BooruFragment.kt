package baka.chaomian.booru.ui

import android.annotation.SuppressLint
import android.app.SearchManager
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
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
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
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
import baka.chaomian.booru.data.Tag
import baka.chaomian.booru.databinding.FragmentBooruBinding
import baka.chaomian.booru.databinding.ToolbarSearchViewBinding
import baka.chaomian.booru.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BooruFragment : Fragment(R.layout.fragment_booru) {

    private lateinit var binding: FragmentBooruBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var postsAdapter: PostsAdapter

    private var searchQuery: String = ""

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
        val suggestionAdapter: CursorAdapter = SimpleCursorAdapter(context,
            android.R.layout.simple_list_item_1,
            null,
            arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1),
            intArrayOf(android.R.id.text1),
            0
        )

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
                            searchQuery = ""
                            submitSearchQuery()
                            return true
                        }
                    })
                }
                searchView.apply {
                    @SuppressLint("RestrictedApi")
                    findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text).threshold = 1
                    queryHint = getString(R.string.search)
                    suggestionsAdapter = suggestionAdapter
                    val suggestions: ArrayList<Tag> = arrayListOf()
                    setOnSuggestionListener(object : SearchView.OnSuggestionListener {
                        override fun onSuggestionSelect(position: Int): Boolean {
                            return false
                        }

                        override fun onSuggestionClick(position: Int): Boolean {
                            searchQuery = if (searchQuery.lastIndexOf(" ") > 0) {
                                searchQuery.replaceAfterLast(" ", "${suggestions[position].name} ")
                            } else {
                                "${suggestions[position].name} "
                            }
                            setQuery(searchQuery, false)
                            suggestions.clear()
                            return true
                        }
                    })

                    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String): Boolean {
                            submitSearchQuery()
                            clearFocus()
                            suggestionsAdapter.cursor.close()
                            return false
                        }

                        override fun onQueryTextChange(newText: String): Boolean {
                            if (!isVisible() || newText.isEmpty() || newText.endsWith(" ")) {
                                // Fragment was replaced or query is empty
                                return true
                            }
                            // TODO 12.12.23 Fix search showing on space
                            searchQuery = newText
                            val lastTag = searchQuery.split(" ").last()
                            viewModel.getTags(lastTag)
                            viewModel.tags.observe(viewLifecycleOwner) { tags ->
                                suggestions.clear()
                                suggestions.addAll(tags)
                                val cursor = MatrixCursor(arrayOf(
                                    BaseColumns._ID,
                                    SearchManager.SUGGEST_COLUMN_TEXT_1,
                                    SearchManager.SUGGEST_COLUMN_INTENT_DATA
                                ))
                                suggestions.forEachIndexed { index, tag ->
                                    val text = if (tag.antecedent != null) "${tag.antecedent} → ${tag.label}" else
                                        tag.label
                                    cursor.addRow(arrayOf(index.toString(), text, tag.name))
                                }
                                suggestionAdapter.swapCursor(cursor)
                            }
                            return false
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

    private fun submitSearchQuery() {
        recyclerView.scrollToPosition(0)
        viewModel.onSearchQueryChanged(searchQuery)
        postsAdapter.refresh()
    }
}
