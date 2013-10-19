package com.github.norwae.whatiread;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.data.BookInfoListAdapter;
import com.github.norwae.whatiread.data.ISBN13;
import com.github.norwae.whatiread.db.BookDBQuery;
import com.github.norwae.whatiread.db.BookDatabase;
import com.github.norwae.whatiread.db.BookDatabaseHelper;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.textservice.TextInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {

	private static final String EAN_13_TYPE = "EAN_13";
	
	private Collection<AsyncTask<?, ?, ?>> backgroundTasks = new HashSet<AsyncTask<?, ?, ?>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		setContentView(R.layout.activity_main);
		
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
	}
	
	@Override
	protected void onDestroy() {
		for (AsyncTask<?, ?, ?> temp : backgroundTasks) {
			temp.cancel(false);
		}
		
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void scanPressed() {
		IntentIntegrator scanIntent = new IntentIntegrator(this);
		
		AlertDialog tempDialog = scanIntent.initiateScan(Collections.singleton(EAN_13_TYPE));
		if (tempDialog != null) {
			tempDialog.show();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (scanResult != null) {
			String code = scanResult.getContents();
			ISBN13 isbn = ISBN13.parse(code);
			
			if (isbn != null) {
				lookupISBN(isbn);
			}
			
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void lookupISBN(ISBN13 isbn) {
		final ProgressDialog progressDialog = ProgressDialog.show(this, getString(R.string.progress_pleaseWait), getString(R.string.progress_initial));
		
		
		BookISBNLookup lookup = new BookISBNLookup(new AsyncCallbackReceiver<BookInfo, String>() {
			
			@Override
			public void onAsyncComplete(BookInfo anObject) {
				progressDialog.dismiss();
				if (anObject != null) {
					Intent tempIntent = new Intent(MainActivity.this, DisplayBookActivity.class);
					tempIntent.putExtra(DisplayBookActivity.BOOK_INFO_VARIABLE, anObject);
					startActivity(tempIntent);
				}
			}

			@Override
			public void onProgressReport(String... someProgress) {
				progressDialog.setMessage(someProgress[0]);
			}				
		}, this);
		
		lookup.execute(isbn);
	}

	private void searchForText(String text) {
		ISBN13 isbn = ISBN13.parse(text);
		
		if (isbn != null) {
			lookupISBN(isbn);
		} else {
			final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this, getString(R.string.progress_pleaseWait), getString(R.string.progress_initial));
			
			BookDBQuery query = new BookDBQuery(new AsyncCallbackReceiver<List<BookInfo>, String>() {
				
				@Override
				public void onProgressReport(String... someProgress) {
					progressDialog.setMessage(someProgress[0]);
				}
				
				@Override
				public void onAsyncComplete(List<BookInfo> anObject) {
					ListView list = (ListView) findViewById(R.id.bookList);
					ListAdapter adapter = new BookInfoListAdapter(anObject);
					list.setAdapter(adapter);
				}
			}, this);
			
			query.execute(text);
		}
	}

}
