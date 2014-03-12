package ru.vang.songoftheday.fragment;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.VkontakteApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ru.vang.songoftheday.R;
import ru.vang.songoftheday.api.Vk;
import ru.vang.songoftheday.util.StringUtils;

//TODO use vk sdk
public class AuthFragment extends Fragment {

    public static final String EXTRA_HIDE_SKIP_BUTTON
            = "ru.vang.songoftheday.EXTRA_HIDE_SKIP_BUTTON";

    public static final int STATUS_COMPLETED = 0;

    public static final int STATUS_SKIPPED = -1;

    public static final int STATUS_INCOMPLETE = -2;

    private static final String CALLBACK_LINK = "http://api.vk.com/blank.html";

    private static final String PERMISSIONS = "audio,offline";

    private static final String CODE_NAME = "code";

    private AuthTask mAuthTask;

    private WebView mWebView;

    private transient OAuthService mAuthSevice;

    private final OnClickListener mSkipClickListener = new OnClickListener() {

        public void onClick(final View view) {
            mOnAuthFinishListener.onAuthFinish(STATUS_SKIPPED);
        }
    };

    private OnProgressChangedListener mOnProgressChangedListener;

    private OnAuthFinishListener mOnAuthFinishListener;

    public interface OnProgressChangedListener {

        public void onProgressChanged(int progress);
    }

    public interface OnAuthFinishListener {

        public void onAuthFinish(int status);
    }

    public static AuthFragment newInstance(final boolean hideSkipButton) {
        final AuthFragment fragment = new AuthFragment();
        final Bundle args = new Bundle(1);
        args.putBoolean(EXTRA_HIDE_SKIP_BUTTON, hideSkipButton);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        mOnProgressChangedListener = (OnProgressChangedListener) activity;
        mOnAuthFinishListener = (OnAuthFinishListener) activity;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mAuthSevice = new ServiceBuilder().provider(VkontakteApi.class)
                .apiKey(Vk.CLIENT_ID).apiSecret(Vk.API_SECRET)
                .scope(PERMISSIONS).callback(CALLBACK_LINK).build();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.vk_auth, container, false);
        mWebView = (WebView) view.findViewById(R.id.web_view);
        setupWebView(mWebView);
        final View skipButton = view.findViewById(R.id.skip);
        if (getArguments().getBoolean(EXTRA_HIDE_SKIP_BUTTON, false)) {
            skipButton.findViewById(R.id.skip).setVisibility(View.GONE);
        } else {
            skipButton.setOnClickListener(mSkipClickListener);
        }

        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            final String url = mAuthSevice.getAuthorizationUrl(null);
            mWebView.loadUrl(url);
        } else {
            mWebView.restoreState(savedInstanceState);
        }
    }

    @Override
    public void onDetach() {
        mOnProgressChangedListener = null;
        mOnAuthFinishListener = null;

        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        mWebView.saveState(outState);
    }

    public boolean isAuthDone() {
        return mAuthTask != null
                && mAuthTask.getStatus() == AsyncTask.Status.FINISHED;
    }

    private void setupWebView(final WebView webView) {
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(final WebView view, final int progress) {
                if (mOnProgressChangedListener != null) {
                    mOnProgressChangedListener.onProgressChanged(progress);
                }
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(final WebView webView,
                    final String url) {
                if (url.contains(CALLBACK_LINK)) {
                    mAuthTask = new AuthTask();
                    mAuthTask.execute(url);
                    return true;
                }

                webView.loadUrl(url);
                return false;
            }
        });
    }

    private class AuthTask extends AsyncTask<String, Void, Void> {

        private static final int URL_INDEX = 0;

        @Override
        protected Void doInBackground(final String... urls) {
            final String code = StringUtils.extractValueFromUrl(
                    urls[URL_INDEX], CODE_NAME);
            final Verifier verifier = new Verifier(code);
            final Token accessToken = mAuthSevice
                    .getAccessToken(null, verifier);
            Vk.saveToken(getActivity().getApplicationContext(), accessToken);

            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            if (mOnAuthFinishListener != null) {
                mOnAuthFinishListener.onAuthFinish(STATUS_COMPLETED);
            }
        }
    }
}
