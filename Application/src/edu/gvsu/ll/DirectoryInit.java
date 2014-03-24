package edu.gvsu.ll;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
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
		vDialog.setCanceledOnTouchOutside(false);
		vDialog.setTitle("Loading data");
		vDialog.setMessage("Please wait...");
		vDialog.show();
	}

	@Override
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

			//always search names (donors or monuments)
			if( search != null )
				query += getSearchQuery(strTable, search);

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
					//TODO -- get user location
					status = createMonumentLocationViews( listCursor, 42.963411, -85.888692);
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
	
	private String getSearchQuery(String strTable, String search){
		//search MONUMENT
		if( strTable.equalsIgnoreCase(Global.TBL_MONUMENT) )
			return "WHERE lower(" + QueryType.STR_SORT_MON_NAME + ") LIKE '%" + search + "%' ";
		//search DONOR
		else
			return 	"WHERE lower(" + Global.COL_TITLE + ") LIKE '%" + search + "%' OR " +
					"lower(" + Global.COL_FNAME + ") LIKE '%" + search + "%' OR " +
					"lower(" + Global.COL_MNAME + ") LIKE '%" + search + "%' OR " +
					"lower(" + Global.COL_LNAME + ") LIKE '%" + search + "%' OR " +
					"lower(" + Global.COL_SUFFIX + ") LIKE '%" + search + "%' ";
	}

	@Override
	protected void onPostExecute(Integer result){
		super.onPostExecute(result);

		vList.setAdapter(lAdapter);
		vDialog.dismiss();	
		DirectoryActivity.sInstance.enableInput(true);
		if( result == ERR_NO_RESULTS )
			Toast.makeText(DirectoryActivity.sInstance, "No results found.", Toast.LENGTH_LONG).show();
		DirectoryActivity.sInstance.resetAsyncTask();
	}
	
	@Override
	protected void onCancelled(Integer result){
		//if this task was cancelled, do not set a new view
		vDialog.dismiss();
		DirectoryActivity.sInstance.enableInput(true);
		super.onCancelled(result);
		DirectoryActivity.sInstance.resetAsyncTask();
	}

	//adds a heading view to the list if necessary
	//returns the current record's type string
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

	private int createMonumentViews(Cursor cMonument, String strSort){
		int nRecords = cMonument.getCount();
		String previousRecord = null;

		//go through the list of monuments. Find its donors and images
		for(int i=0; i<nRecords; i++){
			if( isCancelled() )
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
							"SELECT D." + Global.COL_TITLE + ", D." + Global.COL_FNAME + ", D." + Global.COL_MNAME + ", D." + Global.COL_LNAME + ", D." + Global.COL_SUFFIX + " " +
							"FROM " + Global.TBL_MON_DON + " M, " + Global.TBL_DONOR + " D " +
							"WHERE M." + Global.COL_MON_NAME + " = '" + strMonumentName + "' AND " +
								"M." + Global.COL_DON_ID + "=D." + Global.COL_DON_ID + " " +
							"ORDER BY D." + Global.COL_LNAME + " ASC" );
			donCursor.moveToFirst();
			
			String [] strDonors = new String [donCursor.getCount()];
			for(int j = 0; j < donCursor.getCount(); j++){
				String strDonor = "";
				//build donor name string
				for( int k=0; k<5; k++ )					//TODO make better by removing nulls from database, change to empty strings
					if(donCursor.getString(k) != null)
						strDonor += donCursor.getString(k) + " ";
				strDonors[j] = strDonor.trim();
				donCursor.moveToNext();
			}

			previousRecord = addListHeading( cMonument, lAdapter, previousRecord, strSort );
			lAdapter.addItem(new ListItemView(context, strMonumentName, strDonors, filename, i) );
			cMonument.moveToNext();
		}
		return STATUS_OK;
	}

	private int createDonorViews( Cursor cDonor, String strSort ){
		int nRecords = cDonor.getCount();
		String previousRecord = null;

		//go through the list of donors. Find his/her contributions and images
		for(int i=0; i<nRecords; i++){
			if( isCancelled() )
				return ERR_CANCEL;
			
			String strDonorName = "";
			for(int j=0; j<5; j++){
				if(cDonor.getString(j) != null)
					strDonorName += cDonor.getString(j) + " ";
			}
			strDonorName = strDonorName.trim();			

			//find all monuments associated with this donor
			Cursor cMon = dbm.query(
							"SELECT " + Global.COL_MON_NAME + " " +
							"FROM " + Global.TBL_MON_DON + " " +
							"WHERE " + Global.COL_DON_ID + " = " + cDonor.getInt(5) + " " +
							"ORDER BY " + Global.COL_MON_NAME );
			cMon.moveToFirst();
			String [] strMonuments = new String [cMon.getCount()];
			
			//TODO -- donor in database has no associated monument (Alexander Calder)
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
				
				//TODO?? no image of donor and no image of building donor contributed toward
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
			lAdapter.addItem( new ListItemView( 
					context, strDonorName, strMonuments, cDonImg.getString(0), i) );
			cDonor.moveToNext();
		}
		return STATUS_OK;
	}

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
		
		if( isCancelled() )
			return ERR_CANCEL;

		DecimalFormat decFormat = new DecimalFormat("#.##");
		int i=0;
		for( DistanceRecord thisMonument : sortedRecords ){
			if( isCancelled() )
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
							"SELECT D." + Global.COL_TITLE + ", D." + Global.COL_FNAME + ", D." + Global.COL_MNAME + ", D." + Global.COL_LNAME + ", D." + Global.COL_SUFFIX + " " +
							"FROM " + Global.TBL_MON_DON + " M, " + Global.TBL_DONOR + " D " +
							"WHERE M." + Global.COL_MON_NAME + " = '" + strMonumentName + "' AND " +
							"M." + Global.COL_DON_ID + "=D." + Global.COL_DON_ID );
			donCursor.moveToFirst();
			
			String [] strDonors = new String [donCursor.getCount()];
			for(int j = 0; j < donCursor.getCount(); j++){
				String strDonor = "";
				//build donor name string
				for(int k=0; k<5; k++)
					if(donCursor.getString(k) != null)		//TODO -- remove null from database
						strDonor += donCursor.getString(k) + " ";
				strDonors[j] = strDonor.trim();
				donCursor.moveToNext();
			}

			previousRecord = addDistanceHeading( lAdapter, strDistance, previousRecord );
			lAdapter.addItem( new ListItemView( context, strMonumentName, strDonors, filename, i++ ) );
			cMonument.moveToNext();
		}
		return STATUS_OK;
	}
}


class DistanceRecord implements Comparable<DistanceRecord>
{
	private double mDistance;
	private int mIndex;

	public DistanceRecord( double distance, int index ){
		mDistance = distance;
		mIndex = index;
	}

	public int compareTo(DistanceRecord other) {
		if( mDistance > other.getDistance() )
			return 1;
		else if( mDistance < other.getDistance() )
			return -1;
		else
			return 0;
	}

	public double getDistance(){ return mDistance; }
	public int getIndex(){ return mIndex; }
}