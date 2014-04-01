package edu.gvsu.ll;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**	BioView
 * 	Creates a custom view of a donor to be displayed in a Pager.
 */
public class BioView extends Fragment {

	//--	class member variables	--//
	private String mStrName;
	private String mStrBio;
	private String mStrFilename;
	
	/**	BioView
	 * @param strName : title to be displayed at the top of the view
	 * @param strBio : complete biography of the donor
	 * @param strFilename : filename of the image to load to be displayed
	 */
	public BioView(String strName, String strBio, String strFilename){
		super();
		mStrName = strName;
		mStrBio = strBio;
		mStrFilename = strFilename;
	}
	
	@Override
	/** onCreateView
	 * Creates a custom view from bioview.xml and sets all associated view elements.
	 */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.bioview, container, false);
        
        //set donor name
        ( (TextView) rootView.findViewById(R.id.BIO_txtDonorName) ).setText(mStrName);
        
        //set donor image
        ImageView vImage = (ImageView) rootView.findViewById(R.id.BIO_imgDonor);
        int imgID = BioActivity.sInstance.getResources().getIdentifier(
				mStrFilename, "drawable", Global.PACKAGE);
        vImage.setImageBitmap( BitmapFactory.decodeResource( getResources(), imgID, null ) );
        
        TextView vText = (TextView) rootView.findViewById(R.id.BIO_txtBio);
        vText.setText(mStrBio);
        return rootView;
    }	
}
