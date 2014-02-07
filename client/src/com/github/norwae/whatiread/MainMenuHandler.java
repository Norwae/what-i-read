package com.github.norwae.whatiread;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;

public class MainMenuHandler {
	public boolean handleMenuSelected(Activity source, int id) {
		if (id == R.id.action_about) {
			new AlertDialog.Builder(source)
					.setMessage(R.string.about_text)
					.setNeutralButton(R.string.action_ok, null).show();

			return true;
		}
		
		if (id == R.id.action_settings) {
			Intent call = new Intent(source, SettingsActivity.class);
			source.startActivity(call);
			return true;
		}
		
		return false;
		
	}
}
