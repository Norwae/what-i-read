package com.github.norwae.whatiread;

public interface AsyncCallbackReceiver<Result, Progress> {
	public void onAsyncComplete(Result anObject);
	public void onProgressReport(Progress... someProgress);
}
