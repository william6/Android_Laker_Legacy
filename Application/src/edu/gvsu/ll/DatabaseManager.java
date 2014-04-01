package edu.gvsu.ll;

import java.io.File;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/** Database Manager
 * Manages the SQLite database file.  This object loads a given database file and restricts
 * its use to queries only - the database cannot be updated/changed.
 */
public class DatabaseManager
{
	//--	Private class member variables	--//
	private SQLiteDatabase 	mDatabase;
	private File 			mfDatabase;
	
	/** DatabaseManager
	 * @param context : context of the application
	 * @param database : database file object pointing to the database of application
	 * @param version : database version number
	 */
	public DatabaseManager(Context context, File database, int version){
		//load the database
		mfDatabase = database;
		if( mfDatabase.exists() ){
			try{
				mDatabase = SQLiteDatabase.openDatabase(mfDatabase.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
				Log.d("LAKER_L", "Database " + mfDatabase.getAbsolutePath() + " loaded");
			}
			catch( SQLiteException sqle){
				throw new RuntimeException("ERROR: the application did not download properly. The database file is either corrupted or missing");
			}
		}
	}
	
	/** query
	 * @param query : string value of SQL query to execute on the database
	 * @return Cursor object pointing to the results of the query.  Null if an error occurred.
	 * Queries the database file given the string of a query.
	 */
	public Cursor query(String query){
		try{
			return mDatabase.rawQuery(query, null);
		}
		catch(SQLiteException sqle){
			Log.e("LAKER_L", sqle.getMessage());
			return null;
		}
	}
	
	/** getDatabaseName
	 * @return the name of the database file
	 */
	public String getDatabaseName(){
		return mfDatabase.getName();
	}
}
