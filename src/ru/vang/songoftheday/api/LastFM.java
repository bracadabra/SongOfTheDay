package ru.vang.songoftheday.api;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import ru.vang.songoftheday.exceptions.UpdateException;
import ru.vang.songoftheday.util.HttpHelper;
import ru.vang.songoftheday.util.Logger;
import ru.vang.songoftheday.util.Settings;

public final class LastFM {
	private static final String TAG = LastFM.class.getSimpleName();

	private static final String API_KEY = "b1fd4aabdc55c2df9c7f8dd163aac10e";
	private static final String GET_SIMILAR = "http://ws.audioscrobbler.com/2.0/?method=track.getsimilar&artist=%s&track=%s&limit="
			+ Settings.LAST_FM_LIMIT + "&api_key=" + API_KEY;
	private static final String GET_TOP_TRACKS = "http://ws.audioscrobbler.com/2.0/?method=chart.gettoptracks&limit="
			+ Settings.LAST_FM_LIMIT + "&api_key=" + API_KEY;

	private LastFM() {
	}

	public static List<Track> getSimilarTracks(final String artist, final String title) throws ClientProtocolException,
			IOException {
		final String url = String.format(GET_SIMILAR, URLEncoder.encode(artist), URLEncoder.encode(title));
		return makeRequest(url, "similartracks");
	}

	public static List<Track> getTopTracks() throws ClientProtocolException, IOException {
		return makeRequest(GET_TOP_TRACKS, "tracks");
	}

	private static List<Track> makeRequest(final String url, final String root) throws ClientProtocolException, IOException {
		Logger.debug(TAG, "Requested url: " + url);
		final HttpGet get = new HttpGet(url);

		final HttpResponse response = HttpHelper.getHttpClient().execute(get);
		final int statusCode = response.getStatusLine().getStatusCode();

		// LastFM return bad request if artist or title wasn't found, so far
		// just check bad request, may be it will be worth to
		// parse and check response for error status
		if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_BAD_REQUEST) {
			throw new UpdateException("Unable to connect to LastFM! code: " + response.getStatusLine().getStatusCode());
		}
		final BufferedInputStream bufferedStream = new BufferedInputStream(response.getEntity().getContent());
		final List<Track> tracks = ResponseParser.parse(bufferedStream, root);

		return tracks;
	}
}