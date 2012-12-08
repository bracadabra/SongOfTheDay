package ru.vang.songoftheday.manager;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import ru.vang.songoftheday.api.LastFM;
import ru.vang.songoftheday.api.Track;
import ru.vang.songoftheday.api.Vk;
import ru.vang.songoftheday.api.VkTrack;
import ru.vang.songoftheday.exceptions.VkApiException;
import ru.vang.songoftheday.model.WidgetUpdateInfo;
import android.content.Context;
import android.database.Cursor;

public class TrackManager {
	private transient final Context mContext;
	
	//see MEDIA_PROJECTION in UpdateService
	private static final int MEDIA_ARTIST_INDEX = 1;
	private static final int MEDIA_TITLE_INDEX = 2;

	public TrackManager(final Context context) {
		mContext = context;
	}

	public VkTrack getVkTrack(final String artist, final String title)
			throws ClientProtocolException, IOException, JSONException,
			VkApiException {
		final Vk vk = new Vk(mContext);
		final List<VkTrack> vkTracks = vk.searchAudio(artist, title);

		VkTrack vkTrack = null;
		if (!vkTracks.isEmpty()) {
			vkTrack = vkTracks.get(0);
			//Set artist and title because vk track can contain trash
			vkTrack.setArtist(artist);
			vkTrack.setTitle(title);
		}

		return vkTrack;
	}

	public Track getTopTrack() throws ClientProtocolException, IOException {
		return getRandomTrack(LastFM.getTopTracks());		
	}

	private <T extends Track> T getRandomTrack(final List<T> tracks) {
		final long seed = System.currentTimeMillis();
		final Random random = new Random(seed);
		final int position = random.nextInt(tracks.size());

		return tracks.get(position);
	}

	public Track getLastFmTrack(final String artist, final String title) throws ClientProtocolException, IOException {
		final List<Track> tracks = LastFM.getSimilarTracks(artist, title);
		
		Track track = null;
		if (!tracks.isEmpty()) {
			track = getRandomTrack(tracks);
		}				

		return track;
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
	
	public WidgetUpdateInfo findSimilarTrackInfo(final Cursor cursor) throws VkApiException, ClientProtocolException, IOException, JSONException {
		final WidgetUpdateInfo widgetInfo = new WidgetUpdateInfo();
		VkTrack vkTrack = null;
		final int count = cursor.getCount();
		final Random random = new Random(System.currentTimeMillis());
		int index = 0;		
		do {
			final int position = random.nextInt(count);
			cursor.moveToPosition(position);
			final String originalTitle = cursor.getString(MEDIA_TITLE_INDEX);
			final String originalArtist = cursor.getString(MEDIA_ARTIST_INDEX);
			widgetInfo.setOriginalArtist(originalArtist);
			widgetInfo.setOriginalTitle(originalTitle);
			final Track track  = getLastFmTrack(originalArtist, originalTitle);
			if (track != null) {
				vkTrack = getVkTrack(track.getArtist(), track.getTitle());
			}
			index++;
		} while (vkTrack == null && index < count);
		widgetInfo.setVkTrack(vkTrack);
		
		return widgetInfo;
	}
}
