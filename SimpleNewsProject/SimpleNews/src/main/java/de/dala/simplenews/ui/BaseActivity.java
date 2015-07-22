package de.dala.simplenews.ui;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
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
import de.dala.simplenews.utilities.ColorManager;
import de.dala.simplenews.utilities.PrefUtilities;

public abstract class BaseActivity extends AppCompatActivity {

    private static final int DURATION = 400;
    private Drawable oldBackgroundActivity = null;
    private Drawable oldBackgroundTabs = null;
    private Integer oldStatusBarColor = null;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getTitle());
            if (!(this instanceof NewsActivity)) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowCustomEnabled(true);
            }
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        setLastColor();
    }

    private void setLastColor() {
        int primaryColor = PrefUtilities.getInstance().getCurrentColor();
        int secondaryColor = ColorManager.shiftColor(primaryColor);
        changeColor(primaryColor, secondaryColor);
    }

    void changeTabColor(Drawable drawable) {
    }

    public void changeColor(int primaryColor, int secondaryColor) {
        ColorDrawable colorDrawableActivity = new ColorDrawable(primaryColor);
        ColorDrawable colorDrawableTabs = new ColorDrawable(primaryColor);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            // http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
            //ab.setDisplayShowTitleEnabled(false);
            //ab.setDisplayShowTitleEnabled(true);
            if (oldBackgroundActivity == null) {
                ab.setBackgroundDrawable(colorDrawableActivity);
                changeTabColor(colorDrawableTabs);
            } else {
                TransitionDrawable tdActivity = new TransitionDrawable(new Drawable[]{oldBackgroundActivity, colorDrawableActivity});
                TransitionDrawable tdTabs = new TransitionDrawable(new Drawable[]{oldBackgroundTabs, colorDrawableTabs});
                ab.setBackgroundDrawable(tdActivity);
                changeTabColor(tdTabs);
                tdActivity.startTransition(DURATION);
                tdTabs.startTransition(DURATION);
            }

            animateStatusBar(secondaryColor);
        }
        oldBackgroundActivity = colorDrawableActivity;
        oldBackgroundTabs = colorDrawableTabs;
        PrefUtilities.getInstance().saveCurrentColor(primaryColor);
    }

    private void animateStatusBar(final int secondaryColor) {
        final Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (oldStatusBarColor == null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(secondaryColor);
            } else {
                // animation here
                ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        // Use animation position to blend colors.
                        float position = animation.getAnimatedFraction();

                        // Apply blended color to the status bar.
                        int blended = blendColors(oldStatusBarColor, secondaryColor, position);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            window.setStatusBarColor(blended);
                        }
                    }
                });
                anim.setDuration(DURATION).start();
            }
        }
        oldStatusBarColor = secondaryColor;
    }

    private int blendColors(int from, int to, float ratio) {
        final float inverseRatio = 1f - ratio;

        final float r = Color.red(to) * ratio + Color.red(from) * inverseRatio;
        final float g = Color.green(to) * ratio + Color.green(from) * inverseRatio;
        final float b = Color.blue(to) * ratio + Color.blue(from) * inverseRatio;

        return Color.rgb((int) r, (int) g, (int) b);
    }

    void startCategory(String path) {
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
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
