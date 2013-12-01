package com.github.norwae.whatiread;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.github.norwae.whatiread.data.BookInfo;

public class DisplayBookActivity extends Activity {
	
	public static final String BOOK_INFO_VARIABLE = "EXTRA_BOOK_INFO";
	public static final String WARN_FOR_READ_BOOKS_VARIABLE = "EXTRA_DISPLAY_READ_WARNING";
	
	private BookInfo info;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_display);
		info = (BookInfo) getIntent().getExtras().get(BOOK_INFO_VARIABLE);
		
		initViewFields();
		
		Button save = (Button) findViewById(R.id.save);
		save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				saveAndReturn();
			}
		});
		
		Button delete = (Button) findViewById(R.id.delete);
				
		delete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				deleteAndReturn();
			}
		});
		
		if (getIntent().getExtras().getBoolean(WARN_FOR_READ_BOOKS_VARIABLE) && !info.isAddition()) {
			AlertDialog alert = new AlertDialog.Builder(this).setMessage(getString(R.string.alert_alreadyKnown))
					.setNegativeButton(R.string.action_ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					})
					.setPositiveButton(R.string.action_edit, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// remain in the mask
						}
					})
					.create();
			alert.show();
		}
	}
	
	protected void deleteAndReturn() {

		final ProgressDialog progressDialog = ProgressDialog.show(this, getString(R.string.progress_pleaseWait), getString(R.string.progress_initial));
		
		BookDelete delete = new BookDelete(this, new AsyncCallbackReceiver<Void, String>() {
			@Override
			public void onProgressReport(String... someProgress) {
				progressDialog.setMessage(someProgress[0]);
			}		
			
			@Override
			public void onAsyncComplete(Void anObject) {
				if (progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				
				finish();
			}
		});
		
		delete.execute(info);
	}

	protected void saveAndReturn() {
		TextView input = (TextView) findViewById(R.id.comment);
		info.setComment(stringFromView(input));
		input = (TextView) findViewById(R.id.author);
		info.setAuthor(stringFromView(input));
		input = (TextView) findViewById(R.id.title);
		info.setTitle(stringFromView(input));
		

		
		final ProgressDialog progressDialog = ProgressDialog.show(this, getString(R.string.progress_pleaseWait), getString(R.string.progress_initial));
		

		BookPersistance persist = new BookPersistance(this, new AsyncCallbackReceiver<Void, String>() {
			@Override
			public void onProgressReport(String... someProgress) {
				progressDialog.setMessage(someProgress[0]);
			}		
			
			@Override
			public void onAsyncComplete(Void anObject) {
				if (progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				
				finish();
			}
		});
		
		persist.execute(info);
	}

	private String stringFromView(TextView input) {
		CharSequence text = input.getText();
		return text != null ? text.toString() : null;
	}

	private void initViewFields() {
		
		
		TextView textField = (TextView) findViewById(R.id.author);
		textField.setText(info.getAuthor());
		
		textField = (TextView) findViewById(R.id.title);
		textField.setText(info.getTitle());
		
		textField = (TextView) findViewById(R.id.isbn);
		textField.setText(info.getEan13());
		
		textField = (TextView) findViewById(R.id.firstRead);
		textField.setText(DateFormat.getDateFormat(this).format(info.getFirstView()));
		
		textField = (TextView) findViewById(R.id.comment);
		textField.setText(info.getComment());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
