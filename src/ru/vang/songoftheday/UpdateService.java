package ru.vang.songoftheday;

import static ru.vang.songoftheday.model.WidgetModel.ACTION_ADD;
import static ru.vang.songoftheday.model.WidgetModel.ACTION_UPDATE;
import static ru.vang.songoftheday.model.WidgetModel.DATE_PATTERN;
import static ru.vang.songoftheday.model.WidgetModel.EXTRA_AID;
import static ru.vang.songoftheday.model.WidgetModel.EXTRA_INVALID_ID;
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
import ru.vang.songoftheday.model.WidgetUpdateInfo;
import ru.vang.songoftheday.model.WidgetModel;
import ru.vang.songoftheday.util.DownloadHelper;
import ru.vang.songoftheday.util.DownloadHelper.ProgressListener;
import ru.vang.songoftheday.util.DownloadProgressListener;
import ru.vang.songoftheday.util.Logger;
import ru.vang.songoftheday.util.Settings;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore.Audio.Media;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

public class UpdateService extends IntentService {
	private static final String TAG = UpdateService.class.getSimpleName();

	private static final String EMPTY = "";
	private static final String[] MEDIA_PROJECTION = { Media._ID, Media.ARTIST, Media.TITLE };

	private static final String AUDIO_SELECTION = Media.IS_MUSIC + "!=0";

	public static boolean sIsCancelled = false;

	private transient final Handler mMainHandler = new Handler();

	private class ToastMessage implements Runnable {
		private transient final int mStringId;

		public ToastMessage(final int stringId) {
			mStringId = stringId;
		}

		public void run() {
			Toast.makeText(getApplicationContext(), mStringId, Toast.LENGTH_SHORT).show();
		}

	}

	public UpdateService() {
		super(UpdateService.class.getName());
	}

	private CharSequence checkErroState() {
		final String status = Environment.getExternalStorageState();
		CharSequence errorState = null;
		final Resources resources = getResources();
		if (status.equals(Environment.MEDIA_SHARED) || status.equals(Environment.MEDIA_UNMOUNTED)) {
			errorState = resources.getText(R.string.sdcard_busy_title_nosdcard);
		} else if (status.equals(Environment.MEDIA_REMOVED)) {
			errorState = resources.getText(R.string.sdcard_missing_title_nosdcard);
		}

		if (errorState == null && !isConnectionAvailable()) {
			errorState = resources.getString(R.string.network_unavailable);
		}

		return errorState;
	}

	private WidgetUpdateInfo fetchUpdate() throws ClientProtocolException, IOException, JSONException, VkApiException {
		final Cursor cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, MEDIA_PROJECTION, AUDIO_SELECTION,
				null, null);
		final TrackManager manager = new TrackManager(getApplicationContext());
		WidgetUpdateInfo widgetInfo = null;
		if (cursor == null) {
			widgetInfo = manager.findTopTrackInfo();
		} else {
			widgetInfo = manager.findSimilarTrackInfo(cursor);
			cursor.close();
		}

		if (widgetInfo.getVkTrack() != null) {
			final String path = downloadTrack(widgetInfo.getVkTrack());
			widgetInfo.setPath(path);
		}

