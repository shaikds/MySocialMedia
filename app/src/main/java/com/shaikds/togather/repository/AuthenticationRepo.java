package com.shaikds.togather.repository;

import android.app.Activity;
import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.concurrent.TimeUnit;

public class AuthenticationRepo {
    private Application application;
    private MutableLiveData<FirebaseUser> firebaseUserMutableLiveData;
    private MutableLiveData<Boolean> userIsLoggedMutableLiveData;
    private FirebaseAuth firebaseAuth;
    private String mVerificationId;

    public AuthenticationRepo(Application application) {
        this.application = application;
        firebaseUserMutableLiveData = new MutableLiveData<>();
        userIsLoggedMutableLiveData = new MutableLiveData<>();
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());

        }

    }

    public void register(String email, String pass) {
        firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userIsLoggedMutableLiveData.postValue(true);
                firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());

                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(email.substring(0, 9))
                        .build();
                firebaseAuth.getCurrentUser().updateProfile(profileUpdates);
                Toast.makeText(application, "ההרשמה הושלמה בהצלחה", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(application, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void login(String email, String pass) {
        firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userIsLoggedMutableLiveData.postValue(true);
                firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());

            } else {
                Toast.makeText(application, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteAuth(String email, String password) {
        AuthCredential authCredential = EmailAuthProvider.getCredential(email, password);
        firebaseUserMutableLiveData.getValue().reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                firebaseUserMutableLiveData.getValue().delete();
                firebaseUserMutableLiveData.postValue(null);
                userIsLoggedMutableLiveData.postValue(false);
                firebaseAuth.signOut();
            }
        });
    }

    public void sendVerificationCode(Activity activity, String phoneNumber, String password) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber("+972" + phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(activity)                 // Activity (for callback binding)
                        .setCallbacks(getMCallBacks(activity, phoneNumber, password))          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }


    public void signOut() {
        firebaseAuth.signOut();
        userIsLoggedMutableLiveData.postValue(false);
        firebaseUserMutableLiveData.postValue(null);


    }

    public MutableLiveData<FirebaseUser> getFirebaseUserMutableLiveData() {
        return firebaseUserMutableLiveData;
    }

    public MutableLiveData<Boolean> getUserIsLoggedMutableLiveData() {
        return userIsLoggedMutableLiveData;
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks getMCallBacks(Activity activity, String phoneNumber, String password) {

        return new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
/*                final String code = credential.getSmsCode();
                if (code != null) {
                    verifyCode(code, phoneNumber, phoneNumber);
                }*/
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("AuthRepo", "onVerificationFailed", e);
                Toast.makeText(activity, "שגיאה בקבלת קוד SMS", Toast.LENGTH_SHORT).show();
                // Show a message and update the UI
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, token);
                mVerificationId = verificationId;
                Toast.makeText(activity, "הודעת sms תגיע תוך כמה רגעים...", Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void verifyCode(String code, String phoneNumber, String password) {
        AuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            // phone auth success --> verified user --> unlink it and register .
            task.getResult().getUser().unlink(PhoneAuthProvider.PROVIDER_ID).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // If task is completed we should register to mail now and unlink this phone auth.
                    register(phoneNumber + "@" + phoneNumber + "." + phoneNumber, password);
                }
            });

        });
    }
}