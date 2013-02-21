package ru.vang.songoftheday.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;

import ru.vang.songoftheday.api.VkTrack;
import ru.vang.songoftheday.model.WidgetUpdateInfo;

public class DownloadHelper {
	private DownloadHelper() {
	};

	public static void downloadTo(final WidgetUpdateInfo widgetInfo, final File path,
			final ProgressListener progressListener) throws ClientProtocolException,
			IOException {
		final VkTrack vkTrack = widgetInfo.getVkTrack();		
		final URL requestUrl = new URL(vkTrack.getUrl());
		final HttpURLConnection connection = (HttpURLConnection) requestUrl
				.openConnection();
		final int responseCode = connection.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			clearCache(path);
			final String fileName = getFilename(vkTrack.getArtist(), vkTrack.getTitle());
			final File file = new File(path, fileName);
			final FileOutputStream fos = new FileOutputStream(file);
			final InputStream inputStream = connection.getInputStream();			
			final long contentLength = connection.getContentLength();
			final byte[] data = new byte[1024];
			long total = 0;
			int count;
			try {
				while ((count = inputStream.read(data)) != -1) {
					total += count;
					if (progressListener != null) {
						if (progressListener.isCanceled()) {
							progressListener.onFinish();
							widgetInfo.setStatus(WidgetUpdateInfo.Status.CANCELLED);
							break;
						}
						progressListener.onProgress((int) (total * 100 / contentLength));
					}
					fos.write(data, 0, count);
				}
				fos.flush();

				widgetInfo.setPath(file.getAbsolutePath());
			} finally {
				fos.close();
				inputStream.close();
				if (progressListener != null) {
					progressListener.onFinish();
				}
			}
		}
	}

	private static String getFilename(final String artist, final String title) {
		final String sourceString = artist + title;
		return String.valueOf(sourceString.hashCode());
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
