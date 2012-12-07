package ru.vang.songoftheday;

import ru.vang.songoftheday.util.Settings;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class WidgetPreferenceActivity extends PreferenceActivity {

	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final PreferenceManager prefenceManager = getPreferenceManager();
		prefenceManager.setSharedPreferencesName(Settings.SHARED_PREF_NAME);
		addPreferencesFromResource(R.xml.preferences);
	}
}
