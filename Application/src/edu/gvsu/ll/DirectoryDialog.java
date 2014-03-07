package edu.gvsu.ll;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class DirectoryDialog {
	
	Activity mParent;
	Spinner mSpinList;
	Spinner mSpinSort;
	AlertDialog.Builder mBuilder;
	LinearLayout mDialogView;
	ArrayAdapter<CharSequence> mAdapList;
	ArrayAdapter<CharSequence> mAdapSort;

	public DirectoryDialog(Activity parent){
		
		mParent = parent;
		
		//create dialog view
		mDialogView = new LinearLayout(parent);
		LayoutInflater inflator = (LayoutInflater) mParent.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflator.inflate(R.layout.dir_dialog, mDialogView);

		mSpinList = (Spinner) mDialogView.findViewById(R.id.DIR_DIALOG_spin_listType);
		mSpinSort = (Spinner) mDialogView.findViewById(R.id.DIR_DIALOG_spin_sort);

		// set up the List spinner
		mAdapList = ArrayAdapter.createFromResource(parent,
				R.array.SPIN_listType, android.R.layout.simple_spinner_item);
		mAdapList.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);	//specify layout
		mSpinList.setAdapter(mAdapList);

		// set up the Sort spinner
		mAdapList = ArrayAdapter.createFromResource(parent,
				R.array.SPIN_sortMonument, android.R.layout.simple_spinner_item);
		mAdapList.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);	//specify layout
		mSpinSort.setAdapter(mAdapList);

		setListener();
		buildDialog();
	}
	
	private void setListener(){
		OnItemSelectedListener listListener = new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
				CharSequence selection = (CharSequence)adapter.getItemAtPosition(position);
				//list by monument
				if( selection.toString().equalsIgnoreCase("Building")){
					// set up the Sort spinner
					mAdapList = ArrayAdapter.createFromResource(mParent,
							R.array.SPIN_sortMonument, android.R.layout.simple_spinner_item);
					mAdapList.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);	//specify layout
					mSpinSort.setAdapter(mAdapList);
				}
				//list by donor
				else if( selection.toString().equalsIgnoreCase("Contributor")){
					// set up the Sort spinner
					mAdapList = ArrayAdapter.createFromResource(mParent,
							R.array.SPIN_sortContributor, android.R.layout.simple_spinner_item);
					mAdapList.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);	//specify layout
					mSpinSort.setAdapter(mAdapList);
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		};
		mSpinList.setOnItemSelectedListener(listListener);
	}
	
	private void buildDialog(){
		//build dialog
		mBuilder = new AlertDialog.Builder(mParent);
		mBuilder.setTitle("List options");
		mBuilder.setMessage("Select list sorting options");
		mBuilder.setNegativeButton("Cancel", null);
		mBuilder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				int listBy = (Integer) ((Spinner)(((AlertDialog) dialog).findViewById(R.id.DIR_DIALOG_spin_listType))).getSelectedItemPosition();
				int sortBy = (Integer) ((Spinner)(((AlertDialog) dialog).findViewById(R.id.DIR_DIALOG_spin_sort))).getSelectedItemPosition();
				
				String [] selectCols = null;
				String strTable = null, strSort = null;
				
				if(listBy == 0)
					strTable = GTblVal.TBL_MONUMENT;
				else if (listBy == 1)
					strTable = GTblVal.TBL_DONOR;

				switch( sortBy ){
					case(0):
						strSort = QueryType.STR_SORT_NAME;
						if( strTable.equalsIgnoreCase(GTblVal.TBL_MONUMENT))
							selectCols = new String [] { GTblVal.COL_NAME };
						else if( strTable.equalsIgnoreCase(GTblVal.TBL_DONOR))
							selectCols = new String [] { GTblVal.COL_NAME, GTblVal.COL_DON_ID };
						break;
					case(1):
						strSort = QueryType.STR_SORT_CAMPUS;
						selectCols = new String [] { GTblVal.COL_NAME, GTblVal.COL_CAMPUS };
						break;
					case(2):
						strSort = QueryType.STR_SORT_DATE;
						selectCols = new String [] { GTblVal.COL_NAME, GTblVal.COL_EST };
						break;
					case(3):
						strSort = QueryType.STR_SORT_DISTANCE;
						selectCols = new String [] { GTblVal.COL_NAME, GTblVal.COL_LATITUDE, GTblVal.COL_LONGITUDE };
						break;
				}	
				
				QueryType queryDesc = new QueryType( selectCols, strTable, strSort, null );
				((DirectoryActivity) mParent ).initDirectory(queryDesc);
			}
		});
		mBuilder.setView(mDialogView);
	}
	
	public void show(){
		Dialog dialog = mBuilder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
}