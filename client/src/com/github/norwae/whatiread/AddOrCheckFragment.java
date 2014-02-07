package com.github.norwae.whatiread;

import java.util.Collections;

import android.app.AlertDialog;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.norwae.whatiread.data.ISBN13;
import com.google.zxing.integration.android.IntentIntegrator;

public class AddOrCheckFragment extends Fragment {


	private static final String EAN_13_TYPE = "EAN_13";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_add_or_check, container, false);
		
		Button b = (Button) view.findViewById(R.id.scan);
		b.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				scanPressed();
			}
		});

		b = (Button) view.findViewById(R.id.add_isbn);
		b.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				EditText searchBar = (EditText) view.findViewById(R.id.isbn);
				String text = searchBar.getText().toString();

				ISBN13 isbn = ISBN13.parse(text);
				
				if (isbn != null) {
					((MainActivity) getActivity()).lookupISBN(isbn, getView().getRootView());
				} else {
					Toast.makeText(getActivity(), R.string.invalid_isbn, Toast.LENGTH_SHORT).show();
				}
			}
		});
        
        return view;
	}
	

	
	public void scanPressed() {
		IntentIntegrator scanIntent = new IntentIntegrator(getActivity());

		AlertDialog tempDialog = scanIntent.initiateScan(Collections
				.singleton(EAN_13_TYPE));
		if (tempDialog != null) {
			tempDialog.show();
		}
	}
}
