package com.example.eventplanner.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import com.example.eventplanner.util.Resource
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.flow.flow


class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository{

    override fun loginUser(email: String, password: String): Flow<Resource<AuthResult>> = flow {
        emit(Resource.Loading())// Emit a loading state
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(
                Resource.Error(
                    e.localizedMessage ?: "An unexpected error occured"
                )
            )// Emit error state with message
        }
    }

    override fun registerUser(email: String, password: String): Flow<Resource<AuthResult>> = flow{
        emit(Resource.Loading())
        try{
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            emit(Resource.Success(result))
        }
        catch(e: Exception){
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error ocurred"))
        }
    }

}
