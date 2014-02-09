package edu.gvsu.ll;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.content.res.AssetManager;
import android.util.Xml;

public class XMLProcessManager{

	//Files
	public static final String DONORS_XML	 = "Donor Info.xml";
	public static final String MONUMENTS_XML = "Monument Info.xml";
	public static final String IMAGES_XML	 = "Image Info.xml";
	
	//Tags
	public static final String TAG_NAME 	= "name";
	public static final String TAG_DONOR 	= "donor";
	public static final String TAG_BIO 		= "bio";
	public static final String TAG_IMGID 	= "imageid";
	public static final String TAG_MONUMENT	= "monument";
	public static final String TAG_CAMPUS	= "campus";
	public static final String TAG_YEAR_EST	= "year_est";
	public static final String TAG_GPS		= "gps";
	public static final String TAG_IMAGE	= "image";
	public static final String TAG_FILENAME = "filename";
	
	public static final HashSet<String> TAGSET = new HashSet<String>();
	static{
		TAGSET.add(TAG_NAME);
		TAGSET.add(TAG_DONOR);
		TAGSET.add(TAG_BIO);
		TAGSET.add(TAG_IMGID);
		TAGSET.add(TAG_MONUMENT);
		TAGSET.add(TAG_CAMPUS);
		TAGSET.add(TAG_YEAR_EST);
		TAGSET.add(TAG_GPS);
		TAGSET.add(TAG_IMAGE);
	}
	
	private XmlPullParser mParser;
	private DatabaseManager mDBM;
	
	private final int MAX_SQL = 50 * 1024;
	private final int TOLERANCE = 5 * 1024;
	
	public XMLProcessManager(DatabaseManager dbm){
		mParser = Xml.newPullParser();
		mDBM = dbm;	
	}
	
	public void addDonors(){
		parseDocument( DONORS_XML, GTblVal.TBL_DONOR, TAG_DONOR );
	}
	
	public void addMonuments(){
		parseDocument( MONUMENTS_XML, GTblVal.TBL_MONUMENT, TAG_MONUMENT );
	}
	
	public void addImages(){
		parseDocument( IMAGES_XML, GTblVal.TBL_IMAGE, TAG_IMAGE );
	}
		
	
	private void parseDocument(String strFile, String strTable, String strEntity){
		try{
			AssetManager assets = DirectoryActivity.sInstance.getAssets();
			InputStream instream = assets.open(strFile);
			mParser.setInput(instream, "utf-8");
			
			int nthElement = 0;
			StringBuilder command = new StringBuilder(MAX_SQL);
			command.append("INSERT INTO " + strTable + " ");
			
			int nEvent = mParser.next();
			String tag = null;
			boolean valid = false;
			while( nEvent != XmlPullParser.END_DOCUMENT ){
				
				//START TAG
				if( nEvent == XmlPullParser.START_TAG ){
					tag = mParser.getName();
					
					if(tag.equalsIgnoreCase(strEntity)){
						if(nthElement == 0){
							if(strEntity.equalsIgnoreCase(TAG_DONOR))
								command.append("SELECT NULL AS " + GTblVal.COL_DON_ID + ", ");
							else
								command.append("SELECT ");
						}
						else{
							if(strEntity.equalsIgnoreCase(TAG_DONOR))
								command.append(" UNION SELECT NULL, ");
							else
								command.append(" UNION SELECT ");
						}
						
						nEvent = mParser.next();	//got entity, skip to data
						continue;
					}
					
					if(TAGSET.contains(tag.toLowerCase()))
						valid = true;
					else
						valid = false;
				}

				//CONTENT
				else if( nEvent == XmlPullParser.TEXT ){

					if(!valid || tag == null){
						nEvent = mParser.next();
						continue;
					}
					
					if( mParser.getText().isEmpty() || mParser.getText().equalsIgnoreCase("null") )
						command.append("NULL ");
					else
						command.append("'" + mParser.getText().replaceAll("'", "''") + "'");
					
					//NAME
					if(tag.equalsIgnoreCase(TAG_NAME)){
						if(nthElement == 0)
							command.append(" AS " + GTblVal.COL_NAME);
						command.append(", ");
					}
					
					//BIO
					else if (tag.equalsIgnoreCase(TAG_BIO)){
						if(nthElement == 0)
							command.append(" AS " + GTblVal.COL_BIO);
						command.append(", ");
					}
					
					//IMAGE ID
					else if (tag.equalsIgnoreCase(TAG_IMGID)){
						if(nthElement == 0)
							command.append(" AS " + GTblVal.COL_IMG_ID);
					}
					
					//CAMPUS
					else if (tag.equalsIgnoreCase(TAG_CAMPUS)){
						if(nthElement == 0)
							command.append(" AS " + GTblVal.COL_CAMPUS);
						command.append(", ");
					}
					
					//YEAR_EST
					else if (tag.equalsIgnoreCase(TAG_YEAR_EST)){
						if(nthElement == 0)
							command.append(" AS " + GTblVal.COL_EST);
						command.append(", ");
					}
					
					//GPS
					else if (tag.equalsIgnoreCase(TAG_GPS)){
						if(nthElement == 0)
							command.append(" AS " + GTblVal.COL_GPS);
					}
				}
				
				//END TAG
				else if( nEvent == XmlPullParser.END_TAG){
					if(mParser.getName().equalsIgnoreCase(strEntity)){
						nthElement++;
						if(command.toString().trim().length() >= MAX_SQL-TOLERANCE){
							mDBM.executeSQL(command.toString());
							nthElement = 0;
						}
					}
					tag = null;
				}
				
				nEvent = mParser.next();
			}
			
			instream.close();
			String strSQL = command.toString().trim();
			mDBM.executeSQL(strSQL);
		}
		catch(IOException ioe){
			throw new RuntimeException(ioe.getMessage());
		}
		catch(XmlPullParserException xppe){
			throw new RuntimeException(xppe.getMessage());
		}
	}
}
