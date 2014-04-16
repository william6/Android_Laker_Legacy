package edu.gvsu.ll;

import java.util.TreeMap;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**	BioView
 * 	Creates a custom view of a donor to be displayed in a Pager.
 */
public class BioView extends Fragment {

	//--	class member variables	--//
	private String mStrDonorName;
	private String mStrBio;
	private String mStrDonorImg;
	private TreeMap<String,String> mMapMonuments;
	
	/**	BioView
	 * @param strDonorName : title to be displayed at the top of the view
	 * @param strBio : complete biography of the donor
	 * @param strDonorImg : filename of the image to load to be displayed
	 * @param mapMonuments : map of buildings this donor contributed towards (key) with their images (value)
	 */
	public BioView(String strDonorName, String strBio, String strDonorImg, TreeMap<String,String> mapMonuments){
		super();
		mStrDonorName = strDonorName;
		mStrBio = strBio;
		mStrDonorImg = strDonorImg;
		mMapMonuments = mapMonuments;
	}
	
	@Override
	/** onCreateView
	 * Creates a custom view from bioview.xml and sets all associated view elements.
	 */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.bioview, container, false);
        
        //set donor name
        ( (TextView) rootView.findViewById(R.id.BIO_txtDonorName) ).setText(mStrDonorName);
        
        //set donor image
        ImageView vImage = (ImageView) rootView.findViewById(R.id.BIO_imgDonor);
        int imgID = BioActivity.sInstance.getResources().getIdentifier(
				mStrDonorImg, "drawable", Global.PACKAGE);
        vImage.setImageBitmap( BitmapFactory.decodeResource( getResources(), imgID, null ) );
        
        //create all contributing monument views
        LinearLayout contributionScrollLayout = (LinearLayout) rootView.findViewById(R.id.BIO_lContributions);
        for(String strMonument : mMapMonuments.keySet()){
        	LinearLayout singleContributionLayout = new LinearLayout(BioActivity.sInstance);
        	View.inflate(BioActivity.sInstance, R.layout.bioview_contribution, singleContributionLayout);
        	ImageView img = (ImageView) singleContributionLayout.findViewById(R.id.BIO_Contrib_imgBuilding);
        	( (TextView) singleContributionLayout.findViewById(R.id.BIO_Contrib_txtBuilding) ).setText(strMonument);
        	
        	//grab image and size it down
        	imgID = BioActivity.sInstance.getResources().getIdentifier(
    				mMapMonuments.get(strMonument), "drawable", Global.PACKAGE);
        	img.setImageResource(imgID);
        	
        	//set listener
        	class BuildingListener implements OnClickListener{
        		String strMonument;
        		public BuildingListener(String strMonument){
        			this.strMonument = strMonument;
        		}
				public void onClick(View v) {
					Intent intent = new Intent(BioActivity.sInstance, BuildingActivity.class);
					intent.putExtra(Global.MSG_BUILDING, strMonument );
					BioActivity.sInstance.startActivity(intent);
				}
        	}
        	singleContributionLayout.setOnClickListener(new BuildingListener(strMonument) );
        	
        	//add contribution layout to the scrollview
        	contributionScrollLayout.addView(singleContributionLayout);
        }
        
        TextView vText = (TextView) rootView.findViewById(R.id.BIO_txtBio);
        vText.setText(mStrBio);
        return rootView;
    }	
}