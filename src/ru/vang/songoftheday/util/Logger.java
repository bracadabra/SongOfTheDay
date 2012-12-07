package ru.vang.songoftheday.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.os.Environment;
import android.util.Log;

public class Logger {
	private static final StringBuffer STRING_BUFFER;
	public static final UncaughtExceptionHandler EXCEPTION_HANDLER;
	static {
		STRING_BUFFER = new StringBuffer();
		EXCEPTION_HANDLER = new ExceptionHandler();
	}
	private static final int LOG_LEVEL = Log.VERBOSE;

	public static void append(final String log) {
		STRING_BUFFER.append(log).append('\n');
	}

	public static void flush() {
		try {
			PrintWriter printWriter = new PrintWriter(new FileWriter(Environment.getExternalStorageDirectory() + "/"
					+ Settings.LOG_FILE, true));
			printWriter.append(STRING_BUFFER.toString());
			printWriter.flush();
			printWriter.close();

			STRING_BUFFER.setLength(0);
		} catch (IOException e) {
			// Do nothing
		}
	}

	public static void deleteLog() {
		final File file = new File(Environment.getExternalStorageDirectory() + "/" + Settings.LOG_FILE);
		if (file.exists()) {
			file.delete();
		}
	}

	public static class ExceptionHandler implements UncaughtExceptionHandler {

		public void uncaughtException(Thread thread, Throwable ex) {
			PrintWriter printWriter;
			try {
				printWriter = new PrintWriter(new FileWriter(Environment.getExternalStorageDirectory() + "/"
						+ Settings.LOG_FILE, true));
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
	
	public static void debug(final String tag, final String message) {
		if (isDebugEnabled()) {
			Log.d(tag, message);
		}
	}
}
