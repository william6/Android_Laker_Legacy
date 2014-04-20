package edu.gvsu.ll;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class DirectoryActivity extends Activity implements LocationListener, GpsStatus.Listener
{
	public static DirectoryActivity sInstance;
	private final int GPS_UPDATE_INTERVAL = 3000;		//delay time between location updates in ms
	private final int GPS_UPDATE_DISTANCE = 3;			//distance between location updates in meters (75 meters = 0.05 miles)

	//--	class member variables	--//
	private ListView 					mListView;
	private Spinner 					mSpinList, mSpinSort;
	private ArrayAdapter<CharSequence> 	mAdapList;
	private ArrayAdapter<CharSequence> 	mAdapSort;
	private QueryType 					mCurrQuery;					//current query
	private DirectoryInit 				mAsyncTask;					//null if no async task exists
	private boolean 					mEnableInput;
	private Timer						mSearchTimeout;
	private LocationManager				mLocationManager;
	private Location					mMyLocation;
	
	private final long lSearchTimeout = 1500;		//1.5 second timeout
	
	public DirectoryActivity(){
		mCurrQuery = null;
		mAsyncTask = null;
		mEnableInput = true;
		mSearchTimeout = null;
		mLocationManager = null;
	}
	
	@Override
	/**	onCreate
	 * 	Creates the Directory view by setting up the initial query of the database
	 * and creating the a DatabaseInit asynctask (initDirectory function)
	 */
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.directory);
		
		sInstance = this;

		setUpView();
        
        //query the database and create the list view asynchronously.
        QueryType query = new QueryType(new String[]{Global.COL_MON_NAME}, 
        								QueryType.STR_LIST_MONUMENT,
        								QueryType.STR_SORT_MON_NAME,
        								null);
        initDirectory(query, true);
	}
	
	@Override
	/** onResume
	 * 	When the DirectoryActivity is resumed, the keyboard is displayed automatically.
	 * 	We'd rather hide it by default so we do that here.
	 */
	public void onResume(){
		super.onResume();
		if( mListView != null )
			hideKeyboard(mListView);
	}
	
	/**	onSearchSelected
	 * @param view : view that called this function (from XML)
	 * Function is called from directory.xml when user selects the search button.
	 * Hide the keyboard and start querying the database for the search critera
	 */
	public void onSearchSelected(View view){
        hideKeyboard(view);
		initDirectory( createQuery(getSearch()), true );
	}
	
	/**	hideKeyboard
	 * @param view : view the keyboard is accepting input for
	 * Checks to see if the view is accepting keyboard input.  If it is, they soft keyboard is
	 * likely showing so we hide it.
	 */
	private void hideKeyboard(View view){
		//hide keyboard?
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if( imm.isAcceptingText() )
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);		
	}

	
	/** initDirectory
	 * @param queryDescription : query description to generate a query for the database
	 * 
	 * The provided QueryType object will be saved to this.mCurrQuery so we have a
	 * reference to the currently displayed data.  The database will then be queried
	 * through the async task, DirectoryInit.  When this task completes, the Directory
	 * view will be updated accordingly
	 */
	public void initDirectory( QueryType queryDescription, boolean bDisplayDialog ){
		if( mEnableInput ){			
			//only change the view if the new query differs from the current query
			if( mCurrQuery == null || !mCurrQuery.equals(queryDescription) ){
				mCurrQuery = queryDescription;
				
				//if another task exists, halt it
				if( mAsyncTask != null )
					mAsyncTask.cancel(true);
				mAsyncTask = new DirectoryInit( this, mListView, mMyLocation, bDisplayDialog );
				mAsyncTask.execute(queryDescription);
				mEnableInput = false;
			}
		}
	}
	
	/**	resetAsyncTask
	 * 	Called by DirectoryInit to erase our reference to the AsyncTask in case
	 * the task was cancelled for some reason.
	 */
	public void resetAsyncTask(){
		mAsyncTask = null;
	}
	
	
	/**	setUpView
	 * 	Creates the directory view and customizes all GUI elements/listeners
	 */
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
			// When text is modified, start/reset a timer. when timer expires, start new async task
			public void onTextChanged(CharSequence string, int start, int before, int count) {
				//reset/start search bar timeout timer
				if( mSearchTimeout != null )
					mSearchTimeout.cancel();
				mSearchTimeout = new Timer();
				mSearchTimeout.schedule(new TimerTask(){
					@Override
					public void run() {
						DirectoryActivity.sInstance.runOnUiThread(new Runnable(){
							public void run() {
								initDirectory( createQuery(getSearch()), true );
							}
						});
					}
					
				}, lSearchTimeout);
			}
			public void afterTextChanged(Editable s) {	}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {	}
		});
		
		//set up the spinners
		mSpinList = (Spinner) findViewById(R.id.DIR_spnList);
		mSpinSort = (Spinner) findViewById(R.id.DIR_spnSort);
		mAdapList = ArrayAdapter.createFromResource(this,
				R.array.SPIN_listType, R.layout.spinner);
		mAdapList.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);	//specify layout
		mSpinList.setAdapter(mAdapList);
		mAdapSort = ArrayAdapter.createFromResource(this,
				R.array.SPIN_sortMonument, R.layout.spinner);
		mAdapSort.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);	//specify layout
		mSpinSort.setAdapter(mAdapList);

		setSpinListeners();
		
		//set up the listview
        mListView = (ListView) findViewById(R.id.DIR_ListRoot);
        mListView.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				hideKeyboard(mListView);
			}
        });
	}
	
	
	/**	setSpinListeners
	 * 	Create the listener objects to listen for selection changes in the drop-down
	 * menus (spinners) for listing and sorting the directory
	 */
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
							R.array.SPIN_sortMonument, R.layout.spinner);
					mAdapList.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);	//specify layout
					mSpinSort.setAdapter(mAdapList);
				}

				//list by donor
				else if( selection.toString().equalsIgnoreCase("Donor")){
					// set up the Sort spinner
					mAdapList = ArrayAdapter.createFromResource(DirectoryActivity.sInstance,
							R.array.SPIN_sortContributor, R.layout.spinner);
					mAdapList.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);	//specify layout
					mSpinSort.setAdapter(mAdapList);
				}
				initDirectory( createQuery( getSearch() ), true );
			}
			public void onNothingSelected(AdapterView<?> arg0) { }
		};

		mSpinList.setOnItemSelectedListener(listener);
		mSpinSort.setOnItemSelectedListener(listener);
	}
	
	/**	getSearch
	 * @return trimmed and lowercased string of the search-text entered by the user
	 */
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
	
	/**	createQuery
	 * @param strSearch : string to search when querying. Pass null for no no search
	 * @return QueryType object containing all column, table, sort, and search data for a DB query
	 * This function looks at the current state of the Directory view and generates a query
	 * based on the current state of the application: looks at how the data should be listed,
	 * how it should be sorted, and how/if data should be searched.
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
												Global.COL_DON_ID,
												Global.COL_DUET_ID };
			}
		}
		
		//enable GPS location updates if we're sorting buildings by distance
		if( listBy == 0 && sortBy == 2){
			if( mLocationManager == null )
		       	mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			
			boolean bGPSEnabled = false;
			for(String provider : mLocationManager.getProviders(true))
				if(provider.equals("gps")){
					bGPSEnabled = true;
					break;
				}
	        
	        if( bGPSEnabled ){
	        	mMyLocation = mLocationManager.getLastKnownLocation("gps");
	        	mLocationManager.removeUpdates(this);
	        	mLocationManager.requestLocationUpdates("gps", GPS_UPDATE_INTERVAL, GPS_UPDATE_DISTANCE, this);
	        }
	        else{
	        	Toast.makeText(this, "Please enable GPS", Toast.LENGTH_LONG).show();
	        	mLocationManager.addGpsStatusListener(this);
	        }
		}
		//disable GPS location updates
		else if (mLocationManager != null ){
			mLocationManager.removeUpdates(this);
			mLocationManager.removeGpsStatusListener(this);
		}
		return new QueryType( selectCols, strTable, strSort, strSearch );
	}
	
	
	/**	enableInput
	 * @param enable : true if the Directory view should accept input from the user
	 * Function is called by DirectoryInit async task to enable input when it has
	 * completed generating the ListView (or was cancelled)
	 */
	public void enableInput(boolean enable){
		mEnableInput = enable;
	}
	
	/** onLocationChanged
	 *  This is invoked when listening for GPS updates. This function checks to
	 *  see if the location change is valid (>= 0.01 mile difference)
	 */
	public void onLocationChanged(Location location) {
		//this should only get called if we're listing buildings by distance
		//if we're sorting by distance from user, re-update query
		if( validLocationChange(location) ){
			mMyLocation = location;
			initDirectory( createQuery( getSearch() ), false );
		}
	}
	
	/** validLocationChange
	 *  @param location : new location object to compare to current location (mMyLocation)
	 *  @return true if the new location differs 0.01mi in latitude or longitude from
	 *  		the current location
	 */
	private boolean validLocationChange(Location location){
		if( mMyLocation == null )
			return true;	
		if( Math.abs( mMyLocation.getLatitude()-location.getLatitude()) > 0.0001 )
			return true;
		if( Math.abs( mMyLocation.getLongitude()-location.getLongitude()) > 0.0001 )
			return true;
		return false;
	}

	/** onProviderDisabled
	 * 	Notifies user when GPS has been disabled. Cancels DirectoryInit if one is in progress
	 */
	public void onProviderDisabled(String provider) {
		//if we lose gps, notify user with toast
		Toast.makeText(this, "GPS disabled", Toast.LENGTH_SHORT).show();
		if( mAsyncTask != null )
			mAsyncTask.cancel(true);
	}

	/** onProviderEnabled
	 * 	Notifies user if GPS is re-enabled. Updates the view with user's last known position
	 */
	public void onProviderEnabled(String provider) {
		//re-enable sorting distance from user
		Toast.makeText(this, "GPS enabled", Toast.LENGTH_SHORT).show();
        mMyLocation = mLocationManager.getLastKnownLocation(provider);
        initDirectory( createQuery( getSearch() ), true );
	}
	public void onStatusChanged(String provider, int status, Bundle extras) {	}

	/** onGpsStatusChanged
	 * 	When GPS is re-enabled, start receiving updates from GPS
	 */
	public void onGpsStatusChanged(int event) {
		if( event == GpsStatus.GPS_EVENT_STARTED || event == GpsStatus.GPS_EVENT_FIRST_FIX ){
			mLocationManager.removeUpdates(this);
			mLocationManager.requestLocationUpdates("gps", GPS_UPDATE_INTERVAL, GPS_UPDATE_DISTANCE, this);
		}
	}
}



/**	DBListAdapter
 * 	Database ListAdapter is used to populate the ListView with items queried
 * 	from the database.  The adapter doesn't contain anything until the
 * 	"loadDataFromCursor" function is called. After this is invoked, the
 * 	parent view must be refreshed to show the new results.
 */
class DBListAdapter extends BaseAdapter
{
	private ArrayList<View> listItems;
	private int nMaxItems;
	
	public DBListAdapter(Context ctx, int maxCount){
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