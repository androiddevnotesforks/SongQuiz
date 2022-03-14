package com.aaronfodor.android.songquiz.model

import android.app.Activity
import android.content.Context
import com.aaronfodor.android.songquiz.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injected anywhere as a singleton
 */
@Singleton
class AdvertisementService  @Inject constructor(
    @ApplicationContext val context: Context,
    val loggerService: LoggerService
){
    var rewardedInterstitialAd: RewardedInterstitialAd? = null
    var rewardListener = OnUserEarnedRewardListener{}

    fun init(){
        // Initialize Ads
        MobileAds.initialize(context)
        loadRewardedInterstitialAd()
    }

    fun loadRewardedInterstitialAd(){
        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(context, context.getString(R.string.adMobQuizAdId), adRequest, object : RewardedInterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                loggerService.d(this::class.simpleName, adError.message)
                rewardedInterstitialAd = null
            }

            override fun onAdLoaded(intAd: RewardedInterstitialAd ) {
                loggerService.d(this::class.simpleName, "Ad was loaded.")
                rewardedInterstitialAd = intAd
                setRewardedInterstitialAdContentCallbacks {}
            }
        })
    }

    private fun setRewardedInterstitialAdContentCallbacks(finishedAction: () -> Unit){
        rewardedInterstitialAd?.let {
            // set callbacks
            it.fullScreenContentCallback = object: FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    loggerService.d(this::class.simpleName, "Ad started to show fullscreen content.")
                }

                override fun onAdDismissedFullScreenContent() {
                    loggerService.d(this::class.simpleName, "Ad was dismissed.")
                    finishedAction()
                    loadRewardedInterstitialAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                    loggerService.d(this::class.simpleName, "Ad failed to show.")
                    finishedAction()
                    loadRewardedInterstitialAd()
                }
            }
        }
    }

    fun setRewardedInterstitialAdCallbacks(finishedAction: () -> Unit, rewardAction: (Int) -> Unit){
        rewardedInterstitialAd?.let {
            setRewardedInterstitialAdContentCallbacks(finishedAction)

            rewardListener = OnUserEarnedRewardListener { reward ->
                loggerService.d(this::class.simpleName, "Reward earned.")
                rewardAction(reward.amount)
            }
        }
    }

    fun showRewardedInterstitialAd(activity: Activity){
        if(rewardedInterstitialAd != null) {
            rewardedInterstitialAd?.show(activity, rewardListener)
        }
        else {
            loggerService.d(this::class.simpleName, "The rewarded interstitial ad is not ready yet.")
        }
    }

}