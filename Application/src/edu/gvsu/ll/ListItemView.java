package edu.gvsu.ll;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/*	ListItemView
 * 	Custom view for each item in the ListView
 */
public class ListItemView extends RelativeLayout
{
	private int [] mNDonorIDs;
	
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
		
		if( imgID != 0 )
			icon.setImageBitmap( getScaledImage(context, imgID) );
		
		//set texts
		TextView title = (TextView)this.findViewById(R.id.LI_txtTitle);
		title.setText( strTitle );
		LinearLayout subtitleLayout = (LinearLayout)this.findViewById(R.id.LI_lSubHeadings);
		for( String strSubtitle : strSubtitles ){
			RelativeLayout layout = new RelativeLayout(context);
			View.inflate(context, R.layout.listitem_subheading, layout);
			( (TextView)layout.findViewById(R.id.LI_txtSubtitle) ).setText(strSubtitle);
			subtitleLayout.addView(layout);
		}
		
		//set background
		this.setPadding(0,0,5,5);
		if(index % 2 == 0)
			this.setBackgroundColor( Color.argb(255, 245, 228, 156 ) );
		else
			this.setBackgroundColor( Color.argb( 255, 250, 240, 201 ));
	}

	
	private int calcDeflateRatio( BitmapFactory.Options options, int reqWidth ) {
		//we don't care about height, just scale the image to the approximate desired width
		final int width = options.outWidth;	//actual width of image
		int inSampleSize = 1;
		if ( width > reqWidth) {
			final int halfWidth = width / 2;
			// Calculate the largest inSampleSize value that is a power of 2 and keeps
			// width larger than the requested width.
			while ((halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}
	
	//return a bitmap image scaled to a set width of the screen
	private Bitmap getScaledImage( Context context, int resID ){
		BitmapFactory.Options imgOptions = new BitmapFactory.Options();
		imgOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeResource( getResources(), resID, imgOptions );
		
		final int screenW = DirectoryActivity.sInstance.getWindowManager().getDefaultDisplay().getWidth();
		int imgW = imgOptions.outWidth;
		int imgH = imgOptions.outHeight;
		
		imgOptions.inSampleSize = calcDeflateRatio( imgOptions, (int)(screenW*0.35) );	//35% of screen width
		imgOptions.inJustDecodeBounds = false;
		
		return Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource( getResources(), resID, imgOptions ), 
				(int)(screenW*.35),							//desired image is 35% of screen width
				(int)((screenW*imgH)/((1/0.35)*imgW)),		//keep aspect ratio of image
				false);
	}
}