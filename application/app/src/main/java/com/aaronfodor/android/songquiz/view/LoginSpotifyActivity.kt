package com.aaronfodor.android.songquiz.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.spotify.sdk.android.auth.LoginActivity

class LoginSpotifyActivity : LoginActivity() {

    companion object{
        private const val EXTRA_AUTH_REQUEST = "EXTRA_AUTH_REQUEST"
        private const val EXTRA_AUTH_RESPONSE = "EXTRA_AUTH_RESPONSE"

        fun getSpotifyAuthIntent(contextActivity: Activity, request: AuthorizationRequest): Intent {
            // Put request into a bundle to work around classloader problems on Samsung devices
            // https://stackoverflow.com/questions/28589509/android-e-parcel-class-not-found-when-unmarshalling-only-on-samsung-tab3
            val bundle = Bundle()
            bundle.putParcelable(REQUEST_KEY, request)

            val intent = Intent(contextActivity, LoginSpotifyActivity::class.java)
            intent.putExtra(EXTRA_AUTH_REQUEST, bundle)
            return intent
        }
    }

    override fun onClientComplete(response: AuthorizationResponse?) {
        // Put response into a bundle to work around classloader problems on Samsung devices
        // https://stackoverflow.com/questions/28589509/android-e-parcel-class-not-found-when-unmarshalling-only-on-samsung-tab3
        val bundle = Bundle()
        // to deliver the response to the caller, explicit class loader definition is needed
        bundle.classLoader = LoginSpotifyActivity::class.java.classLoader
        bundle.putParcelable(RESPONSE_KEY, response)

        val resultIntent = Intent()
        resultIntent.putExtra(EXTRA_AUTH_RESPONSE, bundle)
        Log.i(LoginSpotifyActivity.javaClass.simpleName,
            String.format("Spotify auth completed. The response is in EXTRA with key $RESPONSE_KEY"))

        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

}