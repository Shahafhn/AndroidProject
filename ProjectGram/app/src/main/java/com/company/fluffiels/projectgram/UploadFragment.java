package com.company.fluffiels.projectgram;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;

public class UploadFragment extends DialogFragment {
	
	private LinearLayout progressLayout;
	private ProgressBar progressBar;
	private SharedPreferences prefs;
	private TextView progressText;
	private ImageView imgView;
	private Button btnCancel;
	private Button btnUpload;
	private Bitmap bitmap;
	private Activatable activatable;
	private String photoPath;
	private AccountSearchFragment accountSearchFragment;
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.upload_layout, container, false);
		progressLayout = view.findViewById(R.id.uploadLayout);
		progressBar = view.findViewById(R.id.uploadBar);
		progressText = view.findViewById(R.id.lblUpload);
		imgView = view.findViewById(R.id.imgView);
		btnCancel = view.findViewById(R.id.btnCancel);
		btnUpload = view.findViewById(R.id.btnUpload);
		btnUpload.setOnClickListener(onClickListener);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Toast.makeText(getContext(), "Upload Canceled.", Toast.LENGTH_SHORT).show();
				activatable.setFAB(true);
				dismiss();
			}
		});
		int imageWidth = 300;
		int imageHeight = 300;
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(photoPath, bitmapOptions);
		int photoWidth = bitmapOptions.outWidth;
		int photoHeight = bitmapOptions.outHeight;
		int scaleFactor = Math.min(photoWidth / imageWidth, photoHeight / imageHeight);
		bitmapOptions.inJustDecodeBounds = false;
		bitmapOptions.inSampleSize = scaleFactor;
		Bitmap littleBitMap = BitmapFactory.decodeFile(photoPath, bitmapOptions);
		imgView.setImageBitmap(littleBitMap);
		return view;
	}
	
	private View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			btnCancel.setEnabled(false);
			btnUpload.setEnabled(false);
			progressLayout.setVisibility(View.VISIBLE);
			new AsyncTask<Void, Integer, Integer>() {
				@Override
				protected Integer doInBackground(Void... voids) {
					publishProgress(0);
					ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
					bitmap.compress(Bitmap.CompressFormat.JPEG, 50, arrayOutputStream);
					byte[] bytes = arrayOutputStream.toByteArray();
					String link = "http://"+MainActivity.currentIP+":8080/project?";
					String action = "action=upload";
					String user = prefs.getString("user", "");
					String pass = prefs.getString("pass", "");
					String credentials = "&user=" + user + "&pass=" + pass;
					URL url;
					HttpURLConnection connection = null;
					OutputStream uploadStream = null;
					InputStream inputStream = null;
					try {
						url = new URL(link + action + credentials);
						connection = (HttpURLConnection) url.openConnection();
						connection.setUseCaches(false);
						connection.setRequestMethod("POST");
						connection.setDoOutput(true);
						connection.setDoInput(true);
						connection.setRequestProperty("Content-Type", "application/octet-stream");
						connection.connect();
						uploadStream = connection.getOutputStream();
						int maxLength = bytes.length;
						int percent = 0;
						int from = 0;
						int to = 1024;
						while (from < maxLength) {
							if (from + to < maxLength) {
								uploadStream.write(bytes, from, to);
								from += 1024;
							} else {
								uploadStream.write(bytes, from, maxLength - from);
								from = maxLength;
							}
							int temp = (from * 100) / maxLength;
							if (temp > percent) {
								percent = temp;
								publishProgress(percent);
							}
						}
						return connection.getResponseCode();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (inputStream != null) {
							try {
								inputStream.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						if (connection != null) {
							connection.disconnect();
						}
						if (uploadStream != null) {
							try {
								uploadStream.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					return null;
				}
				
				@Override
				protected void onProgressUpdate(Integer... percent) {
					progressBar.setProgress(percent[0]);
					progressText.setText("Uploading picture: " + percent[0] + "%");
				}
				
				@Override
				protected void onPostExecute(Integer result) {
					if (result == 200)
						Toast.makeText(getContext(), "Photo uploaded successfully", Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(getContext(), "Failed to upload photo.", Toast.LENGTH_SHORT).show();
					String name = prefs.getString("user", "");
					int picsTaken = prefs.getInt(name + "PicsTaken", 0);
					prefs.edit().putInt(name + "PicsTaken", picsTaken + 1).apply();
					activatable.setFAB(true);
					accountSearchFragment.searchForAccounts(" ");
					dismiss();
				}
			}.execute();
		}
	};
	
	public void setEverything(Activatable activatable, Bitmap bitmap, SharedPreferences prefs, String photoPath,AccountSearchFragment accountSearchFragment) {
		this.accountSearchFragment = accountSearchFragment;
		this.activatable = activatable;
		this.bitmap = bitmap;
		this.prefs = prefs;
		this.photoPath = photoPath;
	}
}
