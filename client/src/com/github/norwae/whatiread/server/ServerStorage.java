package com.github.norwae.whatiread.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.client.methods.HttpDelete;
import org.json.JSONException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.github.norwae.whatiread.R;
import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.data.ISBN13;
import com.github.norwae.whatiread.data.ISBNStorageProvider;
import com.github.norwae.whatiread.util.IO;

public class ServerStorage implements ISBNStorageProvider {

	private static final String WIR_SERVER_URL = "https://wir-server.appspot.com/";
	private static final String WIR_VOLUMES_URL = WIR_SERVER_URL + "volumes";
	private static final String TAG = "wir-server-storage";

	static {
		CookieHandler.setDefault(new CookieManager());
	}

	@Override
	public String getProgressMessage(Activity sourceContext) {
		return sourceContext.getString(R.string.query_wirServer);
	}

	@Override
	public BookInfo getBookinfo(ISBN13 isbn, Activity sourceActivity) {
		try {
			HttpURLConnection get = (HttpURLConnection) new URL(WIR_VOLUMES_URL
					+ "/" + isbn.toString()).openConnection();

			String json = execute(get, sourceActivity, null);
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
		} catch (IOException e) {
			Log.e(TAG, "IO Error", e);
		}

		return null;
	}

	private String execute(HttpURLConnection request, Activity source,
			byte[] entity) throws IOException {

		try {
			initAuthCookie(source);

			if (entity != null) {
				request.setFixedLengthStreamingMode(entity.length);
				request.setDoInput(true);
				IO.copy(request.getOutputStream(), new ByteArrayInputStream(
						entity));
			}

			if (request.getResponseCode() == HttpURLConnection.HTTP_OK) {
				String contentEncoding = request.getContentEncoding();
				return new String(IO.read(request.getInputStream()),
						contentEncoding != null ? contentEncoding : "UTF-8");

			} else {
				Log.e(TAG,
						"call " + request + " failed with "
								+ request.getResponseCode());
			}
		} catch (OperationCanceledException e) {
			Log.e(TAG, "Authentication cancelled", e);
		} catch (AuthenticatorException e) {
			Log.e(TAG, "Could not log in", e);
		}
		return null;
	}

	private void initAuthCookie(Activity source)
			throws OperationCanceledException, AuthenticatorException,
			IOException {

		AccountManager accountManager = AccountManager.get(source);
		Account[] accounts = accountManager.getAccountsByType("com.google");

		if (accounts.length == 0) {
			return;
		}

		Account account = accounts[0];
		String authToken = buildToken(source, accountManager, account);

		accountManager.invalidateAuthToken(account.type, authToken);
		authToken = buildToken(source, accountManager, account);

		HttpURLConnection tempGet = (HttpURLConnection) new URL(WIR_SERVER_URL
				+ "_ah/login?continue=http://localhost/&auth="
				+ URLEncoder.encode(authToken)).openConnection();
		
		int responseCode = tempGet.getResponseCode();
		if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_MOVED_TEMP) {
			throw new  AuthenticatorException("Could not exchange token for cookie, code: " + responseCode);
		}
	}

	private String buildToken(Activity source, AccountManager accountManager,
			Account account) throws OperationCanceledException, IOException,
			AuthenticatorException {
		AccountManagerFuture<Bundle> accountManagerFuture = accountManager
				.getAuthToken(account, "ah", null, source, null, null);
		Bundle authTokenBundle = accountManagerFuture.getResult();
		String authToken = authTokenBundle.get(AccountManager.KEY_AUTHTOKEN)
				.toString();
		return authToken;
	}

	@Override
	public List<BookInfo> searchForTerm(List<String> terms,
			Activity sourceActivity) {
		StringBuilder url = new StringBuilder(WIR_VOLUMES_URL);
		String sep = "?";
		for (String term : terms) {
			url.append(sep).append("search=").append(URLEncoder.encode(term));
			sep = "&";
		}

		try {
			HttpURLConnection get = (HttpURLConnection) new URL(url.toString())
					.openConnection();

			String json = execute(get, sourceActivity, null);

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

		} catch (IOException e) {
			Log.e(TAG, "IO Error", e);
		}
		return null;
	}

	@Override
	public boolean storeInfo(BookInfo info, Activity sourceActivity) {
		try {
			byte[] json = info.quoteJSON().getBytes("UTF-8");
			HttpURLConnection put = (HttpURLConnection) new URL(WIR_VOLUMES_URL
					+ "/" + info.getIsbn()).openConnection();
			put.setRequestMethod("PUT");

			String reply = execute(put, sourceActivity, json);
			return reply != null;
		} catch (UnsupportedEncodingException e) {
			Log.wtf(TAG, "UTF-8 Unsupported? Unpossible!?!", e);
		} catch (JSONException e) {
			Log.e(TAG, "JSON encoding failed", e);
		} catch (IOException e) {
			Log.e(TAG, "IO Error", e);
		}

		return false;
	}

	@Override
	public boolean deleteInfo(BookInfo info, Activity sourceActivity) {
		HttpURLConnection delete;
		try {
			delete = (HttpURLConnection) new URL(WIR_VOLUMES_URL + "/"
					+ info.getIsbn()).openConnection();

			delete.setRequestMethod("DELETE");

			String reply = execute(delete, sourceActivity, null);
			return reply != null;
		} catch (IOException e) {
			Log.e(TAG, "IO Error", e);
		}

		return false;
	}

}
