package ru.vang.songoftheday.util;

import ru.vang.songoftheday.R;
import ru.vang.songoftheday.SongOfTheDaySettings;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

public final class AvailabilityUtils {
	private static final int DEFAULT_WAITING_TIME = 5000;

	private AvailabilityUtils() {

	}

	public static boolean isConnectionForUpdateAvailable(final Context context) {
		boolean connectionnAvailable = false;
		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			final SharedPreferences preferences = context.getSharedPreferences(
					SongOfTheDaySettings.SHARED_PREF_NAME, Context.MODE_PRIVATE);
			final String wifiPreference = context.getString(R.string.wifi_value);
			final String networkPreference = preferences.getString(
					context.getString(R.string.network_type_key), wifiPreference);
			if (!networkPreference.equals(wifiPreference)
					|| networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				connectionnAvailable = true;
			}
		}

		return connectionnAvailable;
	}

	public static boolean isConnectionAvailable(final Context context) {
		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

		return networkInfo != null && networkInfo.isConnected();
	}

	public static CharSequence checkErrorState(final Context context,
			final boolean isAlarmUpdate) {
		final String status = Environment.getExternalStorageState();
		CharSequence errorState = null;
		final Resources resources = context.getResources();
		if (status.equals(Environment.MEDIA_SHARED)
				|| status.equals(Environment.MEDIA_UNMOUNTED)) {
			errorState = resources.getText(R.string.sdcard_busy_title_nosdcard);
		} else if (status.equals(Environment.MEDIA_REMOVED)) {
			errorState = resources.getText(R.string.sdcard_missing_title_nosdcard);
		}

		if (errorState == null
				&& !AvailabilityUtils.isConnectionForUpdateAvailable(context)) {
			if (isAlarmUpdate) {
				// Wait for 5 seconds while wi fi connecting
				try {
					Thread.sleep(DEFAULT_WAITING_TIME);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!AvailabilityUtils.isConnectionForUpdateAvailable(context)) {
					errorState = resources.getString(R.string.network_unavailable);
				}
			} else {
				errorState = resources.getString(R.string.network_unavailable);
			}
		}

		return errorState;
	}

}
