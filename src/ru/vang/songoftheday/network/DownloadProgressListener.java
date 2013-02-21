package ru.vang.songoftheday.network;

import ru.vang.songoftheday.R;
import ru.vang.songoftheday.SongOfTheDayWidget;
import ru.vang.songoftheday.api.VkTrack;
import ru.vang.songoftheday.model.WidgetModel;
import ru.vang.songoftheday.model.WidgetUpdateInfo;
import ru.vang.songoftheday.network.DownloadHelper.ProgressListener;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

public class DownloadProgressListener implements ProgressListener {
	private static final int NOTIFICATION_ID = 1;

	private transient final NotificationManager mNotificationManager;
	private transient final Notification mNotification;
	private transient long mPrevProgress;
	private transient boolean mIsCancelled = false;

	public DownloadProgressListener(final Context context,
			final WidgetUpdateInfo widgetUpdateInfo) {
		mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setContentTitle(context.getString(R.string.downloading_started))
				.setSmallIcon(android.R.drawable.stat_sys_download);

		final PendingIntent emptyIntent = PendingIntent.getBroadcast(context, 0,
				new Intent(), 0);
		builder.setContentIntent(emptyIntent);

		final RemoteViews contentView = new RemoteViews(context.getPackageName(),
				R.layout.progress_status);
		final VkTrack vkTrack = widgetUpdateInfo.getVkTrack();
		contentView.setTextViewText(R.id.songTitle,
				vkTrack.getTitle() + " - " + vkTrack.getArtist());
		contentView.setProgressBar(R.id.status_progress, 100, 0, false);
		final Intent intent = new Intent(context, SongOfTheDayWidget.class);
		intent.setAction(WidgetModel.ACTION_CANCEL);
		final PendingIntent pendingCancelIntent = PendingIntent.getBroadcast(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		contentView.setOnClickPendingIntent(R.id.download_cancel, pendingCancelIntent);
		builder.setContent(contentView);
		builder.setOngoing(true);

		mNotification = builder.build();
		mPrevProgress = -1;
	}

	public void onProgress(final int progress) {
		if (progress == mPrevProgress) {
			return;
		}

		mNotification.contentView.setTextViewText(R.id.percentage, progress + "%");
		mNotification.contentView.setProgressBar(R.id.status_progress, 100, progress,
				false);
		mNotificationManager.notify(NOTIFICATION_ID, mNotification);
		mPrevProgress = progress;
	}

	public void onFinish() {
		mNotificationManager.cancel(NOTIFICATION_ID);
	}

	public boolean isCanceled() {
		return mIsCancelled;
	}

	public void cancel() {
		mIsCancelled = true;
	}
}