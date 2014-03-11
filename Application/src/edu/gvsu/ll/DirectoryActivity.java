package edu.gvsu.ll;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class DirectoryActivity extends Activity
{
	public static DirectoryActivity sInstance;
	
	private DatabaseManager mDBM;
	private ListView mListView;
	private Spinner mSpinList, mSpinSort;
	private ArrayAdapter<CharSequence> mAdapList;
	private ArrayAdapter<CharSequence> mAdapSort;
	
	private boolean enableInput = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.directory);
		
		sInstance = this;
		
		setUpView();
		
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
        mListView.setSmoothScrollbarEnabled(false);
        mListView.setDividerHeight(10);
        QueryType query = new QueryType(new String[]{Global.COL_NAME}, 
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
//	public void onDirOptsSelected(View view){
//		DirectoryDialog dialog = new DirectoryDialog(this);
//		dialog.show();
//	}
	
	public void initDirectory( QueryType queryDescription ){
		new DirectoryInit( this, mListView, mDBM ).execute(queryDescription);
	}
	
	private void setUpView(){
		//search bar
		((EditText)findViewById(R.id.DIR_txtSearch)).setSelected(false);
		getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		//set up the spinners
		mSpinList = (Spinner) findViewById(R.id.DIR_spnList);
		mSpinSort = (Spinner) findViewById(R.id.DIR_spnSort);

		// set up the List spinner
		mAdapList = ArrayAdapter.createFromResource(this,
				R.array.SPIN_listType, android.R.layout.simple_spinner_item);
		mAdapList.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);	//specify layout
		mSpinList.setAdapter(mAdapList);

		// set up the Sort spinner
		mAdapSort = ArrayAdapter.createFromResource(this,
				R.array.SPIN_sortMonument, android.R.layout.simple_spinner_item);
		mAdapSort.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);	//specify layout
		mSpinSort.setAdapter(mAdapList);

		setSpinListeners();
	}
	
	
	private void setSpinListeners(){
		
		//List spinner
		OnItemSelectedListener listener = new OnItemSelectedListener(){

			//When an item is selected, we want to first update the spinners to display the correct values.
			//After this, we want to update the directory view with the selection
			public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {				
				CharSequence selection = (CharSequence)adapter.getItemAtPosition(position);

				//If we're listing by monument, update the sorting spinner to sort by monument items
				if( selection.toString().equalsIgnoreCase("Building")){
					// set up the Sort spinner
					mAdapList = ArrayAdapter.createFromResource(DirectoryActivity.sInstance,
							R.array.SPIN_sortMonument, android.R.layout.simple_spinner_item);
					mAdapList.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);	//specify layout
					mSpinSort.setAdapter(mAdapList);
				}

				//list by donor
				else if( selection.toString().equalsIgnoreCase("Donor")){
					// set up the Sort spinner
					mAdapList = ArrayAdapter.createFromResource(DirectoryActivity.sInstance,
							R.array.SPIN_sortContributor, android.R.layout.simple_spinner_item);
					mAdapList.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);	//specify layout
					mSpinSort.setAdapter(mAdapList);
				}
				if( enableInput ){
					enableInput = false;
					initDirectory( createQuery() );
				}
			}
			public void onNothingSelected(AdapterView<?> arg0) { }
		};

		mSpinList.setOnItemSelectedListener(listener);
		mSpinSort.setOnItemSelectedListener(listener);
	}
	
	private QueryType createQuery(){
		int listBy = (Integer) ((Spinner)DirectoryActivity.sInstance.findViewById(R.id.DIR_spnList)).getSelectedItemPosition();
		int sortBy = (Integer) ((Spinner)DirectoryActivity.sInstance.findViewById(R.id.DIR_spnSort)).getSelectedItemPosition();
		
		String [] selectCols = null;
		String strTable = null, strSort = null;
		
		if(listBy == 0)
			strTable = Global.TBL_MONUMENT;
		else if (listBy == 1)
			strTable = Global.TBL_DONOR;

		switch( sortBy ){
			case(0):
				strSort = QueryType.STR_SORT_NAME;
				if( strTable.equalsIgnoreCase(Global.TBL_MONUMENT))
					selectCols = new String [] { Global.COL_NAME };
				else if( strTable.equalsIgnoreCase(Global.TBL_DONOR))
					selectCols = new String [] { Global.COL_NAME, Global.COL_DON_ID };
				break;
			case(1):
				strSort = QueryType.STR_SORT_CAMPUS;
				selectCols = new String [] { Global.COL_NAME, Global.COL_CAMPUS };
				break;
//			case(2):
//				strSort = QueryType.STR_SORT_DATE;
//				selectCols = new String [] { Global.COL_NAME, Global.COL_EST };
//				break;
			case(2):
				strSort = QueryType.STR_SORT_DISTANCE;
				selectCols = new String [] { Global.COL_NAME, Global.COL_LATITUDE, Global.COL_LONGITUDE };
				break;
		}	
		return new QueryType( selectCols, strTable, strSort, null );
	}
	
	public void enableInput(boolean enable){
		enableInput = enable;
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
