package edu.gvsu.ll;

public class GTblVal
{
	//Table names
	public static final String TBL_MONUMENT  = "MONUMENT";	//monuments table
	public static final String TBL_DONOR	 = "DONOR";		//donors table
	public static final String TBL_MON_DON	 = "MON_DON";	//monument donors table
	
	//Column names
	public static final String COL_NAME		 = "Name";		//name
	public static final String COL_ADDR		 = "Addr";		//address
	public static final String COL_CAMPUS	 = "Campus";	//campus name
	public static final String COL_EST		 = "Est";		//date established
	public static final String COL_GPS		 = "GPS";		//GPS coordinates of building
	public static final String COL_ID		 = "ID";		//item's ID value
	public static final String COL_BIO		 = "Bio";		//donor biography
	
	//defined values
	public static final long N_BIO_MAX = 32*1024;			//maximum text string for donor's bio (in KB)
}
