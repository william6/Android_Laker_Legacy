package edu.gvsu.ll;


import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class DonateActivity extends Activity
{

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
		
        myWebView.loadUrl("https://secure.gvsu.edu/giving/lakerlegacies");
        setContentView(myWebView);

		}
}
