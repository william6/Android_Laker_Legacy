package edu.gvsu.ll;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

	import android.os.Bundle;
	import android.support.v4.app.FragmentActivity;
	import android.view.Menu;

	public class MainActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // ENTER THE CLASS YOU ARE TESTING RIGHT HERE.
        //IF YOU'RE TESTING THE MAIN ACTIVITY, COMMENT THE FOLLOWING CODE OUT


        Intent intent = new Intent(this, DonateActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);

    }
	}

