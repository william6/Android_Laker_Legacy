package edu.gvsu.ll;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

	String url = "https://secure.gvsu.edu/giving/lakerlegacies";
	private WebView myWebView;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		myWebView = new WebView(this);
		myWebView.getSettings().setJavaScriptEnabled(true);
		final Activity activity = this;

		myWebView.setWebViewClient(new WebViewClient() {
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
			}
		});

		myWebView.loadUrl(url);
		setContentView(myWebView);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.layout.menu, menu);
		return true;
	}


	/**
	 * Event Handling for Individual menu item selected
	 * Identify single menu item by it's id
	 * */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		switch (item.getItemId())
		{
		case R.id.browse:
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}   
}
