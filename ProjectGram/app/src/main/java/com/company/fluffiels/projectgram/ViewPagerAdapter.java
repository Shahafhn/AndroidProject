package com.company.fluffiels.projectgram;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter {
	private String[] titles = {"Settings","Image Feed","Search"};
	private Activatable activatable;
	private Activity activity;
	private ImageFeedFragment imageFeedFragment;
	
	private AccountSearchFragment accountSearchFragment;
	
	public ViewPagerAdapter(FragmentManager fm, Activity activity){
		super(fm);
		this.activity = activity;
		this.activatable = (Activatable) activity;
		imageFeedFragment = new ImageFeedFragment();
		imageFeedFragment.setActivity(activity);
		accountSearchFragment = new AccountSearchFragment();
		accountSearchFragment.setActivity(activity);
		accountSearchFragment.setImageFeed(imageFeedFragment);
	}
	
	@Override
	public Fragment getItem(int position) {
		Fragment fragment = null;
		switch (position) {
			case 0:
				SettingsFragment settingsFragment= new SettingsFragment();
				settingsFragment.setActivatable(activatable);
				fragment = settingsFragment;
				break;
			case 1:
				fragment = imageFeedFragment;
				break;
			case 2:
				fragment = accountSearchFragment;
				break;
		}
		return fragment;
	}
	
	
	public AccountSearchFragment getAccountSearchFragment() {
		return accountSearchFragment;
	}
	
	@Override
	public int getCount() {
		return 3;
	}
	
	@Nullable
	@Override
	public CharSequence getPageTitle(int position) {
		return titles[position];
	}
}
