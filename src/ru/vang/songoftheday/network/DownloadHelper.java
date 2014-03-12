package ru.vang.songoftheday.network;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.vang.songoftheday.api.VkTrack;
import ru.vang.songoftheday.model.WidgetUpdateInfo;

public final class DownloadHelper {

    private static final int HALF_SECOND = 500;

    private DownloadHelper() {
    }

    public static void downloadTo(final WidgetUpdateInfo widgetInfo, final File path,
            final ProgressListener progressListener) throws IOException {
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
            final BufferedInputStream inputStream = new BufferedInputStream(
                    connection.getInputStream());
            final long contentLength = connection.getContentLength();
            final byte[] data = new byte[1024];
            long total = 0;
            int count;
            long lastUpdateTime = System.currentTimeMillis();
            try {
                // Could throw IOException: unexpected end of stream
                while ((count = inputStream.read(data)) != -1) {
                    total += count;
                    if (progressListener != null) {
                        if (progressListener.isCanceled()) {
                            progressListener.onFinish();
                            widgetInfo.setStatus(WidgetUpdateInfo.Status.CANCELLED);
                            break;
                        }
                        if (System.currentTimeMillis() - lastUpdateTime > HALF_SECOND) {
                            progressListener
                                    .onProgress((int) (total * 100 / contentLength));
                            lastUpdateTime = System.currentTimeMillis();
                        }
                    }
                    fos.write(data, 0, count);
                }
                fos.flush();
                connection.disconnect();

                widgetInfo.setPath(file.getAbsolutePath());
            } finally {
                if (progressListener != null) {
                    progressListener.onFinish();
                }
                fos.close();
                inputStream.close();
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
