package com.aaronfodor.android.songquiz.view

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.ActivityAuthBinding
import com.aaronfodor.android.songquiz.view.utils.AppActivity
import com.aaronfodor.android.songquiz.view.utils.AppDialog
import com.aaronfodor.android.songquiz.view.utils.AuthRequestContract
import com.aaronfodor.android.songquiz.viewmodel.AuthAccountState
import com.aaronfodor.android.songquiz.viewmodel.AuthUiState
import com.aaronfodor.android.songquiz.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppActivity(keepScreenAlive = false) {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var viewModel: AuthViewModel

    override var requiredPermissions = listOf(Manifest.permission.INTERNET)

    var loginStarted = false
    var showNextScreenCalled = false

    // whether activity must simply finish self after auth (when true), or needs to explicitly start an another activity
    var forResultAuthNeeded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        // When authentication is ready, does the activity should finish itself
        forResultAuthNeeded = intent.extras?.getBoolean(AuthRequestContract.FOR_RESULT_AUTH_SCREEN_KEY) ?: false
    }

    override fun onBackPressed() {
        val exitDialog = AppDialog(this, getString(R.string.exit_title),
            getString(R.string.exit_dialog), R.drawable.icon_exit)
        exitDialog.setPositiveButton {
            //showing the home screen - app is not visible but running
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }
        exitDialog.show()
    }

    fun skipLoginTapped() {
        val warningDialog = AppDialog(this, getString(R.string.why_login_title),
            getString(R.string.why_login_dialog), R.drawable.icon_info)
        warningDialog.setPositiveButton {
            //showing the next screen
            showNextScreenCalled = false
            showNextScreen(false)
        }
        warningDialog.show()
    }

    override fun subscribeViewModel() {

        binding.btnLogin.setOnClickListener {
            loginStarted = false
            login()
        }

        binding.btnSkip.setOnClickListener {
            skipLoginTapped()
        }
        
        val uiStateObserver = Observer<AuthUiState> { state ->

            if(state == AuthUiState.EMPTY){
                binding.btnLogin.visibility = View.VISIBLE
                binding.btnSkip.visibility = View.VISIBLE
            }

            if(state == AuthUiState.START_LOGIN){
                binding.loadIndicatorProgressBar.visibility = View.VISIBLE
            }
            else{
                binding.loadIndicatorProgressBar.visibility = View.GONE
            }

            when(state){
                AuthUiState.START_LOGIN -> {
                    login()
                }
                AuthUiState.SUCCESS -> {
                    showNextScreen(true)
                }
                AuthUiState.ERROR_DENIED -> {
                    showInfo(AuthUiState.ERROR_DENIED)
                }
                AuthUiState.ERROR_INTERNET -> {
                    showInfo(AuthUiState.ERROR_INTERNET)
                }
                AuthUiState.ERROR -> {
                    showInfo(AuthUiState.ERROR)
                }
                else -> {}
            }
        }
        viewModel.uiState.observe(this, uiStateObserver)

        val accountStateObserver = Observer<AuthAccountState> { accountState ->
            if(accountState == AuthAccountState.LOGGED_IN){
                showNextScreen(true)
            }
            else{
                login()
            }
        }
        viewModel.accountState.observe(this, accountStateObserver)
    }

    override fun appearingAnimations() {
        val topAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_top)
        binding.tvTitle.startAnimation(topAnimation)
        binding.tvTitle.visibility = View.VISIBLE

        val bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom)
        binding.AppIcon.startAnimation(bottomAnimation)
        binding.AppIcon.visibility = View.VISIBLE
    }

    override fun onboardingDialog() {}
    override fun unsubscribeViewModel() {}

    private fun showInfo(infoType: AuthUiState){
        when(infoType){
            AuthUiState.ERROR_INTERNET -> {
                val message = getString(R.string.error_login_internet)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.empty()
            }
            AuthUiState.ERROR_DENIED -> {
                val message = getString(R.string.error_login_denied)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.empty()
            }
            AuthUiState.ERROR -> {
                val message = getString(R.string.error_login)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.empty()
            }
            else -> {}
        }
    }

    private fun login(){
        if(loginStarted){
            return
        }
        loginStarted = true

        val request = viewModel.getLoginRequest()
        val intent = LoginSpotifyActivity.getSpotifyAuthIntent(this, request)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        ActivityResultContracts.StartActivityForResult()
        loginLauncher.launch(intent)
    }

    private var loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        viewModel.processLoginResult(result.resultCode, result.data ?: Intent())
    }

    private fun showNextScreen(isAuthenticated: Boolean){
        if(showNextScreenCalled){
            return
        }
        loginStarted = true
        showNextScreenCalled = true

        if(forResultAuthNeeded){
            // Simply finish this activity, an another started it for a result
            val result = Intent().putExtra(AuthRequestContract.IS_AUTH_SUCCESS_KEY, isAuthenticated)
            setResult(Activity.RESULT_OK, result)
            finish()
        }
        else{
            // Show menu
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }

    }

}