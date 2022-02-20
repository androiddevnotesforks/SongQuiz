/*
 * Copyright (c) Aaron Fodor  - All Rights Reserved
 *
 * https://github.com/aaronfodor
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.aaronfodor.android.songquiz.view.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.*
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.ListableItemBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions

class Listable(
    val id: String,
    val title: String,
    val content1: String,
    val content2: String,
    val imageUri: String,
)

class ListableAction(val action: (Listable) -> Unit,
                     val icon: Drawable?,
                     val text: String
)

interface ListableListener{
    var primaryListableAction: ListableAction
    var secondaryListableAction: ListableAction
    var swipeListableAction: ListableAction
    fun lastListableReached()
}

class ListableAdapter : ListAdapter<Listable, ListableAdapter.ListableViewHolder>(ListableComparator){

    var listableListener: ListableListener? = null

    /* Creates and inflates view and return ListableViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ListableViewHolder {
        val itemBinding = ListableItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListableViewHolder(itemBinding)
    }

    /* Get current playlist and use it to bind view. */
    override fun onBindViewHolder(holder: ListableViewHolder, position: Int){
        // last item reached, invoke callback
        if(position == itemCount-1){
            listableListener?.lastListableReached()
        }

        val item = getItem(position)
        holder.bind(item)
    }

    override fun onViewRecycled(holder: ListableViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(holder.itemBinding.listableImage).clear(holder.itemBinding.listableImage)
    }

    override fun onViewAttachedToWindow(holder: ListableViewHolder) {
        holder.itemView.appear(R.anim.slide_in_left, true)
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: ListableViewHolder) {
        holder.itemView.clearAnimation()
        super.onViewDetachedFromWindow(holder)
    }

    /* ViewHolder for item, takes the view binding, the click behaviors, and the context. */
    inner class ListableViewHolder(val itemBinding: ListableItemBinding) : RecyclerView.ViewHolder(itemBinding.root), View.OnCreateContextMenuListener {

        private val imageSize = itemBinding.root.context.resources.getDimension(R.dimen.list_item_image_pixels).toInt()
        private var onChange = MenuItem.OnMenuItemClickListener { false }

        init {
            itemBinding.root.setOnCreateContextMenuListener(this)
        }

        fun bind(item: Listable){
            itemBinding.listableItemLayout.setOnClickListener {
                listableListener?.primaryListableAction?.action?.invoke(item)
            }
            itemBinding.primaryAction.setOnClickListener {
                listableListener?.primaryListableAction?.action?.invoke(item)
            }
            itemBinding.secondaryAction.setOnClickListener {
                listableListener?.secondaryListableAction?.action?.invoke(item)
            }

            listableListener?.primaryListableAction?.icon?.let {
                itemBinding.primaryAction.background = it
            }
            listableListener?.secondaryListableAction?.icon?.let {
                itemBinding.secondaryAction.background = it
            }

            itemBinding.title.text = item.title
            itemBinding.content1.text = item.content1
            itemBinding.content2.text = item.content2

            val options = RequestOptions()
                .centerCrop()
                // smaller image: only 2 bytes per pixel
                .format(DecodeFormat.PREFER_RGB_565)
                // specific, small image needed as thumbnail
                .override(imageSize, imageSize)
                .placeholder(R.drawable.icon_album)
                .error(R.drawable.icon_album)

            Glide.with(itemBinding.listableImage)
                .load(item.imageUri)
                .transition(DrawableTransitionOptions.with(DrawableCrossFadeFactory()))
                .apply(options)
                .into(itemBinding.listableImage)

            onChange = MenuItem.OnMenuItemClickListener {
                when (it.title) {
                    listableListener?.primaryListableAction?.text -> {
                        listableListener?.primaryListableAction?.action?.invoke(item)
                        true
                    }
                    listableListener?.secondaryListableAction?.text -> {
                        listableListener?.secondaryListableAction?.action?.invoke(item)
                        true
                    }
                    listableListener?.swipeListableAction?.text -> {
                        listableListener?.swipeListableAction?.action?.invoke(item)
                        true
                    }
                    else -> {false}
                }
            }
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            menu?.let {
                if(listableListener?.primaryListableAction?.text?.isNotBlank() == true){
                    val primary = it.add(listableListener?.primaryListableAction?.text)
                    primary.setOnMenuItemClickListener(onChange)
                }

                if(listableListener?.secondaryListableAction?.text?.isNotBlank() == true){
                    val secondary = it.add(listableListener?.secondaryListableAction?.text)
                    secondary.setOnMenuItemClickListener(onChange)
                }

                if(listableListener?.swipeListableAction?.text?.isNotBlank() == true){
                    val swipe = it.add(listableListener?.swipeListableAction?.text)
                    swipe.setOnMenuItemClickListener(onChange)
                }
            }
        }

    }

}