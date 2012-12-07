package ru.vang.songoftheday.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
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

import ru.vang.songoftheday.exceptions.UpdateException;
import ru.vang.songoftheday.exceptions.VkApiException;
import ru.vang.songoftheday.util.Logger;
import ru.vang.songoftheday.util.Settings;
import android.content.Context;
import android.util.Log;

public class Vk {
	private static final String TAG = "Test";

	public static final String CLIENT_ID = "2825622";
	public static final String API_SECRET = "0hm10ji6PMjiqXLsdXU8";

	private static final String CORE = "https://api.vk.com/method/";
	private static final String SEARCH_AUDIO = CORE + "audio.search?q=%s+%s&count=" + Settings.VK_LIMIT;
	private static final String ADD_AUDIO = CORE + "audio.add?aid=%d&oid=%d";

	private transient final OAuthService mAuthSevice;
	private transient final Token mToken;
	private transient final Context mContext;

	public Vk(final Context context) throws ClientProtocolException, IOException {
		mContext = context;
		mAuthSevice = new ServiceBuilder().provider(VkontakteApi.class).apiKey(CLIENT_ID).apiSecret(API_SECRET).build();
		mToken = getToken();
		Logger.append(mToken.toString());
	}

	public List<VkTrack> searchAudio(final String artist, final String title) throws JSONException, VkApiException {
		final OAuthRequest request = new OAuthRequest(Verb.GET, String.format(SEARCH_AUDIO, URLEncoder.encode(artist),
				URLEncoder.encode(title)));		
		Log.d("Test", "audio.search: " + request.getUrl());
		mAuthSevice.signRequest(mToken, request);
		final Response response = request.send();
		if (response.getCode() != 200) {
			throw new UpdateException("Failed to search audio! code: " + response.getCode());
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

	public void addAudio(final long aid, final long oid) throws JSONException, VkApiException {
		final OAuthRequest request = new OAuthRequest(Verb.GET, String.format(ADD_AUDIO, aid, oid));
		mAuthSevice.signRequest(mToken, request);
		final Response response = request.send();
		if (response.getCode() != 200) {
			throw new UpdateException("Failed to add audio");
		}
		final JSONObject jObject = new JSONObject(response.getBody());
		checkErrors(jObject);
	}

	private void checkErrors(final JSONObject jObject) throws JSONException, VkApiException {
		if (jObject.has("error")) {
			final JSONObject error = jObject.getJSONObject("error");
			final int code = error.getInt("error_code");
			final String message = error.getString("error_msg");

			throw new VkApiException(code, message);
		}
	}

	public static void saveToken(final Token token, final File dir) {
		final File file = new File(dir, Settings.TOKEN_FILENAME);
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(token);
		} catch (FileNotFoundException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		} catch (IOException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					Log.e(TAG, Log.getStackTraceString(e));
				}
			}
		}
	}

	private Token getToken() throws ClientProtocolException, IOException {
		Token token = null;

		final File file = new File(mContext.getFilesDir(), Settings.TOKEN_FILENAME);
		if (file.exists()) {
			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(new FileInputStream(file));
				token = (Token) ois.readObject();
			} catch (OptionalDataException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			} catch (ClassNotFoundException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			} catch (IOException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			} finally {
				if (ois != null) {
					ois.close();
				}
			}
		}

		return token;
	}
}
