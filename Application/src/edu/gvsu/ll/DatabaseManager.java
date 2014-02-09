package edu.gvsu.ll;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class DatabaseManager
{
	private SQLiteDatabase 	mDatabase;
	private File 			mfDatabase;
	
	/*
	 * @param context : context of the application
	 * @param databaseName : name of database file (with or without the .db extension)
	 * @param pathToDb : absolute filepath to the directory containing the database
	 * @param version : database version number
	 */
	public DatabaseManager(Context context, String databaseName, String pathToDb, int version){
		
		//modify input values
		if(!pathToDb.endsWith("/"))
			pathToDb += "/";
		if(!databaseName.endsWith(".db"));
			databaseName += ".db";
		
		//check if the given database file exists. If database exists, load it
		mfDatabase = new File(pathToDb + databaseName);
		
		//DEBUG -- TODO - remove db file for debugging
		if( mfDatabase.exists() ) mfDatabase.delete();
		
		if( mfDatabase.exists() ){
			try{
				mDatabase = SQLiteDatabase.openDatabase(pathToDb + databaseName, null, SQLiteDatabase.OPEN_READWRITE);
				Log.d("Database " + databaseName + " loaded from " + pathToDb);
			}
			catch( SQLiteException sqle){
				throw new RuntimeException(sqle.getMessage());
			}
		}
		//if database doesn't exist, create it
		else{
			Log.d("Database " + databaseName + " doesn't exist. Creating it...");
			File dbDir = new File(pathToDb);		//directory of database
			dbDir.mkdirs();
			dbDir.setReadable(true, false);
			dbDir.setWritable(true, false);
			mfDatabase.setReadable(true, false);
			mfDatabase.setWritable(true, false);
			if(dbDir.exists())
				Log.d("Created database directory");
			try{
				mDatabase = SQLiteDatabase.openOrCreateDatabase(mfDatabase, null);
				Log.d("Created database " + databaseName + " at " + pathToDb);
			}
			catch(SQLiteException e){
				throw new RuntimeException("ERROR: couldn't create database on device.  " + e.getMessage());
			}
		}
	}
	
	public boolean createTables(){
		
		boolean status;
		
		//create MONUMENT table
		String command = "CREATE TABLE IF NOT EXISTS " + GTblVal.TBL_MONUMENT + " ( " +
				GTblVal.COL_NAME + " TEXT (128) PRIMARY KEY, " +
//				GTblVal.COL_ADDR + " TEXT (256) NOT NULL, " +
				GTblVal.COL_CAMPUS + " TEXT (32) NOT NULL, " +
				GTblVal.COL_EST + " INTEGER, " +
				GTblVal.COL_GPS + " TEXT (64) )";
		status = executeSQL(command);
		
		if(!status) return status;
		
		//create IMAGE table
		command = "CREATE TABLE IF NOT EXISTS " + GTblVal.TBL_IMAGE + " ( " +
				GTblVal.COL_IMG_ID + " INTEGER PRIMARY KEY, " +
				GTblVal.COL_FILENAME + " TEXT (256) NOT NULL, " +
				GTblVal.COL_NAME + " TEXT(128) REFERENCES " + GTblVal.TBL_MONUMENT + "(" + GTblVal.COL_NAME + ") )";
		status = executeSQL(command);
		
		if(!status) return status;
		
		//create DONOR table
		command = "CREATE TABLE IF NOT EXISTS " + GTblVal.TBL_DONOR + " ( " +
				GTblVal.COL_DON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				GTblVal.COL_NAME + " TEXT (128) NOT NULL, " +
				GTblVal.COL_BIO + " TEXT (" + GTblVal.N_BIO_MAX + ") NOT NULL, " +
				GTblVal.COL_IMG_ID + " INTEGER REFERENCES " + GTblVal.TBL_IMAGE + "(" + GTblVal.COL_IMG_ID + ") )";
		status = executeSQL(command);
		
		if(!status) return status;
		
		//create MONUMENT_DONOR table
		command = "CREATE TABLE IF NOT EXISTS " + GTblVal.TBL_MON_DON + " ( " +
				GTblVal.COL_NAME + " TEXT(128) REFERENCES " + GTblVal.TBL_MONUMENT + "(" + GTblVal.COL_NAME + "), " +
				GTblVal.COL_DON_ID + " INTEGER REFERENCES " + GTblVal.TBL_DONOR + "(" + GTblVal.COL_DON_ID + "), " +
				"PRIMARY KEY(" + GTblVal.COL_NAME + ", " + GTblVal.COL_DON_ID + ") )";
		status = executeSQL(command);
		
		return status;
	}
	
	/*
	 * @param : string value of a SQL command to execute on the database
	 */
	public boolean executeSQL(String command){
		try{
			mDatabase.execSQL(command);
			return true;
		}
		catch(SQLiteException sqle){
			Log.e(sqle.getMessage());
			return false;
		}
	}
	
	/*
	 * @param query : string value of SQL query to execute on the database
	 */
	public Cursor query(String  query){
		
		try{
			return mDatabase.rawQuery(query, null);
		}
		catch(SQLiteException sqle){
			Log.e(sqle.getMessage());
			return null;
		}
		
	}
	
	public void saveDatabase(){
		// TODO -- DEBUG -- copy db file to external storage so we can get the file. This will be unnecessary when porting. delete this function.
		try {
			File fPublic = new File((DirectoryActivity.sInstance.getExternalFilesDir(null)).getAbsolutePath() + "/database.db");
			fPublic.createNewFile();
			
			InputStream fin = new FileInputStream(mfDatabase);
			OutputStream fout = new FileOutputStream(fPublic);
			byte buffer[] = new byte[100*1024];
			fin.read(buffer);
			fout.write(buffer);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch ( IOException ioe){
			
		}
	}
}
