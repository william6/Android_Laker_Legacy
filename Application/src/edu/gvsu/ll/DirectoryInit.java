package edu.gvsu.ll;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class DirectoryInit extends AsyncTask< QueryType, Void, String >
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
		vDialog.setTitle("Loading data");
		vDialog.setMessage("Please wait...");
		vDialog.show();
	}
	
	@Override
	protected String doInBackground(QueryType... params) {
		
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
			query += "WHERE lower(" + QueryType.STR_SORT_NAME + ") LIKE '%" + search.toLowerCase() + "%' ";
		
		if( sortBy.compareTo(QueryType.STR_SORT_DISTANCE) != 0)
			query += "ORDER BY " + sortBy + " ASC";
		
		Cursor listCursor = dbm.query(query);
		listCursor.moveToFirst();
		
		//TODO -- no results found alert - test to make sure this works
		if(listCursor.getCount() == 0){
			lAdapter = new DBListAdapter(context, 1);
			TextView vText = new TextView(context);
			vText.setText( "No results found" );
			lAdapter.addItem(vText);
			return null;
		}

		lAdapter = new DBListAdapter(context, listCursor.getCount());

		// list by monument
		if( strTable.equalsIgnoreCase(GTblVal.TBL_MONUMENT) ){
			
			//before we can sort them
			if( sortBy.compareTo(QueryType.STR_SORT_DISTANCE) == 0 ){
				//TODO -- get user location
				createMonumentLocationViews( listCursor, 42.963411, -85.888692);
			}
			else
				createMonumentViews( listCursor, sortBy );
		}

		// list by donor
		else
			createDonorViews( listCursor, sortBy );
		
		return null;
	}

	@Override
	protected void onPostExecute(String result){
		super.onPostExecute(result);
		
		vList.setAdapter(lAdapter);
		vDialog.dismiss();
	}
	
	//adds a heading view to the list if necessary
	//returns the current record's type string
	private String addListHeading(Cursor cursor, DBListAdapter adapter, String previousRecord, String sortBy ){
		
		//determine current record string
		String header = "";
		String strThisRecord = "";
		if( sortBy.equalsIgnoreCase(GTblVal.COL_NAME ) )
			strThisRecord = cursor.getString(0);
		else if( sortBy.equalsIgnoreCase(GTblVal.COL_CAMPUS ) )
			strThisRecord = cursor.getString(1);
		else if( sortBy.equalsIgnoreCase(GTblVal.COL_EST) )
			strThisRecord = cursor.getString(1);
		
		if(strThisRecord == null)
			strThisRecord = "";
		
		//if previous record is null, there is no previous record. if the previous record is an empty string, the previous record is an empty string :)
		if( previousRecord == null ){
			
			//create name header
			if( sortBy.equalsIgnoreCase(GTblVal.COL_NAME) ){
				header = strThisRecord.substring(0, 1).toUpperCase();
			}
			
			//create campus header
			else if( sortBy.equalsIgnoreCase(GTblVal.COL_CAMPUS) ){
				header = strThisRecord.toUpperCase();
			}
			
			//create date est header
			else if( sortBy.equalsIgnoreCase(GTblVal.COL_EST) ){
				header = strThisRecord;
			}
		}
		else{
			//do we need to add a name header?
			if( sortBy.equalsIgnoreCase(QueryType.STR_SORT_NAME ) ){
				if(previousRecord.substring(0,1).equalsIgnoreCase(strThisRecord.substring(0,1)))
					return strThisRecord;
				else
					header = strThisRecord.substring(0, 1);
			}
			
			//do we need a campus header?
			else if( sortBy.equalsIgnoreCase(QueryType.STR_SORT_CAMPUS) ){
				if(previousRecord.compareTo(strThisRecord) == 0 )		//TODO -- better error check with >= or <=
					return strThisRecord;
				else
					header = strThisRecord.toUpperCase();
			}
			
			//do we need a date-est header?
			else if( sortBy.equalsIgnoreCase(QueryType.STR_SORT_DATE) ){
				if(previousRecord.compareTo(strThisRecord) == 0 )
					return strThisRecord;
				else
					header = strThisRecord;
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
	
	private void createMonumentViews(Cursor cMonument, String strSort){
		int nRecords = cMonument.getCount();
		String previousRecord = null;

		//go through the list of monuments. Find its donors and images
		for(int i=0; i<nRecords; i++){
			String strMonumentName = cMonument.getString(0);
			String strDonors = "";

			//find all images associated with this monument. Pick one to display
			Cursor cMonImg = dbm.query(
					"SELECT " + GTblVal.COL_FILENAME + " " +
					"FROM " + GTblVal.TBL_MON_IMG + " " +
					"WHERE " + GTblVal.COL_NAME + " = '" + strMonumentName + "'" );
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
			
			previousRecord = addListHeading( cMonument, lAdapter, previousRecord, strSort );
			lAdapter.addItem(new ListItemView(context, strMonumentName, strDonors, filename, i) );
			cMonument.moveToNext();
		}
	}
	
	private void createDonorViews( Cursor cDonor, String strSort ){
		int nRecords = cDonor.getCount();
		String previousRecord = null;
		
		//go through the list of donors. Find his/her contributions and images
		for(int i=0; i<nRecords; i++){
			String strDonorName = cDonor.getString(0);
			String strMonuments = "";				
			
			//find all monuments associated with this donor
			Cursor cMon = dbm.query(
					"SELECT " + GTblVal.COL_NAME + " " +
					"FROM " + GTblVal.TBL_MON_DON + " " +
					"WHERE " + GTblVal.COL_DON_ID + " = " + cDonor.getInt(1) + " " );
			cMon.moveToFirst();
			for(int j = 0; j < cMon.getCount(); j++){
				if( j != 0 )
					strMonuments += "\n";
				strMonuments += cMon.getString(0);
				cMon.moveToNext();
			}				
			
			//find all images of this donor. Pick one to display
			Cursor cDonImg = dbm.query(
					"SELECT I." + GTblVal.COL_FILENAME + " " +
					"FROM " + GTblVal.TBL_DON_IMG + " I, " + GTblVal.TBL_DONOR + " D " +
					"WHERE D." + GTblVal.COL_NAME + " = \"" + strDonorName + "\" AND " + 
						  "D." + GTblVal.COL_DON_ID + " = I." + GTblVal.COL_DON_ID );
			cDonImg.moveToFirst();

			//TODO -- if no images of donor, grab an image of a monument this person contributed towards
			if(cDonImg.getCount() == 0){
				cDonor.moveToNext();
				continue;
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
	}
	
	private void createMonumentLocationViews( Cursor cMonument, double myLat, double myLong ){
		
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
        
        DecimalFormat decFormat = new DecimalFormat("#.##");
        int i=0;
        for( DistanceRecord thisMonument : sortedRecords ){
        	
        	cMonument.moveToPosition( thisMonument.getIndex() );
        	String strMonumentName = cMonument.getString(0);
			String strDonors = "";
			String strDistance = decFormat.format( thisMonument.getDistance() ) + " mi";

			//find all images associated with this monument. Pick one to display
			Cursor cMonImg = dbm.query(
					"SELECT " + GTblVal.COL_FILENAME + " " +
					"FROM " + GTblVal.TBL_MON_IMG + " " +
					"WHERE " + GTblVal.COL_NAME + " = '" + strMonumentName + "'" );
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
			
			previousRecord = addDistanceHeading( lAdapter, strDistance, previousRecord );
			lAdapter.addItem( new ListItemView( context, strMonumentName, strDonors, filename, i++ ) );
			cMonument.moveToNext();
        }
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