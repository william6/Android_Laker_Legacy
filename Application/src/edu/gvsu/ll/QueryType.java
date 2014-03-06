package edu.gvsu.ll;

public class QueryType
{
	public static final String STR_LIST_MONUMENT = GTblVal.TBL_MONUMENT;
	public static final String STR_LIST_DONOR = GTblVal.TBL_DONOR;
	public static final String STR_SORT_NAME = GTblVal.COL_NAME;
	public static final String STR_SORT_CAMPUS = GTblVal.COL_CAMPUS;
	public static final String STR_SORT_DATE = GTblVal.COL_EST;
	public static final String STR_SORT_DISTANCE = "DISTANCE";
	
	private String [] astrSelectCol;
	private String strTable;
	private String strSort;
	private String strSearch;
	
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
}