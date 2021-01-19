package com.br.plantai.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.br.plantai.activity.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final static String TAG = "CustomInfoWindowAdapter";

    private final View mWindow;
    private Context mContext;

    public CustomInfoWindowAdapter(Context context) {
        mContext = context;
        mWindow = LayoutInflater.from( context ).inflate( R.layout.custom_info_window, null );
    }

    /**
     * Render the metadata on top of the markers
     *
     * @param marker Object to be marked on the map
     * @param view   a View object
     */
    private void rendWindow(Marker marker, View view) {
        String snippet = marker.getSnippet();
        String title = marker.getTitle();

        TextView tvTitle = (TextView) view.findViewById( R.id.title );
        TextView tvSnippet = (TextView) view.findViewById( R.id.snippet );

        if (!title.equals( "" )) {
            tvTitle.setText( title );
        }

        if (!snippet.equals( "" )) {
            tvSnippet.setText( snippet );
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        rendWindow( marker, mWindow );
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        rendWindow( marker, mWindow );
        return mWindow;
    }
}
