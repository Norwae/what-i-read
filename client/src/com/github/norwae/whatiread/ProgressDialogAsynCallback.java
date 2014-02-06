package com.github.norwae.whatiread;

import android.app.ProgressDialog;
import android.util.Log;

public class ProgressDialogAsynCallback<Result> implements
		AsyncCallbackReceiver<Result, String> {

	private final ProgressDialog progressDialog;

	public ProgressDialogAsynCallback(ProgressDialog progressDialog) {
		this.progressDialog = progressDialog;
	}

	@Override
	public void onProgressReport(String... someProgress) {
		progressDialog.setMessage(someProgress[0]);
	}

	@Override
	public void onAsyncComplete(Result anObject) {
		if (progressDialog.isShowing()) {
			try {
				progressDialog.dismiss();
			} catch (IllegalArgumentException e) {
				// activity exited or restated due to an orientation change. The
				// dialog may be stale even if it reports to be showing. Log
				// this, but don't raise a fuzz
				Log.w("progress callback", "stale dialog", e);
			}
		}
	}

}
