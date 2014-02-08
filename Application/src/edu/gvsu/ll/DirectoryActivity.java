package edu.gvsu.ll;

import java.io.File;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;

public class DirectoryActivity extends Activity
{
	
	public static DirectoryActivity sInstance;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);				//TODO - change to diff layout
		
		sInstance = this;
		
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
//        cursor = dbManager.query("SELECT * FROM " + GTblVal.TBL_IMAGE);
//        cursor.moveToFirst();
//        for(int i=0; i<cursor.getCount(); i++){
//        	for(int j=0; j<cursor.getColumnCount(); j++){
//        		Log.d(cursor.getColumnName(j).toUpperCase() + ": " + cursor.getString(j));
//        	}
//        	cursor.moveToNext();
//        }
	}
	
}
