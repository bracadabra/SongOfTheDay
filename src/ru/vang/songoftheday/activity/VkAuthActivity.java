package ru.vang.songoftheday.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import ru.vang.songoftheday.R;
import ru.vang.songoftheday.SongOfTheDaySettings;
import ru.vang.songoftheday.fragment.AuthFragment;
import ru.vang.songoftheday.fragment.AuthFragment.OnAuthFinishListener;
import ru.vang.songoftheday.fragment.AuthFragment.OnProgressChangedListener;
import ru.vang.songoftheday.service.ThrottleUpdateService;
import ru.vang.songoftheday.util.AvailabilityUtils;

public class VkAuthActivity extends FragmentActivity implements
        OnProgressChangedListener, OnAuthFinishListener {

    private static final String TAG_AUTH_FRAGMENT = AuthFragment.class
            .getCanonicalName();

    private transient int mAppWidgetId;

    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setResult(RESULT_CANCELED);
        if (!AvailabilityUtils.isConnectionAvailable(getApplicationContext())) {
            final DialogFragment errorDialog = NetworkUnavailableDialog
                    .newInstance();
            errorDialog.show(getSupportFragmentManager(),
                    NetworkUnavailableDialog.TAG);
            return;
        }

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (savedInstanceState == null) {
            final boolean hideSkip = extras == null ? true : extras
                    .getBoolean(AuthFragment.EXTRA_HIDE_SKIP_BUTTON);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content,
                            AuthFragment.newInstance(hideSkip),
                            TAG_AUTH_FRAGMENT).commit();
        } else {
            final AuthFragment fragment = (AuthFragment) getSupportFragmentManager()
                    .findFragmentByTag(TAG_AUTH_FRAGMENT);
            if (fragment != null && fragment.isAuthDone()) {
                finishAuth(AuthFragment.STATUS_COMPLETED);
            }
        }
    }

    public void onProgressChanged(final int progress) {
        setProgress(progress * 100);
    }

    public void onAuthFinish(final int status) {
        finishAuth(status);
    }

    private void finishAuth(final int status) {
        final SharedPreferences preferences = getSharedPreferences(
                SongOfTheDaySettings.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        final Editor editor = preferences.edit();
        editor.putInt(SongOfTheDaySettings.PREF_KEY_AUTH_STATUS, status);
        editor.commit();

        final Intent serviceIntent = new Intent(VkAuthActivity.this,
                ThrottleUpdateService.class);
        serviceIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        startService(serviceIntent);

        if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            final Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    mAppWidgetId);
            setResult(RESULT_OK, resultValue);
        }

        finish();
    }

    public static class NetworkUnavailableDialog extends DialogFragment {

        public static final String TAG = NetworkUnavailableDialog.class
                .getSimpleName();

        public static NetworkUnavailableDialog newInstance() {
            return new NetworkUnavailableDialog();
        }

        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(
                    getActivity());
            builder.setTitle(R.string.error_dialog_title);
            builder.setMessage(R.string.network_unavailable_dialog_message);
            builder.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {

                        public void onClick(final DialogInterface dialog,
                                final int which) {
                            getActivity().finish();
                        }
                    }
            );

            return builder.create();
        }
    }
}
