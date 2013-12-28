package com.github.norwae.whatiread;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AddOrCheckFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_add_or_check, container, false);
		
		/*
		Button b = (Button) findViewById(R.id.scan);
		b.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				scanPressed();
			}
		});

		b = (Button) findViewById(R.id.searchTrigger);
		b.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				EditText searchBar = (EditText) findViewById(R.id.search);
				String text = searchBar.getText().toString();

				searchForText(text);
			}
		});

		ListView list = (ListView) findViewById(R.id.bookList);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View itemView,
					int itemCount, long rowID) {
				BookInfo info = ((BookInfoListAdapter) adapter.getAdapter())
						.getInfoAt(itemCount);
				displayBookInfo(info, false);
			}
		});*/
        
        return rootView;
	}
}
