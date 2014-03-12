package ru.vang.songoftheday.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import ru.vang.songoftheday.SongOfTheDaySettings;

public class TimePreference extends DialogPreference {

    private static final String DELIMITER = ":";

    private int mHours;

    private int mMinutes;

    private TimePicker mTimePicker;

    public TimePreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public TimePreference(final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected View onCreateDialogView() {
        mTimePicker = new TimePicker(getContext());
        mTimePicker.setIs24HourView(true);

        return mTimePicker;
    }

    @Override
    protected void onBindDialogView(final View v) {
        super.onBindDialogView(v);

        mTimePicker.setCurrentHour(mHours);
        mTimePicker.setCurrentMinute(mMinutes);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            mHours = mTimePicker.getCurrentHour();
            mMinutes = mTimePicker.getCurrentMinute();

            String time = String.valueOf(mHours) + DELIMITER + String.valueOf(mMinutes);

            if (callChangeListener(time)) {
                persistString(time);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        final String time;
        if (restoreValue) {
            if (defaultValue == null) {
                time = getPersistedString(SongOfTheDaySettings.DEFAULT_UPDATE_TIME);
            } else {
                time = getPersistedString(defaultValue.toString());
            }
        } else {
            time = defaultValue.toString();
        }

        final int[] timeParts = parseTime(time);
        mHours = timeParts[0];
        mMinutes = timeParts[1];
    }

    /*
     * @param time string in format hh:mm
     *
     * @return int[0] - hours, int[1] - minutes
     */
    public static int[] parseTime(final String time) {
        final String[] parts = time.split(DELIMITER, 2);
        final int[] timeParts = {Integer.valueOf(parts[0]), Integer.valueOf(parts[1])};

        return timeParts;
    }

}
