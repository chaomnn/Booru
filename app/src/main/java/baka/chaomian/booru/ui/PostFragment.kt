package baka.chaomian.booru.ui

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import baka.chaomian.booru.R
import baka.chaomian.booru.data.Post
import baka.chaomian.booru.databinding.FragmentPostBinding
import baka.chaomian.booru.viewmodel.DownloadViewModel
import java.io.File
import java.io.FileInputStream

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
        private const val MIME_TYPE_IMAGE = "image/jpeg"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel.file.observe(viewLifecycleOwner) { file ->
            FileInputStream(file.absolutePath).use { input ->
                val fd = input.fd
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFileDescriptor(fd, null, options)
                options.inJustDecodeBounds = false
                // TODO
                val scale = minOf(
                    options.outWidth.toFloat() / resources.displayMetrics.widthPixels,
                    options.outHeight.toFloat() / resources.displayMetrics.heightPixels,
                    options.outWidth.toFloat() / resources.displayMetrics.heightPixels,
                    options.outHeight.toFloat() / resources.displayMetrics.widthPixels
                )
                options.inSampleSize = maxOf(1, scale.toInt().takeHighestOneBit())
                val bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options)
                imageView.setImageBitmap(bitmap)
            }
            progressBar.visibility = View.GONE

        }
        return super.onCreateView(inflater, container, savedInstanceState)
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
        viewModel.downloadImage(post.largeUrl, filename, requireContext().cacheDir)
        imageView.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
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
                        filename = ORIGINAL + filename
                        viewModel.downloadImage(post.originalUrl, filename, requireContext().cacheDir)
                    }

                    R.id.save_to_gallery -> {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        } else {
                            saveToGallery()
                        }
                    }

                    R.id.share -> {
                        val fileUri: Uri =
                            FileProvider.getUriForFile(
                                requireContext(),
                                requireContext().packageName + ".fileprovider",
                                viewModel.file.value!!)
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = MIME_TYPE_IMAGE
                            putExtra(Intent.EXTRA_STREAM, fileUri)
                            setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivity(Intent.createChooser(shareIntent, null))
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
            put(MediaStore.MediaColumns.MIME_TYPE, MIME_TYPE_IMAGE)
            put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis())
            // TODO save to own image folder
        }
        val uri = contentResolver.insert(mediaCollection, imageDetails)!!
        contentResolver.openOutputStream(uri).use { stream ->
            val file = File(requireContext().cacheDir.path, filename)
            stream!!.write(file.readBytes())
        }
    }
}
