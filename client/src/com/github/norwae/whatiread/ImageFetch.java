package com.github.norwae.whatiread;

import java.io.InputStream;
import java.net.URI;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class ImageFetch extends AsyncTask<URI, Void, Bitmap> {
	private final Activity owner;
	private final int viewId;
	
	public ImageFetch(Activity target, int imageView) {
		owner = target;
		viewId = imageView;
	}

	@Override
	protected Bitmap doInBackground(URI... args) {
		try {
			InputStream stream = args[0].toURL().openStream();
			
			return BitmapFactory.decodeStream(stream);
		} catch (Exception e) {
			Log.e("image-fetch", "failed to fetch image @" + args[0], e);
		} 
		return null;
	}
	
	@Override
	protected void onPostExecute(Bitmap result) {
		ImageView view = (ImageView) owner.findViewById(viewId);
		
		if (view != null){
			view.setImageBitmap(result);
		}
	}

}
