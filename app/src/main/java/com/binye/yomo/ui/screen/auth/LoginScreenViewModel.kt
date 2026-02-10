package com.binye.yomo.ui.screen.auth

import androidx.lifecycle.ViewModel
import com.binye.yomo.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    val authRepository: AuthRepository
) : ViewModel()
