package edu.gvsu.ll;

public class Log 
{
	private static final String DEBUG_TAG = "LakerL-D";
	private static final String ERROR_TAG = "LakerL-E";
	
	
	public static void d(String debugMessage){
//		android.util.Log.d(DEBUG_TAG, " ");
//		android.util.Log.d(DEBUG_TAG, "===================================================");
//		android.util.Log.d(DEBUG_TAG, " ");
		android.util.Log.d(DEBUG_TAG, debugMessage);
//		android.util.Log.d(DEBUG_TAG, " ");
//		android.util.Log.d(DEBUG_TAG, "===================================================");
//		android.util.Log.d(DEBUG_TAG, " ");
	}
	
	public static void e(String errorMessage){
		android.util.Log.e(ERROR_TAG, " ");
		android.util.Log.e(ERROR_TAG, "===================================================");
		android.util.Log.e(ERROR_TAG, " ");
		android.util.Log.e(ERROR_TAG, "<<<<  " + errorMessage + "  >>>>");
		android.util.Log.e(ERROR_TAG, " ");
		android.util.Log.e(ERROR_TAG, "===================================================");
		android.util.Log.e(ERROR_TAG, " ");
	}
}
