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

import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import com.aaronfodor.android.songquiz.R
import com.github.appintro.AppIntroBaseFragment
import com.github.appintro.AppIntroFragment
import com.github.appintro.model.SliderPage

class BoardingIntroFragment : AppIntroBaseFragment() {
    override val layoutId: Int get() = R.layout.fragment_boarding_intro

    companion object {
        /**
         * Generates a new instance for [BoardingIntroFragment]
         *
         * @param title CharSequence which will be the slide title
         * @param description CharSequence which will be the slide description
         * @param imageDrawable @DrawableRes (Integer) the image that will be
         *                             displayed, obtained from Resources
         * @param backgroundColor @ColorInt (Integer) custom background color
         * @param titleColor @ColorInt (Integer) custom title color
         * @param descriptionColor @ColorInt (Integer) custom description color
         * @param titleTypefaceFontRes @FontRes (Integer) custom title typeface obtained
         *                             from Resources
         * @param descriptionTypefaceFontRes @FontRes (Integer) custom description typeface obtained
         *                             from Resources
         * @param backgroundDrawable @DrawableRes (Integer) custom background drawable
         *
         * @return An [BoardingIntroFragment] created instance
         */
        @JvmOverloads
        @JvmStatic
        @Deprecated(
            "`newInstance` is deprecated to support color resources instead of color int " +
                "for configuration changes and dark theme support",
            ReplaceWith(
                "createInstance(title, description, imageDrawable, backgroundColor, " +
                    "titleColor, descriptionColor, titleTypefaceFontRes, descriptionTypefaceFontRes, " +
                    "backgroundDrawable)"
            )
        )
        fun newInstance(
            title: CharSequence? = null,
            description: CharSequence? = null,
            @DrawableRes imageDrawable: Int = 0,
            @ColorInt backgroundColor: Int = 0,
            @ColorInt titleColor: Int = 0,
            @ColorInt descriptionColor: Int = 0,
            @FontRes titleTypefaceFontRes: Int = 0,
            @FontRes descriptionTypefaceFontRes: Int = 0,
            @DrawableRes backgroundDrawable: Int = 0
        ): BoardingIntroFragment {
            return createInstance(
                SliderPage(
                    title = title,
                    description = description,
                    imageDrawable = imageDrawable,
                    backgroundColor = backgroundColor,
                    titleColor = titleColor,
                    descriptionColor = descriptionColor,
                    titleTypefaceFontRes = titleTypefaceFontRes,
                    descriptionTypefaceFontRes = descriptionTypefaceFontRes,
                    backgroundDrawable = backgroundDrawable
                )
            )
        }

        /**
         * Generates a new instance for [BoardingIntroFragment]
         *
         * @param title CharSequence which will be the slide title
         * @param description CharSequence which will be the slide description
         * @param imageDrawable @DrawableRes (Integer) the image that will be
         *                             displayed, obtained from Resources
         * @param backgroundColorRes @ColorRes (Integer) custom background color
         * @param titleColorRes @ColorRes (Integer) custom title color
         * @param descriptionColorRes @ColorRes (Integer) custom description color
         * @param titleTypefaceFontRes @FontRes (Integer) custom title typeface obtained
         *                             from Resources
         * @param descriptionTypefaceFontRes @FontRes (Integer) custom description typeface obtained
         *                             from Resources
         * @param backgroundDrawable @DrawableRes (Integer) custom background drawable
         *
         * @return An [BoardingIntroFragment] created instance
         */
        @JvmOverloads
        @JvmStatic
        fun createInstance(
            title: CharSequence? = null,
            description: CharSequence? = null,
            @DrawableRes imageDrawable: Int = 0,
            @ColorRes backgroundColorRes: Int = 0,
            @ColorRes titleColorRes: Int = 0,
            @ColorRes descriptionColorRes: Int = 0,
            @FontRes titleTypefaceFontRes: Int = 0,
            @FontRes descriptionTypefaceFontRes: Int = 0,
            @DrawableRes backgroundDrawable: Int = 0
        ): BoardingIntroFragment {
            return createInstance(
                SliderPage(
                    title = title,
                    description = description,
                    imageDrawable = imageDrawable,
                    backgroundColorRes = backgroundColorRes,
                    titleColorRes = titleColorRes,
                    descriptionColorRes = descriptionColorRes,
                    titleTypefaceFontRes = titleTypefaceFontRes,
                    descriptionTypefaceFontRes = descriptionTypefaceFontRes,
                    backgroundDrawable = backgroundDrawable
                )
            )
        }

        /**
         * Generates an [BoardingIntroFragment] from a given [SliderPage]
         *
         * @param sliderPage the [SliderPage] object which contains all attributes for
         * the current slide
         *
         * @return An [BoardingIntroFragment] created instance
         */
        @JvmStatic
        @Deprecated(
            "`newInstance` is deprecated to support color resources instead of color int " +
                "for configuration changes and dark theme support",
            ReplaceWith(
                "createInstance(sliderPage)"
            )
        )
        fun newInstance(sliderPage: SliderPage) = createInstance(sliderPage)

        /**
         * Generates an [BoardingIntroFragment] from a given [SliderPage]
         *
         * @param sliderPage the [SliderPage] object which contains all attributes for
         * the current slide
         *
         * @return An [BoardingIntroFragment] created instance
         */
        @JvmStatic
        fun createInstance(sliderPage: SliderPage): BoardingIntroFragment {
            val slide = BoardingIntroFragment()
            slide.arguments = sliderPage.toBundle()
            return slide
        }
    }
}