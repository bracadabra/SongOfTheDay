package ru.vang.songoftheday.model;

import java.util.Calendar;

import ru.vang.songoftheday.MediaPlayerService;
import ru.vang.songoftheday.R;
import ru.vang.songoftheday.SongOfTheDayWidget;
import ru.vang.songoftheday.WidgetPreferenceActivity;
import ru.vang.songoftheday.api.VkTrack;
import ru.vang.songoftheday.util.Logger;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class WidgetModel {
	public static final String ACTION_UPDATE = "ru.vang.songoftheday.ACTION_UPDATE";
	public static final String ACTION_ADD = "ru.vang.songoftheday.ACTION_ADD";
	public static final String ACTION_CANCEL = "ru.vang.songoftheday.ACTION_CANCEL"; 
	public static final String DATE_PATTERN = "dd-MM-yyyy hh:mm:ss a";
	public static final String EXTRA_ARTIST = "artsist";
	public static final String EXTRA_TITLE = "title";
	public static final String EXTRA_AID = "aid";
	public static final String EXTRA_OID = "oid";
	public static final long EXTRA_INVALID_ID = -1;
	private static final String EMPTY_STRING = "";

	private final Context mContext;
	private final AppWidgetManager mManager;
	private final ComponentName mComponentName;
	private final RemoteViews mRemoteViews;

	public WidgetModel(final Context context) {
		mContext = context;
		mManager = AppWidgetManager.getInstance(context);
		mComponentName = new ComponentName(context, SongOfTheDayWidget.class);
		mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
	}

	public void bindAdd(final long aid, final long oid) {
		final Intent intent = new Intent(mContext, SongOfTheDayWidget.class);
		intent.setAction(ACTION_ADD);
		intent.putExtra(EXTRA_AID, aid);
		intent.putExtra(EXTRA_OID, oid);
		final PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mRemoteViews.setOnClickPendingIntent(R.id.add, pendingIntent);
	}

	public void bindUpdate() {
		final Intent intent = new Intent(mContext, SongOfTheDayWidget.class);
		intent.setAction(ACTION_UPDATE);
		final PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mRemoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
	}

	public void bindPlay(final VkTrack track) {
		bindPlay(track.getArtist(), track.getTitle(), Uri.parse(track.getPath()), track.getId(), track.getOwnerId(), false);
	}

	public void bindPreference() {
		final Intent intent = new Intent(mContext, WidgetPreferenceActivity.class);
		final PendingIntent pendingIntent = PendingIntent
				.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mRemoteViews.setOnClickPendingIntent(R.id.preference, pendingIntent);
	}

	public void bindPlay(final String artist, final String title, final Uri path, final long aid, final long oid,
			final boolean isPlaying) {
		final ComponentName serviceName = new ComponentName(mContext, MediaPlayerService.class);
		String action;
		if (isPlaying) {
			mRemoteViews.setImageViewResource(R.id.play, R.drawable.stop);
			action = MediaPlayerService.ACTION_STOP;
		} else {
			mRemoteViews.setImageViewResource(R.id.play, R.drawable.play);
			action = MediaPlayerService.ACTION_PLAY;
		}

		final Intent intent = new Intent(action);
		intent.setComponent(serviceName);
		intent.setData(path);
		intent.putExtra(EXTRA_ARTIST, artist);
		intent.putExtra(EXTRA_TITLE, title);
		intent.putExtra(EXTRA_AID, aid);
		intent.putExtra(EXTRA_OID, oid);
		final PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, intent, 0);
		mRemoteViews.setOnClickPendingIntent(R.id.play, pendingIntent);
	}

	public void tooglePlay(final Context context, final Intent intent, final boolean isPlaing) {
		final String artist = intent.getStringExtra(EXTRA_ARTIST);
		final String title = intent.getStringExtra(EXTRA_TITLE);
		final Uri path = intent.getData();

		if (artist == null || title == null || path == null) {
			return;
		}

		mRemoteViews.setTextViewText(R.id.title, title);
		mRemoteViews.setTextViewText(R.id.artist, artist);

		final long aid = intent.getLongExtra(EXTRA_AID, EXTRA_INVALID_ID);
		final long oid = intent.getLongExtra(EXTRA_OID, EXTRA_INVALID_ID);
		bindButtons(artist, title, path, aid, oid, isPlaing);		
		mManager.updateAppWidget(mComponentName, mRemoteViews);
	}
	
	private void bindButtons(final String artist, final String title, final Uri path, final long aid, final long oid, final boolean isPlaing) {
		bindPreference();
		bindPlay(artist, title, path, aid, oid, isPlaing);
		bindAdd(aid, oid);
		bindUpdate();
	}

	public void showProgressBar() {
		mRemoteViews.setViewVisibility(R.id.progressbar_container, View.VISIBLE);
		mRemoteViews.setViewVisibility(R.id.widget_info_container, View.GONE);
		mManager.updateAppWidget(mComponentName, mRemoteViews);
	}
	
	public void hideProgressBar() {
		mRemoteViews.setViewVisibility(R.id.progressbar_container, View.GONE);
		mRemoteViews.setViewVisibility(R.id.widget_info_container, View.VISIBLE);
		mManager.updateAppWidget(mComponentName, mRemoteViews);
	}

	public void setAlarm() {
		final AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.AM_PM, Calendar.AM);
		calendar.set(Calendar.HOUR, 7);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
			calendar.add(Calendar.DATE, 1);
		}

		final Intent intent = new Intent(mContext, SongOfTheDayWidget.class);
		intent.setAction(ACTION_UPDATE);
		final PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

		Log.d("Test", "Alarm is set to: " + DateFormat.format(DATE_PATTERN, calendar.getTimeInMillis()));
		Logger.append("Alarm is set to: " + DateFormat.format(DATE_PATTERN, calendar.getTimeInMillis()));
	}

	public void setWidgetText(final CharSequence primaryText, final CharSequence secondaryText) {
		mRemoteViews.setTextViewText(R.id.title, primaryText);
		mRemoteViews.setTextViewText(R.id.artist, secondaryText);
	}

	public void setWidgetText(final CharSequence primaryText) {
		mRemoteViews.setTextViewText(R.id.title, primaryText);
		mRemoteViews.setTextViewText(R.id.artist, EMPTY_STRING);
	}

	public void setWidgetText(final int primaryTextId) {
		final String primaryText = mContext.getResources().getString(primaryTextId);		
		mRemoteViews.setTextViewText(R.id.title, primaryText);
		mRemoteViews.setTextViewText(R.id.artist, EMPTY_STRING);
	}

	public void updateWidgetState() {
		mManager.updateAppWidget(mComponentName, mRemoteViews);
	}
	
	public void setOnClickEvent(final int viewId, final PendingIntent pendingIntent) {
		mRemoteViews.setOnClickPendingIntent(R.id.widget_info_container, pendingIntent);
	}
}
