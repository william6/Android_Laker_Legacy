package edu.gvsu.ll;

public class GTblVal
{	
	//Table names
	public static final String TBL_MONUMENT  = "MONUMENT";	//monuments table
	public static final String TBL_DONOR	 = "DONOR";		//donors table
	public static final String TBL_IMAGE	 = "IMAGE";		//image table
	public static final String TBL_MON_DON	 = "MON_DON";	//monument donors table
	
	//Column names
	public static final String COL_NAME		 = "name";		//name
	public static final String COL_ADDR		 = "addr";		//address
	public static final String COL_CAMPUS	 = "campus";	//campus name
	public static final String COL_EST		 = "est";		//date established
	public static final String COL_GPS		 = "gps";		//GPS coordinates of building
	public static final String COL_BIO		 = "bio";		//donor biography
	public static final String COL_FILENAME	 = "path";		//filename of image on device
	public static final String COL_DON_ID	 = "donid";		//donor id
	public static final String COL_IMG_ID	 = "imgid";		//image id
	
	//defined values
	public static final long N_BIO_MAX = 32*1024;			//maximum text string for donor's bio (in KB)
}
