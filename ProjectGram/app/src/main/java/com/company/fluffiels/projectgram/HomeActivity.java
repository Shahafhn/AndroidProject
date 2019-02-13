package com.company.fluffiels.projectgram;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;

public class HomeActivity extends AppCompatActivity implements Activatable {
	
	public static final int REQUEST_CODE = 73;
	private ViewPager viewPager;
	private ViewPagerAdapter viewPagerAdapter;
	private SharedPreferences prefs;
	private final String PREFS = "prefs";
	private TabLayout tabLayout;
	private FloatingActionButton fab;
	private String photoPath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_appbar);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
		viewPager = findViewById(R.id.viewPager);
		tabLayout = findViewById(R.id.tabsLayout);
		viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), this);
		viewPager.setAdapter(viewPagerAdapter);
		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				if (position != 2) {
					View view = viewPager;
					hideKeyboard(view, false);
				}
			}
			
			@Override
			public void onPageSelected(int position) {
				if (position == 2){
					EditText editText = findViewById(R.id.edtSearch);
					editText.requestFocus();
					//hideKeyboard(editText,true);
				}
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
		viewPager.setCurrentItem(1, false);
		tabLayout.setupWithViewPager(viewPager);
		fab = findViewById(R.id.fabPic);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				if (photoIntent.resolveActivity(getPackageManager()) != null) {
					fab.setEnabled(false);
					setTabItem(1);
					File externalDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
					String name = prefs.getString("user", "ProjectGram");
					int picturesTaken = prefs.getInt(name + "picsTaken", 0);
					try {
						File file = File.createTempFile(name + "(" + picturesTaken + ")",".jpg",externalDir);
						photoPath = file.getAbsolutePath();
						Uri photoUri = FileProvider.getUriForFile(HomeActivity.this, "com.company.fluffiels.projectgram.fileprovider", file);
						photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
						startActivityForResult(photoIntent, REQUEST_CODE);
					} catch (IOException e) {
						Toast.makeText(HomeActivity.this, "Failed to start camera.", Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	public void hideKeyboard(View view,boolean show){
		InputMethodManager imm = (InputMethodManager) getSystemService((Activity.INPUT_METHOD_SERVICE));
		if (show)
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		else
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		File file = new File(photoPath);
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			try {
				final Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));
				if (bitmap != null) {
					UploadFragment uploadFragment = new UploadFragment();
					uploadFragment.setEverything(this,bitmap,prefs,photoPath,viewPagerAdapter.getAccountSearchFragment());
					uploadFragment.setCancelable(false);
					uploadFragment.show(getFragmentManager(),"");
				}else {
					fab.setEnabled(true);
					Toast.makeText(this, "Failed to retrieve picture!", Toast.LENGTH_SHORT).show();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			file.delete();
			fab.setEnabled(true);
			Toast.makeText(this, "Failed to get picture.", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	@Override
	public void doLogout() {
		prefs.edit().putString("remember", null).apply();
		clearPrefs();
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
	
	@Override
	public void setFAB(boolean enabled) {
		fab.setEnabled(enabled);
	}
	
	@Override
	public void setTabItem(int position) {
		viewPager.setCurrentItem(position, true);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		clearPrefs();
	}
	
	private void clearPrefs() {
		prefs.edit().putString("user", null).putString("pass", null).apply();
	}
	
	@Override
	public SharedPreferences getPrefs() {
		return prefs;
	}
}
