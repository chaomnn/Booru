package baka.chaomian.booru.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import baka.chaomian.booru.data.Post
import baka.chaomian.booru.databinding.PostGridItemBinding
import com.squareup.picasso.Picasso

class PostsAdapter(private val onClickListener: OnPictureClickListener) :
    PagingDataAdapter<Post, PostsAdapter.PostViewHolder>(DiffCallback) {

    class PostViewHolder(var binding: PostGridItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder(PostGridItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        val previewUrl = post!!.previewUrl
        val image = holder.binding.image
        Picasso.get().load(previewUrl).into(image)
        image.setOnClickListener {
            onClickListener.onClick(post)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.originalUrl == newItem.originalUrl
        }
    }

    class OnPictureClickListener(val onClickListener: (post: Post) -> Unit) {
        fun onClick(post: Post) = onClickListener(post)
    }
}
