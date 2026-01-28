package com.example.petrescue

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.petrescue.data.AppDatabase
import com.example.petrescue.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    private val userDao = AppDatabase.getDatabase(application).userDao()

    private val _userLiveData = MutableLiveData<FirebaseUser?>()
    val userLiveData: LiveData<FirebaseUser?> = _userLiveData

    private val _localUserLiveData = MutableLiveData<User?>()
    val localUserLiveData: LiveData<User?> = _localUserLiveData

    private val _errorLiveData = MutableLiveData<String?>()
    val errorLiveData: LiveData<String?> = _errorLiveData

    private val _loadingLiveData = MutableLiveData<Boolean>()
    val loadingLiveData: LiveData<Boolean> = _loadingLiveData

    init {
        _userLiveData.value = auth?.currentUser
    }

    fun clearError() {
        _errorLiveData.value = null
    }

    fun login(email: String, pass: String) {
        clearError()
        _loadingLiveData.value = true
        
        viewModelScope.launch {
            val localUser = userDao.getUserByEmail(email)
            
            if (auth != null && auth.app.options.apiKey != "API_KEY") {
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        _loadingLiveData.postValue(false)
                        if (task.isSuccessful) {
                            _userLiveData.postValue(auth.currentUser)
                        } else if (localUser != null && localUser.password == pass) {
                            _localUserLiveData.postValue(localUser)
                        } else {
                            _errorLiveData.postValue("Invalid email or password")
                        }
                    }
            } else {
                _loadingLiveData.postValue(false)
                if (localUser != null && localUser.password == pass) {
                    _localUserLiveData.postValue(localUser)
                } else {
                    _errorLiveData.postValue("Invalid email or password")
                }
            }
        }
    }

    fun signUp(username: String, email: String, pass: String) {
        clearError()
        _loadingLiveData.value = true
        
        viewModelScope.launch {
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                _loadingLiveData.postValue(false)
                _errorLiveData.postValue("An account with this email already exists.")
                return@launch
            }

            val newUser = User(email, username, pass)
            
            if (auth != null && auth.app.options.apiKey != "API_KEY") {
                auth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            viewModelScope.launch {
                                userDao.insertUser(newUser)
                                _loadingLiveData.postValue(false)
                                _userLiveData.postValue(auth.currentUser)
                            }
                        } else {
                            _loadingLiveData.postValue(false)
                            _errorLiveData.postValue(task.exception?.message ?: "Registration failed")
                        }
                    }
            } else {
                userDao.insertUser(newUser)
                _loadingLiveData.postValue(false)
                _localUserLiveData.postValue(newUser)
            }
        }
    }

    fun logout() {
        auth?.signOut()
        _userLiveData.value = null
        _localUserLiveData.value = null
        clearError()
    }
}