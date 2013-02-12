package com.bjerva.tsplex;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class SignListFragment extends ListFragment {

	private View myView;
	private MainActivity ma;
	SignAdapter mAdapter;
	
	private int index = -1;
    private int top = 0;
    private String oldSearch = "";
    
    //TODO: Spara gamla sökningen
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){
		myView = inflater.inflate(R.layout.sign_list_fragment, container, false);
		return myView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		ma = (MainActivity) getActivity();
		if(ma.gsonSignsLite != null){
			loadSigns();
		}
	}
	
	public void onResume(){
		super.onResume();
		ma.getSupportActionBar().show();
		if(index!=-1){
			this.getListView().setSelectionFromTop(index, top);
		}
		if(mAdapter != null){
			mAdapter.getFilter().filter(oldSearch);
		}
	}

	public void onPause(){
		super.onPause();
		try{
			index = this.getListView().getFirstVisiblePosition();
			View v = this.getListView().getChildAt(0);
			top = (v == null) ? 0 : v.getTop();
		} catch(Throwable t) {
			t.printStackTrace();
		}
		
		try{
			oldSearch = ma.getSearch().getText().toString();
		} catch(Throwable t) {
			oldSearch = "";
			t.printStackTrace();
		}
	}

	void loadSigns(){
		//Create and set adapter
		final List<SimpleGson> tmpSigns = new ArrayList<SimpleGson>();
		for(int i = 0, l = ma.gsonSignsLite.size(); i < l; i++){
			SimpleGson currSign = ma.gsonSignsLite.get(i);
			tmpSigns.add(currSign);
		}
		
		mAdapter = new SignAdapter(ma, android.R.layout.simple_list_item_1, tmpSigns);
		
		getListView().setAdapter(mAdapter);

		//Set listener
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				ma.showLoader();
				
				//Update position
				ma.loadSingleJson(tmpSigns.get(position).getId());//Integer.valueOf(String.valueOf(id));

				//Hide keyboard
				if(ma.getSearch() != null){
					InputMethodManager imm = (InputMethodManager) ma.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(ma.getSearch().getWindowToken(), 0);
				}
				
				if(ma.getDetFragment() == null){
					ma.getSupportActionBar().hide();
					//Create detail fragment
					SignDetailFragment newFragment = new SignDetailFragment();

					//Add to container
					FragmentTransaction transaction = ma.getSupportFragmentManager().beginTransaction();
					transaction.setCustomAnimations(R.anim.slide_fragment_in_on_replace, R.anim.slide_fragment_out_on_replace);
					transaction.replace(R.id.fragment_container, newFragment);
					transaction.addToBackStack(null);
					transaction.commit();
				} else {
					ma.getDetFragment().startUpHelper(ma.currentSign);
				}
			}
		});
	}
}
