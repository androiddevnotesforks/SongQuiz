package com.aaronfodor.android.songquiz.view

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.ActivityAuthBinding
import com.aaronfodor.android.songquiz.view.utils.*
import com.aaronfodor.android.songquiz.viewmodel.*
import com.aaronfodor.android.songquiz.viewmodel.utils.ViewModelAccountState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppActivity(keepScreenAlive = false) {

    private lateinit var binding: ActivityAuthBinding
    override lateinit var viewModel: AuthViewModel

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

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // When authentication is ready, does the activity should finish itself
        forResultAuthNeeded = intent.extras?.getBoolean(AuthRequestContract.FOR_RESULT_AUTH_SCREEN_KEY) ?: false

        val drawableQuestion = ContextCompat.getDrawable(applicationContext, R.drawable.icon_question)
        binding.loginHelp.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableQuestion, null)

        val drawableInfo = ContextCompat.getDrawable(applicationContext, R.drawable.icon_info)
        binding.whyLoginInfo.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableInfo, null)
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

    private fun skipLoginTapped() {
        hideAuthActions()
        //showing the next screen
        showNextScreenCalled = false
        showMenuActivity(false)
    }

    private fun loginHelpTapped(){
        val warningDialog = AppDialog(this, getString(R.string.login_help_title),
            getString(R.string.login_help_dialog), R.drawable.icon_warning)
        warningDialog.setPositiveButton{}
        warningDialog.show()
    }

    private fun whyLoginTapped(){
        val infoDialog = AppDialog(this, getString(R.string.login_why_title),
            getString(R.string.login_why_dialog), R.drawable.icon_info)
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

        binding.loginHelp.setOnClickListener {
            loginHelpTapped()
        }

        binding.whyLoginInfo.setOnClickListener {
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
                    showMenuActivity(true)
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

        val accountStateObserver = Observer<ViewModelAccountState> { accountState ->
            if(accountState == ViewModelAccountState.LOGGED_IN && !viewModel.isAuthNeeded()){
                showMenuActivity(true)
            }
            else{
                login()
            }
        }
        viewModel.accountState.observe(this, accountStateObserver)
    }

    override fun appearingAnimations() {}

    private fun showAuthActions(){
        binding.btnLogin.appear(R.anim.slide_in_bottom, true)
        binding.btnSkip.appear(R.anim.slide_in_bottom, true)
        binding.loginHelp.appear(R.anim.slide_in_bottom, true)
        binding.whyLoginInfo.appear(R.anim.slide_in_bottom, true)
    }

    private fun hideAuthActions(){
        binding.btnLogin.disappear(R.anim.slide_out_bottom)
        binding.btnSkip.disappear(R.anim.slide_out_bottom)
        binding.loginHelp.disappear(R.anim.slide_out_bottom)
        binding.whyLoginInfo.disappear(R.anim.slide_out_bottom)
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

    private fun showMenuActivity(isAuthenticated: Boolean){
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