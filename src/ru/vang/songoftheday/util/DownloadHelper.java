package ru.vang.songoftheday.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import ru.vang.songoftheday.api.VkTrack;
import android.util.Log;

public class DownloadHelper {
	private static final String FILE_TITLE = "%s - %s";

	private DownloadHelper() {
	};

	public static String downloadTo(final VkTrack track, final File path, final ProgressListener progressListener)
			throws ClientProtocolException, IOException {
		final HttpGet get = new HttpGet(track.getUrl());

		final HttpResponse response = HttpHelper.getHttpClient().execute(get);
		if (response.getStatusLine().getStatusCode() == 200) {
			clearCache(path);
			final String fileName = String.format(FILE_TITLE, track.getArtist(), track.getTitle()).replace(File.separator,
					"");
			final File file = new File(path, fileName);
			final FileOutputStream fos = new FileOutputStream(file);
			final InputStream inputStream = response.getEntity().getContent();
			final long contentLength = response.getEntity().getContentLength();
			final byte[] data = new byte[1024];
			long total = 0;
			int count;
			try {
				while ((count = inputStream.read(data)) != -1) {
					total += count;
					if (progressListener != null) {
						if (progressListener.isCanceled()) {
							Log.d("Test", "isCanceled");
							progressListener.onFinish();
							break;
						}
						progressListener.onProgress((int) (total * 100 / contentLength));
					}
					fos.write(data, 0, count);
				}
				fos.flush();

				Log.d("Test", "downloading finished!");
				return file.getAbsolutePath();
			} finally {
				fos.close();
				inputStream.close();
				if (progressListener != null) {
					progressListener.onFinish();
				}
			}
		}

		return null;
	}

	public static void clearCache(final File path) {
		final File[] files = path.listFiles();
		if (files == null || files.length == 0) {
			return;
		}

		for (File file : path.listFiles()) {
			file.delete();
		}
	}

	public interface ProgressListener {

		void onProgress(final int progress);

		void onFinish();

		boolean isCanceled();

	}
}
