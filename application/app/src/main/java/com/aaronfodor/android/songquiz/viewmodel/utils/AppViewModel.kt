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

package com.aaronfodor.android.songquiz.viewmodel.utils

import androidx.lifecycle.*
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.AccountState
import kotlinx.coroutines.launch

enum class ViewModelAccountState{
    LOGGED_IN, LOGGED_OUT
}

enum class AuthNotification{
    AUTH_NEEDED, NONE
}

fun AccountState.toViewModelAccountState() : ViewModelAccountState{
    return when(this){
        AccountState.LOGGED_IN -> ViewModelAccountState.LOGGED_IN
        AccountState.LOGGED_OUT -> ViewModelAccountState.LOGGED_OUT
        else -> ViewModelAccountState.LOGGED_OUT
    }
}

open class AppViewModel(val accountService: AccountService) : ViewModel() {

    // subscribe to the service's MutableLiveData from the ViewModel with Transformations
    // this is needed to be MutableLiveData, as AuthActivity uses it to show next screen or the login screen
    val accountState : LiveData<ViewModelAccountState> = Transformations.map(accountService.accountState) { serviceAccountState ->
        serviceAccountState.toViewModelAccountState()
    }

    val authNotification: MutableLiveData<AuthNotification> by lazy {
        MutableLiveData<AuthNotification>()
    }

    // this code snippet is required to be ran after authentication is successful; if it is not successful, it is empty
    private var blockToRunAfterAuth: () -> Unit = {}
    private var authenticationNeededToLaunch: Boolean = true

    // if authentication fails, block is not executed
    fun mustAuthenticatedLaunch(blockToRun: () -> Unit){
        authenticatedLaunch(true, blockToRun)
    }

    // if authentication fails, block is still executed
    fun tryAuthenticateLaunch(blockToRun: () -> Unit){
        authenticatedLaunch(false, blockToRun)
    }

    private fun authenticatedLaunch(doesAuthenticationNeededToLaunch: Boolean, blockToRun: () -> Unit) = viewModelScope.launch {
        authenticationNeededToLaunch = doesAuthenticationNeededToLaunch

        if(accountService.isAuthNeeded()){
            blockToRunAfterAuth = blockToRun
            authNotification.postValue(AuthNotification.AUTH_NEEDED)
        }
        else{
            blockToRunAfterAuth = {}
            blockToRun()
        }
    }

    fun authenticationReturned(isAuthenticated: Boolean) = viewModelScope.launch {
        if(isAuthenticated || !authenticationNeededToLaunch){
            // authenticated so run the code, or authentication is not a must to run it
            blockToRunAfterAuth()
        }

        blockToRunAfterAuth = {}
    }

}