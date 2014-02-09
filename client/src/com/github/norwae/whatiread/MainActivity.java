package com.github.norwae.whatiread;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.data.BookInfoListAdapter;
import com.github.norwae.whatiread.data.BookInfoListAdapter.OrderFields;
import com.github.norwae.whatiread.data.ISBN13;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends Activity {

	private static final String EAN_13_TYPE = "EAN_13";

	public static class SelectOrderFragment extends DialogFragment implements
			DialogInterface.OnClickListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			ListView displayList = (ListView) getActivity().findViewById(
					R.id.bookList);
			BookInfoListAdapter adapter = (BookInfoListAdapter) displayList
					.getAdapter();
			int selected = adapter != null ? adapter.getOrder().ordinal() : 0;

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setSingleChoiceItems(R.array.order_field_names, selected,
					this);
			return builder.create();
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			ListView displayList = ((MainActivity) getActivity())
					.getDisplayList();

			BookInfoListAdapter adapter = (BookInfoListAdapter) displayList
					.getAdapter();

			if (adapter != null) {
				adapter.setOrder(OrderFields.values()[which]);
				displayList.invalidateViews();
			}
			dialog.dismiss();
		}
	}

	public static class InputISBNFragment extends DialogFragment {
		private EditText inputText;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			inputText = new EditText(getActivity());

			inputText.setInputType(InputType.TYPE_CLASS_NUMBER);
			inputText.setLines(1);
			inputText
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							13) });
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			builder.setMessage(getString(R.string.title_input_isbn))
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									String text = inputText.getText()
											.toString();
									ISBN13 isbn = ISBN13.parse(text);

									if (null != isbn) {
										((MainActivity) getActivity())
												.lookupISBN(isbn);
									} else {
										Toast.makeText(
												getActivity(),
												getString(R.string.invalid_isbn),
												Toast.LENGTH_SHORT).show();
									}
								}
							}).setNegativeButton(android.R.string.cancel, null);
			AlertDialog dialog = builder.create();
			dialog.setView(inputText);

			return dialog;
		}
	}

	private MainMenuHandler menuHandler = new MainMenuHandler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		ListView list = getDisplayList();
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View itemView,
					int itemCount, long rowID) {
				BookInfo info = ((BookInfoListAdapter) adapter.getAdapter())
						.getInfoAt(itemCount);
				displayBookInfo(info, false);
			}
		});

		View search = findViewById(R.id.searchTrigger);

		search.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				TextView textView = (TextView) findViewById(R.id.search);
				searchForText(textView.getText().toString());
			}
		});

		View order = findViewById(R.id.orderTrigger);

		order.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				SelectOrderFragment fragment = new SelectOrderFragment();
				fragment.show(getFragmentManager(), "");
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.start, menu);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_scan:
			scanPressed();
			return true;
		case R.id.action_input_isbn:
			InputISBNFragment fragment = new InputISBNFragment();
			fragment.show(getFragmentManager(), "input-isbn");
			return true;
		}

		return menuHandler.handleMenuSelected(this, item.getItemId())
				|| super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, data);
		if (scanResult != null) {
			String code = scanResult.getContents();
			ISBN13 isbn = ISBN13.parse(code);

			if (isbn != null) {
				lookupISBN(isbn);
			}
		}
	}

	void lookupISBN(ISBN13 isbn) {
		AsyncCallbackReceiver<BookInfo, String> tempCallback = new ProgressBarDialogCallback<BookInfo>(
				getWindow().getDecorView(), R.id.progressBar) {
			@Override
			public void onAsyncResult(BookInfo anObject) {
				if (anObject != null) {
					displayBookInfo(anObject, true);
				} else {
					Toast.makeText(MainActivity.this,
							getString(R.string.serverIOError),
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		BookISBNLookup lookup = new BookISBNLookup(tempCallback, this);

		lookup.execute(isbn);
	}

	void displayBookInfo(BookInfo anObject, boolean warnForExisting) {
		Intent call = new Intent(this, DisplayBookActivity.class);
		call.putExtra(DisplayBookActivity.BOOK_INFO_VARIABLE, anObject);
		call.putExtra(DisplayBookActivity.WARN_FOR_READ_BOOKS_VARIABLE,
				warnForExisting);
		startActivity(call);
	}

	void searchForText(String text) {

		AsyncCallbackReceiver<List<BookInfo>, String> receiver = new ProgressBarDialogCallback<List<BookInfo>>(
				getWindow().getDecorView(), R.id.progressBar,
				R.id.searchTrigger, R.id.orderTrigger) {
			@Override
			protected void onAsyncResult(List<BookInfo> aResult) {
				if (aResult != null) {
					ListView list = getDisplayList();
					Log.d("search-result",
							"Updating list view with " + aResult.size()
									+ " Books");
					ListAdapter adapter = new BookInfoListAdapter(aResult);
					list.setAdapter(adapter);

					list.invalidate();
				} else {
					Toast.makeText(MainActivity.this,
							getString(R.string.serverIOError),
							Toast.LENGTH_SHORT).show();

				}
			}
		};

		BookSearch query = new BookSearch(receiver, this);

		query.execute(text);
	}

	ListView getDisplayList() {
		View list = findViewById(R.id.bookList);
		return (ListView) list;
	}

	void scanPressed() {
		IntentIntegrator scanIntent = new IntentIntegrator(this);

		AlertDialog tempDialog = scanIntent.initiateScan(Collections
				.singleton(EAN_13_TYPE));
		if (tempDialog != null) {
			tempDialog.show();
		}
	}
}
