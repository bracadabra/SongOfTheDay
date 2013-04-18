package ru.vang.songoftheday;

import java.io.IOException;

import ru.vang.songoftheday.model.WidgetModel;
import ru.vang.songoftheday.util.Logger;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

//TODO set foreground
public class MediaPlayerService extends Service implements OnPreparedListener,
		OnErrorListener, OnCompletionListener {
	private static final String TAG = MediaPlayerService.class.getSimpleName();
	public static final String ACTION_PLAY = "ru.vang.songoftheday.action.PLAY";
	public static final String ACTION_STOP = "ru.vang.songoftheday.action.STOP";
	private static final boolean START_PLAYING = true;
	private static final boolean STOP_PLAYING = false;
	private static final int NOTIFICATION_ID = 1;
	
	private transient MediaPlayer mMediaPlayer = null;
	
	private transient Intent mIntent;
	private transient WidgetModel mWidget;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Thread.currentThread().setUncaughtExceptionHandler(Logger.EXCEPTION_HANDLER);
		mWidget = new WidgetModel(getApplicationContext());	
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {		
		mIntent = intent;
		final String action = intent.getAction();
		Logger.debug(TAG, "MediaPlayerService has received action: " + action);
		boolean isPlaying = STOP_PLAYING;
		if (action.equals(ACTION_PLAY)) {
			final Uri uri = intent.getData();
			Logger.debug(TAG, "Data with uri " + uri + " is started to play");
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			try {
				mMediaPlayer.setDataSource(getApplicationContext(), uri);
			} catch (IllegalArgumentException e) {
				Logger.error(TAG, Log.getStackTraceString(e));
			} catch (SecurityException e) {
				Logger.error(TAG, Log.getStackTraceString(e));
			} catch (IllegalStateException e) {
				Logger.error(TAG, Log.getStackTraceString(e));
			} catch (IOException e) {
				Logger.error(TAG, Log.getStackTraceString(e));
			}
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.prepareAsync();		
			isPlaying = START_PLAYING;
		} else if (action.equals(ACTION_STOP)) {
			stopPlayer();
			stopSelf();
		}		
		mWidget.tooglePlay(getApplicationContext(), intent, isPlaying);

		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}

	public void onPrepared(final MediaPlayer mp) {
		final Notification notification = createNotification(mIntent);
		startForeground(NOTIFICATION_ID, notification);
		mp.start();
	}

	public boolean onError(final MediaPlayer mp, final int what, final int extra) {
		Logger.error(TAG, "Error! what: " + what + " ; extra: " + extra);
		Logger.flush();

		return false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopPlayer();		
		Logger.flush();
	}
	
	private Notification createNotification(final Intent intent) {
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(
				MediaPlayerService.this)
				.setContentTitle(getString(R.string.song_playing)).setSmallIcon(
						R.drawable.stat_notify_musicplayer);

		final Intent contentIntent = intent;
		contentIntent.setAction(ACTION_STOP);
		final PendingIntent pendingStopIntent = PendingIntent.getService(
				getApplicationContext(), 0, contentIntent, 0);
		builder.setContentIntent(pendingStopIntent);

		final RemoteViews contentView = new RemoteViews(getApplicationContext()
				.getPackageName(), R.layout.media_status);
		final String artist = intent.getStringExtra(WidgetModel.EXTRA_ARTIST);
		contentView.setTextViewText(R.id.artist, artist);
		final String title = intent.getStringExtra(WidgetModel.EXTRA_TITLE);
		contentView.setTextViewText(R.id.title, title);
		builder.setContent(contentView);

		final Notification notification = builder.build();
		notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;
		
		return notification;	
	}

	public void onCompletion(final MediaPlayer mp) {
		stopPlayer();
		stopForeground(true);
	}

	public void stopPlayer() {
		if (mMediaPlayer != null) {			
			mMediaPlayer.release();
		}	
		mWidget.tooglePlay(getApplicationContext(), mIntent, STOP_PLAYING);
	}
}
