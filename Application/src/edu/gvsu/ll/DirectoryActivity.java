package edu.gvsu.ll;

import java.io.File;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
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
        String query = 	"SELECT " + GTblVal.COL_NAME + " " + 			//TODO -- get custom query (saved setting/last run)
						"FROM " + GTblVal.TBL_MONUMENT + " " + 
        				"ORDER BY " + GTblVal.COL_NAME + " ASC";
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
		mAdapter.loadDataFromCursor(mDBM, cursor);
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

		int imgID = ((android.app.Activity)context).getResources().getIdentifier(strFilename, "drawable", DirectoryActivity.PACKAGE);
		//icon.setImageResource(imgID);
		BitmapFactory.Options imgOptions = new BitmapFactory.Options();
		imgOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeResource( getResources(), imgID, imgOptions );
		imgOptions.inSampleSize = calcIconSize( imgOptions, 100, 100 );
		imgOptions.inJustDecodeBounds = false;
		icon.setImageBitmap( BitmapFactory.decodeResource( getResources(), imgID, imgOptions ) );
		title.setText( strTitle );
		subtitle.setText( strSubtitle );

		if(index % 2 == 0)
			this.setBackgroundColor( Color.argb(255, 245, 228, 156 ) );
		else
			this.setBackgroundColor( Color.argb( 255, 250, 240, 201 ));
	}

	private int calcIconSize( BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
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
	public void loadDataFromCursor(DatabaseManager dbm, Cursor monumentCursor){
		monumentCursor.moveToFirst();
		listItems = new ListItemView[monumentCursor.getCount()];
		
		//go through the list of monuments. Find its donors and images
		for(int i=0; i<monumentCursor.getCount(); i++){
			String strMonumentName = monumentCursor.getString(0);
			String strDonors = "donor names go here";
			
			//find all images associated with this monument. Pick one to display
			String strQuery = 
						"SELECT " + GTblVal.COL_FILENAME + " " +
						"FROM " + GTblVal.TBL_IMAGE + " " +
						"WHERE " + GTblVal.COL_NAME + " = '" + strMonumentName + "'" ;
			Cursor imgCursor = dbm.query( strQuery );
			imgCursor.moveToFirst();
			int imgIndex = 0;
			if( imgCursor.getCount() > 1 ){
				imgIndex = new Random().nextInt(imgCursor.getCount());
			}
			imgCursor.move(imgIndex);
			String filename = imgCursor.getString(0);
			
			//find all major contributor(s) to this monument. Display all [that fit]
			//Cursor donCursor = dbm.query(
			//			"SELECT D." + GTblVal.COL_NAME + " " +
			//			"FROM " + GTblVal.TBL_DONOR + " D, " + GTblVal.TBL_MONUMENT + " M " +
			//			"WHERE D."
			
			
			listItems[i] = new ListItemView(context, strMonumentName, strDonors, filename, i);
			monumentCursor.moveToNext();
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
