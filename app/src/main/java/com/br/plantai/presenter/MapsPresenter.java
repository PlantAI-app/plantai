package com.br.plantai.presenter;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.br.plantai.adapter.CustomInfoWindowAdapter;
import com.br.plantai.model.AnnotationPlantas;
import com.br.plantai.model.User;
import com.br.plantai.util.Utils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class MapsPresenter implements MapsMVP.MapsPresenterImpl {

    private static final String TAG = "MapsActivity";

    // view vars
    private final Context context;
    public GoogleMap mMap;

    // mvp vars
    private MapsMVP.MapsViewImpl mMapsview;

    // constructor
    public MapsPresenter(Context context) {
        this.context = context;
    }

    @Override
    public void setView(MapsMVP.MapsViewImpl mMapsview) {
        this.mMapsview = mMapsview;
    }

    /**
     * Adds the classification made in the firebase database. Based on GMT of Brazil, change to
     * other regions.
     *
     * @param databaseReference reference object pointing to child in firebase
     * @param userName          user name
     * @param currentLocation   an object location of the user
     * @param mAcuracia         accuracy obtained in classification
     * @param mEspecie          Classified species
     */
    @Override
    public void addClassifyToDatabase(DatabaseReference databaseReference, String userName,
                                      Location currentLocation, String mAcuracia,
                                      String mEspecie) {


        Date data = Calendar.getInstance().getTime();

        SimpleDateFormat writeDate = new SimpleDateFormat( "dd/MM/yyyy" );
        writeDate.setTimeZone( TimeZone.getTimeZone( "GMT-03:00" ) );
        String formatted = writeDate.format( data );

        User user = new User( userName,
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                mAcuracia,
                mEspecie,
                formatted );

        databaseReference.push().setValue( user );

        getMakersPoints( databaseReference );
    }

    /**
     * Retrieves the values from the database in real time if there is a change in location or if
     * called.
     *
     * @param databaseReference reference object pointing to child in firebase
     */
    @Override
    public void getMakersPoints(DatabaseReference databaseReference) {
        databaseReference.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Log.d( TAG, "MapsActivity: getMakersPoints: Retrieves the markings on map" );

                    HashMap<String, User> user = (HashMap<String, User>) dataSnapshot.getValue();

                    setMarkersPoints( user );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e( TAG, "MapsActivity: onCancelled: Acquisition error" );
            }
        } );
    }

    /**
     * Set the recovered values on the map.
     *
     * @param markers a hashmap where the key is the user id and the value an object of the class
     *                user.
     */
    @Override
    public void setMarkersPoints(HashMap<String, User> markers) {
        Log.d( TAG, "MapsActivity: setMarkersPoints: User location data." );
        for (Map.Entry<String, User> user : markers.entrySet()) {

            Map usersData = (Map) user.getValue();
            User mUser = new User(
                    usersData.get( "email" ).toString(),
                    (Double) usersData.get( "lat" ),
                    (Double) usersData.get( "lng" ),
                    usersData.get( "classificationRate" ).toString(),
                    usersData.get( "classificationObject" ).toString(),
                    usersData.get( "data" ).toString() );

            // create markers on map
            createMarker( mUser );
        }
    }

    /**
     * Creates the markers on the map, in which the classification information is shown.
     *
     * @param user object of the User class.
     */
    @Override
    public void createMarker(User user) {
        Log.d( TAG, "MapsActivity: createMarker: Creates the markers on the map." );

        // get user classified plant
        String classifiedPlant = Utils.formatSpecieName( user.getClassificationObject() );

        // defines the marker color
        AnnotationPlantas mAnnotationPlantas = new AnnotationPlantas();
        mAnnotationPlantas.setPlanta( Utils.plantColorFinder( classifiedPlant ) );

        // get the marker icon
        int pointMarker = Utils.SpecieColorRiskFinder( mAnnotationPlantas.getPlanta() );

        MarkerOptions options = new MarkerOptions()
                .icon( BitmapDescriptorFactory.fromResource( pointMarker ) )
                .position( new LatLng( user.getLat(),
                        user.getLng() ) )
                .title( "Email: " + user.getEmail() )
                .snippet( "Espécie classificada: " + user.getClassificationObject() +
                        "\nAcurácia obtida: " + Utils.formatAccuracy( user.getClassificationRate() ) +
                        "\nData: " + user.getData() );


        mMap.setInfoWindowAdapter( new CustomInfoWindowAdapter( context ) );
        mMap.addMarker( options );
    }

    /**
     * Get address by name using geocoder. If the location is found, the user is redirected to it,
     * if not, nothing is done.
     */
    @Override
    public void getLocateSearch() {
        Log.d( TAG, "MapsActivity: getLocateSearch: get address by name" );
        Geocoder geocoder = new Geocoder( context );

        List<Address> listAddress = new ArrayList<>();

        try {
            listAddress = geocoder.getFromLocationName( mMapsview.getSearchText(),
                    1 );
        } catch (IOException e) {
            Log.e( TAG, "MapsActivity: getLocateSearch: " + e.getMessage() );
        }

        if (listAddress.size() > 0) {
            Address address = listAddress.get( 0 );
            createMarkerLocation( new LatLng( address.getLatitude(), address.getLongitude() ),
                    address );
            mMapsview.moveCamera( new LatLng( address.getLatitude(), address.getLongitude() )
            );

        }
    }

    /**
     * Creates an icon from the location searched by the user.
     *
     * @param latLng  user latLng object.
     * @param address object from geocoder namespace.
     */
    @Override
    public void createMarkerLocation(LatLng latLng, Address address) {
        mMapsview.hideSoftKeyboard();

        MarkerOptions options = new MarkerOptions()
                .position( latLng )
                .title( "Local pesquisado" )
                .snippet( address.getCountryName() );
        mMap.addMarker( options );
    }
}
