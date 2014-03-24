package edu.gvsu.ll;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

public class MapView extends FragmentActivity   {
   private GoogleMap map;
   
   static final LatLng GVSUPEW = new LatLng(42.963826, -85.677772);
   static final LatLng GVSUALL = new LatLng(42.966746, -85.886718);
		  
   /*The following variables will be used when pulling from the database*/
   double lat;
   double lng;
   LatLng Lat_long = new LatLng(lat,lng);
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        
            
            if (map!=null){
            	
            //This is just a sample waypoint	
              Marker pew = map.addMarker(new MarkerOptions().position(GVSUPEW)
                  .title("GVSU - Pew").snippet("Downtown Campus"));
              Marker allendale = map.addMarker(new MarkerOptions()
                  .position(GVSUALL)
                  .title("GVSU - Allendale")
                  .snippet("Allendale Campus"));
              
              map.moveCamera(CameraUpdateFactory.newLatLngZoom(GVSUPEW, 15));
            }
    }
 
}