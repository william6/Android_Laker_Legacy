package edu.gvsu.ll;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class DatabaseManager
{
	private SQLiteDatabase mDatabase;
	
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
		File fDatabase = new File(pathToDb + databaseName);
		if( fDatabase.exists() ){
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
			if(dbDir.exists())
				Log.d("Created database directory");
			try{
				mDatabase = SQLiteDatabase.openOrCreateDatabase(fDatabase, null);
				createTables();
				Log.d("Created database " + databaseName + " at " + pathToDb);
			}
			catch(SQLiteException e){
				throw new RuntimeException("ERROR: couldn't create database on device.  " + e.getMessage());
			}
		}
	}
	
	private void createTables(){
		//create MONUMENT table
		String command = "CREATE TABLE " + GTblVal.TBL_MONUMENT + " ( " +
				GTblVal.COL_NAME + " TEXT (128) PRIMARY KEY, " +
				GTblVal.COL_ADDR + " TEXT (256) NOT NULL, " +
				GTblVal.COL_CAMPUS + " TEXT (32) NOT NULL, " +
				GTblVal.COL_EST + " DATE, " +
				GTblVal.COL_GPS + " TEXT (64) )";
		executeSQL(command);
		
		//create IMAGE table
		command = "CREATE TABLE " + GTblVal.TBL_IMAGE + " ( " +
				GTblVal.COL_IMG_ID + " INTEGER PRIMARY KEY, " +
				GTblVal.COL_FILEPATH + " TEXT (256) NOT NULL, " +
				GTblVal.COL_NAME + " TEXT(128) REFERENCES " + GTblVal.TBL_MONUMENT + "(" + GTblVal.COL_NAME + ") )";
		executeSQL(command);
		
		//create DONOR table
		command = "CREATE TABLE " + GTblVal.TBL_DONOR + " ( " +
				GTblVal.COL_DON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				GTblVal.COL_NAME + " TEXT (128) NOT NULL, " +
				GTblVal.COL_BIO + " TEXT (" + GTblVal.N_BIO_MAX + ") NOT NULL, " +
				GTblVal.COL_IMG_ID + " INTEGER REFERENCES " + GTblVal.TBL_IMAGE + "(" + GTblVal.COL_IMG_ID + ") )";
		executeSQL(command);
		
		//create MONUMENT_DONOR table
		command = "CREATE TABLE " + GTblVal.TBL_MON_DON + " ( " +
				GTblVal.COL_NAME + " TEXT(128) REFERENCES " + GTblVal.TBL_MONUMENT + "(" + GTblVal.COL_NAME + "), " +
				GTblVal.COL_DON_ID + " INTEGER REFERENCES " + GTblVal.TBL_DONOR + "(" + GTblVal.COL_DON_ID + "), " +
				"PRIMARY KEY(" + GTblVal.COL_NAME + ", " + GTblVal.COL_DON_ID + ") )";
		executeSQL(command);
		
		// /*  ADD TBL DATA -- DEBUGGING
		command = 	"INSERT INTO " + GTblVal.TBL_DONOR + " " +
					"SELECT NULL AS '" + GTblVal.COL_DON_ID + "', 'Kyle Peltier'" + " AS '" + GTblVal.COL_NAME + "', 'Grand Valley student' AS '" + GTblVal.COL_BIO + "', NULL AS '" + GTblVal.COL_IMG_ID + "' " +
					"UNION SELECT NULL, 'Matthew Williams', 'Grand Valley student', NULL " +
					"UNION SELECT NULL, 'Samantha Williams', 'Grand Valley student', NULL ";
		executeSQL(command);
		
		command = 	"INSERT INTO " + GTblVal.TBL_IMAGE + " " +
					"SELECT 0 AS '" + GTblVal.COL_IMG_ID + "', 'donorimg/Cook, Peter.jpg' AS " + GTblVal.COL_FILEPATH + ", NULL AS " + GTblVal.COL_NAME + " " +
					"UNION SELECT 1, 'buildingimg/Kirkhoff.jpg', NULL";
		executeSQL(command);
		// -- DEBUGGING -- */
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
}
