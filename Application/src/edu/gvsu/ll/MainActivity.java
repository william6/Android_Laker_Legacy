package edu.gvsu.ll;

import android.app.Activity;
import android.app.TabActivity;
import android.os.Bundle;

public class MainActivity extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}