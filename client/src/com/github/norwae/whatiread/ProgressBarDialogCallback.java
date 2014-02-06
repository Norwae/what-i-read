package com.github.norwae.whatiread;

import android.view.View;

public abstract class ProgressBarDialogCallback<Result> implements
		AsyncCallbackReceiver<Result, String> {
	
	private ProgressBarDisplayer source;
	private int steps = 0;

	public ProgressBarDialogCallback(ProgressBarDisplayer aDisplayer) {
		source = aDisplayer;
		source.getProgressBar().setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onAsyncComplete(Result anObject) {
		source.getProgressBar().setVisibility(View.INVISIBLE);
		onAsyncResult(anObject);
	}
	
	protected abstract void onAsyncResult(Result aResult);

	@Override
	public void onProgressReport(String... someProgress) {
		source.getProgressBar().setProgress(++steps);
	}
	
	

}
