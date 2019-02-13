package com.company.fluffiels.projectgram;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	public static String currentIP = "79.181.241.172";
	private EditText edtUsername;
	private EditText edtPassword;
	private Button btnSignup;
	private Button btnLogin;
	private TextView errConnection;
	private CheckBox chkRemember;
	private LinearLayout progressBar;
	private SharedPreferences prefs;
	private Button btnChangeIP;
	private final String PREFS = "prefs";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		progressBar = findViewById(R.id.prgLayout);
		edtUsername = findViewById(R.id.edtUser);
		edtPassword = findViewById(R.id.edtPass);
		btnSignup = findViewById(R.id.btnSign);
		btnLogin = findViewById(R.id.btnLog);
		btnChangeIP = findViewById(R.id.btnIPChange);
		errConnection = findViewById(R.id.errConn);
		chkRemember = findViewById(R.id.chkRemember);
		prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
		currentIP = prefs.getString("IP", "79.181.241.172");
		chkRemember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				hideKeyboard(true);
			}
		});
		btnSignup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sendLoginInfo(true);
			}
		});
		btnLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sendLoginInfo(false);
			}
		});
		btnChangeIP.setOnClickListener(changeIPOnClickListener);
		checkIfRemember();
		edtUsername.requestFocus();
		hideKeyboard(false);
	}

	private View.OnClickListener changeIPOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			System.out.println("___________");
			hideKeyboard(false);
			ChangeIPFragment changeIPFragment = new ChangeIPFragment();
			changeIPFragment.setPrefs(prefs);
			changeIPFragment.setCancelable(false);
			changeIPFragment.show(getFragmentManager(),"");
		}
	};
	private void checkIfRemember() {
		String credentials = prefs.getString("remember", null);
		if (credentials != null) {
			String[] strings = credentials.split("&");
			String username = strings[0];
			String password = strings[1];
			if ((username != null && !username.isEmpty()) && (password != null && !password.isEmpty())) {
				edtUsername.setText(username);
				edtPassword.setText(password);
				chkRemember.setChecked(true);
				sendLoginInfo(false);
			}
		}
	}
	
	public void sendLoginInfo(final boolean isNewUser) {
		if (isInvalidInput(true, false, edtUsername, edtPassword)) {
			vibrate();
			return;
		}
		progressBar.setVisibility(View.VISIBLE);
		errConnection.setVisibility(View.GONE);
		hideKeyboard(true);
		final String username = edtUsername.getText().toString().toLowerCase();
		final String password = edtPassword.getText().toString();
		new AsyncTask<Void, Void, Integer>() {
			@Override
			protected Integer doInBackground(Void... voids) {
				return ServerConnection.login(isNewUser, username, password);
			}
			
			@Override
			protected void onPostExecute(Integer responseCode) {
				onResponseCode(responseCode);
			}
		}.execute();
	}
	
	private void vibrate(){
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			v.vibrate(VibrationEffect.createOneShot(100,VibrationEffect.DEFAULT_AMPLITUDE));
		}else
			v.vibrate(100);
	}
	
	private void onResponseCode(final int responseCode) {
		if (responseCode != ServerConnection.SUCCESS) {
			progressBar.setVisibility(View.GONE);
			hideKeyboard(false);
			switch (responseCode) {
				case ServerConnection.USER_EXISTS:
					edtUsername.requestFocus();
					edtUsername.setSelection(edtUsername.getText().toString().length());
					edtUsername.setError("Username already taken!");
					break;
				case ServerConnection.USER_NOT_FOUND:
					edtPassword.requestFocus();
					edtPassword.setSelection(edtPassword.getText().toString().length());
					edtPassword.setError("Username or Password incorrect!");
					break;
				case ServerConnection.NO_INTERNET:
					errConnection.setVisibility(View.VISIBLE);
					break;
			}
			prefs.edit().putString("remember", null).apply();
			vibrate();
		}else {
			String user = edtUsername.getText().toString().toLowerCase();
			String pass = edtPassword.getText().toString();
			if (chkRemember.isChecked()) {
				String credentials = user + "&" + pass;
				prefs.edit().putString("remember", credentials).apply();
			}
			Intent intent = new Intent(this, HomeActivity.class);
			prefs.edit().putString("user", user).putString("pass", pass).apply();
			startActivity(intent);
			finish();
		}
	}
	
	private void hideKeyboard(boolean hide) {
		View view = edtPassword;
		if (edtUsername.isFocused())
			view = edtUsername;
		InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService((Activity.INPUT_METHOD_SERVICE));
		if (!hide)
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		else
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
	private boolean isInvalidInput(boolean firstRecursion, boolean isInvalid, EditText firstEditText, EditText secondEditText) {
		String input = firstEditText.getText().toString().toLowerCase();
		String error = null;
		if (input.length() < 4)
			error = "Too short! Min 4 characters";
		else if (input.length() > 14)
			error = "Too long! Max 14 characters";
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c > 126 || c < 93)
				if (c > 91 || c < 64)
					if (c != 61 && c != 59)
						if (c > 57 || c < 48)
							if (c > 46 || c < 43)
								if (c > 41 || c < 35)
									if (c != 33)
										error = "No Hebrew, Spaces, or \\ / : * ? \" < > |";
		}
		if (error != null) {
			firstEditText.requestFocus();
		}
		firstEditText.setError(error);
		if (firstRecursion)
			return isInvalidInput(false, error == null, secondEditText, null);
		else
			return error != null || !isInvalid;
	}
}
