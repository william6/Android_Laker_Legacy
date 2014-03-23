package edu.gvsu.ll;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

	@SuppressWarnings("deprecation")
	public class MainActivity extends TabActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Resources resources = getResources();
        TabHost tabHost = getTabHost();
        
        	// Home tab
     		Intent in_Home = new Intent().setClass(this, SlideShowActivity.class);
     		TabSpec tabSpecHome = tabHost
     		  .newTabSpec("Home")
     		  .setIndicator("", resources.getDrawable(R.drawable.ic_home))
     		  .setContent(in_Home);
      
     		// Directory tab
     		Intent in_Directory = new Intent().setClass(this, DirectoryActivity.class);
     		TabSpec tabSpecDirectory = tabHost
     		  .newTabSpec("Directory")
     		  .setIndicator("", resources.getDrawable(R.drawable.ic_directory))
     		  .setContent(in_Directory);
      
     		// Near Me tab
     		Intent in_NearMe = new Intent().setClass(this, MapView.class);
     		TabSpec tabSpecNearMe = tabHost
     		  .newTabSpec("Near Me")
     		  .setIndicator("", resources.getDrawable(R.drawable.ic_maps))
     		  .setContent(in_NearMe);
      
     		// Donate tab
     		Intent in_Donate = new Intent().setClass(this, DonateActivity.class);
     		TabSpec tabSpecDonate = tabHost
     		  .newTabSpec("Donate")
     		  .setIndicator("", resources.getDrawable(R.drawable.ic_donate))
     		  .setContent(in_Donate);
      
     		// add all tabs 
     		tabHost.addTab(tabSpecHome);
     		tabHost.addTab(tabSpecDirectory);
     		tabHost.addTab(tabSpecNearMe);
     		tabHost.addTab(tabSpecDonate);
      
     		//set Windows tab as default (zero based)
     		tabHost.setCurrentTab(0);
     	}
        
        // ENTER THE CLASS YOU ARE TESTING RIGHT HERE.
        //IF YOU'RE TESTING THE MAIN ACTIVITY, COMMENT THE FOLLOWING CODE OUT

    	/*
        Intent intent = new Intent(this, SlideShowActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
		*/
		}
