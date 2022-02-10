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

package com.aaronfodor.android.songquiz.model.quiz

sealed class InformationItem()

class Speech(
    val text: String
) : InformationItem()

class SoundURL(
    val url: String
) : InformationItem()

class LocalSound(
    val name: String
) : InformationItem()

class ExitRequest() : InformationItem()

data class GuessItem(
    val truth: String,
    val guess: String,
    val isAccepted: Boolean
)

class GuessFeedback(
    val text: String,
    val items: List<GuessItem>
) : InformationItem()

/**
 * contents: list of InformationItems
 * immediateAnswerNeeded: whether immediate answer is required after broadcasting contents to user
 */
data class InformationPacket(
    val contents: List<InformationItem>,
    val immediateAnswerNeeded: Boolean
)