package com.github.norwae.whatiread;

import java.util.Collections;

import com.github.norwae.whatiread.db.BookDatabase;
import com.github.norwae.whatiread.db.BookInfo;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	private static final String EAN_13_TYPE = "EAN_13";

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
			BookInfo info = BookDatabase.getForEAN13(code);
			
			if (info != null) {
				Intent transfer = new Intent(this, DisplayBookActivity.class);
				transfer.putExtra(DisplayBookActivity.BOOK_INFO_VARIABLE, info);
				startActivity(transfer);
			}			
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

}
