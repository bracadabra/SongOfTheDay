package ru.vang.songoftheday;

import java.nio.charset.Charset;

public final class SongOfTheDaySettings {

	private SongOfTheDaySettings() {
	};

	public static final String LOG_FILE = "songoftheday.log";
	public static final String TOKEN_FILENAME = "token";

	public static final String SHARED_PREF_NAME = "songoftheday";
	public static final String PREF_KEY_COMPLETED = "completed";
	public static final String PREF_KEY_SKIPPED = "skipped";

	public static final int LAST_FM_LIMIT = 25;
	public static final int VK_LIMIT = 25;
	public static final String DEFAULT_CHARSET = Charset.defaultCharset().displayName();
	
	public static final String DEFAULT_UPDATE_TIME = "7:00";
}
