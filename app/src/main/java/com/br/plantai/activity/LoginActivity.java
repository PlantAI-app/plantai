package com.br.plantai.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.br.plantai.presenter.LoginPresenter;
import com.br.plantai.view.GeneralView;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity implements GeneralView {

    private static final String TAG = "LoginActivity";
    // vars
    private static final int RC_SIGN_IN = 123;
    private static LoginPresenter mloginPresenter;

    //firebase vars
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d( TAG, "LoginActivity: onCreate: Starting at login screen." );
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login );

        // var presenter
        if (mloginPresenter == null) {
            mloginPresenter = new LoginPresenter();
        }

        // set activity context
        mloginPresenter.activity = this;

        // vars firebase
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        //LoginActivity sourceClass;
        LoginActivity activity;
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d( TAG, "LoginActivity: onAuthStateChanged: Starting to login logic." );

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    showToast( "Bem-vindo." );
                    startActivity( mloginPresenter.setIntent( user ) );
                } else {
                    // User is signed out
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled( false )
                                    .setAvailableProviders( Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build() ) )
                                    .build(),
                            RC_SIGN_IN );
                }
            }
        };
    }

    /**
     * Checks the permissions granted, if allowed, the map is started.
     *
     * @param requestCode an integer with the status code of the request permissions.
     * @param resultCode  an integer with the status code of the result permissions.
     * @param data        an object from Intent class
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                showToast( "Logado" );
            } else if (resultCode == RESULT_CANCELED) {
                showToast( "Cancelado!" );
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener( mAuthStateListener );
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener( mAuthStateListener );
        }
    }

    @Override
    public void showToast(String message) {
        Toast.makeText( this, message, Toast.LENGTH_SHORT ).show();
    }
}
