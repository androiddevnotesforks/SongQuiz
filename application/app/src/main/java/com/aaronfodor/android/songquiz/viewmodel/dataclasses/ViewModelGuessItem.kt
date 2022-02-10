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

package com.aaronfodor.android.songquiz.viewmodel.dataclasses

import com.aaronfodor.android.songquiz.model.quiz.GuessItem

class ViewModelGuessItem (
    val truth: String = "",
    val guess: String = "",
    val isAccepted: Boolean = false
)

fun GuessItem.toViewModelGuessItem() : ViewModelGuessItem {
    return ViewModelGuessItem(
        truth = this.truth,
        guess = this.guess,
        isAccepted = this.isAccepted
    )
}

fun List<GuessItem>.toViewModelGuessItemList() : List<ViewModelGuessItem> {
    val convertedGuessItems = mutableListOf<ViewModelGuessItem>()
    for(item in this){
        convertedGuessItems.add(item.toViewModelGuessItem())
    }
    return convertedGuessItems
}