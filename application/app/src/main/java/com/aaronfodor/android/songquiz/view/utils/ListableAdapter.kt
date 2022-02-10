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
import androidx.recyclerview.widget.DiffUtil
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

class ListableAdapter(private val context: Context,
                      private val onPrimaryAction: ListableAction,
                      private val onSecondaryAction: ListableAction,
                      private val onSwipeAction: ListableAction,
                      private val onLastItemReached: () -> Unit) :
        ListAdapter<Listable, ListableAdapter.ListableViewHolder>(ListableDiffCallback){

    /* ViewHolder for item, takes the view binding, the click behaviors, and the context. */
    class ListableViewHolder(val itemBinding: ListableItemBinding,
                             val context: Context,
                             val onPrimaryAction: ListableAction,
                             val onSecondaryAction: ListableAction,
                             val onSwipeAction: ListableAction) :
            RecyclerView.ViewHolder(itemBinding.root), View.OnCreateContextMenuListener {

        private val imageSize = context.resources.getDimension(R.dimen.list_item_image_pixels).toInt()
        private var onChange = MenuItem.OnMenuItemClickListener { false }

        init {
            itemBinding.root.setOnCreateContextMenuListener(this)
        }

        fun bind(item: Listable){
            itemBinding.listableItemLayout.setOnClickListener {
                onPrimaryAction.action(item)
            }
            itemBinding.primaryAction.setOnClickListener {
                onPrimaryAction.action(item)
            }
            itemBinding.secondaryAction.setOnClickListener {
                onSecondaryAction.action(item)
            }

            onPrimaryAction.icon?.let {
                itemBinding.primaryAction.background = it
            }
            onSecondaryAction.icon?.let {
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
                    onPrimaryAction.text -> {
                        onPrimaryAction.action(item)
                        true
                    }
                    onSecondaryAction.text -> {
                        onSecondaryAction.action(item)
                        true
                    }
                    onSwipeAction.text -> {
                        onSwipeAction.action(item)
                        true
                    }
                    else -> {false}
                }
            }

        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            menu?.let {
                if(onPrimaryAction.text.isNotBlank()){
                    val primary = it.add(onPrimaryAction.text)
                    primary.setOnMenuItemClickListener(onChange)
                }

                if(onSecondaryAction.text.isNotBlank()){
                    val secondary = it.add(onSecondaryAction.text)
                    secondary.setOnMenuItemClickListener(onChange)
                }

                if(onSwipeAction.text.isNotBlank()){
                    val swipe = it.add(onSwipeAction.text)
                    swipe.setOnMenuItemClickListener(onChange)
                }
            }
        }

    }

    /* Creates and inflates view and return ListableViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ListableViewHolder{
        val itemBinding = ListableItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListableViewHolder(itemBinding, context, onPrimaryAction, onSecondaryAction, onSwipeAction)
    }

    /* Get current playlist and use it to bind view. */
    override fun onBindViewHolder(holder: ListableViewHolder, position: Int){
        // last item reached, invoke callback
        if(position == itemCount-1){
            onLastItemReached()
        }

        val item = getItem(position)
        holder.bind(item)
    }

    private fun setAppearingAnimation(viewToAnimate: View){
        val animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_left)
        viewToAnimate.startAnimation(animation)
    }

    override fun onViewRecycled(holder: ListableViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(holder.itemBinding.listableImage).clear(holder.itemBinding.listableImage)
    }

    override fun onViewAttachedToWindow(holder: ListableViewHolder) {
        setAppearingAnimation(holder.itemView)
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: ListableViewHolder) {
        holder.itemView.clearAnimation()
        super.onViewDetachedFromWindow(holder)
    }

}

object ListableDiffCallback : DiffUtil.ItemCallback<Listable>(){
    override fun areItemsTheSame(oldItem: Listable, newItem: Listable): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Listable, newItem: Listable): Boolean {
        return (
            oldItem.id == newItem.id &&
            oldItem.title == newItem.title &&
            oldItem.imageUri == newItem.imageUri &&
            oldItem.content1 == newItem.content1 &&
            oldItem.content2 == newItem.content2
            )
    }
}