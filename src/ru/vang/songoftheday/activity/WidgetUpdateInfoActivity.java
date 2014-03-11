package ru.vang.songoftheday.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import ru.vang.songoftheday.R;
import ru.vang.songoftheday.model.WidgetModel;

public class WidgetUpdateInfoActivity extends Activity {

    private static final String TITLE_PATTERN = "%s - %s";

    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_update_info);

        final Intent intent = getIntent();
        final String artist = intent.getStringExtra(WidgetModel.EXTRA_ARTIST);
        final String title = intent.getStringExtra(WidgetModel.EXTRA_TITLE);
        final TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setText(String.format(TITLE_PATTERN, title, artist));

        final String originalArtist = intent.getStringExtra(WidgetModel.EXTRA_ORIGINAL_ARTIST);
        final TextView originalArtistView = (TextView) findViewById(R.id.original_artist);
        originalArtistView.setText(originalArtist);

        final String originalTitle = intent.getStringExtra(WidgetModel.EXTRA_ORIGINAL_TITLE);
        final TextView originalTitleView = (TextView) findViewById(R.id.original_title);
        originalTitleView.setText(originalTitle);
    }
}
