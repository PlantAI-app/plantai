package com.br.plantai.presenter;

import android.content.Intent;

import com.google.firebase.auth.FirebaseUser;

public interface LoginMVP {

    interface PresenterLoginImpl {
        Intent setIntent(FirebaseUser userFirebase);
    }
}
