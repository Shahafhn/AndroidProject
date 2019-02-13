package com.company.fluffiels.projectgram;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SettingsFragment extends Fragment {
	
	private Activatable activatable;
	
	public SettingsFragment(){
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		 View view = inflater.inflate(R.layout.activity_settings,container,false);
		
		Button button = view.findViewById(R.id.btnLogout);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				activatable.doLogout();
			}
		});
		
		return view;
	}
	public void setActivatable(Activatable activatable){
		this.activatable = activatable;
	}
}
