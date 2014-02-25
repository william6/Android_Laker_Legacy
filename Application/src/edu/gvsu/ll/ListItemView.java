package edu.gvsu.ll;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/*	ListItemView
 * 	Custom view for each item in the ListView
 */
public class ListItemView extends RelativeLayout
{
	public ListItemView( Context context, String strTitle, String strSubtitle, String strFilename, int index ) {
		super(context);
		
		//inflate custom view
		LayoutInflater inflator = (LayoutInflater) DirectoryActivity.sInstance.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflator.inflate(R.layout.listitem, this);
		
		ImageView icon = (ImageView)this.findViewById(R.id.LI_imgIcon);
		TextView title = (TextView)this.findViewById(R.id.LI_txtTitle);
		TextView subtitle = (TextView)this.findViewById(R.id.LI_txtSubtitle);
		
		//TODO -- filenaming conventions
		strFilename = strFilename.toLowerCase().replaceAll(" ", "_").replaceAll("'", "").replaceAll("-", "_");
		
		//load image
		int imgID = DirectoryActivity.sInstance.getResources().getIdentifier(
				strFilename, "drawable", DirectoryActivity.PACKAGE);
		
		if( imgID != 0 )
			icon.setImageBitmap( getScaledImage(context, imgID) );
		
		
		//set text and background
		title.setText( strTitle );
		subtitle.setText( strSubtitle );
		if(index % 2 == 0)
			this.setBackgroundColor( Color.argb(255, 245, 228, 156 ) );
		else
			this.setBackgroundColor( Color.argb( 255, 250, 240, 201 ));
	}

	
	private int calcDeflateRatio( BitmapFactory.Options options, int reqWidth ) {
		
		//we don't care about height, just scale the image to the correct width

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
	
	private Bitmap getScaledImage( Context context, int resID ){
		
		BitmapFactory.Options imgOptions = new BitmapFactory.Options();
		imgOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeResource( getResources(), resID, imgOptions );
		
		final int screenW = DirectoryActivity.sInstance.getWindowManager().getDefaultDisplay().getWidth();
		int imgW = imgOptions.outWidth;
		int imgH = imgOptions.outHeight;
		
		
		imgOptions.inSampleSize = calcDeflateRatio( imgOptions, (int)(screenW/3) );
		imgOptions.inJustDecodeBounds = false;
		
		return Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource( getResources(), resID, imgOptions ), 
				(int)(screenW/3),
				(int)((screenW*imgH)/(3*imgW)),
				false);
	}
}