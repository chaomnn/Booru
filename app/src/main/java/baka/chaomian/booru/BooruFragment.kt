package baka.chaomian.booru

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import baka.chaomian.booru.databinding.FragmentBooruBinding
import baka.chaomian.booru.viewmodel.MainViewModel

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
        val recyclerView = binding.picsRecycler
        val postsAdapter = PostsAdapter(PostsAdapter.OnPictureClickListener { post ->
            setFragmentResult(KEY_FRAGMENT, bundleOf(Pair(KEY_POST, post)))
        })
        viewModel.posts.observe(viewLifecycleOwner) {
            postsAdapter.submitList(it)
        }
        viewModel.status.observe(viewLifecycleOwner) { status ->
            when (status!!) {
                MainViewModel.Status.ERROR -> {
                    progressBar.visibility = View.GONE
                    errorView.visibility = View.VISIBLE
                }
                MainViewModel.Status.LOADING -> {
                    progressBar.visibility = View.VISIBLE
                }
                MainViewModel.Status.DONE -> {
                    progressBar.visibility = View.GONE
                }
            }
        }
        recyclerView.adapter = postsAdapter
        val layoutManager = GridLayoutManager(context, 2)
        recyclerView.layoutManager = layoutManager
    }
}
