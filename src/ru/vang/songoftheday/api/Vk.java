package ru.vang.songoftheday.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.VkontakteApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import ru.vang.songoftheday.SongOfTheDaySettings;
import ru.vang.songoftheday.exceptions.VkApiException;
import ru.vang.songoftheday.fragment.AuthFragment;
import ru.vang.songoftheday.util.Logger;
import android.content.Context;
import android.content.SharedPreferences;

public class Vk {
	private static final String TAG = Vk.class.getSimpleName();

	public static final String CLIENT_ID = "2825622";
	public static final String API_SECRET = "0hm10ji6PMjiqXLsdXU8";

	private static final String CORE = "https://api.vk.com/method/";
	private static final String SEARCH_AUDIO = CORE + "audio.search?q=%s+%s&count="
			+ SongOfTheDaySettings.VK_LIMIT;
	private static final String ADD_AUDIO = CORE + "audio.add?aid=%s&oid=%s";

	private transient final OAuthService mAuthSevice;
	private transient final Token mToken;

	public Vk(final Context context) throws ClientProtocolException, IOException {
		mAuthSevice = new ServiceBuilder().provider(VkontakteApi.class).apiKey(CLIENT_ID)
				.apiSecret(API_SECRET).build();
		mToken = getToken(context);
		Logger.debug(TAG, "Token: " + mToken.toString());
	}

	public List<VkTrack> searchAudio(final String artist, final String title)
			throws JSONException, VkApiException, UnsupportedEncodingException {
		final OAuthRequest request = new OAuthRequest(Verb.GET, String.format(
				SEARCH_AUDIO,
				URLEncoder.encode(artist, SongOfTheDaySettings.DEFAULT_CHARSET),
				URLEncoder.encode(title, SongOfTheDaySettings.DEFAULT_CHARSET)));
		Logger.debug(TAG, "audio.search: " + request.getUrl());
		mAuthSevice.signRequest(mToken, request);
		final Response response = request.send();
		if (response.getCode() != 200) {
			throw new VkApiException("Failed to search audio! code: "
					+ response.getCode());
		}

		final List<VkTrack> tracks = new ArrayList<VkTrack>();

		final JSONObject jObject = new JSONObject(response.getBody());
		checkErrors(jObject);
		final JSONArray jArray = jObject.getJSONArray("response");
		for (int i = 1; i < jArray.length(); i++) {
			final VkTrack track = VkTrack.valueOf(jArray.getJSONObject(i));
			if (track != null) {
				tracks.add(track);
			}
		}

		return tracks;
	}

	public void addAudio(final String aid, final String oid) throws JSONException,
			VkApiException {
		final OAuthRequest request = new OAuthRequest(Verb.GET, String.format(ADD_AUDIO,
				aid, oid));
		mAuthSevice.signRequest(mToken, request);
		final Response response = request.send();
		if (response.getCode() != 200) {
			throw new VkApiException("Failed to add audio");
		}
		final JSONObject jObject = new JSONObject(response.getBody());
		checkErrors(jObject);
	}

	private void checkErrors(final JSONObject jObject) throws JSONException,
			VkApiException {
		if (jObject.has("error")) {
			final JSONObject json = jObject.getJSONObject("error");
			final int code = json.getInt("error_code");
			final VkErrors error = VkErrors.getValueByCode(code);

			throw new VkApiException(error);
		}
	}

	public static boolean saveToken(final Context context, final Token token) {
		final SharedPreferences preferences = context.getSharedPreferences(
				SongOfTheDaySettings.SHARED_PREF_NAME, Context.MODE_PRIVATE);
		return preferences.edit()
				.putString(SongOfTheDaySettings.PREF_KEY_TOKEN, token.getToken())
				.commit();
	}

	public static boolean hasVkAccount(final Context context) {
		final SharedPreferences preferences = context.getSharedPreferences(
				SongOfTheDaySettings.SHARED_PREF_NAME, Context.MODE_PRIVATE);
		final int status = preferences.getInt(SongOfTheDaySettings.PREF_KEY_AUTH_STATUS,
				AuthFragment.STATUS_COMPLETED);
		final boolean hasVkAccount = status == AuthFragment.STATUS_COMPLETED;
		return hasVkAccount;
	}

	private static Token getToken(final Context context) throws ClientProtocolException,
			IOException {
		final SharedPreferences preferences = context.getSharedPreferences(
				SongOfTheDaySettings.SHARED_PREF_NAME, Context.MODE_PRIVATE);
		final String tokenStr = preferences.getString(
				SongOfTheDaySettings.PREF_KEY_TOKEN, null);
		final Token token = new Token(tokenStr, API_SECRET);

		return token;
	}
}
