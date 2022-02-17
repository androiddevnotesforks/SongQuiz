package com.aaronfodor.android.songquiz.viewmodel

import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.viewmodel.utils.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(accountService: AccountService) : AppViewModel(accountService) {}