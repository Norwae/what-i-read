package com.github.norwae.whatiread;

import java.util.List;

import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.data.BookInfoListAdapter;
import com.github.norwae.whatiread.db.BookDBQuery;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BrowseFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {


		final View view = inflater.inflate(R.layout.fragment_browse, container, false);
		
		ListView list = (ListView) view.findViewById(R.id.bookList);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View itemView,
					int itemCount, long rowID) {
				BookInfo info = ((BookInfoListAdapter) adapter.getAdapter())
						.getInfoAt(itemCount);
				((MainActivity) getActivity()).displayBookInfo(info, false);
			}
		});
		
		Button trigger = (Button) view.findViewById(R.id.searchTrigger);
		
		trigger.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TextView textView = (TextView) view.findViewById(R.id.search);
				searchForText(textView.getText().toString());
			}
		});
		return view;
	}
	
	private void searchForText(String text) {
		
			final ProgressDialog progressDialog = ProgressDialog.show(
					getActivity(), getString(R.string.progress_pleaseWait),
					getString(R.string.progress_initial));

			AsyncCallbackReceiver<List<BookInfo>, String> receiver = new AsyncCallbackReceiver<List<BookInfo>, String>() {

				@Override
				public void onProgressReport(String... someProgress) {
					progressDialog.setMessage(someProgress[0]);
				}

				@Override
				public void onAsyncComplete(List<BookInfo> anObject) {
					ListView list = (ListView) getActivity().findViewById(R.id.bookList);
					Log.d("search-result", "Updating list view with "
							+ anObject.size() + " Books");
					ListAdapter adapter = new BookInfoListAdapter(
							anObject);
					list.setAdapter(adapter);
					list.invalidate();

					progressDialog.dismiss();
				}
			};
			BookDBQuery query = new BookDBQuery(receiver, this.getActivity());

			query.execute(text);
	}
}
