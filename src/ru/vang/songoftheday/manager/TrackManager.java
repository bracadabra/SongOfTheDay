package ru.vang.songoftheday.manager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import ru.vang.songoftheday.api.LastFM;
import ru.vang.songoftheday.api.LastFmTrack;
import ru.vang.songoftheday.api.Track;
import ru.vang.songoftheday.api.Vk;
import ru.vang.songoftheday.api.VkTrack;
import ru.vang.songoftheday.database.SongOfTheDayDbHelper;
import ru.vang.songoftheday.exceptions.VkApiException;
import ru.vang.songoftheday.model.WidgetUpdateInfo;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Media;

public class TrackManager {
	public static final String[] MEDIA_PROJECTION = { Media._ID, Media.ARTIST, Media.TITLE };
	private static final int MEDIA_ARTIST_INDEX = 1;
	private static final int MEDIA_TITLE_INDEX = 2;

	private static final String EMPTY_ARTIST = null;

	private transient final Context mContext;

	public TrackManager(final Context context) {
		mContext = context;
	}

	public VkTrack getVkTrack(final String artist, final String title) throws ClientProtocolException, IOException,
			JSONException, VkApiException {
		final Vk vk = new Vk(mContext);
		final List<VkTrack> vkTracks = vk.searchAudio(artist, title);

		VkTrack vkTrack = null;
		if (!vkTracks.isEmpty()) {
			vkTrack = vkTracks.get(0);
			// Set artist and title to original because vk track can contain
			// trash
			vkTrack.setArtist(artist);
			vkTrack.setTitle(title);
		}

		return vkTrack;
	}

	public LastFmTrack getTopTrack() throws ClientProtocolException, IOException {
		final List<LastFmTrack> topTracks = LastFM.getTopTracks();
		return getNewLastFmTrack(topTracks, EMPTY_ARTIST);
	}

	private LastFmTrack getNewLastFmTrack(final List<LastFmTrack> lastFmTracks, final String orginalArtist) {
		LastFmTrack track = null;
		if (!lastFmTracks.isEmpty()) {
			final SongOfTheDayDbHelper dbHelper = new SongOfTheDayDbHelper(mContext);
			// Shuffle list cause of tracks ordered by similarity
			Collections.shuffle(lastFmTracks, getRandom());
			for (LastFmTrack lastFmTrack : lastFmTracks) {
				if (!(ifSameArtist(orginalArtist, lastFmTrack.getArtist()) || dbHelper.ifMbidExists(lastFmTrack.getMbid()))) {
					track = lastFmTrack;
					break;
				}
			}
		}
		return track;
	}

	private boolean ifSameArtist(final String originalArtist, final String foundArtist) {
		return originalArtist != null && originalArtist.equals(foundArtist) ? true : false;
	}

	public LastFmTrack getLastFmTrack(final String artist, final String title) throws ClientProtocolException, IOException {
		final List<LastFmTrack> lastFmTracks = LastFM.getSimilarTracks(artist, title);
		final LastFmTrack foundTrack = getNewLastFmTrack(lastFmTracks, artist);

		return foundTrack;
	}

	public WidgetUpdateInfo findTopTrackInfo() throws VkApiException, ClientProtocolException, IOException, JSONException {
		final WidgetUpdateInfo widgetInfo = new WidgetUpdateInfo();
		VkTrack vkTrack = null;
		do {
			final Track track = getTopTrack();
			if (track != null) {
				widgetInfo.setOriginalArtist(track.getArtist());
				widgetInfo.setOriginalTitle(track.getTitle());
				vkTrack = getVkTrack(track.getArtist(), track.getTitle());
			}
		} while (vkTrack == null);
		widgetInfo.setVkTrack(vkTrack);

		return widgetInfo;
	}

	public WidgetUpdateInfo findSimilarTrackInfo(final Cursor cursor) throws VkApiException, ClientProtocolException,
			IOException, JSONException {
		final WidgetUpdateInfo widgetInfo = new WidgetUpdateInfo();
		VkTrack vkTrack = null;
		final int count = cursor.getCount();
		final Random random = getRandom();
		int index = 0;
		do {
			final int position = random.nextInt(count);
			cursor.moveToPosition(position);
			final String originalTitle = cursor.getString(MEDIA_TITLE_INDEX);
			final String originalArtist = cursor.getString(MEDIA_ARTIST_INDEX);
			widgetInfo.setOriginalArtist(originalArtist);
			widgetInfo.setOriginalTitle(originalTitle);
			final LastFmTrack track = getLastFmTrack(originalArtist, originalTitle);
			if (track != null) {
				vkTrack = getVkTrack(track.getArtist(), track.getTitle());
			}
			index++;
		} while (vkTrack == null && index < count);
		widgetInfo.setVkTrack(vkTrack);

		return widgetInfo;
	}

	private Random getRandom() {
		return new Random(System.currentTimeMillis());
	}
}
