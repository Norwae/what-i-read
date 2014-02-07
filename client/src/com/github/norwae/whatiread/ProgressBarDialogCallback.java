package com.github.norwae.whatiread;

import android.view.View;

public abstract class ProgressBarDialogCallback<Result> implements
		AsyncCallbackReceiver<Result, String> {

	private View source;

	private int progress;
	private int[] buttons;

	public ProgressBarDialogCallback(View root, int progressId,
			int... buttonIDs) {
		source = root;

		progress = progressId;
		buttons = buttonIDs;
		
		
		View progress = source.findViewById(progressId);
		if (progress != null) {
			progress.setVisibility(View.VISIBLE);
		}

		for (int next : buttons) {
			View button = source.findViewById(next);
			
			if (button != null) {
				button.setEnabled(false);
			}
		}
	}

	@Override
	public void onAsyncComplete(Result anObject) {
		View progressBar = source.findViewById(progress);
		if (progressBar != null) {
			progressBar.setVisibility(View.INVISIBLE);
		}

		for (int next : buttons) {
			View button = source.findViewById(next);
			
			if (button != null) {			
				button.setEnabled(true);
			}
		}
		onAsyncResult(anObject);
	}

	protected abstract void onAsyncResult(Result aResult);

	@Override
	public void onProgressReport(String... someProgress) {
	}

}
