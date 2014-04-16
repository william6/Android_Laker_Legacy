package edu.gvsu.ll;

/**	QueryType
 * Represents a query as a single object. This object stores the columns to be
 * returned from a query, the table to query, the column to sort by, and a
 * filter to search the query for.
 */
public class QueryType
{
	//--	statically accessable class variables	--//
	public static final String STR_LIST_MONUMENT = Global.TBL_MONUMENT;
	public static final String STR_LIST_DONOR = Global.TBL_DONOR;
	
	public static final String STR_SORT_MON_NAME = Global.COL_MON_NAME;
	public static final String STR_SORT_DON_NAME = Global.COL_LNAME;
	public static final String STR_SORT_CAMPUS = Global.COL_CAMPUS;
	public static final String STR_SORT_DISTANCE = "DISTANCE";
	
	//--	private class member variables	--//
	private String [] 	astrSelectCol;
	private String 		strTable;
	private String 		strSort;
	private String 		strSearch;
	
	/**	QueryType
	 * @param selectColumns : array of string column names to be returned in a Cursor
	 * @param strTable : name of the table to query
	 * @param strSort : column to sort the query by
	 * @param strSearch : string to search the data with (searches names only)
	 */
	public QueryType( String [] selectColumns, String strTable, String strSort, String strSearch ){
		this.astrSelectCol = selectColumns;
		this.strTable = strTable;
		this.strSort = strSort;
		this.strSearch = strSearch;
	}
	
	public String [] getSelectColumns(){
		return astrSelectCol;
	}
	
	public String getTableField(){
		return strTable;
	}
	
	public String getSortField(){
		return strSort;
	}
	
	public String getSearchField(){
		return strSearch;
	}
	
	
	@Override
	/**	equals
	 * @param obj : QueryType object to compare this QueryType object against
	 * Compares this QueryType object with a given QueryType object and determines
	 * if the queries are identical
	 */
	public boolean equals(Object obj){
		QueryType other = (QueryType) obj;
		
		//if different select columns, return false
		String [] thisSelect = this.getSelectColumns();
		String [] otherSelect = other.getSelectColumns();
		if( thisSelect.length == otherSelect.length ){
			for(int i=0; i<thisSelect.length; i++)
				if( !thisSelect[i].equalsIgnoreCase(otherSelect[i]) )
					return false;
		}
		else
			return false;
		
		// if different tables, return false
		if( !this.getTableField().equalsIgnoreCase(other.getTableField() ) )
			return false;
		
		// if diff sorts, return false
		if( !this.getSortField().equalsIgnoreCase(other.getSortField() ) )
			return false;
		
		//if diff search, return false
		if( this.getSearchField() == null ){
			if( other.getSearchField() != null )
				return false;
		}
		else{
			if( other.getSearchField() == null || 
					!this.getSearchField().equalsIgnoreCase( other.getSearchField() ) )
				return false;
		}
		
		//if we're querying for building distances, we want to continue with the query
		//so we return false
		if( this.getSortField().equalsIgnoreCase("Distance from me") )
			return false;
		
		return true;	//otherwise, return true
	}
}