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
import com.aaronfodor.android.songquiz.view.utils.RequiredPermission
import com.aaronfodor.android.songquiz.viewmodel.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppActivity(keepScreenAlive = false) {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var viewModel: AuthViewModel

    override var requiredPermissions: List<RequiredPermission> = listOf()

    var loginStarted = false
    var showNextScreenCalled = false

    // whether activity must simply finish self after auth (when true), or needs to explicitly start an another activity
    var forResultAuthNeeded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requiredPermissions = listOf(RequiredPermission(Manifest.permission.INTERNET, getString(R.string.permission_internet), getString(R.string.permission_internet_explanation)))

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
        hideAuthActions()
        //showing the next screen
        showNextScreenCalled = false
        showNextScreen(false)
    }

    fun loginInfoTapped(){
        val warningDialog = AppDialog(this, getString(R.string.login_info_title),
            getString(R.string.login_info_dialog), R.drawable.icon_warning)
        warningDialog.setPositiveButton{}
        warningDialog.show()
    }

    fun whyLoginTapped(){
        val infoDialog = AppDialog(this, getString(R.string.why_login_title),
            getString(R.string.why_login_dialog), R.drawable.icon_info)
        infoDialog.setPositiveButton{}
        infoDialog.show()
    }

    override fun subscribeViewModel() {

        binding.btnLogin.setOnClickListener {
            loginStarted = false
            hideAuthActions()
            login()
        }

        binding.btnSkip.setOnClickListener {
            skipLoginTapped()
        }

        binding.btnLoginInfo.setOnClickListener {
            loginInfoTapped()
        }

        binding.btnWhyLogin.setOnClickListener {
            whyLoginTapped()
        }
        
        val uiStateObserver = Observer<AuthUiState> { state ->
            if(state == AuthUiState.EMPTY){
                showAuthActions()
            }

            if(state == AuthUiState.START_LOGIN){
                hideAuthActions()
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
                AuthUiState.EMPTY -> {}
            }
        }
        viewModel.uiState.observe(this, uiStateObserver)

        val notificationObserver = Observer<AuthNotification> { notification ->
            when(notification){
                AuthNotification.ERROR_INTERNET -> {
                    val message = getString(R.string.error_login_internet)
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(AuthNotification.NONE)
                }
                AuthNotification.ERROR_DENIED -> {
                    val message = getString(R.string.error_login_denied)
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(AuthNotification.NONE)
                }
                AuthNotification.ERROR -> {
                    val message = getString(R.string.error_login)
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(AuthNotification.NONE)
                }
                AuthNotification.NONE -> {}
                else -> {}
            }
        }
        viewModel.notification.observe(this, notificationObserver)

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

    override fun appearingAnimations() {}

    private fun showAuthActions(){
        val topAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_top)
        binding.tvTitle.startAnimation(topAnimation)
        binding.tvTitle.visibility = View.VISIBLE

        val bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom)
        binding.AppIcon.startAnimation(bottomAnimation)
        binding.AppIcon.visibility = View.VISIBLE

        binding.btnLogin.startAnimation(bottomAnimation)
        binding.btnLogin.visibility = View.VISIBLE

        binding.btnSkip.startAnimation(bottomAnimation)
        binding.btnSkip.visibility = View.VISIBLE

        binding.btnLoginInfo.startAnimation(bottomAnimation)
        binding.btnLoginInfo.visibility = View.VISIBLE

        binding.btnWhyLogin.startAnimation(bottomAnimation)
        binding.btnWhyLogin.visibility = View.VISIBLE
    }

    private fun hideAuthActions(){
        val topAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_out_top)
        binding.tvTitle.startAnimation(topAnimation)
        binding.tvTitle.visibility = View.GONE

        val bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom)
        binding.AppIcon.startAnimation(bottomAnimation)
        binding.AppIcon.visibility = View.GONE

        binding.btnLogin.startAnimation(bottomAnimation)
        binding.btnLogin.visibility = View.GONE

        binding.btnSkip.startAnimation(bottomAnimation)
        binding.btnSkip.visibility = View.GONE

        binding.btnLoginInfo.startAnimation(bottomAnimation)
        binding.btnLoginInfo.visibility = View.GONE

        binding.btnWhyLogin.startAnimation(bottomAnimation)
        binding.btnWhyLogin.visibility = View.GONE
    }

    override fun boardingDialog() {}
    override fun unsubscribeViewModel() {}

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