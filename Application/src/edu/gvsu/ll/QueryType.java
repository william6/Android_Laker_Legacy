package edu.gvsu.ll;

public class QueryType
{
	public static final String STR_LIST_MONUMENT = Global.TBL_MONUMENT;
	public static final String STR_LIST_DONOR = Global.TBL_DONOR;
	
	public static final String STR_SORT_MON_NAME = Global.COL_MON_NAME;
	public static final String STR_SORT_DON_NAME = Global.COL_LNAME;
	public static final String STR_SORT_CAMPUS = Global.COL_CAMPUS;
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
	
	@Override
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
		return true;	//otherwise, return true
	}
}