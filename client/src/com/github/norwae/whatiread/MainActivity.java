package com.github.norwae.whatiread;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.data.ISBN13;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends FragmentActivity implements ProgressBarDisplayer {

	private static class PageAdapter extends FragmentPagerAdapter {
		public PageAdapter(FragmentManager manager) {
			super(manager);
		}

		@Override
		public Fragment getItem(int item) {
			switch (item) {
			case 0:
				return new AddOrCheckFragment();
			case 1:
				return new BrowseFragment();
			}
			return null;
		}

		@Override
		public int getCount() {
			return 2;
		}

	}

	private ViewPager viewPager;
	private PageAdapter pageAdapter;
	private ActionBar actionBar;
	
	private MainMenuHandler menuHandler = new MainMenuHandler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		viewPager = (ViewPager) findViewById(R.id.pager);
		pageAdapter = new PageAdapter(getSupportFragmentManager());
		actionBar = getActionBar();

		viewPager.setAdapter(pageAdapter);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setHomeButtonEnabled(false);

		TabListener tabListener = new TabListener() {

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			}

			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				viewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
			}
		};

		OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		};

		String[] titles = { getString(R.string.fragment_title_register),
				getString(R.string.fragment_title_browse) };

		for (String name : titles) {
			actionBar.addTab(actionBar.newTab().setText(name)
					.setTabListener(tabListener));
		}

		viewPager.setOnPageChangeListener(pageChangeListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {		
		return menuHandler.handleMenuSelected(this, item.getItemId()) || super.onMenuItemSelected(featureId, item);
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
		

		AsyncCallbackReceiver<BookInfo, String> tempCallback = new ProgressBarDialogCallback<BookInfo>(this) {
			@Override
			public void onAsyncResult(BookInfo anObject) {
				if (anObject != null) {
					displayBookInfo(anObject, true);
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
	
	@Override
	public ProgressBar getProgressBar() {
		return (ProgressBar) findViewById(R.id.progressBar);
	}
}
