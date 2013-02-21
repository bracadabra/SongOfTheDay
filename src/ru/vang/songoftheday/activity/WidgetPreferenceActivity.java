package ru.vang.songoftheday.activity;

import ru.vang.songoftheday.R;
import ru.vang.songoftheday.SongOfTheDaySettings;
import ru.vang.songoftheday.preference.TimePreference;
import ru.vang.songoftheday.util.AlarmHelper;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class WidgetPreferenceActivity extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final PreferenceManager prefenceManager = getPreferenceManager();
		prefenceManager.setSharedPreferencesName(SongOfTheDaySettings.SHARED_PREF_NAME);
		addPreferencesFromResource(R.xml.preferences);

		final TimePreference timePreference = (TimePreference) prefenceManager
				.findPreference(getString(R.string.time_key));
		timePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(final Preference preference,
					final Object newValue) {
				final int[] timeParts = TimePreference.parseTime(String.valueOf(newValue));
				AlarmHelper.resetAlarm(WidgetPreferenceActivity.this, timeParts[0],
						timeParts[1]);
				return true;
			}
		});
	}
}
