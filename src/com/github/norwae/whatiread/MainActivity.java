package com.github.norwae.whatiread;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.github.norwae.whatiread.db.BookDatabase;
import com.github.norwae.whatiread.db.BookDatabaseHelper;
import com.github.norwae.whatiread.db.BookISBNLookup;
import com.github.norwae.whatiread.db.BookInfo;
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
import android.widget.Button;

public class MainActivity extends Activity {

	private static final String EAN_13_TYPE = "EAN_13";
	private BookDatabase bookDatabase;
	
	private Collection<AsyncTask<?, ?, ?>> backgroundTasks = new HashSet<AsyncTask<?, ?, ?>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		bookDatabase = new BookDatabase(this);
		
		setContentView(R.layout.activity_main);
		
		Button b = (Button) findViewById(R.id.scan);
		b.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				scanPressed();
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
		
		scanIntent.initiateScan(Collections.singleton(EAN_13_TYPE));
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (scanResult != null) {
			String code = scanResult.getContents();
			
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
			});
			
			lookup.execute(bookDatabase, code);
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

}
