package ru.vang.songoftheday;

import ru.vang.songoftheday.fragment.AuthFragment;
import ru.vang.songoftheday.model.WidgetModel;
import ru.vang.songoftheday.service.ThrottleUpdateService;
import ru.vang.songoftheday.util.AlarmHelper;
import ru.vang.songoftheday.util.Logger;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

public class SongOfTheDayWidget extends AppWidgetProvider {
	private static final String TAG = SongOfTheDayWidget.class.getSimpleName();

	@Override
	public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
			final int[] appWidgetIds) {
		Logger.debug(TAG, "onUpdate is called");
		final Intent serviceIntent = new Intent(context, ThrottleUpdateService.class);
		serviceIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		context.startService(serviceIntent);
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		super.onReceive(context, intent);

		final String action = intent.getAction();
		Logger.debug(TAG, "Action " + action + " is recieved");
		if (WidgetModel.ACTION_ADD.equals(action)) {
			intent.setComponent(new ComponentName(context, ThrottleUpdateService.class));
			context.startService(intent);
		} else if (WidgetModel.ACTION_CANCEL.equals(action)) {
			final Intent stopServiceIntent = new Intent(context,
					ThrottleUpdateService.class);
			context.stopService(stopServiceIntent);
		} else if (WidgetModel.ACTION_NO_VK_ACCOUNT.equals(action)) {
			Toast.makeText(context, R.string.toast_no_vk_account, Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public void onDisabled(final Context context) {
		Logger.debug(TAG, "onDisabled is called");
		super.onDisabled(context);
		context.stopService(new Intent(context, ThrottleUpdateService.class));
		AlarmHelper.cancelAlaram(context);

		// Currently multiple instances isn't supported, thats why set completed
		// flag here
		final SharedPreferences preferences = context.getSharedPreferences(
				SongOfTheDaySettings.SHARED_PREF_NAME, Context.MODE_PRIVATE);
		final Editor editor = preferences.edit();
		editor.putInt(SongOfTheDaySettings.PREF_KEY_AUTH_STATUS,
				AuthFragment.STATUS_INCOMPLETED);
		editor.apply();
		// Logger.deleteLog();
	}
}
