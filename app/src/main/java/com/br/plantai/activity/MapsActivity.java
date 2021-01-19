package com.br.plantai.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.br.plantai.event.Classificacao;
import com.br.plantai.presenter.MapsMVP;
import com.br.plantai.presenter.MapsPresenter;
import com.br.plantai.view.GeneralView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GeneralView, MapsMVP.MapsViewImpl {

    private static final String TAG = "MapsActivity";

    // location vars
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    // permission var
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // MVP vars
    private MapsPresenter mMapsPresenter;

    // maps vars
    private GoogleMap mMap;
    private Boolean mLocationPermissionsGranted = false;
    private String userName;
    private Location currentLocation;

    // firebase vars
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    // widgets
    private ImageView mCamera;
    private ImageView mLocation;
    private AutoCompleteTextView mSearchText;

    // intent var
    private Intent setIntencao() {
        return (new Intent( this, CameraActivity.class ));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        Log.d( TAG, "MapsActivity: onCreate: start features." );
        setContentView( R.layout.activity_maps );

        // starting the references for the view
        mCamera = findViewById( R.id.ic_camera );
        mLocation = findViewById( R.id.ic_location );
        mSearchText = findViewById( R.id.ic_input_search );

        if (mMapsPresenter == null) {
            mMapsPresenter = new MapsPresenter( MapsActivity.this );
        }
        mMapsPresenter.setView( this );

        // EventBus register
        EventBus.getDefault().register( this );

        // getting email from user login
        Intent intent = getIntent();
        userName = intent.getStringExtra( "email" );

        // starting firebase variables
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child( "location" );

        mFirebaseAuth = FirebaseAuth.getInstance();

        getLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d( TAG, "MapsActivity: onStop: Callback onStop." );

        // EventBus Unregister
        EventBus.getDefault().unregister( MapsActivity.class );
    }

    private void init() {
        Log.d( TAG, "MapsActivity: init: start init features." );

        mCamera.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startActivity( new Intent( setIntencao() ) );
            }
        } );

        // move user to the current location
        mLocation.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                getDeviceLocation();
            }
        } );

        mSearchText.setOnEditorActionListener( new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if ((actionId == EditorInfo.IME_ACTION_SEARCH)
                        || (actionId == EditorInfo.IME_ACTION_DONE)
                        || (keyEvent.getAction() == KeyEvent.ACTION_DOWN)
                        || (keyEvent.getAction() == KeyEvent.KEYCODE_ENTER)) {
                    mMapsPresenter.getLocateSearch();

                }
                return false;
            }
        } );
    }

    /**
     * Adds the classification made in the firebase database. Based on GMT of Brazil, change to
     * other regions.
     *
     * @param classificacao an object from Classificacao class
     */
    @Subscribe
    public void onEvent(Classificacao classificacao) {
        Log.d( TAG, "MapsActivity: onEvent: Receives the values of the camera activity." );
        String mEspecie = classificacao.getEspecie();
        String mAcuracia = classificacao.getAcuracia();

        //addClassifyToDatabase();
        mMapsPresenter.addClassifyToDatabase( mDatabaseReference, userName, currentLocation,
                mAcuracia, mEspecie );
    }

    /**
     * Initialize the map fragment.
     */
    @Override
    public void initMap() {
        Log.d( TAG, "MapsActivity: initMap: Initializing the map." );
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById( R.id.map );
        mapFragment.getMapAsync( MapsActivity.this );
    }

    /**
     * Callback created for when the map is ready.
     *
     * @param googleMap GoogleMap class object.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d( TAG, "MapsActivity: onMapReady: Callback onMapReady." );
        showToast( "Iniciando o mapa" );


        mMap = googleMap;
        mMapsPresenter.mMap = mMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION )
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this,
                    Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mMap.setMyLocationEnabled( true );
            mMap.getUiSettings().setMyLocationButtonEnabled( false );
            mMap.getUiSettings().setMapToolbarEnabled( false );

            mMapsPresenter.getMakersPoints( mDatabaseReference );

            init();
        }
    }

    /**
     * Method that takes the user's location in real time.
     */
    private void getDeviceLocation() {
        Log.d( TAG, "MapsActivity: getDeviceLocation: Getting the user's location." );

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient( this );

        try {
            if (mLocationPermissionsGranted) {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener( new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d( TAG, "onComplete: found location" );
                            currentLocation = (Location) task.getResult();

                            moveCamera( new LatLng( currentLocation.getLatitude(),
                                    currentLocation.getLongitude() )
                            );
                        } else {
                            Log.d( TAG, "onComplete: current location is null" );
                            showToast( "Impossibilitado de obter a localização atual" );
                        }
                    }
                } );
            }
        } catch (SecurityException e) {
            Log.e( TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    /**
     * Moves the user's camera according to the location with a specified point.
     *
     * @param latLng user latLng object.
     */
    @Override
    public void moveCamera(LatLng latLng) {
        mMap.moveCamera( CameraUpdateFactory.newLatLngZoom( latLng, MapsActivity.DEFAULT_ZOOM ) );
    }

    /**
     * Request for user location permissions.
     */
    private void getLocationPermission() {
        Log.d( TAG, "MapsActivity: getLocationPermission: User localization permission" );

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission( this.getApplicationContext(),
                FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission( this.getApplicationContext(),
                    COURSE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions( this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE );
            }
        } else {
            ActivityCompat.requestPermissions( this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE );
        }
    }

    /**
     * Checks the permissions granted, if allowed, the map is started.
     *
     * @param requestCode  an integer with the status code of the request permissions.
     * @param permissions  an arrayList with the permissions requested.
     * @param grantResults an ArrayList with results of the permissions provided.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        Log.d( TAG, "MapsActivity: onRequestPermissionsResult: User permission result." );
        mLocationPermissionsGranted = false;

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionsGranted = false;
                        Log.d( TAG, "onRequestPermissionsResult: permission failed" );
                        return;
                    }
                }
                Log.d( TAG, "onRequestPermissionsResult: permission granted" );
                mLocationPermissionsGranted = true;
                //initialize our map
                getLocationPermission();
            }
        }
    }

    @Override
    public void hideSoftKeyboard() {
        this.getWindow()
                .setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN );
    }

    @Override
    public void showToast(String message) {
        Toast.makeText( this, message, Toast.LENGTH_SHORT ).show();
    }

    @Override
    public String getSearchText() {
        return (mSearchText.getText().toString());
    }
}
