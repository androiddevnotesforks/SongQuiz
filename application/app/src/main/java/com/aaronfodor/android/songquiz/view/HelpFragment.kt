package com.aaronfodor.android.songquiz.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.aaronfodor.android.songquiz.R

class HelpFragment : Fragment(R.layout.fragment_help) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set boarding flag as needed
        val keyBoardingFlag = getString(R.string.PREF_KEY_BOARDING_SHOWED)
        val keyBoardingQuizFlag = getString(R.string.PREF_KEY_BOARDING_QUIZ_SHOWED)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        // persist showed flag to preferences
        with(sharedPreferences.edit()){
            remove(keyBoardingFlag)
            putBoolean(keyBoardingFlag, false)
            remove(keyBoardingQuizFlag)
            putBoolean(keyBoardingQuizFlag, false)
            apply()
        }

        // check whether boarding is needed - here it is needed
        (requireActivity() as MenuActivity).boardingCheck()
        showHomeScreen()
    }

    private fun showHomeScreen(){
        val navController = NavHostFragment.findNavController(this)
        // make sure that the current destination is the current fragment (filter duplicated calls)
        if(navController.currentDestination == navController.findDestination(R.id.nav_help)){
            val action = HelpFragmentDirections.actionNavHelpToNavHome()
            navController.navigate(action)
        }
    }

}