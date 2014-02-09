package edu.gvsu.ll;

import java.io.File;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DirectoryActivity extends Activity
{
	
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
//    	xmlManager.addImages();
		xmlManager.addDonors();
		dbManager.saveDatabase();	//TODO -- for debugging. delete on port
        
		//set up the list view
		ListView vList = (ListView) findViewById(R.id.directory_ListRoot);
        mAdapter = new DBListAdapter(this);

        //create a separate thread to query the database and load the views
        class DBLoader implements Runnable{
        	private DatabaseManager mDBM;
        	
        	public DBLoader(DatabaseManager dbm){
        		mDBM = dbm;
        	}
        	
			public void run() {
				Cursor cursor = mDBM.query(
							"SELECT " + GTblVal.COL_NAME + " " +
							"FROM " + GTblVal.TBL_MONUMENT + " " +
							"ORDER BY " + GTblVal.COL_NAME + " ASC");
				mAdapter.loadDataFromCursor(cursor);
			}
        }
        
        //start the loader thread
        Thread loadData = new Thread(new DBLoader(dbManager));
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


/*	ListItemView
 * 	Custom view for each item in the ListView
 */
class ListItemView extends TextView
{
	public ListItemView(Context context, String text) {
		super(context);
		setText(text);
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
			listItems[i] = new ListItemView(context, cursor.getString(0));
			if(i%2 == 0)
				listItems[i].setTextColor(Color.WHITE);
			else
				listItems[i].setTextColor(Color.YELLOW);
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
