package ru.vang.songoftheday.api;

import org.xml.sax.SAXException;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Log;
import android.util.Xml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.vang.songoftheday.util.Logger;

public final class ResponseParser {

    private static final String TAG = ResponseParser.class.getSimpleName();

    private ResponseParser() {
    }

    public static List<Track> parse(final BufferedInputStream bufferedStream,
            final String rootElement) {
        final List<Track> tracks = new ArrayList<Track>();
        final Track currentTrack = new Track();

        final RootElement lfm = new RootElement("lfm");
        final Element root = lfm.getChild(rootElement);
        final Element track = root.getChild("track");
        track.setEndElementListener(new EndElementListener() {

            public void end() {
                tracks.add(currentTrack.copy());
            }
        });
        final Element name = track.getChild("name");
        name.setEndTextElementListener(new EndTextElementListener() {

            public void end(final String body) {
                currentTrack.setTitle(body);
            }
        });
        final Element mbid = track.getChild("mbid");
        mbid.setEndTextElementListener(new EndTextElementListener() {

            public void end(String body) {
                currentTrack.setId(body);
            }

        });

        final Element artist = track.getChild("artist");
        final Element artistName = artist.getChild("name");
        artistName.setEndTextElementListener(new EndTextElementListener() {

            public void end(final String body) {
                currentTrack.setArtist(body);
            }
        });

        try {
            Xml.parse(bufferedStream, Xml.Encoding.UTF_8, lfm.getContentHandler());
        } catch (IOException e) {
            Logger.error(TAG, Log.getStackTraceString(e));
        } catch (SAXException e) {
            Logger.error(TAG, Log.getStackTraceString(e));
        }
        Logger.debug(TAG, "Parsed tracks size: " + tracks.size());

        return tracks;
    }
}
