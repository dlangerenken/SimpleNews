package de.dala.simplenews.ui;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import de.dala.simplenews.R;
import de.dala.simplenews.common.News;
import de.dala.simplenews.utilities.ColorManager;
import de.dala.simplenews.utilities.PrefUtilities;

public class BaseActivity extends AppCompatActivity {

    private Drawable oldBackgroundActivity = null;
    private Drawable oldBackgroundTabs = null;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setLastColor();

        if (!(this instanceof NewsActivity)) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowCustomEnabled(true);
                actionBar.setTitle(getString(R.string.simple_news_title));
            }
        }
    }

    private void setLastColor() {
        int primaryColor = PrefUtilities.getInstance().getCurrentColor();
        int secondaryColor = ColorManager.shiftColor(primaryColor);
        changeColor(primaryColor, secondaryColor);
    }

    protected void changeTabColor(Drawable drawable) {
    }

    public void changeColor(int primaryColor, int secondaryColor) {
        ColorDrawable colorDrawableActivity = new ColorDrawable(primaryColor);
        ColorDrawable colorDrawableTabs = new ColorDrawable(primaryColor);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            // http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayShowTitleEnabled(true);
            if (oldBackgroundActivity == null) {
                ab.setBackgroundDrawable(colorDrawableActivity);
                changeTabColor(colorDrawableTabs);
            } else {
                TransitionDrawable tdActivity = new TransitionDrawable(new Drawable[]{oldBackgroundActivity, colorDrawableActivity});
                TransitionDrawable tdTabs = new TransitionDrawable(new Drawable[]{oldBackgroundTabs, colorDrawableTabs});
                ab.setBackgroundDrawable(tdActivity);
                changeTabColor(tdTabs);
                tdActivity.startTransition(400);
                tdTabs.startTransition(400);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(secondaryColor);
            }
        }
        oldBackgroundActivity = colorDrawableActivity;
        oldBackgroundTabs = colorDrawableTabs;
        PrefUtilities.getInstance().saveCurrentColor(primaryColor);
    }


    protected void startCategory(String path) {
        Intent intent = new Intent(BaseActivity.this, CategoryActivity.class);
        if (path != null) {
            intent.putExtra(CategoryActivity.PATH, path);
        }
        startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_category:
                startCategory(null);
                return true;
            case R.id.action_opml:
                Intent intent = new Intent(BaseActivity.this, OpmlActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                intent = new Intent(BaseActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_rate:
                RateMyApp.showRateDialog(this);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
