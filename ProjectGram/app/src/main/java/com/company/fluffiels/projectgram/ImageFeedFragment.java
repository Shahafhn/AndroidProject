package com.company.fluffiels.projectgram;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ImageFeedFragment extends Fragment {
	
	private TextView textView;
	private ImageFeedAdapter imageFeedAdapter;
	private Activity activity;
	private ListView listView;

	public ImageFeedFragment(){
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_imagefeed,container,false);
		textView = view.findViewById(R.id.lblImageFeed);
		listView = view.findViewById(R.id.imgListView);
		listView.setAdapter(imageFeedAdapter);
		return view;
	}
	
	public void setActivity(Activity activity) {
		this.activity = activity;
		imageFeedAdapter = new ImageFeedAdapter(activity,new ArrayList<ProfileImages>());
	}
	
	public void setAdapter(String name,int numOfPictures){
		textView.setText(name);
		listView.smoothScrollToPosition(0);
		imageFeedAdapter.setNewProfile(name,numOfPictures);
	}
}
