package com.company.fluffiels.projectgram;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

public class AccountSearchFragment extends Fragment {
	
	private EditText edtSearch;
	private ListView listView;
	private Activity activity;
	private ImageFeedFragment imageFeed;
	private ProfileSearchAdapter profileSearchAdapter;
	private LinearLayout progressSearch;
	private LinearLayout noResults;
	private LinearLayout noInternet;
	private SharedPreferences prefs;
	
	public AccountSearchFragment() {
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_search, container, false);
		edtSearch = view.findViewById(R.id.edtSearch);
		listView = view.findViewById(R.id.lstView);
		progressSearch = view.findViewById(R.id.searchProgress);
		noResults = view.findViewById(R.id.layResultsFound);
		noInternet = view.findViewById(R.id.layInternetCon);
		showProgressView(false);
		prefs = ((Activatable)activity).getPrefs();
		edtSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}
			
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				final String search = charSequence.toString();
					searchForAccounts(search);
			}
			
			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
		profileSearchAdapter = new ProfileSearchAdapter(activity, new ArrayList<UserProfile>(), imageFeed);
		profileSearchAdapter.setParentFrag(this);
		listView.setAdapter(profileSearchAdapter);
		searchForAccounts("");
		return view;
	}
	
	public void searchForAccounts(final String text){
		showProgressView(true);
		showNoResults(false);
		new Thread(){
			UserProfile[] userProfiles;
			@Override
			public void run() {
				getSearchStrings(text);
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						profileSearchAdapter.setProfilesList(userProfiles);
						showNoInternet(userProfiles == null);
						showProgressView(false);
					}
				});
			}
			
			public void getSearchStrings(String text) {
				String search = " ";
				if (text.length() > 0)
					search = text;
				String user = prefs.getString("user", "");
				String pass = prefs.getString("pass", "");
				userProfiles =ServerConnection.getUsersStrings(user, pass, search);
			}
		}.start();
	}
	
	public void setActivity(Activity activity) {
		this.activity = activity;
	}
	public void setImageFeed(ImageFeedFragment imageFeed) {
		this.imageFeed = imageFeed;
	}
	public void showProgressView(boolean show) {
		progressSearch.setVisibility(show ? View.VISIBLE : View.GONE);
	}
	public void showNoResults(boolean show){
		noResults.setVisibility(show ? View.VISIBLE : View.GONE);
	}
	public void showNoInternet(boolean show){
		noInternet.setVisibility(show ? View.VISIBLE : View.GONE);
	}
	
}
