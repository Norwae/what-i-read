package com.github.norwae.whatiread;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.data.BookInfoListAdapter;
import com.github.norwae.whatiread.data.BookInfoListAdapter.OrderFields;

public class BrowseFragment extends Fragment {
	public static class SelectOrderFragment extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			ListView displayList = (ListView) getActivity().findViewById(
					R.id.bookList);
			BookInfoListAdapter adapter = (BookInfoListAdapter) displayList
					.getAdapter();
			int selected = adapter != null ? adapter.getOrder().ordinal() : 0;

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setSingleChoiceItems(R.array.order_field_names, selected,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							ListView displayList = (ListView) getActivity()
									.findViewById(R.id.bookList);
							BookInfoListAdapter adapter = (BookInfoListAdapter) displayList
									.getAdapter();

							if (adapter != null) {
								adapter.setOrder(OrderFields.values()[which]);
								displayList.invalidateViews();
							}
							dialog.dismiss();
						}
					});
			return builder.create();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_browse, container,
				false);

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

		View search = view.findViewById(R.id.searchTrigger);

		search.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				TextView textView = (TextView) view.findViewById(R.id.search);
				searchForText(textView.getText().toString());
			}
		});

		View order = view.findViewById(R.id.orderTrigger);

		order.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				SelectOrderFragment fragment = new SelectOrderFragment();
				fragment.show(getActivity().getFragmentManager(), "");
			}
		});
		return view;
	}

	private void searchForText(String text) {

		AsyncCallbackReceiver<List<BookInfo>, String> receiver = new ProgressBarDialogCallback<List<BookInfo>>(
				getView(), R.id.progressBar, R.id.searchTrigger,
				R.id.orderTrigger) {
			@Override
			protected void onAsyncResult(List<BookInfo> aResult) {
				Activity activity = getActivity();
				if (activity != null && aResult != null) {
					ListView list = getDisplayList();
					Log.d("search-result",
							"Updating list view with " + aResult.size()
									+ " Books");
					ListAdapter adapter = new BookInfoListAdapter(aResult);
					list.setAdapter(adapter);

					list.invalidate();
				}
			}
		};

		BookSearch query = new BookSearch(receiver, this.getActivity());

		query.execute(text);
	}

	protected ListView getDisplayList() {
		View list = getActivity().findViewById(R.id.bookList);
		return (ListView) list;
	}
}
