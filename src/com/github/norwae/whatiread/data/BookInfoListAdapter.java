package com.github.norwae.whatiread.data;

import java.util.ArrayList;
import java.util.List;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
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
	public Object getItem(int idx) {
		return data.get(idx).getTitle();
	}

	@Override
	public long getItemId(int idx) {
		return idx;
	}

	@Override
	public View getView(int idx, View old, ViewGroup parent) {
		if (old instanceof TextView) {
			return old;
		}
		
		return new TextView(parent.getContext());
	}
}
