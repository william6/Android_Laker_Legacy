package edu.gvsu.ll;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.directory);
		
		sInstance = this;
		
		EditText searchBar = ((EditText)findViewById(R.id.DIR_txtSearch));
		searchBar.setSelected(false);
		searchBar.setMinimumWidth((int)(getWindowManager().getDefaultDisplay().getWidth() * 2 / 3) );	//TODO -- set width of search bar
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
        new DirectoryInit(getApplicationContext(), (ListView) findViewById(R.id.DIR_ListRoot), mDBM ).execute();
	}
	
	
	@Override
	public void onBackPressed(){
		//if user leaves this screen by way of the back button, just hide the activity so we can just
		//bring it to front when they come back to it, that way we don't have to reload the view
		this.moveTaskToBack(true);
	}
	
	//called from XML. user selected search button
	public void onSearchSelected(View view){
		
	}
	
	//called from XML. user selected directory options
	public void onDirOptsSelected(View view){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("List options");
		builder.setMessage("Select list sorting options");
		builder.setNegativeButton("Cancel", null);
		builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(DirectoryActivity.sInstance, "Ha, good luck with that", Toast.LENGTH_SHORT).show();
			}
		});
		
		LinearLayout dialogView = new LinearLayout(this);
		LayoutInflater inflator = (LayoutInflater) DirectoryActivity.sInstance.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflator.inflate(R.layout.dir_dialog, dialogView);
		
		Spinner spinList = (Spinner) dialogView.findViewById(R.id.DIR_DIALOG_spin_listType);
		Spinner spinSort = (Spinner) dialogView.findViewById(R.id.DIR_DIALOG_spin_sort);
		
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> listAdapter = ArrayAdapter.createFromResource(this,
		        R.array.SPIN_listType, android.R.layout.simple_spinner_item);
		listAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);	//specify layout
		spinList.setAdapter(listAdapter);
		
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(this,
				R.array.SPIN_sortMonument, android.R.layout.simple_spinner_item);
		sortAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);	//specify layout
		spinSort.setAdapter(sortAdapter);

		builder.setView(dialogView);		
		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
}



class DirectoryInit extends AsyncTask< Object, Void, String >
{
	private Context context;
	private ListView vList;
	private DatabaseManager dbm;
	private DBListAdapter lAdapter;
	private ProgressDialog vDialog;
	
	public DirectoryInit(Context context, ListView vList, DatabaseManager dbm ){
		this.context = context;
		this.vList = vList;
		this.dbm = dbm;
	}
	
	
	@Override
	protected void onPreExecute(){
		super.onPreExecute();
		
		vDialog = new ProgressDialog(DirectoryActivity.sInstance);
		vDialog.setTitle("Loading database");
		vDialog.setMessage("Please wait...");
		vDialog.show();
	}
	
	@Override
	protected String doInBackground(Object... params) {
		
		//params can be a custom object which contains info such as query information.
		//EX. major data item (donor/monument), sorting criteria (name, campus, date est, etc.), and
		// a search string ("LIKE %search_string%"). This can automate the DB loading process entirely
		
		//for now, let's just do a simple query to test this async task works and stuff
		String query = 	
				"SELECT " + GTblVal.COL_NAME + " " + 			//TODO -- get custom query (saved setting/last run)
				"FROM " + GTblVal.TBL_MONUMENT + " " + 
				"ORDER BY " + GTblVal.COL_NAME + " ASC";
		
		Cursor cursor = dbm.query(query);
		cursor.moveToFirst();
		int totalHits = cursor.getCount();
		
		//right now we only have a monument cursor. so just parse the monuments.		
		lAdapter = new DBListAdapter(context, cursor.getCount());
		
		//go through the list of monuments. Find its donors and images
		for(int i=0; i<totalHits; i++){
			String strMonumentName = cursor.getString(0);
			String strDonors = "";
			
			//find all images associated with this monument. Pick one to display
			Cursor imgCursor = dbm.query(
						"SELECT " + GTblVal.COL_FILENAME + " " +
						"FROM " + GTblVal.TBL_IMAGE + " " +
						"WHERE " + GTblVal.COL_NAME + " = '" + strMonumentName + "'" );
			imgCursor.moveToFirst();
			int imgIndex = 0;
			if( imgCursor.getCount() > 1 ){
				imgIndex = new Random().nextInt(imgCursor.getCount());
			}
			imgCursor.move(imgIndex);
			String filename = imgCursor.getString(0);
			if(imgIndex >= 1)
				filename += " " + (imgIndex+1);
			
			//find all major contributor(s) to this monument. Display all [that fit]
			Cursor donCursor = dbm.query(
						"SELECT D." + GTblVal.COL_NAME + " " +
						"FROM " + GTblVal.TBL_MON_DON + " M, " + GTblVal.TBL_DONOR + " D " +
						"WHERE M." + GTblVal.COL_NAME + " = '" + strMonumentName + "' AND " +
							"M." + GTblVal.COL_DON_ID + "=D." + GTblVal.COL_DON_ID );
			donCursor.moveToFirst();
			for(int j = 0; j < donCursor.getCount(); j++){
				if( j != 0 )
					strDonors += "\n";
				strDonors += donCursor.getString(0);
				donCursor.moveToNext();
			}
			
			lAdapter.addItem(new ListItemView(context, strMonumentName, strDonors, filename, i), i);
			cursor.moveToNext();
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(String result){
		super.onPostExecute(result);
		
		vList.setAdapter(lAdapter);
		vDialog.dismiss();
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
	int nMaxItems;
	
	public DBListAdapter(Context ctx, int maxCount){
		context = ctx;
		nMaxItems = maxCount;
		listItems = new ListItemView[maxCount];
	}
	
	public int getMaxCount(){
		return nMaxItems;
	}
	
	public void addItem(ListItemView vItem, int index){
		listItems[index] = vItem;
	}
	
	
	public int getCount() {
		return listItems.length;
	}

	public ListItemView getItem(int index) {
		return listItems[index];
	}

	public long getItemId(int index) {
		return listItems[index].getId();
	}

	public int getItemViewType(int position) {
		return 0;
	}

	public View getView(int index, View convertView, ViewGroup parent) {
		return listItems[index];
	}

	public int getViewTypeCount() {
		return listItems.length;
	}

	public boolean hasStableIds() {
		return false;
	}

	public boolean isEmpty() {
		if(listItems == null || listItems.length == 0)
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
