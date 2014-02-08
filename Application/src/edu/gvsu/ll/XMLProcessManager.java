package edu.gvsu.ll;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.AssetManager;
import android.util.Xml;

public class XMLProcessManager{

	//Files
	public static final String TXT_DONORS	 = "Donor Info.xml";
	
	//Tags
	public static final String TAG_NAME 	= "name";
	public static final String TAG_DONOR 	= "donor";
	public static final String TAG_BIO 		= "bio";
	public static final String TAG_IMGID 	= "imageid";
	
	
	private XmlPullParser mParser;
	private DatabaseManager mDBM;
	
	public XMLProcessManager(DatabaseManager dbm){
		mDBM = dbm;
	}
	
	public void addDonors(){
		String TXT_DONORS = "Donor Info.xml";
		int STR_MAX = 50*1024;
		int STR_TOL = 4*1024;
		
		HashMap<String,String> tagmap = new HashMap<String,String>();
		tagmap.put("name", "");
		tagmap.put("bio", "");
		tagmap.put("imageid", "");
		
		XmlPullParser parser = Xml.newPullParser();
		try{
			AssetManager assets = DirectoryActivity.sInstance.getAssets();
			InputStream instream = assets.open(TXT_DONORS);
			parser.setInput(instream, "utf-8");
			
			int nthDonor = 0;
			StringBuilder command = new StringBuilder(STR_MAX);
			command.append("INSERT INTO " + GTblVal.TBL_DONOR + " ");
			
			int nEvent = parser.next();
			String tag = null;
			boolean valid = false;
			while( nEvent != XmlPullParser.END_DOCUMENT ){
				
				//START TAG
				if( nEvent == XmlPullParser.START_TAG ){
					tag = parser.getName();
					
					if(tagmap.containsKey(tag.toLowerCase()))
						valid = true;
					else
						valid = false;

					if(tag.equalsIgnoreCase("name")){
						if(nthDonor == 0)
							command.append("SELECT NULL AS " + GTblVal.COL_DON_ID + ", ");
						else
							command.append(" UNION SELECT NULL, ");
					}
				}

				//CONTENT
				else if( nEvent == XmlPullParser.TEXT ){

					if(!valid || tag == null){
						nEvent = parser.next();
						continue;
					}
					
					if( parser.getText().isEmpty() || parser.getText().equalsIgnoreCase("null") )
						command.append("NULL ");
					else
						command.append("'" + parser.getText().replaceAll("'", "''") + "'");
					
					//NAME
					if(tag.equalsIgnoreCase("name")){
						if(nthDonor == 0)
							command.append(" AS " + GTblVal.COL_NAME);
						command.append(", ");
					}
					
					//BIO
					else if (tag.equalsIgnoreCase("bio")){
						if(nthDonor == 0)
							command.append(" AS " + GTblVal.COL_BIO);
						command.append(", ");
					}
					
					//IMAGE ID
					else if (tag.equalsIgnoreCase("imageid")){
						if(nthDonor == 0)
							command.append(" AS " + GTblVal.COL_IMG_ID);
						command.append(" ");
					}						
				}
				
				//END TAG
				else if( nEvent == XmlPullParser.END_TAG){
					if(parser.getName().equalsIgnoreCase("donor")){
						nthDonor++;
						if(command.toString().trim().length() >= STR_MAX-STR_TOL){
							mDBM.executeSQL(command.toString());
							nthDonor = 0;
						}
					}
					tag = null;
				}
				
				nEvent = parser.next();
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
