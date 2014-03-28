package edu.gvsu.ll;

import java.io.Serializable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class BioActivity extends FragmentActivity {

	public static BioActivity sInstance;
	
	private int 			mNumPages;
	private int				mCurrPage;
	private ViewPager 		mPager;
	private BioPagerAdapter mPagerAdapter;
	private int [] 			mNDonorIDs;
	private ImageView [] 	mPageIndicators;
	private ImageView 		mVLeftPage, mVRightPage;
	private Bitmap			mImgDimPage, mImgCurrPage;
	private Bitmap			mImgDimLeft, mImgLeft, mImgDimRight, mImgRight;
	private Resources 		mRes;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.biopager);

		sInstance = this;	
		mRes = getResources();

		//grab donor id's passed to this activity
		BioActivityDesc desc = (BioActivityDesc) getIntent().getExtras().getSerializable(Global.MSG_DONORS);
		mNDonorIDs = desc.getDonorIDs();
		mNumPages = mNDonorIDs.length;
		mCurrPage = 0;
		
		//set the page images (if applicable)
		if( mNumPages > 1 ){
			//grab left and right page buttons and set listeners
			mVLeftPage = (ImageView) findViewById(R.id.PAG_imgLeftPage);
			mVLeftPage.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					mPager.setCurrentItem(mCurrPage-1, true);
				}
			});
			mVRightPage = (ImageView) findViewById(R.id.PAG_imgRightPage);
			mVRightPage.setOnClickListener(new OnClickListener(){
				public void onClick(View v){
					mPager.setCurrentItem(mCurrPage+1, true);
				}
			});
			
			//load all page images
			mImgDimPage = getDimPageImage();
	        mImgCurrPage = getCurrentPageImage();
	        mImgDimLeft = getDimPageLeftImage();
	        mImgLeft = getPageLeftImage();
	        mImgDimRight = getDimPageRightImage();
	        mImgRight = getPageRightImage();
			
	        //set the correct page images
	        mVLeftPage.setImageBitmap( mImgDimLeft );
	        mVLeftPage.setClickable(false);
	        mVRightPage.setImageBitmap( mImgRight );
	        mVRightPage.setClickable(true);
	       
	        //create and set the page indicator images
	        mPageIndicators = new ImageView [mNumPages];
	        LinearLayout layout = (LinearLayout) findViewById(R.id.PAG_lPages);
	        for(int i=0; i<mNumPages; i++){
	        	mPageIndicators[i] = new ImageView(this);
	        	mPageIndicators[i].setPadding(10, 0, 10, 0);
	        	if( i == 0 )
	        		mPageIndicators[i].setImageBitmap( mImgCurrPage );
	        	else
	        		mPageIndicators[i].setImageBitmap( mImgDimPage );
	        	layout.addView(mPageIndicators[i]);
	        }
		}
		
		// Instantiate ViewPager and PagerAdapter.
		mPager = (ViewPager) findViewById(R.id.PAG_pgPager);
		mPager.setOnPageChangeListener(new OnPageChangeListener(){
			public void onPageScrollStateChanged(int state) {}
			public void onPageScrolled(int position, float positionOffset, int offsetPixels) {}

			public void onPageSelected(int position) {
				mCurrPage = position;
				for(int i=0; i<mNumPages; i++){
					if( i == position )
						mPageIndicators[i].setImageBitmap( mImgCurrPage );
					else
						mPageIndicators[i].setImageBitmap( mImgDimPage );
				}
				
				//check endpoints
				if( position == 0){
					mVLeftPage.setImageBitmap(mImgDimLeft);
					mVLeftPage.setClickable(false);
				}
				else{
					mVLeftPage.setImageBitmap(mImgLeft);
					mVLeftPage.setClickable(true);
				}
				
				if( position == (mNumPages-1) ){
					mVRightPage.setImageBitmap(mImgDimRight);
					mVRightPage.setClickable(false);
				}
				else{
					mVRightPage.setImageBitmap(mImgRight);
					mVRightPage.setClickable(true);
				}
			}
		});
		mPagerAdapter = new BioPagerAdapter( getSupportFragmentManager(), mNumPages );
		new BioInit( mPager, mPagerAdapter ).execute( desc );
	}
	
	public Bitmap getCurrentPageImage(){
		int id = mRes.getIdentifier("page_current", "drawable", Global.PACKAGE);
		 return BitmapFactory.decodeResource(mRes, id);
	}
	
	public Bitmap getDimPageImage(){
		int id = mRes.getIdentifier("page_dim", "drawable", Global.PACKAGE);
		return BitmapFactory.decodeResource(mRes, id);
	}
	
	public Bitmap getDimPageLeftImage(){
		int id = mRes.getIdentifier("page_left_dim", "drawable", Global.PACKAGE);
		return BitmapFactory.decodeResource(mRes, id);
	}
	
	public Bitmap getPageLeftImage(){
		int id = mRes.getIdentifier("page_left", "drawable", Global.PACKAGE);
		return BitmapFactory.decodeResource(mRes, id);
	}
	
	public Bitmap getDimPageRightImage(){
		int id = mRes.getIdentifier("page_right_dim", "drawable", Global.PACKAGE);
		return BitmapFactory.decodeResource(mRes, id);
	}
	
	public Bitmap getPageRightImage(){
		int id = mRes.getIdentifier("page_right", "drawable", Global.PACKAGE);
		return BitmapFactory.decodeResource(mRes, id);
	}
}

//TODO -- create left/right page buttons and page number indicator
class BioPagerAdapter extends FragmentStatePagerAdapter
{
	
	private int mPages;
	private BioView [] mBioPages;
	
	public BioPagerAdapter(FragmentManager fm, int nPages) {
		super(fm);
		mPages = nPages;
		mBioPages = new BioView [mPages];
	}

	public void addItem(BioView page, int position){
		mBioPages[position] = page;
	}
	
	@Override
	public Fragment getItem(int position) {
		return mBioPages[position];
	}

	@Override
	public int getCount() {
		return mPages;
	}
}

class BioActivityDesc implements Serializable
{
	
	private static final long serialVersionUID = Global.SERIAL_NUM;
	private int [] mnDonorIDs;
	
	public BioActivityDesc(int [] nDonorIDs){
		mnDonorIDs = nDonorIDs;
	}
	
	public int [] getDonorIDs(){
		return mnDonorIDs;
	}
}