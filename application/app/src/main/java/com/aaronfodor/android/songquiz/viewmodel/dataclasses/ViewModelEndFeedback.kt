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

import com.aaronfodor.android.songquiz.model.quiz.EndFeedback

class ViewModelEndFeedback (
    val winnerNames: String = "",
    val numWinners: Int = -1
)

fun EndFeedback.toViewModelEndFeedback() : ViewModelEndFeedback {
    return ViewModelEndFeedback(
        winnerNames = this.winnerNames,
        numWinners = this.numWinners
    )
}