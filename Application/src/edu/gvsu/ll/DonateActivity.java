package edu.gvsu.ll;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewFragment;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class DonateActivity extends Activity
{
	/*
	 * I'm planning on keeping this code for good. Once I can get tabs to call activities, this will be
	 * all set and will work. Lets not edit this commented piece of code. - Matthew
	 */
/*
	private WebView myWebView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
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
		public void onCreate(Bundle savedInstanceState) 
		{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
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
		}
}
