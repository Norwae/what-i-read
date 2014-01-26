package com.github.norwae.whatiread.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import android.content.Context;
import android.util.Log;

import com.github.norwae.whatiread.R;
import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.data.ISBN13;
import com.github.norwae.whatiread.data.ISBNStorageProvider;

public class ServerStorage implements ISBNStorageProvider {

	private static final String WIR_SERVER_URL = "http://wir-server.appspot.com/volumes";
	private static final String TAG = "wir-server-storage";

	@Override
	public String getProgressMessage(Context sourceContext) {
		return sourceContext.getString(R.string.query_wirServer);
	}

	@Override
	public BookInfo getBookinfo(ISBN13 isbn, Context sourceActivity) {
		HttpGet get = new HttpGet(WIR_SERVER_URL + "/" + isbn.toString());

		String json = execute(get);
		
		if (json != null) {
			BookInfo result = new BookInfo();
			try {
				result.parseJSON(json);

				
				return result;
			} catch (JSONException e) {
				Log.e(TAG, "Parse error for JSON " + json, e);
			} catch (URISyntaxException e) {
				Log.e(TAG, "Malformed URI in JSON " + json, e);
			}
		}

		return null;
	}

	private String execute(HttpUriRequest request) {
		Log.d(TAG, "Executing " + request);

		try {
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream entity = new ByteArrayOutputStream();
				response.getEntity().writeTo(entity);

				return entity.toString("UTF-8");
			} else {
				Log.e(TAG, "call " + request + " failed with "
						+ response.getStatusLine().toString());
			}
		} catch (IOException e) {
			Log.e(TAG, "IO Error", e);
		}
		return null;
	}

	@Override
	public List<BookInfo> searchForTerm(List<String> terms,
			Context sourceActivity) {
		StringBuilder url = new StringBuilder(WIR_SERVER_URL);
		String sep = "?";
		for (String term : terms) {
			url.append(sep).append("search=").append(URLEncoder.encode(term));
			sep = "&";
		}
		
		HttpGet get = new HttpGet(url.toString());

		String json = execute(get);
		
		if (json != null) {
			try {
				List<BookInfo> result = BookInfo.parseJSONMulti(json);
				return result;				
			} catch (JSONException e) {
				Log.e(TAG, "Parse error for JSON " + json, e);
			} catch (URISyntaxException e) {
				Log.e(TAG, "Malformed URI in JSON " + json, e);
			}
		}

		return null;
	}

	@Override
	public boolean storeInfo(BookInfo info, Context sourceActivity) {
		HttpPut put = new HttpPut(WIR_SERVER_URL + "/" + info.getIsbn());
		try {
			put.setEntity(new StringEntity(info.quoteJSON(), "UTF-8"));
			
			String reply = execute(put);
			return reply != null;
		} catch (UnsupportedEncodingException e) {
			Log.wtf(TAG, "UTF-8 Unsupported? Unpossible!?!", e);
		} catch (JSONException e) {
			Log.e(TAG, "JSON encoding failed", e);
		}
		
		return false;
	}

	@Override
	public boolean deleteInfo(BookInfo info, Context sourceActivity) {
		HttpDelete delete = new HttpDelete(WIR_SERVER_URL + "/" + info.getIsbn());
		String reply = execute(delete);
		return reply != null;
	}

}
