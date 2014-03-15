package edu.gvsu.ll;

public class Global
{	
	//Application values
	public static final String PACKAGE = "edu.gvsu.ll";
	
	//Table names
	public static final String TBL_MONUMENT  = "MONUMENT";	//monuments table
	public static final String TBL_MON_IMG	 = "MON_IMG";	//monument image table
	public static final String TBL_DONOR	 = "DONOR";		//donors table
	public static final String TBL_DON_IMG	 = "DON_IMG";	//donor image table
	public static final String TBL_MON_DON	 = "MON_DON";	//monument donors table
	
	//Column names
	public static final String COL_MON_NAME	 = "name";		//name of monument
	public static final String COL_ADDR		 = "addr";		//address
	public static final String COL_CAMPUS	 = "campus";	//campus name
//	public static final String COL_EST		 = "est";		//date established
	public static final String COL_LATITUDE	 = "latitude";	//GPS latitude
	public static final String COL_LONGITUDE = "longitude";	//GPS longitude
	public static final String COL_TITLE	 = "title";		//Donor Title
	public static final String COL_FNAME	 = "name_f";	//First name
	public static final String COL_MNAME	 = "name_m";	//Middle name
	public static final String COL_LNAME	 = "name_l";	//Last name
	public static final String COL_SUFFIX	 = "name_s";	//name suffix
	public static final String COL_BIO		 = "bio";		//donor biography
	public static final String COL_FILENAME	 = "filename";	//filename of image on device
	public static final String COL_DON_ID	 = "donid";		//donor id
	public static final String COL_IMG_ID	 = "imgid";		//image id
	
	//defined values
	public static final long N_BIO_MAX = 32*1024;			//maximum text string for donor's bio (in KB)
}
