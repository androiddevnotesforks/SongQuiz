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

package com.aaronfodor.android.songquiz.model.repository.dataclasses

import com.aaronfodor.android.songquiz.model.api.dataclasses.ImagesDTO

fun Array<ImagesDTO>.getProperImageUri() : String {
    var imageUri = ""

    this.let {
        var imageDim = 0
        val maxImageDim = 680

        for(image in it){
            if(imageUri.isBlank() && image.url.isNotBlank()){
                imageUri = image.url
                imageDim = image.height
            }

            if((image.height in imageDim until maxImageDim) && image.url.isNotBlank()){
                imageUri = image.url
                imageDim = image.height
            }
        }
    }

    return imageUri
}