package edu.gvsu.ll;

import java.util.Random;

import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;

/**	BioInit
 * Asynchronous task that queries the database for information on each
 * donor. When the information on each donor is loaded, this task
 * sets the pager adapter of the BioActivity and displays the results.
 */
public class BioInit extends AsyncTask< BioActivityDesc, Void, Integer >
{
	private final int STATUS_OK = 0;		// AsyncTask returned okay
	
	private ViewPager mPager;
	private BioPagerAdapter mAdapter;
	
	/**	BioInit
	 * @param pager : ViewPager object to hold the pages this task will create
	 * @param adapter : adapter object to insert the created pages into
	 */
	public BioInit( ViewPager pager, BioPagerAdapter adapter ){
		mPager = pager;
		mAdapter = adapter;
	}

	@Override
	/**	doInBackground
	 * 	Given the set of donor IDs passed to it through params, each donor is queried
	 * from the database, his/her information pulled, and individual BioView created.
	 * Each BioView is added to the adapter as it is created. When the task completes,
	 * the ViewPager is set to the adapter to render the views
	 */
	protected Integer doInBackground(BioActivityDesc... params) {
		
		int [] nDonIDs = params[0].getDonorIDs();
		
		//no IDs passed
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
			
			//if no images of donor, grab image of building
			if( cDonImg.getCount() == 0 ){
				cDonImg = Global.gDBM.query(
						"SELECT MI." + Global.COL_FILENAME + " " + 
						"FROM " + Global.TBL_MON_IMG + " MI, " + Global.TBL_MON_DON + " MD " +
						"WHERE MI." + Global.COL_MON_NAME + " = MD." + Global.COL_MON_NAME + " AND " +
								"MD." + Global.COL_DON_ID + " = " + cDon.getInt(0) );	
				cDonImg.moveToFirst();
			}

			//grab a random image of this donor
			int imgIndex = 0;
			if( cDonImg.getCount() > 1 ){
				imgIndex = new Random().nextInt(cDonImg.getCount());
			}
			cDonImg.move(imgIndex);
			strFilename = cDonImg.getString(0);
			
			//create BioView of donor and add to the pager adapter
			mAdapter.addItem( new BioView( strDonor, strBio, strFilename ), i);
			cDon.moveToNext();
		}
		return STATUS_OK;
	}
	
	@Override
	/**	onPostExecute
	 * 	When the AsyncTask has completed, set the PagerAdapter so it displays the
	 * 	Created pages (BioViews)
	 */
	protected void onPostExecute(Integer result){
		super.onPostExecute(result);
		mPager.setAdapter(mAdapter);
	}
}