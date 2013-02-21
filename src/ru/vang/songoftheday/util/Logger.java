package ru.vang.songoftheday.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import ru.vang.songoftheday.SongOfTheDaySettings;

import android.os.Environment;
import android.util.Log;

public class Logger {
	private static final StringBuffer STRING_BUFFER = new StringBuffer();
	public static final UncaughtExceptionHandler EXCEPTION_HANDLER = new ExceptionHandler();
	private static final int LOG_LEVEL = Log.VERBOSE;
	private static final boolean WRITE_TO_FILE = true;

	public static void append(final String log) {
		STRING_BUFFER.append(log).append('\n');
	}

	public static void flush() {
		try {
			PrintWriter printWriter = new PrintWriter(new FileWriter(
					Environment.getExternalStorageDirectory() + "/"
							+ SongOfTheDaySettings.LOG_FILE, true));
			printWriter.append(STRING_BUFFER.toString());
			printWriter.flush();
			printWriter.close();

			STRING_BUFFER.setLength(0);
		} catch (IOException e) {
			// Do nothing
		}
	}

	public static void deleteLog() {
		final File file = new File(Environment.getExternalStorageDirectory() + "/"
				+ SongOfTheDaySettings.LOG_FILE);
		if (file.exists()) {
			file.delete();
		}
	}

	public static class ExceptionHandler implements UncaughtExceptionHandler {

		public void uncaughtException(Thread thread, Throwable ex) {
			PrintWriter printWriter;
			try {
				printWriter = new PrintWriter(new FileWriter(
						Environment.getExternalStorageDirectory() + "/"
								+ SongOfTheDaySettings.LOG_FILE, true));
			} catch (IOException e) {
				throw new RuntimeException("Failed to create log writer!");
			}

			Logger.flush();
			ex.printStackTrace(printWriter);
			printWriter.flush();
			printWriter.close();
		}
	}

	public static boolean isVerboseEnabled() {
		return LOG_LEVEL <= Log.VERBOSE;
	}

	public static boolean isDebugEnabled() {
		return LOG_LEVEL <= Log.DEBUG;
	}

	public static boolean isInfoEnabled() {
		return LOG_LEVEL <= Log.INFO;
	}

	public static boolean isWarnEnabled() {
		return LOG_LEVEL <= Log.WARN;
	}

	public static boolean isErrorEnabled() {
		return LOG_LEVEL <= Log.ERROR;
	}

	public static boolean isAssertEnabled() {
		return LOG_LEVEL <= Log.ASSERT;
	}

	public static void writeToFile(final String message) {
		if (WRITE_TO_FILE) {
			append(message);
		}
	}

	public static void debug(final String tag, final String message) {
		if (isDebugEnabled()) {
			Log.d(tag, message);
			writeToFile(message);
		}
	}

	public static void error(final String tag, final String message) {
		if (isErrorEnabled()) {
			Log.e(tag, message);
			writeToFile(message);
		}
	}
}
