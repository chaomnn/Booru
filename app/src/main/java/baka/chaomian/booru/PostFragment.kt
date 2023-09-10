package baka.chaomian.booru

import android.Manifest
import android.content.ContentValues
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import baka.chaomian.booru.data.Post
import baka.chaomian.booru.databinding.FragmentPostBinding
import baka.chaomian.booru.viewmodel.DownloadViewModel
import java.io.File
import kotlinx.coroutines.launch

class PostFragment() : Fragment(R.layout.fragment_post) {

    constructor(post: Post) : this() {
        arguments = bundleOf(
            KEY_POST to post
        )
    }

    private val viewModel: DownloadViewModel by viewModels()

    private lateinit var binding: FragmentPostBinding
    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var post: Post
    private lateinit var filename: String

    private var isOriginal = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                saveToGallery()
            } else {
                Toast.makeText(requireContext(), R.string.missing_permission, Toast.LENGTH_SHORT).show()
            }
        }

    companion object {
        private const val KEY_POST = "post"
        private const val ORIGINAL = "original_"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPostBinding.bind(view)
        imageView = binding.image
        progressBar = binding.progressBar
        val arguments = requireArguments()
        post = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments.getParcelable(KEY_POST, Post::class.java)!!
        } else {
            @Suppress("Deprecation")
            arguments.getParcelable(KEY_POST)!!
        }
        filename = "${post.id}.jpg"
        showImage()
        imageView.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_overflow, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.view_original -> {
                        progressBar.visibility = View.VISIBLE
                        isOriginal = true
                        filename = ORIGINAL + filename
                        showImage()
                    }

                    R.id.save_to_gallery -> {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        } else {
                            saveToGallery()
                        }
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun saveToGallery() {
        val contentResolver = requireContext().contentResolver
        val mediaCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        else
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val imageDetails = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis())
            // TODO save to own image folder
        }
        val uri = contentResolver.insert(mediaCollection, imageDetails)!!
        contentResolver.openOutputStream(uri).use { stream ->
            val file = File(requireContext().cacheDir.path, filename)
            stream!!.write(file.readBytes())
        }
    }

    private fun showImage() {
        lifecycleScope.launch {
            val url = if (isOriginal) post.originalUrl else post.largeUrl
            viewModel.downloadImage(url, filename, requireContext().cacheDir)
            viewModel.file.observe(viewLifecycleOwner) { file ->
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                imageView.setImageBitmap(bitmap)
                progressBar.visibility = View.GONE
            }
        }
    }
}
