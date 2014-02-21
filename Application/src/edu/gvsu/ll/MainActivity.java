package edu.gvsu.ll;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class MainActivity extends Activity {
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /* ENTER THE CLASS YOU ARE TESTING RIGHT HERE.
         * IF YOU'RE TESTING THE MAIN ACTIVITY, COMMENT THE FOLLOWING CODE OUT
         **/
        Intent intent = new Intent(this, DirectoryActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
    }
}