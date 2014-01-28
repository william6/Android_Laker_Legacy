package edu.gvsu.ll;

import java.io.File;


import android.R;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends Activity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //Create database manager
        File fInternalDir = getFilesDir();	//app data directory
        String strDbName = "Laker_Legacy_DB";
        DatabaseManager dbManager = new DatabaseManager(getApplicationContext(), strDbName, fInternalDir.getAbsolutePath() + "/database/", 1);   
        
        //query database to make sure it's working
        Cursor cursor = dbManager.query("SELECT * FROM " + GTblVal.TBL_DONOR);
        cursor.moveToFirst();
        for(int i=0; i<cursor.getCount(); i++){
        	for(int j=0; j<cursor.getColumnCount(); j++){
        		Log.d(cursor.getColumnName(j).toUpperCase() + ": " + cursor.getString(j));
        	}
        	cursor.moveToNext();
        }
        cursor = dbManager.query("SELECT * FROM " + GTblVal.TBL_IMAGE);
        cursor.moveToFirst();
        for(int i=0; i<cursor.getCount(); i++){
        	for(int j=0; j<cursor.getColumnCount(); j++){
        		Log.d(cursor.getColumnName(j).toUpperCase() + ": " + cursor.getString(j));
        	}
        	cursor.moveToNext();
        }
        
        //create tabs on main page
        Resources resources = getResources();
        TabHost tabHost = getTabHost();
        	Intent intentAndroid = new Intent().setClass(this, MainActivity.class);
        
        	//Create first tab
        TabSpec Tab1 = tabHost.newTabSpec("Android")
        				.setIndicator("",resources.getDrawable(R.drawable.ic_lock_idle_low_battery)) //random icon
        				.setContent(intentAndroid);
        
    }
}