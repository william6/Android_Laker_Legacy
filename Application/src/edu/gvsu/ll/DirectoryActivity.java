package edu.gvsu.ll;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class DirectoryActivity extends Activity
{
	public static String PACKAGE = "edu.gvsu.ll";
	public static DirectoryActivity sInstance;
	private DatabaseManager mDBM;
	private ListView mListView;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.directory);
		
		sInstance = this;
		
		EditText searchBar = ((EditText)findViewById(R.id.DIR_txtSearch));
		searchBar.setSelected(false);
		//TODO -- set width of search bar
		searchBar.setMinimumWidth((int)(getWindowManager().getDefaultDisplay().getWidth() * 2 / 3) );
		getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		
		//copy database file from assets to internal directory so we can use it
		String strDbName = "Laker Legacies.sqlite";
		InputStream input = null;
		FileOutputStream output = null;
		File internalDB = null;
		try{
			input = getAssets().open(strDbName);
			internalDB = new File( getFilesDir().getAbsolutePath() + "/" + strDbName );
			internalDB.createNewFile();
			output = new FileOutputStream(internalDB);
			
			byte[] buffer = new byte[100*1024];
			while(input.available() > 0){
				input.read(buffer);
				output.write(buffer);
			}
			input.close();
			output.close();
		}
		catch(IOException ioe){

		}

		//load the database file
        mDBM = new DatabaseManager(this, internalDB, 1);
        
        //query the database and create the list view asynchronously.
        //This opens a loading dialog while it works
        //set default query type
        mListView = (ListView) findViewById(R.id.DIR_ListRoot);
        QueryType query = new QueryType(new String[]{GTblVal.COL_NAME}, 
        								QueryType.STR_LIST_MONUMENT,
        								QueryType.STR_SORT_NAME,
        								null);
        initDirectory(query);
	}
	
	
	@Override
	public void onBackPressed(){
		//if user leaves this screen by way of the back button, just hide the activity so we can just
		//bring it to front when they come back to it, that way we don't have to reload the view
		this.moveTaskToBack(true);
	}
	
	//called from XML. user selected search button
	public void onSearchSelected(View view){
		//hide keyboard
		getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		//TODO -- set up new query for database and run it.
	}
	
	//called from XML. user selected directory options
	public void onDirOptsSelected(View view){
		DirectoryDialog dialog = new DirectoryDialog(this);
		dialog.show();
	}
	
	public void initDirectory( QueryType queryDescription ){
		new DirectoryInit( this, mListView, mDBM ).execute(queryDescription);
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
	private ArrayList<View> listItems;
	private Context context;
	private int nMaxItems;
	
	public DBListAdapter(Context ctx, int maxCount){
		context = ctx;
		nMaxItems = maxCount;
		listItems = new ArrayList<View>();
	}
	
	public int getMaxCount(){
		return nMaxItems;
	}
	
	public void addItem(View vItem){
		listItems.add(vItem);
	}
	
	
	public int getCount() {
		return listItems.size();
	}

	public View getItem(int index) {
		return listItems.get(index);
	}

	public long getItemId(int index) {
		return getItem(index).getId();
	}

	public int getItemViewType(int position) {
		return 0;
	}

	public View getView(int index, View convertView, ViewGroup parent) {
		return getItem(index);
	}

	public int getViewTypeCount() {
		return getCount();
	}

	public boolean hasStableIds() {
		return false;
	}

	public boolean isEmpty() {
		if(listItems == null || getCount() == 0)
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
