package ru.vang.songoftheday;

import ru.vang.songoftheday.model.WidgetModel;
import ru.vang.songoftheday.util.Logger;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

public class SongOfTheDayWidget extends AppWidgetProvider {
	private static final String TAG = SongOfTheDayWidget.class.getSimpleName();

	@Override
	public void onUpdate(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		Logger.debug(TAG, "onUpdate is called");
		final Intent serviceIntent = new Intent(context, UpdateService.class);
		serviceIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		context.startService(serviceIntent);
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		super.onReceive(context, intent);

		final String action = intent.getAction();
		Logger.debug(TAG, "Action " + action + " is recieved");
		if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
			final Bundle extras = intent.getExtras();
			if (extras == null
					|| !extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
				onUpdate(context, null, null);
			}
		} else if (WidgetModel.ACTION_ADD.equals(action)) {
			intent.setComponent(new ComponentName(context, UpdateService.class));
			context.startService(intent);
		} else if (WidgetModel.ACTION_CANCEL.equals(action)) {
			final Intent stopServiceIntent = new Intent(context,
					UpdateService.class);
			context.stopService(stopServiceIntent);
		}
	}

	@Override
	public void onDisabled(final Context context) {
		Logger.debug(TAG, "onDisabled is called");
		super.onDisabled(context);
		context.stopService(new Intent(context, UpdateService.class));

		// Currently multiple instances isn't supported, thats why set completed
		// flag here
		final SharedPreferences preferences = context.getSharedPreferences(
				SongOfTheDaySettings.SHARED_PREF_NAME, Context.MODE_PRIVATE);
		final Editor editor = preferences.edit();
		editor.putBoolean(SongOfTheDaySettings.PREF_KEY_COMPLETED, false);
		editor.commit();
		// Logger.deleteLog();
	}
}
