package edu.gvsu.ll;

import java.util.Random;

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
		int totalHits = listCursor.getCount();
		
		//TODO -- no results found alert
		if(totalHits == 0){
			return null;
		}
		
		lAdapter = new DBListAdapter(context, listCursor.getCount());

		// LIST BY MONUMENT
		if( strTable.equalsIgnoreCase(GTblVal.TBL_MONUMENT) ){
			
			// TODO -- if we're sorting by distance, we need to calculate all of the distances
			//before we can sort them
			if( sortBy.compareTo(QueryType.STR_SORT_DISTANCE) == 0 ){
				
			}
			else{
				
				String previousRecord = null;
				
				//go through the list of monuments. Find its donors and images
				for(int i=0; i<totalHits; i++){
					String strMonumentName = listCursor.getString(0);
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
					
					previousRecord = addListHeading(listCursor, lAdapter, previousRecord, sortBy);
					lAdapter.addItem(new ListItemView(context, strMonumentName, strDonors, filename, i) );
					listCursor.moveToNext();
				}
			}
		}

		// LIST BY DONOR
		//TODO -- add donor images to database and application resources
		else{
			//go through the list of donors. Find his/her contributions and images
			for(int i=0; i<totalHits; i++){
				String strDonorName = listCursor.getString(0);
				String strMonuments = "";

				//find all images of this person(s). Pick one to display
				Cursor imgCursor = dbm.query(
						"SELECT I." + GTblVal.COL_FILENAME + " " +
						"FROM " + GTblVal.TBL_IMAGE + " I, " + GTblVal.TBL_DONOR + " D " +
						"WHERE D." + GTblVal.COL_NAME + " = '" + strDonorName + "' AND " + 
							  "D." + GTblVal.COL_IMG_ID + " = I." + GTblVal.COL_IMG_ID );
				imgCursor.moveToFirst();
				int imgIndex = 0;
				if( imgCursor.getCount() > 1 ){
					imgIndex = new Random().nextInt(imgCursor.getCount());
				}
				imgCursor.move(imgIndex);
				String filename = imgCursor.getString(0);

				//find all major contributor(s) to this monument. Display all [that fit]
//				Cursor donCursor = dbm.query(
//						"SELECT D." + GTblVal.COL_NAME + " " +
//						"FROM " + GTblVal.TBL_MON_DON + " M, " + GTblVal.TBL_DONOR + " D " +
//						"WHERE M." + GTblVal.COL_NAME + " = '" + strMonumentName + "' AND " +
//						"M." + GTblVal.COL_DON_ID + "=D." + GTblVal.COL_DON_ID );
//				donCursor.moveToFirst();
//				for(int j = 0; j < donCursor.getCount(); j++){
//					if( j != 0 )
//						strDonors += "\n";
//					strDonors += donCursor.getString(0);
//					donCursor.moveToNext();
//				}
//				
//				lAdapter.addItem(new ListItemView(context, strMonumentName, strDonors, filename, i), i);
				listCursor.moveToNext();
			}
		}
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
		else
			strThisRecord = "distance";
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
			
			//create GPS header
			else{
				header = "distance";		//TODO
			}
		}
		else{
			
			//do we need to add a name header?
			if( sortBy.equalsIgnoreCase(GTblVal.COL_NAME ) ){
				if(previousRecord.substring(0,1).equalsIgnoreCase(strThisRecord.substring(0,1)))
					return strThisRecord;
				else
					header = strThisRecord.substring(0, 1);
			}
			
			//do we need a campus header?
			else if( sortBy.equalsIgnoreCase(GTblVal.COL_CAMPUS) ){
				if(previousRecord.compareTo(strThisRecord) == 0 )		//TODO -- better error check with >= or <=
					return strThisRecord;
				else
					header = strThisRecord.toUpperCase();
			}
			
			//do we need a date-est header?
			else if( sortBy.equalsIgnoreCase(GTblVal.COL_EST) ){
				if(previousRecord.compareTo(strThisRecord) == 0 )
					return strThisRecord;
				else
					header = strThisRecord;
			}
		
			//do we need a GPS header?
			else if( sortBy.equalsIgnoreCase(GTblVal.COL_GPS) ){
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
}