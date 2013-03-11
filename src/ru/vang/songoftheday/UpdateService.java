package ru.vang.songoftheday;

import static ru.vang.songoftheday.model.WidgetModel.ACTION_ADD;
import static ru.vang.songoftheday.model.WidgetModel.EXTRA_AID;
import static ru.vang.songoftheday.model.WidgetModel.EXTRA_OID;

import java.io.IOException;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import ru.vang.songoftheday.activity.VkAuthActivity;
import ru.vang.songoftheday.api.Vk;
import ru.vang.songoftheday.api.VkTrack;
import ru.vang.songoftheday.exceptions.UpdateException;
import ru.vang.songoftheday.exceptions.VkApiException;
import ru.vang.songoftheday.manager.TrackManager;
import ru.vang.songoftheday.model.WidgetModel;
import ru.vang.songoftheday.model.WidgetUpdateInfo;
import ru.vang.songoftheday.network.DownloadHelper;
import ru.vang.songoftheday.network.DownloadProgressListener;
import ru.vang.songoftheday.util.AlarmHelper;
import ru.vang.songoftheday.util.AvailabilityUtils;
import ru.vang.songoftheday.util.Logger;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore.Audio.Media;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

public class UpdateService extends IntentService {
	private static final String TAG = UpdateService.class.getSimpleName();

	private static final String EMPTY = "";
	private static final String AUDIO_SELECTION = Media.IS_MUSIC + "!=0";

	private transient final Handler mMainHandler = new Handler();
	private transient DownloadProgressListener mDownloadProgressListener = null;

	public UpdateService() {
		super(UpdateService.class.getName());
	}

	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}

	@Override
	protected void onHandleIntent(final Intent intent) {
		Thread.currentThread().setUncaughtExceptionHandler(Logger.EXCEPTION_HANDLER);
		final SharedPreferences sharedPreferences = getSharedPreferences(
				SongOfTheDaySettings.SHARED_PREF_NAME, Context.MODE_PRIVATE);
		final boolean isCompleted = sharedPreferences.getBoolean(
				SongOfTheDaySettings.PREF_KEY_COMPLETED, false);
		final boolean isAuthSkipped = sharedPreferences.getBoolean(
				SongOfTheDaySettings.PREF_KEY_SKIPPED, false);
		if (!isCompleted && !isAuthSkipped) {
			Logger.debug(TAG, "Update was skipped.");
			return;
		}

		final String action = intent.getAction();
		if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
			updateWidget();
		} else if (ACTION_ADD.equals(action)) {
			addTrack(intent);
		}
		Logger.flush();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mDownloadProgressListener != null) {
			mDownloadProgressListener.cancel();
		}
	}

	private void updateWidget() {
		Logger.debug(
				TAG,
				"Update is started at "
						+ DateFormat.format(AlarmHelper.DATE_PATTERN, new Date()));

		final WidgetModel widget = new WidgetModel(getApplicationContext());
		final CharSequence errorState = AvailabilityUtils
				.checkErrorState(UpdateService.this);
		Logger.debug(TAG, "errorState: " + errorState);
		try {
			widget.startUpdate();
			if (errorState == null) {
				stopService(new Intent(getApplicationContext(), MediaPlayerService.class));
				buildUpdate(widget);
			} else {
				widget.setWidgetText(errorState, EMPTY);
			}
		} finally {
			widget.bindPreference();
			widget.bindUpdate();

			widget.finishUpdate();
			AlarmHelper.setAlarm(UpdateService.this);
		}

		Logger.debug(
				TAG,
				"Update was finished at "
						+ DateFormat.format(AlarmHelper.DATE_PATTERN, new Date()));
	}

	private void addTrack(final Intent intent) {
		try {
			final Vk vkApi = new Vk(getApplicationContext());
			final String aid = intent.getStringExtra(EXTRA_AID);
			final String oid = intent.getStringExtra(EXTRA_OID);
			if (aid == null || oid == null) {
				throw new IllegalArgumentException();
			}

			vkApi.addAudio(aid, oid);
			mMainHandler.post(new ToastMessage(R.string.toast_add));
			Logger.debug(TAG, "Track " + aid + " was added");
		} catch (final Exception e) {
			// Catch all exceptions to inform user
			Logger.error(TAG, Log.getStackTraceString(e));
			mMainHandler.post(new ToastMessage(R.string.toast_error));
		}
	}

	private WidgetUpdateInfo fetchUpdate() throws ClientProtocolException, IOException,
			JSONException, VkApiException {
		final Cursor cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI,
				TrackManager.MEDIA_PROJECTION, AUDIO_SELECTION, null, null);
		final TrackManager manager = new TrackManager(getApplicationContext());
		WidgetUpdateInfo widgetInfo = null;
		if (cursor == null || cursor.getCount() == 0) {
			widgetInfo = manager.findTopTrackInfo();
		} else {
			widgetInfo = manager.findSimilarTrackInfo(cursor);
		}
		if (cursor != null) {
			cursor.close();
		}
		if (widgetInfo.getVkTrack() != null) {
			downloadTrack(widgetInfo);
		}

		return widgetInfo;
	}

	private void downloadTrack(final WidgetUpdateInfo widgetInfo)
			throws ClientProtocolException, IOException {
		mDownloadProgressListener = new DownloadProgressListener(getApplicationContext(),
				widgetInfo);
		DownloadHelper.downloadTo(widgetInfo, getExternalCacheDir(),
				mDownloadProgressListener);
	}

	// TODO display correct error messages
	private void buildUpdate(final WidgetModel widget) {
		final Context context = getApplicationContext();
		try {
			final WidgetUpdateInfo widgetInfo = fetchUpdate();
			if (widgetInfo.isCancelled() || widgetInfo.getTrack() == null) {
				widget.setWidgetText(R.string.not_found);
			} else {
				widget.setWidgetText(widgetInfo.getTitle(), widgetInfo.getArtist());
				final VkTrack vkTrack = widgetInfo.getVkTrack();
				if (vkTrack != null) {
					widget.bindPlay(vkTrack);
					widget.bindAdd(vkTrack.getId(), vkTrack.getOwnerId());
					if (widgetInfo.isOriginalEmpty()) {
						widget.hideInfo();
					} else {
						widget.bindInfo(widgetInfo.getOriginalArtist(),
								widgetInfo.getOriginalTitle(), vkTrack.getArtist(),
								vkTrack.getTitle());
					}
				}
			}
		} catch (ClientProtocolException e) {
			Logger.error(TAG, Log.getStackTraceString(e));
			widget.setWidgetText(R.string.exception);
		} catch (IOException e) {
			Logger.error(TAG, Log.getStackTraceString(e));
			widget.setWidgetText(R.string.exception);
		} catch (JSONException e) {
			Logger.error(TAG, Log.getStackTraceString(e));
			widget.setWidgetText(R.string.exception);
		} catch (VkApiException e) {
			Logger.error(TAG, Log.getStackTraceString(e));
			widget.setWidgetText(e.getUserMessage());
			final Intent intent = new Intent(context, VkAuthActivity.class);
			final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);
			widget.setOnClickEvent(R.id.details_container, pendingIntent);
		} catch (UpdateException e) {
			Logger.error(TAG, Log.getStackTraceString(e));
			widget.setWidgetText(e.getMessage());
		}
	}

	private class ToastMessage implements Runnable {
		private transient final int mStringId;

		public ToastMessage(final int stringId) {
			mStringId = stringId;
		}

		public void run() {
			Toast.makeText(getApplicationContext(), mStringId, Toast.LENGTH_SHORT).show();
		}

	}
}