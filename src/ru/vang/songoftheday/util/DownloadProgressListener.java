package ru.vang.songoftheday.util;

import ru.vang.songoftheday.R;
import ru.vang.songoftheday.SongOfTheDayWidget;
import ru.vang.songoftheday.UpdateService;
import ru.vang.songoftheday.model.WidgetModel;
import ru.vang.songoftheday.util.DownloadHelper.ProgressListener;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class DownloadProgressListener implements ProgressListener {
	private static final int NOTIFICATION_ID = 1;

	private transient final NotificationManager mNotificationManager;
	private transient final Notification mNotification;
	private transient long mPrevProgress;

	public DownloadProgressListener(final Context context, final String artist, final String title) {
		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotification = new Notification(android.R.drawable.stat_sys_download,
				context.getString(R.string.downloading_started), System.currentTimeMillis());
		mNotification.flags = mNotification.flags | Notification.FLAG_ONGOING_EVENT;
		mNotification.contentIntent = PendingIntent.getBroadcast(context, 0, new Intent(), 0);
		mNotification.contentView = new RemoteViews(context.getPackageName(), R.layout.progress_status);
		mNotification.contentView.setTextViewText(R.id.songTitle, artist + " - " + title);
		mNotification.contentView.setProgressBar(R.id.status_progress, 100, 0, false);
		final Intent intent = new Intent(context, SongOfTheDayWidget.class);
		intent.setAction(WidgetModel.ACTION_CANCEL);
		final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mNotification.contentView.setOnClickPendingIntent(R.id.download_cancel, pendingIntent);
		mPrevProgress = -1;
	}	

	public void onProgress(final int progress) {
		if (progress == mPrevProgress) {
			return;
		}

		mNotification.contentView.setTextViewText(R.id.percentage, progress + "%");
		mNotification.contentView.setProgressBar(R.id.status_progress, 100, progress, false);
		mNotificationManager.notify(NOTIFICATION_ID, mNotification);
		mPrevProgress = progress;
	}

	public void onFinish() {
		mNotificationManager.cancel(NOTIFICATION_ID);
	}

	public boolean isCanceled() {		
		return UpdateService.sIsCancelled;
	}
}