package com.binye.yomo.ui.screen.settings

import androidx.lifecycle.ViewModel
import com.binye.yomo.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: FirebaseUser?
        get() = authRepository.currentUser

    fun signOut() {
        authRepository.signOut()
    }
}
