package edu.gvsu.ll;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class MapView extends FragmentActivity {
    GoogleMap map;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
 
        map = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        if (map == null) {
            Toast.makeText(this, "Google Maps not available", 
                Toast.LENGTH_LONG).show();
        }
    }
 
}