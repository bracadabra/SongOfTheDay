package ru.vang.songoftheday.util;

public interface Settings {
	public static final String LOG_FILE = "songoftheday.log";
	public static final String COOKIES_STORE = "cookies";
	public static final String DOMAIN = ".vk.com";
	public static final String TOKEN_FILENAME = "token";
	
	public static final String SHARED_PREF_NAME = "songoftheday";
	public static final String PREF_KEY_LOGIN = "login";
	public static final String PREF_KEY_PASSWORD = "password";
	public static final String PREF_KEY_COMPLETED = "completed";
	public static final String PREF_KEY_POSITION = "position";
	
	public static final int LAST_FM_LIMIT = 25;
	public static final int VK_LIMIT = 25;
}
