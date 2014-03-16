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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class DirectoryActivity extends Activity
{
	public static DirectoryActivity sInstance;
	
	private DatabaseManager 			mDBM;
	private ListView 					mListView;
	private Spinner 					mSpinList, mSpinSort;
	private ArrayAdapter<CharSequence> 	mAdapList;
	private ArrayAdapter<CharSequence> 	mAdapSort;
	private QueryType 					mCurrQuery;					//current query
	private DirectoryInit 				mAsyncTask;					//null if no async task exists
	private boolean 					enableInput;
	
	
	public DirectoryActivity(){
		mCurrQuery = null;
		mAsyncTask = null;
		enableInput = true;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.directory);
		
		sInstance = this;

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

		setUpView();
        
        //query the database and create the list view asynchronously.
        QueryType query = new QueryType(new String[]{Global.COL_MON_NAME}, 
        								QueryType.STR_LIST_MONUMENT,
        								QueryType.STR_SORT_MON_NAME,
        								null);
        initDirectory(query);
	}
	
	
	//TODO ??
//	@Override
//	public void onBackPressed(){
//		//if user leaves this screen by way of the back button, just hide the activity so we can just
//		//bring it to front when they come back to it, that way we don't have to reload the view
//	}
	
	//called from XML. user selected search button
	public void onSearchSelected(View view){
		//hide keyboard?
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if( imm.isAcceptingText() )
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);		
        
		initDirectory( createQuery(getSearch()) );
	}

	
	/** 
	 * 
	 * @param queryDescription : query description to generate a query for the database
	 * 
	 * The provided QueryType object will be saved to this.mCurrQuery and the DB will be
	 * queried accordingly.  UI will update based on the results.
	 */
	public void initDirectory( QueryType queryDescription ){
		if( enableInput ){
			enableInput = false;
			
			//only change the view if the new query differs from the current query
			if( mCurrQuery == null || !mCurrQuery.equals(queryDescription) ){
				mCurrQuery = queryDescription;
				
				//if another task exists, halt it
				if( mAsyncTask != null )
					mAsyncTask.cancel(true);
				mAsyncTask = new DirectoryInit( this, mListView, mDBM );
				mAsyncTask.execute(queryDescription);
			}
		}
	}
	
	public void resetAsyncTask(){
		mAsyncTask = null;
	}
	
	private void setUpView(){
		//search bar - hide search bar on start
		EditText vSearchBar = (EditText)findViewById(R.id.DIR_txtSearch);
		vSearchBar.setSelected(false);
		getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		//listen for soft keyboard event
		vSearchBar.setOnEditorActionListener(new OnEditorActionListener(){
			public boolean onEditorAction(TextView view, int actionID, KeyEvent event) {
				if( actionID == EditorInfo.IME_ACTION_SEARCH ){
					onSearchSelected(view);
					return true;
				}
				else
					return false;
			}
        });
		//listen for text changes to the search bar
		vSearchBar.addTextChangedListener(new TextWatcher(){
			//TODO -- set up a timer that times out the keyboard. don't query if timeout hasn't happend
			// (give user time to edit entry).  When timer times out, then start new async task
			public void onTextChanged(CharSequence string, int start, int before, int count) {
				initDirectory( createQuery(getSearch()) );
			}
			public void afterTextChanged(Editable s) {	}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {	}
		});
		
		//set up the spinners
		mSpinList = (Spinner) findViewById(R.id.DIR_spnList);
		mSpinSort = (Spinner) findViewById(R.id.DIR_spnSort);
		mAdapList = ArrayAdapter.createFromResource(this,
				R.array.SPIN_listType, android.R.layout.simple_spinner_item);
		mAdapList.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);	//specify layout
		mSpinList.setAdapter(mAdapList);
		mAdapSort = ArrayAdapter.createFromResource(this,
				R.array.SPIN_sortMonument, android.R.layout.simple_spinner_item);
		mAdapSort.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);	//specify layout
		mSpinSort.setAdapter(mAdapList);

		setSpinListeners();
		
		//set up the listview
        mListView = (ListView) findViewById(R.id.DIR_ListRoot);
        mListView.setSmoothScrollbarEnabled(false);
        mListView.setDividerHeight(10);
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
				initDirectory( createQuery( getSearch() ) );
			}
			public void onNothingSelected(AdapterView<?> arg0) { }
		};

		mSpinList.setOnItemSelectedListener(listener);
		mSpinSort.setOnItemSelectedListener(listener);
	}
	
	private String getSearch(){
		String strSearch = ((EditText)findViewById(R.id.DIR_txtSearch)).getText().toString();
		if( strSearch == null || strSearch.length() == 0)
			return null;
		else{
			strSearch = strSearch.trim().toLowerCase();
			if(strSearch.length() == 0)
				return null;
			else
				return strSearch;
		}
	}
	
	/**
	 * 
	 * @param strSearch : string to search when querying. Pass null for no no search
	 * @return QueryType object containing all column, table, sort, and search data for a DB query
	 */
	private QueryType createQuery(String strSearch){
		int listBy = (Integer) ((Spinner)DirectoryActivity.sInstance.findViewById(R.id.DIR_spnList)).getSelectedItemPosition();
		int sortBy = (Integer) ((Spinner)DirectoryActivity.sInstance.findViewById(R.id.DIR_spnSort)).getSelectedItemPosition();
		
		String [] selectCols = null;
		String strTable = null, strSort = null;
		
		if(listBy == 0){
			strTable = Global.TBL_MONUMENT;
			switch( sortBy ){
				case(0):
					strSort = QueryType.STR_SORT_MON_NAME;
					selectCols = new String [] { Global.COL_MON_NAME };
					break;
				case(1):
					strSort = QueryType.STR_SORT_CAMPUS;
					selectCols = new String [] { Global.COL_MON_NAME, 
												Global.COL_CAMPUS };
					break;
				case(2):
					strSort = QueryType.STR_SORT_DISTANCE;
					selectCols = new String [] { Global.COL_MON_NAME, 
												Global.COL_LATITUDE, 
												Global.COL_LONGITUDE };
			}	
		}
		else if (listBy == 1){
			strTable = Global.TBL_DONOR;
			switch(sortBy){
				case(0):
					strSort = QueryType.STR_SORT_DON_NAME;
					selectCols = new String[] { Global.COL_TITLE, 
												Global.COL_FNAME, 
												Global.COL_MNAME, 
												Global.COL_LNAME, 
												Global.COL_SUFFIX,
												Global.COL_DON_ID };
			}
		}
		return new QueryType( selectCols, strTable, strSort, strSearch );
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
