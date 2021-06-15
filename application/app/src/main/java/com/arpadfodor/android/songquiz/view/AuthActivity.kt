package com.arpadfodor.android.songquiz.view

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.databinding.ActivityAuthBinding
import com.arpadfodor.android.songquiz.view.utils.AppActivity
import com.arpadfodor.android.songquiz.view.utils.AppDialog
import com.arpadfodor.android.songquiz.viewmodel.AuthAccountState
import com.arpadfodor.android.songquiz.viewmodel.AuthUiState
import com.arpadfodor.android.songquiz.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppActivity(keepScreenAlive = false) {

    companion object{
        const val DESTROY_SELF_WHEN_READY_KEY = "destroy self when ready"
    }

    private lateinit var binding: ActivityAuthBinding
    private lateinit var viewModel: AuthViewModel

    override var requiredPermissions = listOf(Manifest.permission.INTERNET)

    var loginStarted = false
    var showNextScreenCalled = false

    var finishSelfWhenReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        // When authentication is ready, does the activity should finish itself
        finishSelfWhenReady = intent.extras?.getBoolean(DESTROY_SELF_WHEN_READY_KEY) ?: false
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

    override fun subscribeViewModel() {

        binding.btnLogin.setOnClickListener {
            loginStarted = false
            login()
        }

        binding.btnSkip.setOnClickListener {
            showNextScreenCalled = false
            showNextScreen()
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
                    showNextScreen()
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
                showNextScreen()
            }
            else{
                login()
            }
        }
        viewModel.accountState.observe(this, accountStateObserver)
    }

    override fun appearingAnimations() {}
    override fun unsubscribeViewModel() {}

    private fun showInfo(infoType: AuthUiState){
        when(infoType){
            AuthUiState.ERROR_INTERNET -> {
                val message = getString(R.string.error_login_internet)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.uiState.postValue(AuthUiState.EMPTY)
            }
            AuthUiState.ERROR_DENIED -> {
                val message = getString(R.string.error_login_denied)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.uiState.postValue(AuthUiState.EMPTY)
            }
            AuthUiState.ERROR -> {
                val message = getString(R.string.error_login)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.uiState.postValue(AuthUiState.EMPTY)
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

    private fun showNextScreen(){
        if(showNextScreenCalled){
            return
        }
        loginStarted = true
        showNextScreenCalled = true

        if(finishSelfWhenReady){
            // Simply finish this activity
            finish()
        }
        else{
            // Show menu
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }
    }

}