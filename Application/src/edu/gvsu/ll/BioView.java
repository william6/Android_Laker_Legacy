package edu.gvsu.ll;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class BioView extends Fragment {

	private FragmentManager manager;
	private String mStrName;
	private String mStrBio;
	private String mStrFilename;
	
	public BioView(String strName, String strBio, String strFilename){
		super();
		mStrName = strName;
		mStrBio = strBio;
		mStrFilename = strFilename;
	}
	
	@Override
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
