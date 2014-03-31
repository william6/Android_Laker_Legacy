package edu.gvsu.ll;

import java.util.Random;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

public class BioInit extends AsyncTask< BioActivityDesc, Void, Integer >
{
	private final int STATUS_OK 		= 0;		// AsyncTask returned okay
	private final int ERR_NO_RESULTS 	= 1;		// no results found
	private final int ERR_CANCEL		= 2;		//task was cancelled and halted
	private final int ERR_GENERAL 		= 3;		// some error occurred
	
	private ViewPager mPager;
	private BioPagerAdapter mAdapter;
	private ProgressDialog vDialog;
	
	public BioInit( ViewPager pager, BioPagerAdapter adapter ){
		mPager = pager;
		mAdapter = adapter;
	}


	@Override
	protected void onPreExecute(){
		super.onPreExecute();
		vDialog = new ProgressDialog(BioActivity.sInstance);
		vDialog.setCanceledOnTouchOutside(false);
		vDialog.setTitle("Loading data");
		vDialog.setMessage("Please wait...");
		vDialog.show();
	}

	@Override
	protected Integer doInBackground(BioActivityDesc... params) {
		
		int [] nDonIDs = params[0].getDonorIDs();
		
		//TODO?? - no IDs passed
		if( nDonIDs.length == 0)
			throw new RuntimeException("ERR: no donor IDs passed to Bio viewer");
		
		//query all donors at a single time and order them by name
		String query =	"SELECT * " +
				"FROM " + Global.TBL_DONOR + " " +
				"WHERE " + Global.COL_DON_ID + " = ";
		for( int i=0; i<nDonIDs.length; i++ ){
			if( i == 0 )
				query += "" + nDonIDs[i] + " ";
			else
				query += "OR " + Global.COL_DON_ID + " = " + nDonIDs[i] + " ";
		}

		query += "ORDER BY " + Global.COL_LNAME + " ASC";
		
		Cursor cDon = Global.gDBM.query(query);
		cDon.moveToFirst();
		if( cDon.getCount() != nDonIDs.length )
			throw new RuntimeException ("ERR: donor cursor number and id number is different");
		
		//for each donor grab an image of them, piece together their name, throw in a bio, and add to adapter
		for( int i = 0; i<cDon.getCount(); i++ ){
			
			//extract donor name (columns 1-5)
			String strDonor = "";
			for(int j = 1; j < 6; j++)
				strDonor += cDon.getString(j) + " ";
			strDonor = strDonor.replaceAll("  ", " ").trim();
			
			//extract bio
			String strBio = cDon.getString(6);
			
			//find donor image
			String strFilename = "";
			Cursor cDonImg = Global.gDBM.query(
							"SELECT I." + Global.COL_FILENAME + " " +
							"FROM " + Global.TBL_DON_IMG + " I, " + Global.TBL_DONOR + " D " +
							"WHERE D." + Global.COL_DON_ID + " = " + cDon.getInt(0) + " AND " + 
							"D." + Global.COL_DON_ID + " = I." + Global.COL_DON_ID );
			cDonImg.moveToFirst();
			
			//TODO -- if no images of donor, grab image of building
			if( cDonImg.getCount() == 0 )
				strFilename = "arnold_ott";
			else{
				//grab a random image of this donor
				int imgIndex = 0;
				if( cDonImg.getCount() > 1 ){
					imgIndex = new Random().nextInt(cDonImg.getCount());
				}
				cDonImg.move(imgIndex);
				strFilename = cDonImg.getString(0);
			}
			
			mAdapter.addItem( new BioView( strDonor, strBio, strFilename ), i);
			cDon.moveToNext();
		}
		return STATUS_OK;
	}
	
	@Override
	protected void onPostExecute(Integer result){
		super.onPostExecute(result);

		mPager.setAdapter(mAdapter);
		vDialog.dismiss();	
		if( result == ERR_NO_RESULTS )
			Toast.makeText(DirectoryActivity.sInstance, "No results found.", Toast.LENGTH_LONG).show();
	}
	
	@Override
	protected void onCancelled(Integer result){
		//if this task was cancelled, do not set a new view
		vDialog.dismiss();
		super.onCancelled(result);
	}
}