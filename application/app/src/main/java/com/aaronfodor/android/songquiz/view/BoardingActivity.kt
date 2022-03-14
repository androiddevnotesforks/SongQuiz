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

package com.aaronfodor.android.songquiz.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.aaronfodor.android.songquiz.R
import com.github.appintro.AppIntro

class BoardingActivity : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make sure you don't call setContentView!
        // full screen
        setImmersiveMode()
        // Skip button is replaced with back arrow
        isWizardMode = true
        // color transition flag
        isColorTransitionsEnabled = true

        val mascotName = getString(R.string.mascot_name)
        val appName = getString(R.string.app_name)

        // Call addSlide passing your Fragments.
        // You can use AppIntroFragment to use a pre-built fragment
        addSlide(BoardingIntroFragment.createInstance(
            title = getString(R.string.boarding1_title),
            description = getString(R.string.boarding1_content, mascotName, appName),
            imageDrawable = R.drawable.song_quiz,
            backgroundColorRes = R.color.colorAccent,
            titleColorRes = R.color.colorText,
            descriptionColorRes = R.color.colorText,
            titleTypefaceFontRes = R.font.font_subtitle,
            descriptionTypefaceFontRes = R.font.font_paragraph,
        ))
        addSlide(BoardingIntroFragment.createInstance(
            title = getString(R.string.boarding2_title),
            description = getString(R.string.boarding2_content),
            imageDrawable = R.drawable.icon_question,
            backgroundColorRes = R.color.colorBackground,
            titleColorRes = R.color.colorText,
            descriptionColorRes = R.color.colorText,
            titleTypefaceFontRes = R.font.font_subtitle,
            descriptionTypefaceFontRes = R.font.font_paragraph,
        ))
        addSlide(BoardingIntroFragment.createInstance(
            title = getString(R.string.boarding3_title),
            description = getString(R.string.boarding3_content),
            imageDrawable = R.drawable.icon_profile,
            backgroundColorRes = R.color.colorAccent,
            titleColorRes = R.color.colorText,
            descriptionColorRes = R.color.colorText,
            titleTypefaceFontRes = R.font.font_subtitle,
            descriptionTypefaceFontRes = R.font.font_paragraph,
        ))
        addSlide(BoardingIntroFragment.createInstance(
            title = getString(R.string.boarding4_title),
            description = getString(R.string.boarding4_content),
            imageDrawable = R.drawable.icon_start_game,
            backgroundColorRes = R.color.colorBackground,
            titleColorRes = R.color.colorText,
            descriptionColorRes = R.color.colorText,
            titleTypefaceFontRes = R.font.font_subtitle,
            descriptionTypefaceFontRes = R.font.font_paragraph,
        ))
        addSlide(BoardingIntroFragment.createInstance(
            title = getString(R.string.boarding5_title),
            description = getString(R.string.boarding_quiz_speech),
            imageDrawable = R.drawable.icon_sound_on,
            backgroundColorRes = R.color.colorAccent,
            titleColorRes = R.color.colorText,
            descriptionColorRes = R.color.colorText,
            titleTypefaceFontRes = R.font.font_subtitle,
            descriptionTypefaceFontRes = R.font.font_paragraph,
        ))
        addSlide(BoardingIntroFragment.createInstance(
            title = getString(R.string.boarding6_title),
            description = getString(R.string.boarding_quiz_user_input),
            imageDrawable = R.drawable.icon_mic_on,
            backgroundColorRes = R.color.colorBackground,
            titleColorRes = R.color.colorText,
            descriptionColorRes = R.color.colorText,
            titleTypefaceFontRes = R.font.font_subtitle,
            descriptionTypefaceFontRes = R.font.font_paragraph,
        ))
        addSlide(BoardingIntroFragment.createInstance(
            title = getString(R.string.boarding7_title),
            description = getString(R.string.boarding7_content),
            imageDrawable = R.drawable.icon_keyboard,
            backgroundColorRes = R.color.colorAccent,
            titleColorRes = R.color.colorText,
            descriptionColorRes = R.color.colorText,
            titleTypefaceFontRes = R.font.font_subtitle,
            descriptionTypefaceFontRes = R.font.font_paragraph,
        ))
        addSlide(BoardingIntroFragment.createInstance(
            title = getString(R.string.boarding8_title),
            description = getString(R.string.boarding8_content),
            imageDrawable = R.drawable.icon_favourite,
            backgroundColorRes = R.color.colorBackground,
            titleColorRes = R.color.colorText,
            descriptionColorRes = R.color.colorText,
            titleTypefaceFontRes = R.font.font_subtitle,
            descriptionTypefaceFontRes = R.font.font_paragraph,
        ))
        addSlide(BoardingIntroFragment.createInstance(
            title = getString(R.string.boarding9_title),
            description = getString(R.string.boarding9_content),
            imageDrawable = R.drawable.icon_playlists,
            backgroundColorRes = R.color.colorAccent,
            titleColorRes = R.color.colorText,
            descriptionColorRes = R.color.colorText,
            titleTypefaceFontRes = R.font.font_subtitle,
            descriptionTypefaceFontRes = R.font.font_paragraph,
        ))
        addSlide(BoardingIntroFragment.createInstance(
            title = getString(R.string.boarding10_title),
            description = getString(R.string.boarding10_content),
            imageDrawable = R.drawable.icon_add_playlist,
            backgroundColorRes = R.color.colorBackground,
            titleColorRes = R.color.colorText,
            descriptionColorRes = R.color.colorText,
            titleTypefaceFontRes = R.font.font_subtitle,
            descriptionTypefaceFontRes = R.font.font_paragraph,
        ))
        addSlide(BoardingIntroFragment.createInstance(
            title = getString(R.string.boarding11_title),
            description = getString(R.string.boarding11_content),
            imageDrawable = R.drawable.song_quiz,
            backgroundColorRes = R.color.colorAccent,
            titleColorRes = R.color.colorText,
            descriptionColorRes = R.color.colorText,
            titleTypefaceFontRes = R.font.font_subtitle,
            descriptionTypefaceFontRes = R.font.font_paragraph,
        ))
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Decide what to do when the user clicks on "Skip"
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        // Decide what to do when the user clicks on "Done"
        finish()
    }
}