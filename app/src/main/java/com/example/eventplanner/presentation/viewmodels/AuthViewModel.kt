package com.example.eventplanner.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventplanner.data.AuthRepository
import com.example.eventplanner.util.Resource
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthViewModel  @Inject constructor(
    private val repository : AuthRepository
):ViewModel() {

    //state for login operation
    private val _loginState = MutableStateFlow<Resource<AuthResult>?>(null)
    val loginState: StateFlow<Resource<AuthResult>?> = _loginState

    //State for registration operation
    private val _registerState = MutableStateFlow<Resource<AuthResult>?>(null)
    val registerState: StateFlow<Resource<AuthResult>?> = _registerState

    //function to log in user
    fun loginUser(email:String, password:String){
        viewModelScope.launch{
            //coroutine

        }
    }
}