		return widgetInfo;
	}

	private String downloadTrack(final VkTrack vkTrack) throws ClientProtocolException, IOException {
		final ProgressListener progressListener = new DownloadProgressListener(getApplicationContext(), vkTrack.getArtist(),
				vkTrack.getTitle());
		final String path = DownloadHelper.downloadTo(vkTrack, getExternalCacheDir(), progressListener);

		return path;
	}

	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}

	@Override
	protected void onHandleIntent(final Intent intent) {
		final SharedPreferences sharedPreferences = getSharedPreferences(Settings.SHARED_PREF_NAME, Context.MODE_PRIVATE);
		if (!sharedPreferences.getBoolean(Settings.PREF_KEY_COMPLETED, false)) {
			return;
		}

		final String action = intent.getAction();
		if (ACTION_UPDATE.equals(action)) {
			sIsCancelled = false;
			Thread.currentThread().setUncaughtExceptionHandler(Logger.EXCEPTION_HANDLER);
			Logger.append("Sync is started at " + DateFormat.format(DATE_PATTERN, new Date()));
			Logger.debug(TAG, "Sync is started at " + DateFormat.format(DATE_PATTERN, new Date()));

			final WidgetModel widget = new WidgetModel(getApplicationContext());
			final CharSequence errorState = checkErroState();
			try {
				// TODO showProgressBar update widget state, find better
				// approach
				widget.showProgressBar();
				if (errorState == null) {
					stopService(new Intent(getApplicationContext(), MediaPlayerService.class));
					buildUpdate(widget);
				} else {
					widget.setWidgetText(errorState, EMPTY);
				}
			} finally {
				widget.bindPreference();
				widget.bindUpdate();

				// TODO hideProgressBar update widget state, find better
				// approach
				widget.hideProgressBar();
				widget.setAlarm();
			}

			Logger.append("Sync is finished at " + DateFormat.format(DATE_PATTERN, new Date()));
			Logger.flush();
		} else if (ACTION_ADD.equals(action)) {
			try {
				final Vk vkApi = new Vk(getApplicationContext());
				final long aid = intent.getLongExtra(EXTRA_AID, EXTRA_INVALID_ID);
				final long oid = intent.getLongExtra(EXTRA_OID, EXTRA_INVALID_ID);
				if (aid == EXTRA_INVALID_ID || oid == EXTRA_INVALID_ID) {
					throw new IllegalArgumentException();
				}

				vkApi.addAudio(aid, oid);
				mMainHandler.post(new ToastMessage(R.string.toast_add));
			} catch (Exception e) {
				Log.e(TAG, Log.getStackTraceString(e));
				mMainHandler.post(new ToastMessage(R.string.toast_error));
			}
		} else if (ACTION_UPDATE.equals(action)) {
			sIsCancelled = true;
		}
	}

	private void buildUpdate(final WidgetModel widget) {
		final Context context = getApplicationContext();
		try {
			final WidgetUpdateInfo widgetInfo = fetchUpdate();
			if (widgetInfo.getVkTrack() == null) {
				widget.setWidgetText(R.string.not_found);
			} else {
				final VkTrack vkTrack = widgetInfo.getVkTrack();
				widget.setWidgetText(vkTrack.getTitle(), vkTrack.getArtist());
				widget.bindPlay(vkTrack);
				widget.bindAdd(vkTrack.getId(), vkTrack.getOwnerId());
				widget.bindInfo(widgetInfo.getOriginalArtist(), widgetInfo.getOriginalTitle(), vkTrack.getArtist(),
						vkTrack.getTitle());
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, Log.getStackTraceString(e));
			widget.setWidgetText(R.string.exception);

			Logger.debug(TAG, "ClientProtocolException");
		} catch (IOException e) {
			Log.e(TAG, Log.getStackTraceString(e));
			widget.setWidgetText(R.string.exception);

			Logger.debug(TAG, "IOException");
		} catch (JSONException e) {
			Log.e(TAG, Log.getStackTraceString(e));
			widget.setWidgetText(R.string.exception);

			Logger.debug(TAG, "JSONException");
		} catch (VkApiException e) {
			Log.e(TAG, Log.getStackTraceString(e));

			widget.setWidgetText(e.getUserMessage());
			final Intent intent = new Intent(context, VkAuthActivity.class);
			final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			widget.setOnClickEvent(R.id.details_container, pendingIntent);

			Logger.debug(TAG, "VkApiException");
		} catch (UpdateException e) {
			widget.setWidgetText(e.getMessage());

			Log.e(TAG, Log.getStackTraceString(e));
			Logger.debug(TAG, "UpdateException");
		}
	}

	private boolean isConnectionAvailable() {
		boolean connectionnAvailable = false;
		final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			final SharedPreferences preferences = getSharedPreferences(Settings.SHARED_PREF_NAME, Context.MODE_PRIVATE);
			final String wifiPreference = getString(R.string.wifi_value);
			final String networkPreference = preferences.getString(getString(R.string.network_type_key), wifiPreference);
			Logger.debug(TAG, "network preference: " + networkPreference);
			Logger.debug(TAG, "network type: " + networkInfo.getType());
			if (!networkPreference.equals(wifiPreference) || networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				connectionnAvailable = true;
			}
		}
		Logger.debug(TAG, "network available: " + connectionnAvailable);

		return connectionnAvailable;
	}
}