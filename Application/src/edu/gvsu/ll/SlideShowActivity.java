package edu.gvsu.ll;

import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.TextView;

public class SlideShowActivity extends Activity {

	int n = 10;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.slideshow);
        this.imageView=(ImageView)this.findViewById(R.id.imageView);
        updateUI();
	}
	
	    private ImageView imageView;
	    int i=0;
	    int imgid[]={R.drawable.alumni_house,R.drawable.arboretum,R.drawable.cook_devos,};
	    RefreshHandler refreshHandler=new RefreshHandler();
	    
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
	        int currentInt=n+10;
	        if(currentInt<=300){
	            refreshHandler.sleep(2000);
	            n = currentInt;
	            if(i<imgid.length){
	                imageView.setImageResource(imgid[i]);
	                
	                // imageView.setPadding(left, top, right, bottom);
	                i++;
	            }else
	            	i = 0;
	        }
	    }

}
