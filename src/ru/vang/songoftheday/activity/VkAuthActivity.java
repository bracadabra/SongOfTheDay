package ru.vang.songoftheday.activity;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.VkontakteApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import ru.vang.songoftheday.R;
import ru.vang.songoftheday.SongOfTheDaySettings;
import ru.vang.songoftheday.UpdateService;
import ru.vang.songoftheday.api.Vk;
import ru.vang.songoftheday.util.StringUtils;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class VkAuthActivity extends Activity {
	private static final String CALLBACK_LINK = "http://api.vk.com/blank.html";
	private static final String PERMISSIONS = "audio,offline";
	private static final String CODE_NAME = "code";

	private transient int mAppWidgetId;
	private transient OAuthService mAuthSevice;

	private OnClickListener mSkipClickListener = new OnClickListener() {

		public void onClick(View v) {
			finishActivity(SongOfTheDaySettings.PREF_KEY_SKIPPED, true);
		}
	};

	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		setResult(RESULT_CANCELED);
		setContentView(R.layout.vk_auth);

		final WebView webView = (WebView) findViewById(R.id.webView);
		setupWebView(webView);

		mAuthSevice = new ServiceBuilder().provider(VkontakteApi.class)
				.apiKey(Vk.CLIENT_ID).apiSecret(Vk.API_SECRET)
				.scope(PERMISSIONS).callback(CALLBACK_LINK).build();
		final String url = mAuthSevice.getAuthorizationUrl(null);
		webView.loadUrl(url);

		final View skipButton = findViewById(R.id.skip);
		skipButton.setOnClickListener(mSkipClickListener);

		final Intent intent = getIntent();
		final Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}
	}

	private void setupWebView(final WebView webView) {
		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				VkAuthActivity.this.setProgress(progress * 1000);
			}
		});
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(final WebView webView,
					final String url) {
				processUrl(webView, url);

				return false;
			}
		});

		final WebSettings webSettings = webView.getSettings();
		webSettings.setSavePassword(false);
	}

	private void processUrl(final WebView webView, final String url) {
		if (url.contains(CALLBACK_LINK)) {
			new AuthTask().execute(url);
		} else {
			webView.loadUrl(url);
		}
	}

	private void finishActivity(final String preferenceKey,
			final boolean preferenceValue) {
		final SharedPreferences preferences = getSharedPreferences(
				SongOfTheDaySettings.SHARED_PREF_NAME, Context.MODE_PRIVATE);
		final Editor editor = preferences.edit();
		editor.putBoolean(preferenceKey, preferenceValue);
		editor.commit();

		final Intent serviceIntent = new Intent(VkAuthActivity.this,
				UpdateService.class);
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

	private class AuthTask extends AsyncTask<String, Void, Token> {

		@Override
		protected Token doInBackground(final String... urls) {
			final String code = StringUtils.extractValueFromUrl(urls[0],
					CODE_NAME);
			final Verifier verifier = new Verifier(code);
			final Token accessToken = mAuthSevice
					.getAccessToken(null, verifier);

			return accessToken;
		}

		@Override
		protected void onPostExecute(final Token result) {
			Vk.saveToken(result, getFilesDir());
			finishActivity(SongOfTheDaySettings.PREF_KEY_COMPLETED, true);
		}
	}
}
