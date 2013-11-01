package com.github.norwae.whatiread;


import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.db.BookDatabase;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

public class DisplayBookActivity extends Activity {
	
	public static final String BOOK_INFO_VARIABLE = "EXTRA_BOOK_INFO";
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
	}

	protected void saveAndReturn() {
		TextView comment = (TextView) findViewById(R.id.comment);
		info.setComment(comment.getText().toString());

		
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
