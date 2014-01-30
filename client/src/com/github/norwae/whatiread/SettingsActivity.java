package com.github.norwae.whatiread;

import com.github.norwae.whatiread.server.ServerPreferences;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.serversettings);
		
		AccountManager accountManager = AccountManager.get(this);
		Account[] accounts = accountManager.getAccountsByType("com.google");

		if (accounts.length == 0) {
			return;
		}
		
		String[] names = new String[accounts.length];
		for (int i = 0; i < accounts.length; i++) {
			names[i] = accounts[i].name;
		}
		
		ListPreference list = (ListPreference) findPreference(ServerPreferences.ACCOUNT_NAME);
		list.setEntries(names);
		list.setEntryValues(names);
	}
}
