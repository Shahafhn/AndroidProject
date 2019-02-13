package com.company.fluffiels.projectgram;


import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class ProfileSearchAdapter extends ArrayAdapter<UserProfile> {

	private Activity activity;
	private List<UserProfile> profilesList;
	private ImageFeedFragment imageFeed;
	private AccountSearchFragment accountSearchFragment;
	
	public ProfileSearchAdapter(Activity activity, List<UserProfile> list, ImageFeedFragment imageFeed){
		super(activity,R.layout.item_profile,list);
		this.activity = activity;
		this.profilesList = list;
		this.imageFeed = imageFeed;
	}
	
	static class ViewContainer{
		TextView lblNumOfImages;
		TextView lblUserName;
		Button button;
	}
	@Override
	public View getView(int position,View convertView,ViewGroup parent) {
		View view = convertView;
		if (profilesList.size() > 0) {
			final ViewContainer viewContainer;
			if (view == null) {
				view = activity.getLayoutInflater().inflate(R.layout.item_profile, parent, false);
				viewContainer = new ViewContainer();
				viewContainer.lblNumOfImages = view.findViewById(R.id.lblImagesNum);
				viewContainer.lblUserName = view.findViewById(R.id.lblName);
				viewContainer.button = view.findViewById(R.id.btnToProfile);
				viewContainer.button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						hideKeyboard(true);
						int tag = (int) view.getTag();
						((Activatable) activity).setTabItem(1);
						imageFeed.setAdapter(profilesList.get(tag).getUsername(),profilesList.get(tag).getNumOfImages());
					}
					private void hideKeyboard(boolean hide) {
						View view = viewContainer.lblUserName;
						InputMethodManager imm = (InputMethodManager) activity.getSystemService((Activity.INPUT_METHOD_SERVICE));
						if (!hide)
							imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
						else
							imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
					}
					
				});
				view.setTag(viewContainer);
			} else
				viewContainer = (ViewContainer) view.getTag();
			viewContainer.lblUserName.setText(profilesList.get(position).getUsername());
			viewContainer.lblNumOfImages.setText("images: " + profilesList.get(position).getNumOfImages());
			viewContainer.button.setTag(position);
		}
		return view;
	}
	
	public void setProfilesList(UserProfile[] searchResults){
		profilesList.clear();
		profilesList.addAll(Arrays.asList(searchResults));
		if (profilesList.size() == 0)
			accountSearchFragment.showNoResults(true);
		notifyDataSetChanged();
	}
	
	public void setParentFrag(AccountSearchFragment accountSearchFragment) {
		this.accountSearchFragment = accountSearchFragment;
	}
}

