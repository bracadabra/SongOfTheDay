package ru.vang.songoftheday;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.VkontakteApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import ru.vang.songoftheday.api.Vk;
import ru.vang.songoftheday.model.WidgetModel;
import ru.vang.songoftheday.util.Settings;
import ru.vang.songoftheday.util.StringUtils;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class VkAuthActivity extends Activity {
	private static final String CALLBACK_LINK = "http://api.vk.com/blank.html";
	private static final String PERMISSIONS = "audio,offline";
	private static final String CODE_NAME = "code";

	private transient int mAppWidgetId;
	private transient OAuthService mAuthSevice;

	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		setResult(RESULT_CANCELED);

		final WebView webview = new WebView(this);
		setContentView(webview);
		webview.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				VkAuthActivity.this.setProgress(progress * 1000);
			}
		});
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(final WebView webView, final String url) {
				processUrl(webView, url);

				return false;
			}
		});

		final Intent intent = getIntent();
		final Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		mAuthSevice = new ServiceBuilder().provider(VkontakteApi.class).apiKey(Vk.CLIENT_ID).apiSecret(Vk.API_SECRET)
				.scope(PERMISSIONS).callback(CALLBACK_LINK).build();
		final String url = mAuthSevice.getAuthorizationUrl(null);
		webview.loadUrl(url);
	}

	private void processUrl(final WebView webView, final String url) {
		if (url.contains(CALLBACK_LINK)) {
			new AuthTask().execute(url);
		} else {
			webView.loadUrl(url);
		}
	}

	private class AuthTask extends AsyncTask<String, Void, Token> {

		@Override
		protected Token doInBackground(String... urls) {
			final String code = StringUtils.extractValueFromUrl(urls[0], CODE_NAME);
			final Verifier verifier = new Verifier(code);
			final Token accessToken = mAuthSevice.getAccessToken(null, verifier);

			return accessToken;
		}

		@Override
		protected void onPostExecute(Token result) {
			Vk.saveToken(result, getFilesDir());

			final SharedPreferences preferences = getSharedPreferences(Settings.SHARED_PREF_NAME, Context.MODE_PRIVATE);
			final Editor editor = preferences.edit();
			editor.putBoolean(Settings.PREF_KEY_COMPLETED, true);
			editor.commit();

			final Intent serviceIntent = new Intent(VkAuthActivity.this, UpdateService.class);
			serviceIntent.setAction(WidgetModel.ACTION_UPDATE);
			startService(serviceIntent);

			if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				final Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				setResult(RESULT_OK, resultValue);
			}

			finish();
		}
	}
}
