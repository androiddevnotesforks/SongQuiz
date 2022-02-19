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

import androidx.recyclerview.widget.DiffUtil

object ListableComparator : DiffUtil.ItemCallback<Listable>(){

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