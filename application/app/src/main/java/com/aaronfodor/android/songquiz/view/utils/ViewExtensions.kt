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

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import android.view.animation.AnimationUtils
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

fun View.tappableInfiniteAnimation() : ObjectAnimator {
    this.tappableEndAnimation().start()

    val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.1f)
    val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.1f)
    val animation = ObjectAnimator.ofPropertyValuesHolder(this, scaleX, scaleY)
    animation.interpolator = FastOutSlowInInterpolator()
    animation.duration = 500L
    animation.repeatCount = ObjectAnimator.INFINITE
    animation.repeatMode = ObjectAnimator.REVERSE
    return animation
}

fun View.tappableEndAnimation() : ObjectAnimator {
    val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.00f)
    val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.00f)
    val animation = ObjectAnimator.ofPropertyValuesHolder(this, scaleX, scaleY)
    animation.interpolator = FastOutSlowInInterpolator()
    animation.duration = 0L
    animation.repeatCount = 0
    animation.repeatMode = ObjectAnimator.RESTART
    return animation
}

fun View.changedAnimation() : ObjectAnimator {
    val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.25f)
    val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.25f)
    val animation = ObjectAnimator.ofPropertyValuesHolder(this, scaleX, scaleY)
    animation.interpolator = FastOutSlowInInterpolator()
    animation.duration = 500L
    animation.repeatCount = 1 // because it should go back to the original state
    animation.repeatMode = ObjectAnimator.REVERSE
    return animation
}

fun View.appear(animationRes: Int, forceAnimation: Boolean = false){
    if(this.visibility != View.VISIBLE || forceAnimation){
        val animation = AnimationUtils.loadAnimation(context, animationRes)
        this.startAnimation(animation)
        this.visibility = View.VISIBLE
    }
}

fun View.disappear(animationRes: Int, forceAnimation: Boolean = false){
    if(this.visibility == View.VISIBLE || forceAnimation){
        val animation = AnimationUtils.loadAnimation(context, animationRes)
        this.startAnimation(animation)
        this.visibility = View.INVISIBLE
    }
}