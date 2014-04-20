package edu.gvsu.ll;

import java.io.File;
import java.util.Random;

import android.os.Bundle;
import android.app.Activity;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

public class SlideShowActivity extends Activity {

	private DatabaseManager dbm;
	int imgid[];
	int n = 10;
	int i=0;
	private ImageView imageView;
	RefreshHandler refreshHandler=new RefreshHandler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.slideshow);
		this.imageView=(ImageView)this.findViewById(R.id.HOME_imgDonor);

		dbm = Global.gDBM; //new DatabaseManager(this, new File( this.getFilesDir().getPath() +  "/Laker Legacies.sqlite"), 1);

		String filename;
		String query =	"SELECT filename FROM DON_IMG";
		Cursor donorCursor = dbm.query(query);
		int nTotalImages = donorCursor.getCount();
		imgid = new int [nTotalImages];

		donorCursor.moveToFirst();

		//populate array with image IDs
		for (int i = 0; i < nTotalImages; i++){
			filename = donorCursor.getString(0);
			imgid[i] = getResources().getIdentifier( filename, "drawable", "edu.gvsu.ll");
			donorCursor.moveToNext();
		}
		
		//randomly shuffle image IDs
		Random r = new Random();
		for(int i=0; i<nTotalImages; i++){
			int a = r.nextInt(nTotalImages);
			int b = r.nextInt(nTotalImages);
			int temp = imgid[a];
			imgid[a] = imgid[b];
			imgid[b] = temp;
		}
		updateUI();
	}

	class RefreshHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			SlideShowActivity.this.updateUI();
		}
		public void sleep(long delayMillis){
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	};

	public void updateUI(){
		int currentInt=n+1;
		if(currentInt<=10000){
			refreshHandler.sleep(7500);
			n = currentInt;
			if(i<imgid.length){
				imageView.setImageResource(imgid[i]);
				i++;
			}else{
				i = 0;
			}
		}
	}
}
