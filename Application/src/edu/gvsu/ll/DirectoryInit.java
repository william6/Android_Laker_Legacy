package edu.gvsu.ll;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DirectoryInit extends AsyncTask< QueryType, Void, Integer >
{
	private final int STATUS_OK 		= 0;		// AsyncTask returned okay
	private final int ERR_NO_RESULTS 	= 1;		// no results found
	private final int ERR_CANCEL		= 2;		//task was cancelled and halted
	private final int ERR_GENERAL 		= 3;		// some error occurred
	private final int ERR_NO_LOCATION	= 4;		// we do not have a last known location
	
	//--	class member variables	--//
	private Context 		context;
	private ListView 		vList;
	private DatabaseManager dbm;
	private DBListAdapter 	lAdapter;
	private ProgressDialog 	vDialog;
	private boolean			bShowDialog;
	private Location		lastKnownLocation;

	/**	DirectoryInit
	 * @param context : context of the Directory
	 * @param vList : ListView to display all of the ListItemViews created
	 */
	public DirectoryInit(Context context, ListView vList, Location lastKnownLocation, boolean bShowDialog ){
		this.context = context;
		this.vList = vList;
		this.dbm = Global.gDBM;
		this.bShowDialog = bShowDialog;
		this.lastKnownLocation = lastKnownLocation;
	}

	@Override
	/**	onPreExecute
	 * 	Before we start crunching data, display a dialog to the user so they
	 * know we're processing data
	 */
	protected void onPreExecute(){
		super.onPreExecute();
		if( bShowDialog ){
			vDialog = new ProgressDialog(DirectoryActivity.sInstance);
			vDialog.setCanceledOnTouchOutside(false);
			vDialog.setTitle("Loading data");
			Cursor facts = dbm.query("SELECT " + Global.COL_FACT + " FROM " + Global.TBL_FACTS);
			facts.moveToFirst();
			int rIndex = new Random().nextInt(facts.getCount());
			facts.moveToPosition(rIndex);
			vDialog.setMessage(facts.getString(0));
			vDialog.show();
		}
	}

	@Override
	/**	doInBackground
	 * 	Given a QueryType object passed by params, this function constructs an initial
	 * query, queries the database for the desired info, creates a view for each item
	 * for the list, and then populates the list.
	 */
	protected Integer doInBackground(QueryType... params) {
		try{
			//grab the query description and create a query
			String  [] selectColumns = params[0].getSelectColumns();
			String strTable = params[0].getTableField();
			String sortBy = params[0].getSortField();
			String search = params[0].getSearchField();

			String columns = selectColumns[0];
			for( int i=1; i<selectColumns.length; i++)
				columns += ", " + selectColumns[i];

			String query =	"SELECT " + columns + " " +
							"FROM " + strTable + " ";
				
			//add filters (WHERE clause) - filters search and donors if we're searching donors
			query += getQueryFilter(strTable, search);

			if( sortBy.compareTo(QueryType.STR_SORT_DISTANCE) != 0)
				query += "ORDER BY " + sortBy + " ASC";

			if( isCancelled() )
				return ERR_CANCEL;
			
			Cursor listCursor = dbm.query(query);
			listCursor.moveToFirst();
			
			if( isCancelled() )
				return ERR_CANCEL;
			
			if(listCursor.getCount() == 0){
				return ERR_NO_RESULTS;
			}

			lAdapter = new DBListAdapter(context, listCursor.getCount());

			int status = STATUS_OK;
			
			// list by monument
			if( strTable.equalsIgnoreCase(Global.TBL_MONUMENT) ){
				//check if we're sorting by distance
				if( sortBy.compareTo(QueryType.STR_SORT_DISTANCE) == 0 ){
					if( lastKnownLocation != null )
						status = createMonumentLocationViews( 
								listCursor, lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
					else
						status = ERR_NO_LOCATION;
				}
				else
					status = createMonumentViews( listCursor, sortBy );
			}

			// list by donor
			else
				status = createDonorViews( listCursor, sortBy );

			return status;
		}
		catch(Exception e){
			return ERR_GENERAL;
		}
	}
	
	/**	getQueryFilter
	 * @param strTable : name of the table we're querying
	 * @param search : string to search the data for (searches name's only). Can be null
	 * @return string representing the WHERE clause of the query
	 */
	private String getQueryFilter(String strTable, String search){		
		
		//search MONUMENT
		if( strTable.equalsIgnoreCase(Global.TBL_MONUMENT) ){
			if( search == null )
				return "";
			else
				return "WHERE lower(" + QueryType.STR_SORT_MON_NAME + ") LIKE '%" + search + "%' ";
		}
		
		//search DONOR
		else{
			//since we're searching for donors, some donors are 'duet' entities
			// (man and wife contribution).  In these cases, we only want to display the
			// pair, or duet, rather than each donor individually. We accomplish this
			// by only grabbing the 'duet' donor IDs and removing the other IDs from the list.
			// For instance, "Bill & Sally Seidman" is a duet entity and "William/Bill Seidman"
			// is his own entity.  Rather than display both "Bill & Sally" and "William", we
			// want to display only "Bill & Sally" by grabbing only the ID of "Bill & Sally" and
			// excluding the ID of "William"
			String filter = "WHERE " + Global.COL_DON_ID + " NOT IN " + 
									"(SELECT " + Global.COL_DUET_ID + 
									" FROM " + strTable + 
									" WHERE " + Global.COL_DUET_ID + " IS NOT NULL)";
			if( search == null )
				return filter;
			else
				return 	filter += 
						" AND (lower(" + Global.COL_TITLE + ") LIKE '%" + search + "%' OR " +
						"lower(" + Global.COL_FNAME + ") LIKE '%" + search + "%' OR " +
						"lower(" + Global.COL_MNAME + ") LIKE '%" + search + "%' OR " +
						"lower(" + Global.COL_LNAME + ") LIKE '%" + search + "%' OR " +
						"lower(" + Global.COL_SUFFIX + ") LIKE '%" + search + "%' ) ";
		}
	}

	@Override
	/**	onPostExecute
	 * 	This is executed only when 'doInBackground' is completed. Dismisses the dialog
	 * box, sets the ListView adapter to the newly created adapter, and cleans up resources.
	 * This function is bypassed if the async task is cancelled (onCancelled)
	 */
	protected void onPostExecute(Integer result){
		super.onPostExecute(result);
		if(bShowDialog)
			vDialog.dismiss();	
		
		DirectoryActivity.sInstance.enableInput(true);
		
		switch(result){
			case(ERR_NO_LOCATION):
				lAdapter.addItem(new ListItemView( context ));	//create blank item
			case(STATUS_OK):
				vList.setAdapter(lAdapter);
				break;
			case(ERR_NO_RESULTS):
				Toast.makeText(DirectoryActivity.sInstance, "No results found.", Toast.LENGTH_LONG).show();
				break;
			case(ERR_CANCEL):
			case(ERR_GENERAL):
				break;
		}			
		DirectoryActivity.sInstance.resetAsyncTask();
	}
	
	@Override
	/**	onCancelled
	 * 	cleans up resources if this task is halted prematurely
	 */
	protected void onCancelled(Integer result){
		//if this task was cancelled, do not set a new view
		if(bShowDialog)
			vDialog.dismiss();
		DirectoryActivity.sInstance.enableInput(true);
		super.onCancelled(result);
		DirectoryActivity.sInstance.resetAsyncTask();
	}

	
	/**	addListHeading
	 * @param cursor : list cursor containing the data we're displaying in the listView
	 * @param adapter : ListView adapter where the heading will be placed along with the List items
	 * @param previousRecord : String representing the previous record in the list
	 * @param sortBy : field the list is being sorted by
	 * @return String representing the current record
	 * Checks to see if a new heading should be added to the ListView.  It determines this by looking at the
	 * previous record in the list and by looking at what the list is being sorted by. This function
	 * supports creating headers for Donor names, building names, and building campuses.
	 */
	private String addListHeading(Cursor cursor, DBListAdapter adapter, String previousRecord, String sortBy ){
		//determine current record string
		String header = "";
		String strThisRecord = "";
		if( sortBy.equalsIgnoreCase(QueryType.STR_SORT_MON_NAME ) )
			strThisRecord = cursor.getString(0);	//monument name column of query
		else if( sortBy.equalsIgnoreCase(QueryType.STR_SORT_DON_NAME))
			strThisRecord = cursor.getString(3);	//last name column of query
		else if( sortBy.equalsIgnoreCase(Global.COL_CAMPUS ) )
			strThisRecord = cursor.getString(1);	//campus column of query

		if(strThisRecord == null)
			strThisRecord = "";

		//if previous record is null, there is no previous record. if the previous record is an empty string, the previous record is an empty string :)
		if( previousRecord == null ){

			//create name header
			if( sortBy.equalsIgnoreCase(QueryType.STR_SORT_MON_NAME) || sortBy.equalsIgnoreCase(QueryType.STR_SORT_DON_NAME) ){
				header = strThisRecord.substring(0, 1).toUpperCase();
			}

			//create campus header
			else if( sortBy.equalsIgnoreCase(Global.COL_CAMPUS) ){
				header = strThisRecord.toUpperCase();
			}
		}
		else{
			//do we need to add a name header?
			if( sortBy.equalsIgnoreCase(QueryType.STR_SORT_MON_NAME ) || sortBy.equalsIgnoreCase(QueryType.STR_SORT_DON_NAME) ){
				if(previousRecord.substring(0,1).equalsIgnoreCase(strThisRecord.substring(0,1)))
					return strThisRecord;
				else
					header = strThisRecord.substring(0, 1);
			}

			//do we need a campus header?
			else if( sortBy.equalsIgnoreCase(QueryType.STR_SORT_CAMPUS) ){
				if(previousRecord.compareTo(strThisRecord) == 0 )
					return strThisRecord;
				else
					header = strThisRecord.toUpperCase();
			}
		}

		//if we need to create a header, inflate a view and add it to the list adapter
		//inflate custom view
		LinearLayout lHeading = new LinearLayout(context);
		LayoutInflater inflator = (LayoutInflater) DirectoryActivity.sInstance.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflator.inflate(R.layout.listheader, lHeading);
		TextView vHeading = (TextView)lHeading.findViewById(R.id.LH_txtHeading);
		vHeading.setText(header);
		adapter.addItem( lHeading );
		return strThisRecord;
	}

	/**	addDistanceHeading
	 * @param adapter : list adapter that holds all items in the list
	 * @param strThisRecord : string representing this record (distance in miles - 2 decimal places)
	 * @param previousRecord : string representing the previous record in the list (distance in miles - 2 decimal places)
	 * @return String representing the current record's heading string (distance in miles - 2 decimal places)
	 * Determines if a new heading needs to be created in the ListView for displaying this record.  If a new
	 * heading is needed, it is created and added to the listAdapter. A string representing the current
	 * record's heading string is returned
	 */
	private String addDistanceHeading( DBListAdapter adapter, String strThisRecord, String previousRecord ){

		//determine current record string
		String header = "";

		//if previous record is null, create new header
		if( previousRecord == null ){
			header = strThisRecord;
		}

		//do we need to create a header?
		else{
			if(previousRecord.compareTo(strThisRecord) == 0 )
				return strThisRecord;	//don't create a header, return
			else
				header = strThisRecord;
		}

		//if we need to create a header, inflate a view and add it to the list adapter
		//inflate custom view
		LinearLayout lHeading = new LinearLayout(context);
		LayoutInflater inflator = (LayoutInflater) DirectoryActivity.sInstance.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflator.inflate(R.layout.listheader, lHeading);
		TextView vHeading = (TextView)lHeading.findViewById(R.id.LH_txtHeading);
		vHeading.setText(header);
		adapter.addItem( lHeading );
		return strThisRecord;
	}

	/**
	 * @param cMonument : cursor of monument objects to create views for
	 * @param strSort : field in which the monuments are sorted by
	 * @return status of the execution process. Can be STATUS_OK, ERR_CANCEL,
	 * 			ERR_NO_RESULTS, or ERR_GENERAL
	 * Steps through each record in the list of the cursor and creates a ListItemView for
	 * each monument.
	 */
	private int createMonumentViews(Cursor cMonument, String strSort){
		int nRecords = cMonument.getCount();
		String previousRecord = null;

		//go through the list of monuments. Find its donors and images
		for(int i=0; i<nRecords; i++){
			if( isCancelled() )		//if the async task has been cancelled, break out
				return ERR_CANCEL;
			
			String strMonumentName = cMonument.getString(0);

			//find all images associated with this monument. Pick one to display
			Cursor cMonImg = dbm.query(
							"SELECT " + Global.COL_FILENAME + " " +
							"FROM " + Global.TBL_MON_IMG + " " +
							"WHERE " + Global.COL_MON_NAME + " = '" + strMonumentName + "'" );
			cMonImg.moveToFirst();
			int imgIndex = 0;
			if( cMonImg.getCount() > 1 ){
				imgIndex = new Random().nextInt(cMonImg.getCount());
			}
			cMonImg.move(imgIndex);
			String filename = cMonImg.getString(0);
			if(imgIndex >= 1)
				filename += " " + (imgIndex+1);

			//find all major contributor(s) to this monument. Display all [that fit]
			Cursor donCursor = dbm.query(
							"SELECT D." + Global.COL_TITLE + ", D." + Global.COL_FNAME + ", D." + Global.COL_MNAME + ", D." + Global.COL_LNAME + ", D." + Global.COL_SUFFIX + ", D." + Global.COL_DON_ID + " " +
							"FROM " + Global.TBL_MON_DON + " M, " + Global.TBL_DONOR + " D " +
							"WHERE M." + Global.COL_MON_NAME + " = '" + strMonumentName + "' AND " +
								"M." + Global.COL_DON_ID + "=D." + Global.COL_DON_ID + " " +
							"ORDER BY D." + Global.COL_LNAME + " ASC" );
			donCursor.moveToFirst();
			
			String [] strDonors = new String [donCursor.getCount()];	//store all donor names
			int [] nDonors = new int [donCursor.getCount()];			//store all donor IDs
			for(int j = 0; j < donCursor.getCount(); j++){
				String strDonor = "";
				nDonors[j] = donCursor.getInt(5);
				//build donor name string
				for( int k=0; k<5; k++ )
					strDonor += donCursor.getString(k) + " ";
				strDonors[j] = strDonor.replaceAll("  ", " ").trim();
				donCursor.moveToNext();
			}

			previousRecord = addListHeading( cMonument, lAdapter, previousRecord, strSort );
			lAdapter.addItem( new ListItemView(
					context, Global.eVIEWTYPE.BUILDING, strMonumentName, strDonors, filename, nDonors, i) );
			cMonument.moveToNext();
		}
		return STATUS_OK;
	}

	/**	createDonorViews
	 * @param cDonor : cursor pointing to the list of donors to create ListItemViews for
	 * @param strSort : field by which the donors should be sorted by
	 * @return status of the execution process. Can be STATUS_OK, ERR_NO_RESULTS,
	 * 				ERR_CANCELLED, or ERR_GENERAL
	 * Steps through each record in the list of the cursor and creates a ListItemView for
	 * each donor.
	 */
	private int createDonorViews( Cursor cDonor, String strSort ){
		int nRecords = cDonor.getCount();
		String previousRecord = null;

		//go through the list of donors. Find his/her contributions and images
		for(int i=0; i<nRecords; i++){
			if( isCancelled() )		//if the async task has been cancelled, break out
				return ERR_CANCEL;
			
			String strDonorName = "";
			for(int j=0; j<5; j++)
				strDonorName += cDonor.getString(j) + " ";
			strDonorName = strDonorName.replaceAll("  ", " ").trim();			

			//find all monuments associated with this donor and his/her duet (if applicable)
			String query = 	"SELECT " + Global.COL_MON_NAME + " " +
							"FROM " + Global.TBL_MON_DON + " " +
							"WHERE " + Global.COL_DON_ID + " = " + cDonor.getInt(5) + " ";
			if( !cDonor.isNull(6) )
				query += 		"OR " + Global.COL_DON_ID + " = " + cDonor.getInt(6) + " ";
			
			query += 		"ORDER BY " + Global.COL_MON_NAME;
			Cursor cMon = Global.gDBM.query(query);
			cMon.moveToFirst();
			String [] strMonuments = new String [cMon.getCount()];
			
			//donor in database has no associated monument (this shouldn't happen ever)
			if( strMonuments.length == 0){
				cDonor.moveToNext();
				continue;
			}
			for(int j = 0; j < cMon.getCount(); j++){
				strMonuments [j] = cMon.getString(0);
				cMon.moveToNext();
			}				

			//find all images of this donor. Pick one to display
			Cursor cDonImg = dbm.query(
							"SELECT I." + Global.COL_FILENAME + " " +
							"FROM " + Global.TBL_DON_IMG + " I, " + Global.TBL_DONOR + " D " +
							"WHERE D." + Global.COL_DON_ID + " = " + cDonor.getInt(5) + " AND " + 
							"D." + Global.COL_DON_ID + " = I." + Global.COL_DON_ID );
			cDonImg.moveToFirst();
			
			//if no images of donor, grab an image of a monument this person contributed towards
			if(cDonImg.getCount() == 0){
				
				int rand = 0;
				if( strMonuments.length > 1 )
					rand = new Random().nextInt(strMonuments.length);
				cDonImg = dbm.query(
							"SELECT " + Global.COL_FILENAME + " " +
							"FROM " + Global.TBL_MON_IMG + " " +
							"WHERE " + Global.COL_MON_NAME + " = '" + strMonuments[rand] + "' ");
				cDonImg.moveToFirst();
				
				//no image of donor and no image of building donor contributed toward (this should never happen)
				if(cDonImg.getCount() == 0){
					cDonor.moveToNext();
					continue;
				}
			}
			
			//grab a random image of this donor
			int imgIndex = 0;
			if( cDonImg.getCount() > 1 ){
				imgIndex = new Random().nextInt(cDonImg.getCount());
			}
			cDonImg.move(imgIndex);
			previousRecord = addListHeading(cDonor, lAdapter, previousRecord, strSort);
			lAdapter.addItem( new ListItemView( context, Global.eVIEWTYPE.DONOR, strDonorName, strMonuments, 
					cDonImg.getString(0), new int[] { cDonor.getInt(5) }, i) );
			cDonor.moveToNext();
		}
		return STATUS_OK;
	}

	/**	createMonumentLocationViews
	 * @param cMonument : cursor containg data for the monuments to display in the list
	 * @param myLat : user's latitude location
	 * @param myLong : user's longitude location
	 * @return status of the execution process. Can be STATUS_OK, ERR_NO_RESULTS,
	 * 				ERR_CANCELLED, or ERR_GENERAL
	 * Does the same thing as 'createMonumentViews' except this function creates the views
	 * in the sorted order of distance from the user to each building
	 */
	private int createMonumentLocationViews( Cursor cMonument, double myLat, double myLong ){

		final double EARTH_RADIUS = 3963.16637510;     //radius of the earth in miles

		// Convert cordinates from degrees to radians
		myLat = Math.toRadians(myLat);
		myLong = Math.toRadians(myLong);

		int nRecords = cMonument.getCount();
		String previousRecord = null;

		//create sorted set of distance records
		SortedSet<DistanceRecord> sortedRecords = new TreeSet<DistanceRecord>();

		//go through all of the monuments and their locations, calculate their distances from user's location, and sort by distance
		for( int i=0; i<nRecords; i++){
			double thisLat = Math.toRadians(cMonument.getDouble(1));	//current monument's latitude
			double thisLong = Math.toRadians(cMonument.getDouble(2));	// and longitude

			// calc distance (miles) between coordinates
			double tempA = Math.cos(myLat) * Math.cos(myLong) * Math.cos(thisLat) * Math.cos(thisLong);
			double tempB = Math.cos(myLat) * Math.sin(myLong) * Math.cos(thisLat) * Math.sin(thisLong);
			double tempC = Math.sin(myLat)*Math.sin(thisLat);

			double distance = Math.acos( tempA + tempB + tempC ) * EARTH_RADIUS;
			sortedRecords.add( new DistanceRecord( distance, i ) );
			cMonument.moveToNext();
		}	
		
		//if the task has been cancelled, break out
		if( isCancelled() )
			return ERR_CANCEL;

		DecimalFormat decFormat = new DecimalFormat("#.##");
		int i=0;
		for( DistanceRecord thisMonument : sortedRecords ){
			if( isCancelled() )		//if the task has been cancelled, break out
				return ERR_CANCEL;
			
			cMonument.moveToPosition( thisMonument.getIndex() );
			String strMonumentName = cMonument.getString(0);
			String strDistance = decFormat.format( thisMonument.getDistance() ) + " mi";

			//find all images associated with this monument. Pick one to display
			Cursor cMonImg = dbm.query(
							"SELECT " + Global.COL_FILENAME + " " +
							"FROM " + Global.TBL_MON_IMG + " " +
							"WHERE " + Global.COL_MON_NAME + " = '" + strMonumentName + "'" );
			cMonImg.moveToFirst();
			int imgIndex = 0;
			if( cMonImg.getCount() > 1 ){
				imgIndex = new Random().nextInt(cMonImg.getCount());
			}
			cMonImg.move(imgIndex);
			String filename = cMonImg.getString(0);
			if(imgIndex >= 1)
				filename += " " + (imgIndex+1);

			//find all major contributor(s) to this monument. Display all [that fit]
			Cursor donCursor = dbm.query(
							"SELECT D." + Global.COL_TITLE + ", D." + Global.COL_FNAME + ", D." + Global.COL_MNAME + ", D." + Global.COL_LNAME + ", D." + Global.COL_SUFFIX + ", D." + Global.COL_DON_ID + " " +
							"FROM " + Global.TBL_MON_DON + " M, " + Global.TBL_DONOR + " D " +
							"WHERE M." + Global.COL_MON_NAME + " = '" + strMonumentName + "' AND " +
							"M." + Global.COL_DON_ID + "=D." + Global.COL_DON_ID );
			donCursor.moveToFirst();
			
			String [] strDonors = new String [donCursor.getCount()];	//donor names
			int [] nDonors = new int [donCursor.getCount()];			//donor IDs
			for(int j = 0; j < donCursor.getCount(); j++){
				String strDonor = "";
				nDonors[j] = donCursor.getInt(5);
				//build donor name string
				for(int k=0; k<5; k++)
					strDonor += donCursor.getString(k) + " ";
				strDonors[j] = strDonor.replaceAll("  ", " ").trim();
				donCursor.moveToNext();
			}

			previousRecord = addDistanceHeading( lAdapter, strDistance, previousRecord );
			lAdapter.addItem( new ListItemView( 
					context, Global.eVIEWTYPE.BUILDING, strMonumentName, strDonors, filename, nDonors, i++ ) );
			cMonument.moveToNext();
		}
		return STATUS_OK;
	}
}


/**	DistanceRecord
 * Object that stores a distance of a building to the user and the index the
 * building is within a cursor.  This object is used to sort building distances
 * from the user and uses the index to go back through the cursor and grab
 * the associated building
 */
class DistanceRecord implements Comparable<DistanceRecord>
{
	private double mDistance;
	private int mCursorIndex;

	/**	DistanceRecord
	 * @param distance : distance associated with the building in cursor location 'cursorIndex'
	 * @param cursorIndex : index of building in a cursor
	 */
	public DistanceRecord( double distance, int cursorIndex){
		mDistance = distance;
		mCursorIndex = cursorIndex;
	}

	/**	compareTo
	 * 	@param other : DistanceRecord to compare this DistanceRecord against
	 * @return -1 if this DistanceRecord holds a smaller distance value than the 'other' DistanceRecord,
	 * 			0 if the two distances are equal, and
	 * 			1 if this DistanceRecord holds a larger distance value than the 'other' DistanceRecord
	 */
	public int compareTo(DistanceRecord other) {
		if( mDistance > other.getDistance() )
			return 1;
		else if( mDistance < other.getDistance() )
			return -1;
		else
			return 0;
	}

	public double getDistance(){ return mDistance; }
	public int getIndex(){ return mCursorIndex; }
}