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
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**	BioActivity
 * 	Creates a ViewPager that is able to cycle a set of BioViews. This class sets
 * up the ViewPager to have the left/right page-change buttons as well as the
 * page indicator images.  A new BioActivity is created every time a list item
 * from DirectoryActivity is selected. When that happens, BioActivity fires
 * an asynchronous task (BioInit) that creates each individual BioView to be displayed
 * in the pager.
 */
public class BioActivity extends FragmentActivity {

	//--	static instance of this object	--//
	public static BioActivity sInstance;
	
	//--	class member variables	--//
	private int 			mNumPages;				//number of pages to be displayed in the pager
	private int				mCurrPage;				//the page number currently displayed
	private ViewPager 		mPager;					//pager object keeping track of the pages
	private BioPagerAdapter mPagerAdapter;			//pager adapter that holds each page
	private int [] 			mNDonorIDs;				//set of donor id's associated with the respective pages
	
	private ImageView [] 	mPageIndicators;		//set of GUI elements to represent the active and inactive pages
	private ImageView 		mVLeftPage, mVRightPage;//GUI elements to represent the left and right page buttons
	private TextView		mVPageNum;				//page numbers index
	
	private Bitmap			mImgDimPage, mImgCurrPage;	//Image objects of an inactive page and active page, respectively
	private Bitmap			mImgDimLeft, mImgLeft, mImgDimRight, mImgRight;	//Image objects of the page buttons and their dimmed versions
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
		
		//grab left and right GUI page buttons and set their listeners
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
		
		//load all page images (do this so we don't ever have to load them again as user moves through pages)
		mImgDimPage 	= loadImage("ic_page_dim");
        mImgCurrPage 	= loadImage("ic_page_current");
        mImgDimLeft 	= loadImage("ic_page_left_dim");
        mImgLeft 		= loadImage("ic_page_left");
        mImgDimRight 	= loadImage("ic_page_right_dim");
        mImgRight 		= loadImage("ic_page_right");
		
        //set the correct page images
        mVPageNum = (TextView) findViewById(R.id.PAG_txtPageNum);
        setPagerButtons(mCurrPage);
       
        //create and set the GUI page indicator images (active and inactive page indicators)
        mPageIndicators = new ImageView [mNumPages];
        LinearLayout layout = (LinearLayout) findViewById(R.id.PAG_lPages);
        for(int i=0; i<mNumPages; i++){
        	mPageIndicators[i] = new ImageView(this);
        	mPageIndicators[i].setPadding(10, 0, 10, 0);
        	if( i == 0 )
        		mPageIndicators[i].setImageBitmap( mImgCurrPage );
        	else
        		mPageIndicators[i].setImageBitmap( mImgDimPage );
        	
        	class MyListener implements OnClickListener{
        		private int pageIndex;
        		public MyListener(int pageIndex){ this.pageIndex = pageIndex; }
				public void onClick(View arg0) {
					mPager.setCurrentItem(pageIndex, true);
				}
        	};
        	
        	mPageIndicators[i].setOnClickListener(new MyListener(i));
        	
        	layout.addView(mPageIndicators[i]);
        }
		
		// Instantiate ViewPager and PagerAdapter.
		mPager = (ViewPager) findViewById(R.id.PAG_pgPager);
		mPager.setOnPageChangeListener(new OnPageChangeListener(){
			public void onPageScrollStateChanged(int state) {}
			public void onPageScrolled(int position, float positionOffset, int offsetPixels) {}

			//on a page change, set the correct GUI images accordingly
			public void onPageSelected(int position) {
				mCurrPage = position;
				for(int i=0; i<mNumPages; i++){
					if( i == position )
						mPageIndicators[i].setImageBitmap( mImgCurrPage );
					else
						mPageIndicators[i].setImageBitmap( mImgDimPage );
				}
				setPagerButtons(position);
			}
		});
		
		//create an adapter to keep track of the pages
		mPagerAdapter = new BioPagerAdapter( getSupportFragmentManager(), mNumPages );
		
		//now that everything is set up, query the database and set each page's info accordinly
		new BioInit( mPager, mPagerAdapter ).execute( desc );
	}
	
	/**	setPagerButtons
	 * @param pageIndex : index of the currently displayed page (0 to mNumPages-1)
	 * Based on the currently displayed page, each GUI element in the view is updated
	 * to display the correct image to indicate page movement and page status.
	 */
	public void setPagerButtons(int pageIndex){
		
		// check endpoints. If the currently displayed page is an endpoint, dim the pager next/previous button
		// check first page endpoint
		if( pageIndex == 0){
			mVLeftPage.setImageBitmap(mImgDimLeft);
			mVLeftPage.setClickable(false);
		}
		else{
			mVLeftPage.setImageBitmap(mImgLeft);
			mVLeftPage.setClickable(true);
		}
		
		//check last page endpoint
		if( pageIndex == (mNumPages-1) ){
			mVRightPage.setImageBitmap(mImgDimRight);
			mVRightPage.setClickable(false);
		}
		else{
			mVRightPage.setImageBitmap(mImgRight);
			mVRightPage.setClickable(true);
		}
		
		//set page text
		mVPageNum.setText("Page " + (pageIndex+1) + "/" + mNumPages);
	}
	
	/**	loadImage
	 * @param strFilename : filename of drawable resource to be loaded
	 * @return Bitmap image of the given image filename
	 * Loads an image from the drawable resources of the application and returns it
	 */
	private Bitmap loadImage(String strFilename){
		int id = mRes.getIdentifier(strFilename, "drawable", Global.PACKAGE);
		return BitmapFactory.decodeResource(mRes, id);
	}
}

/**	BioPagerAdapter
 * 	PagerAdapter that keeps track of each BioView to be displayed in
 * the pager. BioViews are held in an array.
 */
class BioPagerAdapter extends FragmentStatePagerAdapter
{
	//--	class member variables	--//
	private int mPages;				//number of pages
	private BioView [] mBioPages;	//array of pages (BioView objects)
	
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

/**	BioActivityDesc
 * 	This is a Serializable object needed to pass a set of data
 * to this Activity as a parameter. This object holds the
 * IDs of the donors that should be displayed in the pager.
 */
class BioActivityDesc implements Serializable
{
	private static final long serialVersionUID = Global.SERIAL_NUM;
	private int [] mnDonorIDs;
	private Global.eVIEWTYPE meFromViewType;
	
	public BioActivityDesc(Global.eVIEWTYPE type, int [] nDonorIDs){
		mnDonorIDs = nDonorIDs;
		meFromViewType = type;
	}
	
	public int [] getDonorIDs(){
		return mnDonorIDs;
	}
	
	public Global.eVIEWTYPE getFromViewType(){
		return meFromViewType;
	}
}