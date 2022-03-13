package com.aaronfodor.android.songquiz.model

import android.app.Activity
import android.content.Context
import android.util.Log
import com.aaronfodor.android.songquiz.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injected anywhere as a singleton
 */
@Singleton
class AdvertisementService  @Inject constructor(
    @ApplicationContext val context: Context
){
    var interstitialAd: InterstitialAd? = null

    fun init(){
        // Initialize Ads
        MobileAds.initialize(context)
        loadInterstitialAd()
    }

    fun loadInterstitialAd(){
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, context.getString(R.string.adMobQuizAdId), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(this::class.simpleName, adError.message)
                interstitialAd = null
            }

            override fun onAdLoaded(intAd: InterstitialAd) {
                Log.d(this::class.simpleName, "Ad was loaded.")
                interstitialAd = intAd
                setInterstitialAdContentCallbacks()
            }
        })
    }

    private fun setInterstitialAdContentCallbacks(){
        interstitialAd?.let {
            // set callbacks
            it.fullScreenContentCallback = object: FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(this::class.simpleName, "Ad was dismissed.")
                    loadInterstitialAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                    Log.d(this::class.simpleName, "Ad failed to show.")
                    loadInterstitialAd()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(this::class.simpleName, "Ad showed fullscreen content.")
                    loadInterstitialAd()
                }
            }
        }
    }

    fun showInterstitialAd(activity: Activity){
        if(interstitialAd != null) {
            interstitialAd?.show(activity)
        }
        else {
            Log.d(this::class.simpleName, "The interstitial ad is not ready yet.")
        }
    }

}