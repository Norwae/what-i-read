package com.github.norwae.whatiread.data;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BookInfoListAdapter extends BaseAdapter{
	
	private final List<BookInfo> data;

	public BookInfoListAdapter(List<BookInfo> data) {
		this.data = new ArrayList<BookInfo>(data);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public String getItem(int idx) {
		return data.get(idx).getTitle();
	}

	@Override
	public long getItemId(int idx) {
		return Long.parseLong(data.get(idx).getIsbn());
	}
	
	public BookInfo getInfoAt(int idx) {
		return data.get(idx);
	}

	@Override
	public View getView(int idx, View old, ViewGroup parent) {
		TextView view;
		if (old instanceof TextView) {
			view = (TextView) old;
		} else {		
			view = new TextView(parent.getContext());
		}
		
		view.setText(getItem(idx));
		
		return view;
	}
}
