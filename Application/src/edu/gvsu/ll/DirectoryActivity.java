package edu.gvsu.ll;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DirectoryActivity extends Activity
{
	public static String PACKAGE = "edu.gvsu.ll";
	public static DirectoryActivity sInstance;
	private DBListAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.directory);
		
		sInstance = this;
		
		//Create database manager
        File fInternalDir = getFilesDir();	//app data directory
        String strDbName = "Laker_Legacy_DB";
        DatabaseManager dbManager = new DatabaseManager(getApplicationContext(), strDbName, fInternalDir.getAbsolutePath() + "/database/", 1);   
        dbManager.createTables();
		XMLProcessManager xmlManager = new XMLProcessManager(dbManager);
		xmlManager.addMonuments();
    	xmlManager.addImages();
		xmlManager.addDonors();
		dbManager.saveDatabase();	//TODO -- for debugging. delete on port
        
		//set up the list view
		ListView vList = (ListView) findViewById(R.id.DIR_ListRoot);
        mAdapter = new DBListAdapter(this);
        
        //start the loader thread
        //monument name, donor name(s), filepath to image(s)
        String query = 	"SELECT M." + GTblVal.COL_NAME + ", I." + GTblVal.COL_FILENAME + " " +			//TODO -- get custom query (saved setting/last run)
						"FROM " + GTblVal.TBL_MONUMENT + " M, " + GTblVal.TBL_IMAGE + " I " +
						"WHERE M." + GTblVal.COL_NAME + "=I." + GTblVal.COL_NAME + " " +
						"ORDER BY M." + GTblVal.COL_NAME + " ASC";
        Thread loadData = new Thread(new DBLoader(dbManager, mAdapter, query));
        loadData.start();
        
        // TODO -- throw up a loading screen (if necessary)
        while(loadData.isAlive()){   }
        
        vList.setAdapter(mAdapter);	//the data has been loaded. Show the listview
		
        
        //query database to make sure it's working
/*        Cursor cursor = dbManager.query("SELECT * FROM " + GTblVal.TBL_MONUMENT);
        cursor.moveToFirst();
        for(int i=0; i<cursor.getCount(); i++){
        	for(int j=0; j<cursor.getColumnCount(); j++){
        		Log.d(cursor.getColumnName(j).toUpperCase() + ": " + cursor.getString(j));
        	}
        	Log.d(" ");
        	cursor.moveToNext();
        }
		
        cursor = dbManager.query("SELECT * FROM " + GTblVal.TBL_IMAGE);
        cursor.moveToFirst();
        for(int i=0; i<cursor.getCount(); i++){
        	for(int j=0; j<cursor.getColumnCount(); j++){
        		Log.d(cursor.getColumnName(j).toUpperCase() + ": " + cursor.getString(j));
        	}
        	cursor.moveToNext();
        }
*/
	}
}

//create a separate thread to query the database and load the views (must be separate thread. required by Android 4.0.3
class DBLoader implements Runnable{
	private DatabaseManager mDBM;
	private DBListAdapter mAdapter;
	private String mstrQuery;
	
	public DBLoader(DatabaseManager dbm, DBListAdapter adapter, String query){
		mDBM = dbm;
		mAdapter = adapter;
		mstrQuery = query;
	}
	
	public void run() {
		Cursor cursor = mDBM.query( mstrQuery );
		mAdapter.loadDataFromCursor(cursor);
	}
}


/*	ListItemView
 * 	Custom view for each item in the ListView
 */
class ListItemView extends RelativeLayout
{
	public ListItemView( Context context, String strTitle, String strSubtitle, String strFilename, int index ) {
		super(context);
		LayoutInflater inflator = (LayoutInflater) DirectoryActivity.sInstance.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflator.inflate(R.layout.listitem, this);
		ImageView icon = (ImageView)this.findViewById(R.id.LI_imgIcon);
		TextView title = (TextView)this.findViewById(R.id.LI_txtTitle);
		TextView subtitle = (TextView)this.findViewById(R.id.LI_txtSubtitle);
		
		//int imgID = ((android.app.Activity)context).getResources().getIdentifier(strFilename, "drawable", DirectoryActivity.PACKAGE);
		//icon.setImageResource(imgID);
		title.setText( strTitle );
		subtitle.setText( strSubtitle );
		
		if(index % 2 == 0)
			this.setBackgroundColor(Color.CYAN);
		else
			this.setBackgroundColor(Color.MAGENTA);
	}
}

/*	DBListAdapter
 * 	Database ListAdapter is used to populate the ListView with items queried
 * 	from the database.  The adapter doesn't contain anything until the
 * 	"loadDataFromCursor" function is called. After this is invoked, the
 * 	parent view must be refreshed to show the new results.
 */
class DBListAdapter implements ListAdapter
{
	ListItemView listItems[];
	Context context;
	
	public DBListAdapter(Context ctx){
		context = ctx;
		listItems = null;
	}
	
	//given a cursor from a query, add the item to the list
	public void loadDataFromCursor(Cursor cursor){
		cursor.moveToFirst();
		listItems = new ListItemView[cursor.getCount()];
		
		for(int i=0; i<cursor.getCount(); i++){
			String strName = cursor.getString(0);
			String filepath = cursor.getString(1);
			String subtitle = "donor names go here";
			listItems[i] = new ListItemView(context, strName, subtitle, filepath, i);
			cursor.moveToNext();
		}
	}
	
	public int getCount() {
		if(listItems == null)
			return 1;
		else
			return listItems.length;
	}

	public ListItemView getItem(int index) {
		if(listItems == null)
			return null;
		else
			return listItems[index];
	}

	public long getItemId(int index) {
		if(listItems == null)
			return -1;
		else
			return listItems[index].getId();
	}

	public int getItemViewType(int position) {
		return 0;
	}

	public View getView(int index, View convertView, ViewGroup parent) {
		if(listItems == null)
			return null;
		else
			return listItems[index];
	}

	public int getViewTypeCount() {
		if(listItems == null)
			return 1;
		else
			return listItems.length;
	}

	public boolean hasStableIds() {
		return false;
	}

	public boolean isEmpty() {
		if(listItems == null)
			return true;
		else
			return false;
	}

	public void registerDataSetObserver(DataSetObserver observer) {
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
	
	}

	public boolean areAllItemsEnabled() {
		return true;
	}

	public boolean isEnabled(int arg0) {
		return true;
	}
}
