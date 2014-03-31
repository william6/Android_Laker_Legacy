package edu.gvsu.ll;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class MapActivity extends FragmentActivity   { 
   static final LatLng GVSUPEW = new LatLng(42.963826, -85.677772);
   static final LatLng GVSUALL = new LatLng(42.966746, -85.886718);
   private GoogleMap map;
		  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        
        //index to move through the items of the database
        int i = 0;
        int count = 0;
        double lat = 0.0;//This is the latitude of the waypoint
        double lng = 0.0; //This is the longitude of the waypoint
        String mon_Name;
        String snippet;
        
        //Get's the map
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
        
        //Query the database to get all the names and lat&long information
        Cursor monWaypoint;
        
        //This pulls the name, lat, long, and campus from the database
        monWaypoint = Global.gDBM.query("SELECT" + " " + Global.COL_MON_NAME + ", " + Global.COL_LATITUDE + ", " +
        		Global.COL_LONGITUDE + ", " + Global.COL_CAMPUS + " FROM" + " " + Global.TBL_MONUMENT);
        
        //Get the size of the cursor
        count = monWaypoint.getCount();
        
        //create an array list for all the information from the database
        List<Marker> mon_Way = new ArrayList<Marker>();
        
        //Move the cursor to the top item
        monWaypoint.moveToFirst();
        
        for(i = 0;i < count;i++)
        {
        	mon_Name = monWaypoint.getString(0); //pulls the monument name
        	lat = monWaypoint.getDouble(1); //pulls the monument latitude
        	lng = monWaypoint.getDouble(2); //pulls the monument longitude
        	snippet = monWaypoint.getString(3); //pulls the monuments campus
        	
	        //Create the new marker
	        Marker mark = map.addMarker(new MarkerOptions().position(new LatLng (lat,lng))
	                .title(mon_Name).snippet(snippet));
	        mon_Way.add(mark);
	        
	      //move to the next record
	        monWaypoint.moveToNext(); 
        }
        
        //add the my location button
        map.setMyLocationEnabled(true);
        
        //Zooms to GVSU PEW on start
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(GVSUPEW, 15));
    }
 
}