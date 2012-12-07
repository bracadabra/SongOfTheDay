package ru.vang.songoftheday;

import ru.vang.songoftheday.model.WidgetModel;
import ru.vang.songoftheday.util.Logger;
import ru.vang.songoftheday.util.Settings;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SongOfTheDayWidget extends AppWidgetProvider {
	private static final String TAG = SongOfTheDayWidget.class.getSimpleName();

	@Override
	public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		startUpdate(context);
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		super.onReceive(context, intent);

		final String action = intent.getAction();
		Logger.debug(TAG, "Action " + action + " is recieved");
		if (WidgetModel.ACTION_UPDATE.equals(action) || WidgetModel.ACTION_ADD.equals(action)) {
			intent.setComponent(new ComponentName(context, UpdateService.class));
			context.startService(intent);
		} else if (WidgetModel.ACTION_CANCEL.equals(action)) {
			UpdateService.sIsCancelled = true;
		}
	}

	private void startUpdate(final Context context) {
		final Intent serviceIntent = new Intent(context, UpdateService.class);
		serviceIntent.setAction(WidgetModel.ACTION_UPDATE);
		context.startService(serviceIntent);
	}

	@Override
	public void onEnabled(final Context context) {
		startUpdate(context);
	}

	@Override
	public void onDisabled(final Context context) {
		super.onDisabled(context);
		context.stopService(new Intent(context, UpdateService.class));

		// Currently multiple instances isn't supported, thats why set completed
		// flag here
		final SharedPreferences preferences = context.getSharedPreferences(Settings.SHARED_PREF_NAME, Context.MODE_PRIVATE);
		final Editor editor = preferences.edit();
		editor.putBoolean(Settings.PREF_KEY_COMPLETED, false);
		editor.commit();
		// Logger.deleteLog();
	}
}
