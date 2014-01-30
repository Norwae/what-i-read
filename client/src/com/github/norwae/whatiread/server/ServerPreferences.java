package com.github.norwae.whatiread.server;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ServerPreferences {
	public static final String ACCOUNT_NAME = "account_name";
	public static final String HTTPS = "https";
	public static final String SERVER_URL = "server_url";

	private static final String WIR_SERVER_URL = "wir-server.appspot.com";

	private String protocol;
	private String url;
	private Account account;

	public ServerPreferences(Activity owner) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(owner);
		protocol = prefs.getBoolean(HTTPS, true) ? "https" : "http";
		url = prefs.getString(SERVER_URL, WIR_SERVER_URL);

		String accountName = prefs.getString(ACCOUNT_NAME, null);

		AccountManager manager = AccountManager.get(owner);
		Account[] accounts = manager.getAccountsByType("com.google");

		if (accounts.length > 0) {
			if (accountName == null) {
				account = accounts[0];
			} else {
				for (Account temp : accounts) {
					if (temp.name.equals(accountName)) {
						account = temp;
						return;
					}
				}
			}
		}

	}

	public String getServerURL() {
		return protocol + "://" + url + "/";
	}

	public Account getAccount() {
		return account;
	}
}
