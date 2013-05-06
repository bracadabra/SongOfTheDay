package ru.vang.songoftheday.util;

import java.util.Calendar;

import ru.vang.songoftheday.R;
import ru.vang.songoftheday.SongOfTheDaySettings;
import ru.vang.songoftheday.SongOfTheDayWidget;
import ru.vang.songoftheday.preference.TimePreference;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.DateFormat;

public final class AlarmHelper {
	public static final String DATE_PATTERN = "dd-MM-yyyy hh:mm:ss a";
	public static final String EXTRA_ALARM_UPDATE = "ru.vang.songoftheday.EXTRA_ALARM_UPDATE";
	private static final String TAG = AlarmHelper.class.getSimpleName();	

	private AlarmHelper() {
	}

	public static void setAlarm(final Context context) {
		final Calendar calendar = Calendar.getInstance();
		setUpdateTimeFromPreferences(context, calendar);
		setAlarm(context, calendar);
	}

	public static void setAlaram(final Context context, final int hours, final int minutes) {
		final Calendar calendar = Calendar.getInstance();
		setUpdateTime(calendar, hours, minutes);
		setAlarm(context, calendar);
	}

	public static void setAlarm(final Context context, final Calendar calendar) {
		if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
			calendar.add(Calendar.DATE, 1);
		}

		final AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		final PendingIntent alarmIntent = createAlarmIntent(context);
		alarmManager
				.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);

		Logger.debug(
				TAG,
				"Alarm is set to: "
						+ DateFormat.format(DATE_PATTERN, calendar.getTimeInMillis()));
	}

	public static void cancelAlaram(final Context context) {
		final PendingIntent alarmIntent = createAlarmIntent(context);
		final AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(alarmIntent);
	}

	public static void resetAlarm(final Context context, final int hours,
			final int minutes) {
		cancelAlaram(context);
		setAlaram(context, hours, minutes);
	}

	private static void setUpdateTimeFromPreferences(final Context context,
			final Calendar calendar) {
		final SharedPreferences preferences = context.getSharedPreferences(
				SongOfTheDaySettings.SHARED_PREF_NAME, Context.MODE_PRIVATE);
		final String key = context.getString(R.string.key_time);
		final String time = preferences.getString(key,
				SongOfTheDaySettings.DEFAULT_UPDATE_TIME);
		final int[] timeParts = TimePreference.parseTime(time);
		setUpdateTime(calendar, timeParts[0], timeParts[1]);
	}

	private static void setUpdateTime(final Calendar calendar, final int hours,
			final int minutes) {
		calendar.set(Calendar.HOUR_OF_DAY, hours);
		calendar.set(Calendar.MINUTE, minutes);
		calendar.set(Calendar.SECOND, 0);
	}

	private static PendingIntent createAlarmIntent(final Context context) {
		final Intent intent = new Intent(context, SongOfTheDayWidget.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intent.putExtra(EXTRA_ALARM_UPDATE, true);
		// Add fake id to launch update. onUpdate won't called w/o widget id in
		// extra
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
				new int[] { AppWidgetManager.INVALID_APPWIDGET_ID });
		return PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}
}
