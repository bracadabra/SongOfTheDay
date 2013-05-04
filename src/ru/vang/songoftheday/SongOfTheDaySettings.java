package ru.vang.songoftheday;

import java.nio.charset.Charset;

public final class SongOfTheDaySettings {

	private SongOfTheDaySettings() {
	};

	public static final String LOG_FILE = "songoftheday.log";

	public static final String SHARED_PREF_NAME = "songoftheday";
	public static final String PREF_KEY_AUTH_STATUS = "auth_status";
	public static final String PREF_KEY_TOKEN = "token";

	public static final int LAST_FM_LIMIT = 25;
	public static final int VK_LIMIT = 25;
	public static final String DEFAULT_CHARSET = Charset.defaultCharset().displayName();
	
	public static final String DEFAULT_UPDATE_TIME = "7:00";
}
