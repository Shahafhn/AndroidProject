package com.company.fluffiels.projectgram;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class ChangeIPFragment extends DialogFragment {
	
	private EditText editText;
	private Button btnChange;
	private Button btnCancel;
	private SharedPreferences prefs;
	
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_changeip,container,false);
		editText = view.findViewById(R.id.edtChange);
		btnChange = view.findViewById(R.id.btnChange);
		btnCancel = view.findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});
		btnChange.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String text = editText.getText().toString();
				MainActivity.currentIP = text;
				prefs.edit().putString("IP",text).apply();
				dismiss();
			}
		});
		
		
		
		return view;
	}
	
	public void setPrefs(SharedPreferences prefs) {
		this.prefs = prefs;
	}
}
