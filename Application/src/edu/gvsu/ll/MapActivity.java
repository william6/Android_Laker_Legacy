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
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

public class MapActivity extends FragmentActivity   
{ 
	/*Constants used for the GOTO menu items*/
   static final LatLng GVSUPEW = new LatLng(42.963826, -85.677772);
   static final LatLng GVSUALL = new LatLng(42.966746, -85.886718);
   
   /*Variables used for Google Maps*/
   private GoogleMap map;
   Button menu_button;
   
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        
        /*This calls the method to check to see if GPS is enabled*/
        CheckEnableGPS();
        
        menu_button = (Button) findViewById(R.id.menu_button);
        menu_button.setOnClickListener(new OnClickListener() 
        {
            public void onClick(View v) 
            {  
             //Creating the instance of PopupMenu  
             PopupMenu popup = new PopupMenu(MapActivity.this, menu_button);  
             
             //Inflating the Popup using xml file  
             popup.getMenuInflater().inflate(R.menu.mapmenu, popup.getMenu());

             /*registering popup with OnMenuItemClickListener*/  
             popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() 
             {  
              public boolean onMenuItemClick(MenuItem item) 
              {   
            	/*Menu Option Handler*/
            	if(item.getOrder() == 1) //standard mode
            		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            	if (item.getOrder() == 2)  //hybrid
            		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            	if(item.getOrder() == 3)//)show traffic
            	{
            		map.setTrafficEnabled(true);
            	}
            	if(item.getOrder() == 4)//satellite
            		map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            	if(item.getOrder() == 5) //go to allendale
            		map.moveCamera(CameraUpdateFactory.newLatLngZoom(GVSUALL, 15));
            	if(item.getOrder() == 6) //go to PEW
            		map.moveCamera(CameraUpdateFactory.newLatLngZoom(GVSUPEW,15));
            		  
               return true;  
              }  
             });  
             popup.show();//showing popup menu  
            }  
           });   
    
        /*index to move through the items of the database*/
        int i = 0;
        int count = 0;
        double lat = 0.0;//This is the latitude of the waypoint
        double lng = 0.0; //This is the longitude of the waypoint
        String mon_Name;
        String snippet;
        
        /*Get's the map*/
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
        
        /*Query the database to get all the names and lat&long information*/
        Cursor monWaypoint;
        
        /*This pulls the name, lat, long, and campus from the database*/
        monWaypoint = Global.gDBM.query("SELECT" + " " + Global.COL_MON_NAME + ", " + Global.COL_LATITUDE + ", " +
        		Global.COL_LONGITUDE + ", " + Global.COL_CAMPUS + " FROM" + " " + Global.TBL_MONUMENT);
        
        /*Get the size of the cursor*/
        count = monWaypoint.getCount();
        
        /*create an array list for all the information from the database*/
        List<Marker> mon_Way = new ArrayList<Marker>();
        
        /*Move the cursor to the top item*/
        monWaypoint.moveToFirst();
        
        /*Loop through all instances of a monument in the database*/
        for(i = 0;i < count;i++)
        {
        	/*pulls the information for adding the monuments to the map*/
        	mon_Name = monWaypoint.getString(0); 
        	lat = monWaypoint.getDouble(1); 
        	lng = monWaypoint.getDouble(2); 
        	snippet = monWaypoint.getString(3); 
        	
	        /*Create the new marker*/
	        Marker mark = map.addMarker(new MarkerOptions().position(new LatLng (lat,lng))
	                .title(mon_Name).snippet(snippet));
	        mon_Way.add(mark);
	        
	        /*move to the next record*/
	        monWaypoint.moveToNext(); 
        }
        
        /*add the my location button*/
        map.setMyLocationEnabled(true);
        
        //find my location and zoom in to it.
        
        /*Zooms to GVSU ALL on start*/
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(GVSUALL, 15));
    }
    
    /*
     * This method is used to check if GPS is enabled. If it isn't enabled
     * it says to enable GPS and sends the user to their settings to enable it
     */
    private void CheckEnableGPS()
    {
    	/*Gets the provider services and checks for GPS*/
        String provider = Settings.Secure.getString(getContentResolver(),
        Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        
            if(!provider.contains("gps"))
            {  	
	            Toast.makeText(MapActivity.this, "Please Enable GPS",
	            Toast.LENGTH_LONG).show();
	            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	               startActivity(intent);
            }
     }
}