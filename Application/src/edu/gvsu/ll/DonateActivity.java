package edu.gvsu.ll;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewFragment;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class DonateActivity extends FragmentActivity
{
    ImageView image;
	ImageView directory;
	/*
	 * I'm planning on keeping this code for good. Once I can get tabs to call activities, this will be
	 * all set and will work. Lets not edit this commented piece of code. - Matthew
	 */

	private WebView myWebView;
	GoogleMap map;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		/*
		super.onCreate(savedInstanceState);

		//**THIS WORKS** Added for webview 3/6/14
		myWebView = new WebView(this);
		myWebView.getSettings().setJavaScriptEnabled(true);
		final Activity activity = this;
		
        myWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }
        });
		
        myWebView.loadUrl("https://secure.gvsu.edu/giving/lakerlegacies");
        setContentView(myWebView);
*/		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		TabHost tabHost=(TabHost)findViewById(R.id.tabHost);
		tabHost.setup();

        //Home Page
        TabSpec spec1=tabHost.newTabSpec("Home");
        spec1.setContent(R.id.tab1);
        //image.setImageResource(R.drawable.bill_and_sally_seidman);
        spec1.setIndicator("Home",getResources().getDrawable(R.drawable.home));


        //Directory
        TabSpec spec2=tabHost.newTabSpec("Directory");
        spec2.setContent(R.id.tab2);
        //directory.setImageResource(R.drawable.directory_temp);
        spec2.setIndicator("Directory",getResources().getDrawable(R.drawable.directory));


        //Near Me
        TabSpec spec3=tabHost.newTabSpec("Near Me");
        spec3.setIndicator("Near Me",getResources().getDrawable(R.drawable.maps));
        spec3.setContent(R.id.tab3);
        
        map = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.Map)).getMap();
        if (map == null) {
            Toast.makeText(this, "Google Maps not available", 
                Toast.LENGTH_LONG).show();
        }
        
        //Donate
        TabSpec spec4=tabHost.newTabSpec("Donate");
        spec4.setIndicator("Donate",getResources().getDrawable(R.drawable.donate));
        spec4.setContent(R.id.tab4);
        
        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);
        tabHost.addTab(spec4);
		}
	
//		public boolean onCreateOptionsMenu(Menu menu)
//		{
//			//getMenuInflater().inflate(R.menu.activity_main, menu);
//			return true;
//		}
	}
