package com.company.fluffiels.projectgram;

import android.content.SharedPreferences;

public interface Activatable {
	void doLogout();
	void setTabItem(int position);
	void setFAB(boolean enabled);
	SharedPreferences getPrefs();
}
