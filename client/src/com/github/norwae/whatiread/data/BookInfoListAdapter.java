package com.github.norwae.whatiread.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.norwae.whatiread.R;
import com.github.norwae.whatiread.data.BookInfoListAdapter.OrderFields;
import com.github.norwae.whatiread.util.Strings;

public class BookInfoListAdapter extends BaseAdapter {
	public static enum OrderFields implements Comparator<BookInfo>{
		TITLE() {
			@Override
			public int compare(BookInfo left, BookInfo right) {
				return left.getTitle().compareTo(right.getTitle());
			}
		},
		SUBTITLE() {
			@Override
			public int compare(BookInfo left, BookInfo right) {
				return left.getSubtitle().compareTo(right.getSubtitle());
			}
		},
		AUTHOR() {
			@Override
			public int compare(BookInfo left, BookInfo right) {
				if (left.getAuthors().isEmpty()) {
					return right.getAuthors().isEmpty() ? 0 : -1;
				}
				
				if (right.getAuthors().isEmpty()) {
					return 1;
				}
				
				String leftName = left.getAuthors().get(0);
				String rightName = right.getAuthors().get(0);
				
				int space = leftName.lastIndexOf(' ');
				if (space >= 0) {
					leftName = leftName.substring(space + 1);
				}
				
				space = rightName.lastIndexOf(' ');
				if (space >= 0) {
					rightName = rightName.substring(space + 1);
				}
				
				return leftName.compareTo(rightName);
			}
		},
		SERIES() {
			@Override
			public int compare(BookInfo left, BookInfo right) {
				return left.getSeries().compareTo(right.getSeries());
			}
		},
		PUBLISHER() {
			@Override
			public int compare(BookInfo left, BookInfo right) {
				return left.getPublisher().compareTo(right.getPublisher());
			}
		}

		
	}

	private final List<BookInfo> data;
	private final SparseArray<View> createdViews = new SparseArray<View>();
	
	private OrderFields order = OrderFields.TITLE;
	
	public BookInfoListAdapter(List<BookInfo> data) {
		Collections.sort(data, OrderFields.TITLE);
		this.data = new ArrayList<BookInfo>(data);
	}
	
	public void setOrder(OrderFields newValue) {
		order = newValue;
		Collections.sort(data, order);
		createdViews.clear();
	}

	public OrderFields getOrder() {
		return order;
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
