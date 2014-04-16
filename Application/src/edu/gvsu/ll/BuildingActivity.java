package edu.gvsu.ll;

import java.util.Random;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

/**	
 */
public class BuildingActivity extends Activity {

	//--	static instance of this object	--//
	public static BuildingActivity sInstance;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.buildingview);

		sInstance = this;	

		//grab building name passed to this activity
		String strMonument = (String) getIntent().getExtras().getSerializable(Global.MSG_BUILDING);
		
		//grab image of building
		Cursor cursor = Global.gDBM.query(	
				"SELECT " + Global.COL_FILENAME + " " +
				"FROM " + Global.TBL_MON_IMG + " " + 
				"WHERE " + Global.COL_MON_NAME + " = '" + strMonument + "'" );
		
		int imgIndex = 0;
		if( cursor.getCount() > 1)
			imgIndex = ( new Random() ).nextInt(cursor.getCount());
		cursor.moveToPosition(imgIndex);
		int imgID = getResources().getIdentifier(cursor.getString(0), "drawable", Global.PACKAGE);
		
		//set the building image and text
		( (ImageView) findViewById(R.id.BUILD_imgBuilding) ).setImageResource(imgID);
		( (TextView) findViewById(R.id.BUILD_txtBuildingName) ).setText(strMonument);
	}
}