package ru.vang.songoftheday.api;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import ru.vang.songoftheday.SongOfTheDaySettings;
import ru.vang.songoftheday.api.ErrorParser.Response;
import ru.vang.songoftheday.exceptions.LastFmException;
import ru.vang.songoftheday.util.Logger;

public final class LastFM {
	private static final String TAG = LastFM.class.getSimpleName();

	private static final String API_KEY = "b1fd4aabdc55c2df9c7f8dd163aac10e";
	private static final String GET_SIMILAR = "http://ws.audioscrobbler.com/2.0/?method=track.getsimilar&artist=%s&track=%s&limit="
			+ SongOfTheDaySettings.LAST_FM_LIMIT
			+ "&api_key="
			+ API_KEY
			+ "&autocorrect=1";
	private static final String GET_TOP_TRACKS = "http://ws.audioscrobbler.com/2.0/?method=chart.gettoptracks&limit="
			+ SongOfTheDaySettings.LAST_FM_LIMIT + "&api_key=" + API_KEY;

	private LastFM() {
	}

	public static List<Track> getSimilarTracks(final String artist, final String title)
			throws ClientProtocolException, IOException {
		final String url = String.format(GET_SIMILAR,
				URLEncoder.encode(artist, SongOfTheDaySettings.DEFAULT_CHARSET),
				URLEncoder.encode(title, SongOfTheDaySettings.DEFAULT_CHARSET));
		return makeRequest(url, "similartracks");
	}

	public static List<Track> getTopTracks() throws ClientProtocolException, IOException {
		return makeRequest(GET_TOP_TRACKS, "tracks");
	}

	private static List<Track> makeRequest(final String url, final String root)
			throws ClientProtocolException, IOException {
		Logger.debug(TAG, "Requested url: " + url);
		final URL requestUrl = new URL(url);
		final HttpURLConnection connection = (HttpURLConnection) requestUrl
				.openConnection();
		//TODO handle java.io.EOFException
		final int responseCode = connection.getResponseCode();

		final List<Track> tracks;
		if (responseCode == HttpURLConnection.HTTP_OK) {
			final BufferedInputStream bufferedStream = new BufferedInputStream(
					connection.getInputStream());
			tracks = ResponseParser.parse(bufferedStream, root);
			bufferedStream.close();
		} else
		/*
		 * LastFM return bad request if artist or title wasn't found, so far
		 * just check bad request, may be it will be worth to parse and check
		 * response for error status
		 */
		if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
			tracks = Collections.emptyList();
		} else {
			final BufferedInputStream bufferedStream = new BufferedInputStream(
					connection.getErrorStream());
			final Response response = ErrorParser.parse(bufferedStream);
			final LastFmErrors error = LastFmErrors.getValueByCode(response.code);
			bufferedStream.close();
			throw new LastFmException(error);
		}

		return tracks;
	}
}