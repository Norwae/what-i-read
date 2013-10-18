package com.github.norwae.whatiread;

import com.github.norwae.whatiread.db.BookInfo;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.widget.RatingBar;
import android.widget.TextView;

public class DisplayBookActivity extends Activity {
	
	public static final String BOOK_INFO_VARIABLE = "EXTRA_BOOK_INFO";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_display);
		
		initViewFields();
	}

	private void initViewFields() {
		BookInfo info = (BookInfo) getIntent().getExtras().get(BOOK_INFO_VARIABLE);
		
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
