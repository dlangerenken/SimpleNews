package de.dala.simplenews.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;

import de.dala.simplenews.R;

public class NewsActivity extends BaseActivity {

    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setTitle(getString(R.string.simple_news_title));
        }

        RateMyApp.appLaunched(this);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (getIntent().getDataString() != null) {
                String path = getIntent().getDataString();
                startCategory(path);
                return;
            }
            transaction.replace(R.id.container, NewsOverViewFragment.getInstance(NewsOverViewFragment.ALL)).commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.open_scale, R.anim.close_translate);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void changeTabColor(Drawable drawable) {
        if (mTabLayout != null) {
            mTabLayout.setBackground(drawable);
        }
    }

    public TabLayout getTabLayout() {
        return mTabLayout;
    }
}
