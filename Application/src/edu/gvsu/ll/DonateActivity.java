package edu.gvsu.ll;

import edu.gvsu.ll.R.drawable;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewFragment;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class DonateActivity extends Activity 
{
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		String url = "https://secure.gvsu.edu/giving/lakerlegacies";
		
		TabHost tabHost=(TabHost)findViewById(R.id.tabHost);
		tabHost.setup();
	    ImageView image = null;

        
        //Home Page
        TabSpec spec1=tabHost.newTabSpec("Home");
        spec1.setIndicator("Home",getResources().getDrawable(R.drawable.home));
        spec1.setContent(R.id.tab1);

        
        //Directory
        TabSpec spec2=tabHost.newTabSpec("Directory");
        spec2.setIndicator("Directory",getResources().getDrawable(R.drawable.directory));
        spec2.setContent(R.id.tab2);

        //Near Me
        TabSpec spec3=tabHost.newTabSpec("Near Me");
        spec3.setIndicator("Near Me",getResources().getDrawable(R.drawable.maps));
        spec3.setContent(R.id.tab3);
        
        //Donate
        TabSpec spec4=tabHost.newTabSpec("Donate");
        spec4.setIndicator("Donate",getResources().getDrawable(R.drawable.donate));
        spec4.setContent(R.id.tab4);
        
        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);
        tabHost.addTab(spec4);

        /*
         * Sam, this is commented out because I am working on the tabs view
         */
//		/** Called when the activity is first created. */
//		WebView myWebView = (WebView) findViewById(R.id.tab4);	
//		myWebView.getSettings().setJavaScriptEnabled(true);
//		myWebView.setWebViewClient(new WebViewClient() 
//		{
//			@Override
//			public boolean shouldOverrideUrlLoading(WebView view, String url) 
//			{
//				view.loadUrl(url);
//				return true;
//			}
//		});
//    myWebView.loadUrl(url);

	}
}