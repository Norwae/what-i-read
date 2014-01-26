package com.github.norwae.whatiread;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;

import com.github.norwae.whatiread.data.ISBNStorageProvider;
import com.github.norwae.whatiread.server.ServerStorage;

public abstract class StorageInteraction<Params, Progress, Result> extends
		CallbackAsync<Params, Progress, Result> {

	protected final List<? extends ISBNStorageProvider> allProviders = Arrays.asList(
				new ServerStorage()
		);
	protected final Activity context;

	public StorageInteraction(AsyncCallbackReceiver<Result, Progress> receiver, Activity origin) {
		super(receiver);
		context = origin;
	}

}