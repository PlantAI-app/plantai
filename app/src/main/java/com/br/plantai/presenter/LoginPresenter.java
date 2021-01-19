package com.br.plantai.presenter;

import android.content.Intent;

import com.br.plantai.activity.LoginActivity;
import com.br.plantai.activity.MapsActivity;
import com.google.firebase.auth.FirebaseUser;

public class LoginPresenter implements LoginMVP.PresenterLoginImpl {

    public LoginActivity activity;

    /**
     * Set the intent values to transfer to other activities.
     *
     * @param userFirebase an UserFirebase object
     * @return an intent Object
     */
    @Override
    public Intent setIntent(FirebaseUser userFirebase) {
        Intent intent = new Intent( activity, MapsActivity.class );
        intent.putExtra( "email", userFirebase.getEmail() );
        return (intent);
    }
}

