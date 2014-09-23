package io.seal.swarmwear.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.devspark.progressfragment.ProgressFragment;
import io.seal.swarmwear.BuildConfig;
import io.seal.swarmwear.BusProvider;
import io.seal.swarmwear.EventManager;
import io.seal.swarmwear.R;
import io.seal.swarmwear.event.FoursquareAccessTokenAvailableEvent;
import io.seal.swarmwear.networking.Foursquare;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebLoginFragment extends ProgressFragment {

    private static final String TAG = "LoginFragment";

    private static final String PARAM_VALUE_REGEXP = "[?&]*access_token=(.+)[?&]*";

    private WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mWebView = new WebView(getActivity());

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new FoursquareLoginWebViewClient());
        mWebView.loadUrl(Foursquare.AUTH_URL);

        setContentView(mWebView);

    }

    @Override
    public void onResume() {
        super.onResume();
        EventManager.trackScreenView(TAG);
    }

    @Override
    public void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            refresh();
        }

        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        mWebView.loadUrl(mWebView.getUrl());
    }

    private void logDebug(String action) {
        EventManager.handleEvent(TAG, action, Log.DEBUG);
    }

    private class FoursquareLoginWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            setContentShown(false);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            setContentShown(true);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            logDebug("shouldOverrideUrlLoading");
            logDebug("url: " + url);

            if (!url.contains(Foursquare.UNIVERSAL_HOST)) {
                Uri uri = Uri.parse(url);

                Pattern pattern = Pattern.compile(PARAM_VALUE_REGEXP);
                String uriFragment = uri.getFragment();

                if (TextUtils.isEmpty(uriFragment)) {
                    EventManager.handleEvent(TAG, "uri fragment is empty", Log.WARN);
                    loadLogin(view);
                } else {
                    Matcher matcher = pattern.matcher(uriFragment);

                    if (matcher.find()) {
                        String accessToken = matcher.group(1);

                        if (BuildConfig.DEBUG) {
                            logDebug("accessToken: " + accessToken);
                        }

                        Foursquare.saveAccessToken(view.getContext(), accessToken);
                        BusProvider.getInstance().post(new FoursquareAccessTokenAvailableEvent());
                    }
                }
            }

            return false; // do NOT leave current application
        }

        private void loadLogin(WebView view) {
            view.loadUrl(Foursquare.AUTH_URL);
        }

    }

}
