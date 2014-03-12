package ru.vang.songoftheday.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import ru.vang.songoftheday.R;
import ru.vang.songoftheday.SongOfTheDaySettings;
import ru.vang.songoftheday.api.Vk;
import ru.vang.songoftheday.fragment.AuthFragment;
import ru.vang.songoftheday.preference.TimePreference;
import ru.vang.songoftheday.util.AlarmHelper;

public class WidgetPreferenceActivity extends PreferenceActivity {

    public static final String EXTRA_ENABLE_UPDATE_TOKEN
            = "ru.vang.songoftheday.activity.EXTRA_ENABLE_UPDATE_TOKEN";

    private PreferenceScreen mAddAccountPreference;

    @SuppressWarnings("deprecation")
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager
                .setSharedPreferencesName(SongOfTheDaySettings.SHARED_PREF_NAME);
        addPreferencesFromResource(R.xml.preferences);
        final Intent intent = getIntent();

        final Intent authIntent = new Intent(this, VkAuthActivity.class);
        authIntent.putExtra(AuthFragment.EXTRA_HIDE_SKIP_BUTTON, true);
        if (Vk.hasVkAccount(this)) {
            if (intent.hasExtra(EXTRA_ENABLE_UPDATE_TOKEN)) {
                final Preference updateTokenPreference = preferenceManager
                        .findPreference(getString(R.string.key_update_token));
                updateTokenPreference.setIntent(authIntent);
                updateTokenPreference.setEnabled(intent.getBooleanExtra(
                        EXTRA_ENABLE_UPDATE_TOKEN, false));
            }
        } else {
            mAddAccountPreference = preferenceManager
                    .createPreferenceScreen(this);
            mAddAccountPreference.setIntent(authIntent);
            mAddAccountPreference.setKey(getString(R.string.key_add_account));
            mAddAccountPreference.setTitle(R.string.title_add_account);
            getPreferenceScreen().addPreference(mAddAccountPreference);
        }

        final TimePreference timePreference = (TimePreference) preferenceManager
                .findPreference(getString(R.string.key_time));
        timePreference
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    public boolean onPreferenceChange(
                            final Preference preference, final Object newValue) {
                        final int[] timeParts = TimePreference.parseTime(String
                                .valueOf(newValue));
                        AlarmHelper.resetAlarm(WidgetPreferenceActivity.this,
                                timeParts[0], timeParts[1]);
                        return true;
                    }
                });
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onResume() {
        super.onResume();
        if (Vk.hasVkAccount(this) && mAddAccountPreference != null) {
            getPreferenceScreen().removePreference(mAddAccountPreference);
        }
    }
}
