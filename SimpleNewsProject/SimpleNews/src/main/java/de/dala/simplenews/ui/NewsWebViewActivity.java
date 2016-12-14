package de.dala.simplenews.ui;

import android.annotation.TargetApi;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ocpsoft.pretty.time.PrettyTime;

import java.util.Date;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Entry;

public class NewsWebViewActivity extends BaseActivity {

    public static final String ENTRY_KEY = "entry";
    private static final String LOADING_KEY = "loading";
    private Entry mEntry;
    private WebView mWebView;
    private MenuItem mRefreshItem;
    private boolean isLoading;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_web_view);
        mWebView = (WebView) findViewById(R.id.web_view);
        setUpWebViewDefaults();

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                mEntry = null;
            } else {
                mEntry = (Entry) getIntent().getSerializableExtra(ENTRY_KEY);
            }
        } else {
            mWebView.restoreState(savedInstanceState);
            mEntry = (Entry) savedInstanceState.getSerializable(ENTRY_KEY);
            isLoading = savedInstanceState.getBoolean(LOADING_KEY);
            invalidateOptionsMenu();
            setRefreshActionButtonState(isLoading);
        }
        if (mEntry != null) {
            loadPage();

            String formattedDate = "";
            if (mEntry.getDate() != null) {
                long current = Math.min(new Date().getTime(), mEntry.getDate());
                formattedDate = new PrettyTime().format(new Date(current));
            }

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(mEntry.getTitle());
                actionBar.setSubtitle(String.format("%s - %s", mEntry.getSrcName(), formattedDate));
            }
        }
    }

    private void loadPage() {
        if (!isNetworkAvailable()) { // loading offline
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        mWebView.loadUrl(mEntry.getLink());
    }

    /**
     * Convenience method to set some generic defaults for a
     * given WebView
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setUpWebViewDefaults() {
        WebSettings settings = mWebView.getSettings();

        // Enable Javascript
        settings.setJavaScriptEnabled(true);

        // Use WideViewport and Zoom out if there is no viewport defined
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        // Enable pinch to zoom without the zoom buttons
        settings.setBuiltInZoomControls(true);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            // Hide the zoom controls for HONEYCOMB+
            settings.setDisplayZoomControls(false);
        }

        // Enable remote debugging via chrome://inspect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        // We set the WebViewClient to ensure links are consumed by the WebView rather
        // than passed to a browser if it can
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                setRefreshActionButtonState(progress < 100);
            }
        });

        mWebView.getSettings().setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAppCacheEnabled(true);
    }


    private void setRefreshActionButtonState(boolean refreshing) {
        if (mRefreshItem != null) {
            if (refreshing) {
                if (mRefreshItem.getActionView() == null) {
                    mRefreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
                }
            } else {
                mRefreshItem.setActionView(null);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(ENTRY_KEY, mEntry);
        outState.putBoolean(LOADING_KEY, isLoading);
        super.onSaveInstanceState(outState);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mRefreshItem = menu.findItem(R.id.menu_refresh);
        setRefreshActionButtonState(isLoading);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_news_web, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT); // load online by default
            mWebView.reload();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!getSupportFragmentManager().popBackStackImmediate()) {
            startNewsActivity();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
