package ru.vang.songoftheday.model;

import ru.vang.songoftheday.MediaPlayerService;
import ru.vang.songoftheday.R;
import ru.vang.songoftheday.SongOfTheDayWidget;
import ru.vang.songoftheday.activity.WidgetPreferenceActivity;
import ru.vang.songoftheday.activity.WidgetUpdateInfoActivity;
import ru.vang.songoftheday.api.VkTrack;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

public class WidgetModel {
	@SuppressWarnings("unused")
	private static final String TAG = WidgetModel.class.getSimpleName();
	public static final String ACTION_UPDATE = "ru.vang.songoftheday.ACTION_UPDATE";
	public static final String ACTION_ADD = "ru.vang.songoftheday.ACTION_ADD";
	public static final String ACTION_CANCEL = "ru.vang.songoftheday.ACTION_CANCEL";
	public static final String EXTRA_ORIGINAL_ARTIST = "ru.vang.songoftheday.originalArtsist";
	public static final String EXTRA_ORIGINAL_TITLE = "ru.vang.songoftheday.originalTitle";
	public static final String EXTRA_ARTIST = "ru.vang.songoftheday.artsist";
	public static final String EXTRA_TITLE = "ru.vang.songoftheday.title";
	public static final String EXTRA_AID = "ru.vang.songoftheday.aid";
	public static final String EXTRA_OID = "ru.vang.songoftheday.oid";
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

	public void bindAdd(final String aid, final String oid) {
		final Intent intent = new Intent(mContext, SongOfTheDayWidget.class);
		intent.setAction(ACTION_ADD);
		intent.putExtra(EXTRA_AID, aid);
		intent.putExtra(EXTRA_OID, oid);
		final PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mRemoteViews.setOnClickPendingIntent(R.id.add, pendingIntent);
	}

	public void bindUpdate() {
		final Intent intent = new Intent(mContext, SongOfTheDayWidget.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		final PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mRemoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
	}

	public void bindPlay(final VkTrack track) {
		bindPlay(track.getArtist(), track.getTitle(), Uri.parse(track.getPath()),
				track.getId(), track.getOwnerId(), false);
	}

	public void bindPreference() {
		final Intent intent = new Intent(mContext, WidgetPreferenceActivity.class);
		final PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mRemoteViews.setOnClickPendingIntent(R.id.preference, pendingIntent);
	}

	public void bindPlay(final String artist, final String title, final Uri path,
			final String aid, final String oid, final boolean isPlaying) {
		String action;
		if (isPlaying) {
			mRemoteViews.setImageViewResource(R.id.play, R.drawable.ic_stop);
			action = MediaPlayerService.ACTION_STOP;
		} else {
			mRemoteViews.setImageViewResource(R.id.play, R.drawable.ic_play);
			action = MediaPlayerService.ACTION_PLAY;
		}

		final Intent intent = new Intent(action);
		final ComponentName serviceName = new ComponentName(mContext,
				MediaPlayerService.class);
		intent.setComponent(serviceName);
		intent.setData(path);
		intent.putExtra(EXTRA_ARTIST, artist);
		intent.putExtra(EXTRA_TITLE, title);
		intent.putExtra(EXTRA_AID, aid);
		intent.putExtra(EXTRA_OID, oid);
		final PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, intent,
				0);
		mRemoteViews.setOnClickPendingIntent(R.id.play, pendingIntent);
	}

	public void bindInfo(final String originalArtist, final String originalTitle,
			final String artist, final String title) {
		final Intent intent = new Intent(mContext, WidgetUpdateInfoActivity.class);
		intent.putExtra(EXTRA_ORIGINAL_ARTIST, originalArtist);
		intent.putExtra(EXTRA_ORIGINAL_TITLE, originalTitle);
		intent.putExtra(EXTRA_ARTIST, artist);
		intent.putExtra(EXTRA_TITLE, title);
		final PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mRemoteViews.setOnClickPendingIntent(R.id.widget_info, pendingIntent);
	}

	private void bindButtons(final String artist, final String title, final Uri path,
			final String aid, final String oid, final boolean isPlaing) {
		bindPreference();
		bindPlay(artist, title, path, aid, oid, isPlaing);
		bindAdd(aid, oid);
		bindUpdate();
	}

	public void tooglePlay(final Context context, final Intent intent,
			final boolean isPlaing) {
		final String artist = intent.getStringExtra(EXTRA_ARTIST);
		final String title = intent.getStringExtra(EXTRA_TITLE);
		final Uri path = intent.getData();

		if (artist == null || title == null || path == null) {
			return;
		}

		mRemoteViews.setTextViewText(R.id.title, title);
		mRemoteViews.setTextViewText(R.id.artist, artist);

		final String aid = intent.getStringExtra(EXTRA_AID);
		final String oid = intent.getStringExtra(EXTRA_OID);
		bindButtons(artist, title, path, aid, oid, isPlaing);
		mManager.updateAppWidget(mComponentName, mRemoteViews);
	}

	public void startUpdate() {
		showProgressBar();
		mManager.updateAppWidget(mComponentName, mRemoteViews);
	}

	public void showProgressBar() {
		mRemoteViews.setViewVisibility(R.id.progressbar_container, View.VISIBLE);
		mRemoteViews.setViewVisibility(R.id.details_container, View.GONE);
	}

	public void finishUpdate() {
		hideProgressBar();
		mManager.updateAppWidget(mComponentName, mRemoteViews);
	}

	public void hideProgressBar() {
		mRemoteViews.setViewVisibility(R.id.progressbar_container, View.GONE);
		mRemoteViews.setViewVisibility(R.id.details_container, View.VISIBLE);
	}

	public void setWidgetText(final CharSequence primaryText,
			final CharSequence secondaryText) {
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
		mRemoteViews.setOnClickPendingIntent(viewId, pendingIntent);
	}
}
