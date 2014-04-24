package edu.gvsu.ll;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {
	
	public static MainActivity sInstance;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		sInstance = this;
		
		loadDatabaseFile();
		Resources resources = getResources();
		TabHost tabHost = getTabHost();

		// Home tab
		Intent in_Home = new Intent().setClass(this, SlideShowActivity.class);
		TabSpec tabSpecHome = tabHost
				.newTabSpec("Home")
				.setIndicator("Home", resources.getDrawable(R.drawable.selector_home))
				.setContent(in_Home);

		// Directory tab
		Intent in_Directory = new Intent().setClass(this, DirectoryActivity.class);
		TabSpec tabSpecDirectory = tabHost
				.newTabSpec("Directory")
				.setIndicator("Directory", resources.getDrawable(R.drawable.selector_directory))
				.setContent(in_Directory);

		// Near Me tab
		Intent in_NearMe = new Intent().setClass(this, MapActivity.class);
		TabSpec tabSpecNearMe = tabHost
				.newTabSpec("Near Me")
				.setIndicator("Near Me", resources.getDrawable(R.drawable.selector_maps))
				.setContent(in_NearMe);

		// Donate tab
		Intent in_Donate = new Intent().setClass(this, DonateActivity.class);
		TabSpec tabSpecDonate = tabHost
				.newTabSpec("Give")
				.setIndicator("Give", resources.getDrawable(R.drawable.selector_donate))
				.setContent(in_Donate);

		// add all tabs 
		tabHost.addTab(tabSpecHome);
		tabHost.addTab(tabSpecDirectory);
		tabHost.addTab(tabSpecNearMe);
		tabHost.addTab(tabSpecDonate);

		//set Windows tab as default (zero based)
		tabHost.setCurrentTab(0);
	}
	
	private void loadDatabaseFile(){
		//copy database file from assets to internal directory so we can use it
		
		String strDBName = "Laker Legacies.sqlite";
		String strDBPath = getFilesDir().getAbsolutePath();
		
		if(strDBPath.endsWith("/"))
			strDBPath += strDBName;
		else
			strDBPath += "/" + strDBName;
		File fInternalDB = new File ( strDBPath );
		
		//if the file doesn't exist, copy it from assets to internal memory
		if( !fInternalDB.exists() ){
			InputStream input = null;
			FileOutputStream output = null;

			try{
				input = getAssets().open(strDBName);
				fInternalDB.createNewFile();
				output = new FileOutputStream(fInternalDB);

				byte[] buffer = new byte[100*1024];
				while(input.available() > 0){
					input.read(buffer);
					output.write(buffer);
				}
				input.close();
				output.close();
			}
			catch(IOException ioe){

			}
		}

		//load the database file
		Global.gDBM = new DatabaseManager(this, fInternalDB, 1);
	}
}