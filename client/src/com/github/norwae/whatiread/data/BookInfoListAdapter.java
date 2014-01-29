package com.github.norwae.whatiread.data;

import java.util.ArrayList;
import java.util.List;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.norwae.whatiread.R;
import com.github.norwae.whatiread.util.Strings;

public class BookInfoListAdapter extends BaseAdapter {

	private final List<BookInfo> data;
	private SparseArray<View> createdViews = new SparseArray<View>();

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


	private void initTextField(View src, CharSequence value, int id) {
		TextView view = (TextView) src.findViewById(id);
		view.setText(value);
	}
	
	@Override
	public View getView(int idx, View old, ViewGroup parent) {
		View view = createdViews.get(idx);
		if (view == null) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());

			view = inflater.inflate(R.layout.bookresult, parent, false);
			BookInfo info = data.get(idx);
			initTextField(view, info.getTitle(), R.id.resultTitle);
			initTextField(view, info.getSubtitle(), R.id.resultSubtitle);
			initTextField(view, Strings.join(", ", info.getAuthors()), R.id.resultAuthors);
			
			createdViews.put(idx, view);			
		}
		
		return view;
	}
}
