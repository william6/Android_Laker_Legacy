package edu.gvsu.ll;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewFragment;

public class DonateActivity extends Activity {

	String url = "https://secure.gvsu.edu/giving/lakerlegacies";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		
	    WebView myWebView = (WebView) findViewById(R.id.webview);	
	    myWebView.getSettings().setJavaScriptEnabled(true);
	    myWebView.setWebViewClient(new WebViewClient() {
	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	            view.loadUrl(url);
	            return true;
	        }
	    });
	    myWebView.loadUrl(url);
	}
}
