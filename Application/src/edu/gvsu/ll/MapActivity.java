package edu.gvsu.ll;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

public class MapActivity extends FragmentActivity   { 
   static final LatLng GVSUPEW = new LatLng(42.963826, -85.677772);
   static final LatLng GVSUALL = new LatLng(42.966746, -85.886718);
   private GoogleMap map;
   Button menu_button;
		  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        
        menu_button = (Button) findViewById(R.id.menu_button);
        menu_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {  
             //Creating the instance of PopupMenu  
             PopupMenu popup = new PopupMenu(MapActivity.this, menu_button);  
             //Inflating the Popup using xml file  
             popup.getMenuInflater().inflate(R.menu.mapmenu, popup.getMenu());

             //registering popup with OnMenuItemClickListener  
             popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {  
              public boolean onMenuItemClick(MenuItem item) {  
               Toast.makeText(MapActivity.this,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();  
               return true;  
              }  
             });  

             popup.show();//showing popup menu  
            }  
           });//closing the setOnClickListener method   
    
    
    
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
        
        //find my location and zoom in to it.
//        Location myLocation = map.getMyLocation();
//        LatLng myLatLng = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
       
        /*TODO
         * Need to add if statements for when the menu button is clicked and what is selected. 
         * Menu button needs 5 options:
         * Satellite View
         * Traffic View
         * Map View
         * GoTo Allendale
         * GoTo Pew GR
         */
        
        /*
         * TODO
         * Add an if statement to check if GPS is enabled, if not ask them to enable it on start of Maps. 
         * If they decline, zoom to Pew Campus. If they accept, zoom to their location.
         * 
         */
        
        //Zooms to GVSU PEW on start
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(GVSUPEW, 15));
    }

	
   /**
    * Event Handling for Individual menu item selected
    * Identify single menu item by it's id
    * */
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) 
        {
 
        case R.id.menu_sethybrid:
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            break;
        }
        return true; 
    }
}