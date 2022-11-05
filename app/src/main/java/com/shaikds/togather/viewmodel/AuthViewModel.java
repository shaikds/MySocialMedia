package com.shaikds.togather.viewmodel;

import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.shaikds.togather.repository.AuthenticationRepo;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends AndroidViewModel {
    private AuthenticationRepo repository;
    private MutableLiveData<FirebaseUser> userData;
    private MutableLiveData<Boolean> loggedStatus;


    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthenticationRepo(application);
        userData = repository.getFirebaseUserMutableLiveData();
        loggedStatus = repository.getUserIsLoggedMutableLiveData();
    }

    public void register(String email, String password) {
        repository.register(email, password);
    }

    public void signIn(String email, String password) {
        repository.login(email, password);
    }

    public void deleteUserAuth(String email, String password) {
        repository.deleteAuth(email, password);
    }

    public void signOut() {
        repository.signOut();
    }

    public MutableLiveData<FirebaseUser> getUserData() {
        return userData;
    }

    public MutableLiveData<Boolean> getLoggedStatus() {
        return loggedStatus;
    }

    public void sendVerificationCode(Activity activity, String phoneNumber, String password) {
        repository.sendVerificationCode(activity, phoneNumber, password);
    }

    public void verifySmsCode(String code, String phoneNumber, String password) {
        repository.verifyCode(code, phoneNumber, password);
    }
}
