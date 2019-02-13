package com.company.fluffiels.projectgram;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;


public class ImageFeedAdapter extends ArrayAdapter<ProfileImages> {
	
	private List<ProfileImages> list;
	private Activity activity;
	private String profileName;
	private int numOfPictures;
	private boolean[] didDownload;
	
	public ImageFeedAdapter(Activity activity, List<ProfileImages> list) {
		super(activity, R.layout.item_images, list);
		this.list = list;
		this.activity = activity;
	}
	
	static class ViewContainer {
		ImageView imageView;
		LinearLayout progressBar;
		ProgressBar imageLoadBar;
		ProgressBar imageLoadCircle;
		TextView lblImageLoad;
	}
	
	
	@NonNull
	@Override
	public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		View view = convertView;
		final ViewContainer viewContainer;
		if (view == null) {
			view = activity.getLayoutInflater().inflate(R.layout.item_images, parent, false);
			viewContainer = new ViewContainer();
			viewContainer.progressBar = view.findViewById(R.id.imageProgressLayout);
			viewContainer.imageView = view.findViewById(R.id.imgFeed);
			viewContainer.imageLoadBar = view.findViewById(R.id.imageLoadBar);
			viewContainer.imageLoadCircle = view.findViewById(R.id.imageLoadCircle);
			viewContainer.lblImageLoad = view.findViewById(R.id.lblImageLoad);
			view.setTag(viewContainer);
		} else
			viewContainer = (ViewContainer) view.getTag();
		viewContainer.progressBar.setVisibility(View.VISIBLE);
		if (list.get(position).getBytes() == null) {
			if (!didDownload[position]) {
				new AsyncTask<Void, Integer, byte[]>() {
					@Override
					protected byte[] doInBackground(Void... voids) {
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								viewContainer.imageLoadCircle.setVisibility(View.GONE);
								viewContainer.imageLoadBar.setVisibility(View.VISIBLE);
								viewContainer.lblImageLoad.setVisibility(View.VISIBLE);
								viewContainer.lblImageLoad.setText("Downloading image: 0%");
								viewContainer.imageLoadBar.setProgress(0);
							}
						});
						SharedPreferences prefs = ((Activatable) activity).getPrefs();
						String user = prefs.getString("user", "");
						String pass = prefs.getString("pass", "");
						String link = ServerConnection.getCurrentLink();
						String action = "action=getpic";
						String credentials = "&user=" + user + "&pass=" + pass;
						String passParam = "&profile=" + profileName + "&position=" + (numOfPictures - 1 - position);
						URL url;
						HttpURLConnection connection = null;
						InputStream inputStream = null;
						ByteArrayOutputStream arrayOutputStream = null;
						try {
							url = new URL(link + action + credentials + passParam);
							connection = (HttpURLConnection) url.openConnection();
							connection.setRequestMethod("POST");
							connection.setUseCaches(false);
							connection.setDoOutput(true);
							connection.setDoInput(true);
							connection.setRequestProperty("Content-Type", "image/jpg");
							connection.connect();
							arrayOutputStream = new ByteArrayOutputStream();
							inputStream = connection.getInputStream();
							byte[] bytes = new byte[4];
							int actuallyRead;
							int maxSize;
							int currentSize = 0;
							int percent = 1;
							inputStream.read(bytes);
							maxSize = ByteBuffer.wrap(bytes).getInt();
							bytes = new byte[2048];
							while ((actuallyRead = inputStream.read(bytes)) != -1) {
								arrayOutputStream.write(bytes, 0, actuallyRead);
								currentSize += actuallyRead;
								int temp = (currentSize * 100) / maxSize;
								if (temp > percent) {
									percent = temp;
									publishProgress(percent);
								}
							}
							bytes = arrayOutputStream.toByteArray();
							return bytes;
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							if (connection != null)
								connection.disconnect();
							if (inputStream != null) {
								try {
									inputStream.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							if (arrayOutputStream != null) {
								try {
									arrayOutputStream.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
						return null;
					}
					
					@Override
					protected void onProgressUpdate(Integer... percent) {
						viewContainer.imageLoadBar.setProgress(percent[0]);
						viewContainer.lblImageLoad.setText("Downloading image: " + percent[0] + "%");
					}
					
					@Override
					protected void onPostExecute(byte[] bytes) {
						viewContainer.lblImageLoad.setText("Download complete");
						list.get(position).setBytes(bytes);
						notifyDataSetChanged();
					}
				}.execute();
				didDownload[position] = true;
			}
			return view;
		}
		byte[] bytes = list.get(position).getBytes();
		Bitmap bitmap = getLittleBitmap(bytes);
		viewContainer.imageView.setImageBitmap(bitmap);
		viewContainer.progressBar.setVisibility(View.GONE);
		return view;
	}
	
	public void setNewProfile(String profileName, int numOfPictures) {
		this.profileName = profileName;
		this.numOfPictures = numOfPictures;
		didDownload = new boolean[numOfPictures];
		for (int i = 0; i < didDownload.length; i++) {
			didDownload[i] = false;
		}
		list.clear();
		for (int i = 0; i < numOfPictures; i++) {
			list.add(new ProfileImages());
		}
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return list.size();
	}
	
	private Bitmap getLittleBitmap(byte[] bytes) {
		int imageWidth = 800;
		int imageHeight = 800;
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bitmapOptions);
		int photoWidth = bitmapOptions.outWidth;
		int photoHeight = bitmapOptions.outHeight;
		int scaleFactor = Math.min(photoWidth / imageWidth, photoHeight / imageHeight);
		bitmapOptions.inJustDecodeBounds = false;
		bitmapOptions.inSampleSize = scaleFactor;
		Bitmap littleBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bitmapOptions);
		return littleBitmap;
	}
}
