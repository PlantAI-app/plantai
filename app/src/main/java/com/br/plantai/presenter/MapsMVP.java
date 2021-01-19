package com.br.plantai.presenter;

import android.location.Address;
import android.location.Location;

import com.br.plantai.model.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

public interface MapsMVP {

    interface MapsPresenterImpl {
        void addClassifyToDatabase(DatabaseReference databaseReference, String userName,
                                   Location currentLocation, String mAcuracia,
                                   String mEspecie);

        void getMakersPoints(DatabaseReference databaseReference);

        void setMarkersPoints(HashMap<String, User> markers);

        void createMarker(User user);

        void getLocateSearch();

        void setView(MapsMVP.MapsViewImpl mMapsview);

        void createMarkerLocation(LatLng latLng, Address address);

    }

    interface MapsViewImpl {
        void initMap();

        String getSearchText();

        void hideSoftKeyboard();

        void moveCamera(LatLng latLng);
    }
}
