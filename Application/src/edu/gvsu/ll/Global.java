package edu.gvsu.ll;

/**	Global
 * All values defined here are application-specific and make it public for all
 * classes in the application to access. The only 'modifiable' variable is the
 * DatabaseManager which is created on application startup.
 */
public class Global
{	
	//--	Application values	--//
	public static final String 	PACKAGE 	= "edu.gvsu.ll";
	public static final String 	MSG_DONORS 	= "edu.gvsu.ll.donors";
	
	//--	Database table names	--//
	public static final String TBL_MONUMENT  = "MONUMENT";	//monuments table
	public static final String TBL_MON_IMG	 = "MON_IMG";	//monument image table
	public static final String TBL_DONOR	 = "DONOR";		//donors table
	public static final String TBL_DON_IMG	 = "DON_IMG";	//donor image table
	public static final String TBL_MON_DON	 = "MON_DON";	//monument donors table
	public static final String TBL_FACTS	 = "FACTOID";	//table of GV facts
	
	//--	Database column names	--//
	public static final String COL_MON_NAME	 = "name";		//name of monument
	public static final String COL_ADDR		 = "addr";		//address
	public static final String COL_CAMPUS	 = "campus";	//campus name
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
	public static final String COL_DUET_ID	 = "duetid";	//id of duet donor (donor paired with another)
	public static final String COL_IMG_ID	 = "imgid";		//image id
	public static final String COL_FACT_ID	 = "factid";	//fact id
	public static final String COL_FACT		 = "fact";		//fact string
	
	//--	defined values	--//
	public static final long 	N_BIO_MAX 	= 32*1024;		//maximum text string for donor's bio (in KB)
	public static final long	SERIAL_NUM	= 4262014L;		//Number used for serialization of BioActivity parameters
	
	//--	global variable (database access)	--//
	public static DatabaseManager gDBM = null;
}
