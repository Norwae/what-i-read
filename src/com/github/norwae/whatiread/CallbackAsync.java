package com.github.norwae.whatiread;

import android.os.AsyncTask;

public abstract class CallbackAsync<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

	private final AsyncCallbackReceiver<Result, Progress> callback;

	protected CallbackAsync(AsyncCallbackReceiver<Result, Progress> receiver) {
		callback = receiver;
	}

	@Override
	protected void onPostExecute(Result result) {
		callback.onAsyncComplete(result);
	}
	
	@Override
	protected void onProgressUpdate(Progress... values) {
		callback.onProgressReport(values);
	}
}
