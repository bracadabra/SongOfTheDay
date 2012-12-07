package ru.vang.songoftheday;

import java.io.IOException;

import ru.vang.songoftheday.model.WidgetModel;
import ru.vang.songoftheday.util.Logger;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class MediaPlayerService extends Service implements OnPreparedListener, OnErrorListener, OnCompletionListener {
	private static final String TAG = "Test";
	public static final String ACTION_PLAY = "ru.vang.songoftheday.action.PLAY";
	public static final String ACTION_STOP = "ru.vang.songoftheday.action.STOP";
	private static final boolean START_PLAYING = true;
	private static final boolean STOP_PLAYING = false;
	private static final int NOTIFICATION_ID = 1;
	private transient MediaPlayer mMediaPlayer = null;
	private transient NotificationManager mNotificationManager;
	private transient Intent mIntent;
	private transient WidgetModel mWidget;

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		Thread.currentThread().setUncaughtExceptionHandler(Logger.EXCEPTION_HANDLER);
		mIntent = intent;
		final String action = intent.getAction();
		Log.d("Test", "MediaPlayerService received action: " + action);
		boolean isPlaying = STOP_PLAYING;
		if (action.equals(ACTION_PLAY)) {
			final Uri uri = intent.getData();
			Log.d("Test", "uri: " + uri);
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			try {
				mMediaPlayer.setDataSource(getApplicationContext(), uri);
			} catch (IllegalArgumentException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			} catch (SecurityException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			} catch (IllegalStateException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			} catch (IOException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			}
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.prepareAsync();
			showStatus(intent);
			isPlaying = START_PLAYING;
		} else if (action.equals(ACTION_STOP)) {
			stopPlayer();
			stopSelf();
		}

		mWidget = new WidgetModel(getApplicationContext());
		mWidget.tooglePlay(getApplicationContext(), intent, isPlaying);

		// return START_NOT_STICKY because we don't need to restart service
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(final Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onPrepared(final MediaPlayer mp) {
		mp.start();
	}

	public boolean onError(final MediaPlayer mp, final int what, final int extra) {
		Logger.append("Error! what: " + what + " ; extra: " + extra);
		Logger.flush();
		// TODO Handle errors appropriately
		return false;
	}

	@Override
	public void onDestroy() {
		stopPlayer();
		super.onDestroy();
	}

	private void showStatus(final Intent intent) {
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification notification = new Notification(R.drawable.stat_notify_musicplayer, "Song of the day is playing",
				System.currentTimeMillis());
		notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;

		final Intent contentIntent = intent;
		contentIntent.setAction(ACTION_STOP);
		notification.contentIntent = PendingIntent.getService(getApplicationContext(), 0, contentIntent, 0);
		notification.contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.media_status);

		final String artist = intent.getStringExtra(WidgetModel.EXTRA_ARTIST);
		final String title = intent.getStringExtra(WidgetModel.EXTRA_TITLE);

		notification.contentView.setTextViewText(R.id.artist, artist);
		notification.contentView.setTextViewText(R.id.title, title);

		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}

	public void onCompletion(final MediaPlayer mp) {
		stopPlayer();
	}

	public void stopPlayer() {
		if (mMediaPlayer != null) {
			mNotificationManager.cancel(NOTIFICATION_ID);
			mMediaPlayer.release();
		}
		mWidget.tooglePlay(getApplicationContext(), mIntent, STOP_PLAYING);
	}
}
