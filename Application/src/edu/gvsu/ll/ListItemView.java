package edu.gvsu.ll;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**	ListItemView
 * 	Creates a custom view for each item in the ListView.
 */
public class ListItemView extends RelativeLayout
{
	//--	private class member variables	--//
	private int [] mNDonorIDs;
	
	
	/**	ListItemView
	 * @param context : context of the ListItem
	 * @param strTitle : string to be displayed as the dominant text of the list item
	 * @param strSubtitles : string to be displayed as the secondary text of the list item
	 * @param strFilename : filename of the image to be loaded in the list item
	 * @param nDonorIDs : donor ids associated with this item (needed for BioView)
	 * @param index : index of this item in the list
	 * Creates a custom view of a list item to be displayed in the DirectoryActivity.
	 * All list items have a small image to be displayed on the left side of the list item.
	 * To the right side of the image is displayed the title of the list item, followed
	 * by subtext. This view loads the given image from app resources and scales it to size.
	 */
	public ListItemView( Context context, String strTitle, String [] strSubtitles, String strFilename, 
						 int [] nDonorIDs, int index ) {
		super(context);
		mNDonorIDs = nDonorIDs;
		
		//inflate custom listitemview.xml
		View.inflate(context, R.layout.listitem, this);
		this.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Intent intent = new Intent(DirectoryActivity.sInstance, BioActivity.class);
				intent.putExtra(Global.MSG_DONORS, new BioActivityDesc( mNDonorIDs ) );
				DirectoryActivity.sInstance.startActivity(intent);
			}
		});
		
		//load image
		ImageView icon = (ImageView)this.findViewById(R.id.LI_imgIcon);
		int imgID = DirectoryActivity.sInstance.getResources().getIdentifier(
				strFilename, "drawable", Global.PACKAGE);
		
		//scale the image to the right size on the screen
		if( imgID != 0 )
			icon.setImageBitmap( getScaledImage(context, imgID) );
		
		//set texts
		TextView title = (TextView)this.findViewById(R.id.LI_txtTitle);
		title.setText( strTitle );
		LinearLayout subtitleLayout = (LinearLayout)this.findViewById(R.id.LI_lSubHeadings);
		//create a separate view for each subtitle
		for( String strSubtitle : strSubtitles ){
			RelativeLayout layout = new RelativeLayout(context);
			View.inflate(context, R.layout.listitem_subheading, layout);
			( (TextView)layout.findViewById(R.id.LI_txtSubtitle) ).setText(strSubtitle);
			subtitleLayout.addView(layout);
		}
		
		//set background color
		this.setPadding(0,0,5,5);
		if(index % 2 == 0)
			this.setBackgroundColor( Color.argb(255, 245, 228, 156 ) );
		else
			this.setBackgroundColor( Color.argb( 255, 250, 240, 201 ));
	}

	
	/**	getScaledImage
	 * @param context : context of application
	 * @param resID : ID of the image to be scaled
	 * @return Bitmap image scaled to a set width of the screen
	 * Calculates the size the image needs to be in order to fit on exactly 35% of
	 * the screen (width). Renders the Bitmap object to this size and returns it.
	 */
	private Bitmap getScaledImage( Context context, int resID ){
		//grab data about the image by decoding the resource with no options
		BitmapFactory.Options imgOptions = new BitmapFactory.Options();
		imgOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeResource( getResources(), resID, imgOptions );
		
		//get the data returned
		@SuppressWarnings("deprecation")
		final int screenW = DirectoryActivity.sInstance.getWindowManager().getDefaultDisplay().getWidth();
		int imgW = imgOptions.outWidth;
		int imgH = imgOptions.outHeight;
		
		//calculate the fast-scale deflation of the image (powers of 2) to approximate the size we want
		imgOptions.inSampleSize = calcDeflateRatio( imgOptions, (int)(screenW*0.35) );	//35% of screen width
		imgOptions.inJustDecodeBounds = false;
		
		//render a new Bitmap image fine-tuned to the width we want
		return Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource( getResources(), resID, imgOptions ), 
				(int)(screenW*.35),							//desired width is 35% of screen width
				(int)((screenW*imgH)/((1/0.35)*imgW)),		//desired height should maintain aspect ratio (see below)
				false);
		
		// we keep the aspect ratio of the image by the property given below
		// and then solving for 'new Height'. 
		//	old Width(imgW)			new Width(screenW*0.35)
		//	---------------		=	----------
		//	old Height(imgH)		new Height
	}
	
	/**	calcDeflateRaio
	 * @param options : image options
	 * @param reqWidth : width we want the image to be scaled down to
	 * @return
	 */
	private int calcDeflateRatio( BitmapFactory.Options options, int reqWidth ) {
		//we don't care about height, just scale the image to the approximate desired width
		final int width = options.outWidth;	//actual width of image
		int inSampleSize = 1;
		if ( width > reqWidth) {
			final int halfWidth = width / 2;
			// Calculate the largest inSampleSize value that is a power of 2 and keeps
			// width larger than the requested width.
			while ((halfWidth / inSampleSize) > reqWidth)
				inSampleSize *= 2;
		}
		return inSampleSize;
	}
}