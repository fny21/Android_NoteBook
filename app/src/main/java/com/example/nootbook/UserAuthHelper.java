package com.example.nootbook;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import androidx.annotation.NonNull;

public class UserAuthHelper {

    private FirebaseAuth mAuth;

    public UserAuthHelper() {
        this.mAuth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void registerUser(String email, String password, final AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(mAuth.getCurrentUser());
                        } else {
                            callback.onFailure(task.getException());
                        }
                    }
                });
    }

    public void loginUser(String email, String password, final AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(mAuth.getCurrentUser());
                        } else {
                            callback.onFailure(task.getException());
                        }
                    }
                });
    }

    public void updateUserProfile(String displayName, String photoUri, final AuthCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .setPhotoUri(photoUri != null ? Uri.parse(photoUri) : null)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                callback.onSuccess(user);
                            } else {
                                callback.onFailure(task.getException());
                            }
                        }
                    });
        }
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }
}
