package de.dala.simplenews.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.PagerSlidingTabStrip;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.News;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.database.IDatabaseHandler;
import de.dala.simplenews.dialog.ChangeLogDialog;
import de.dala.simplenews.parser.XmlParser;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;

public class MainActivity extends SherlockFragmentActivity implements ViewPager.OnPageChangeListener, NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final String DE_DALA_SIMPLENEWS = "de.dala.simplenews";
    public static final String XML_LOADED = "xmlLoaded";
    private static String TAG = "MainActivity";
    private IDatabaseHandler databaseHandler;
    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private MyPagerAdapter adapter;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private View progressView;
    private Drawable oldBackground = null;
    private int currentColor = 0xFF666666;
    private List<Category> categories;
    private RelativeLayout bottomView;
    private Crouton crouton;
    private int loadingNews = -1;
    private TextView progressText;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        databaseHandler = DatabaseHandler.getInstance(this);
        if (!xmlIsAlreadyLoaded()) {
            loadXml();
        }
        categories = databaseHandler.getCategories(null, null);

        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);
        tabs.setOnPageChangeListener(this);

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);
        bottomView = (RelativeLayout) findViewById(R.id.bottom_view);
        createProgressView();

        changeColor(currentColor);
    }

    private void loadXml() {
        try {
            News news = new XmlParser(this).readDefaultNewsFile();
            for (Category category : news.getCategories()) {
                if (category != null) {
                    databaseHandler.addCategory(category, false, false);
                }
            }
            saveLoading();
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Error in adding xml to Database");
            e.printStackTrace();
        } catch (IOException io) {
            Log.e(TAG, "Error in adding xml to Database");
            io.printStackTrace();
        }
    }

    private boolean xmlIsAlreadyLoaded() {
        SharedPreferences prefs = this.getSharedPreferences(
                DE_DALA_SIMPLENEWS, Context.MODE_PRIVATE);
        return prefs.getBoolean(XML_LOADED, false);
    }

    private void saveLoading() {
        SharedPreferences prefs = this.getSharedPreferences(
                DE_DALA_SIMPLENEWS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(XML_LOADED, true).commit();
    }

    private View createProgressView() {
        progressView = getLayoutInflater().inflate(R.layout.progress_layout, null);
        progressView.setBackgroundColor(currentColor);
        progressText = (TextView) progressView.findViewById(R.id.progress_text);
        progressText.setText(getString(R.string.update_news));
        return progressView;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DialogFragment dialog;
        switch (item.getItemId()) {
            case R.id.changelog:
                dialog = new ChangeLogDialog();
                dialog.show(getSupportFragmentManager(), "ChangeLog");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeColor(int newColor) {
        tabs.setIndicatorColor(newColor);

        Drawable colorDrawable = new ColorDrawable(newColor);
        Drawable bottomDrawable = getResources().getDrawable(R.drawable.actionbar_bottom);
        LayerDrawable ld = new LayerDrawable(new Drawable[]{colorDrawable, bottomDrawable});

        if (oldBackground == null) {
            getSupportActionBar().setBackgroundDrawable(ld);
        } else {
            //getSupportActionBar().setBackgroundDrawable(ld); //BUG otherwise
            TransitionDrawable td = new TransitionDrawable(new Drawable[] { oldBackground, ld });
            getSupportActionBar().setBackgroundDrawable(td);
            td.startTransition(400);
        }
        mNavigationDrawerFragment.changeColor(ld, newColor);

        progressView.setBackgroundColor(newColor);
        oldBackground = ld;
        currentColor = newColor;

        // http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentColor", currentColor);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentColor = savedInstanceState.getInt("currentColor");
        changeColor(currentColor);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }

    @Override
    public void onPageSelected(int i) {
        changeColor(categories.get(i).getColor());
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        switch (position) {
            case 0: //TODO open fragment, changing mtitle
                break;
        }
    }



    public void updateNews() {
        if (crouton != null) {
            crouton.cancel();
        }
        Configuration config = new Configuration.Builder().setOutAnimation(R.anim.abc_slide_out_bottom).setInAnimation(R.anim.abc_slide_in_bottom).setDuration(Configuration.DURATION_INFINITE).build();
        crouton = Crouton.make(this, createProgressView(), bottomView);
        crouton.setConfiguration(config);
        crouton.show();
    }

    public void showLoadingNews() {
        loadingNews++;
        if (loadingNews == 0) {
            updateNews();
        }
    }

    public void cancelLoadingNews() {
        loadingNews--;
        if (loadingNews >= -1) {
            crouton.cancel();
            loadingNews = -1;
        }
    }

    public void updateNews(String text, long categoryId) {
        if (progressText != null){
            progressText.setText(text);
        }
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return categories.get(position).getName();
        }

        @Override
        public int getCount() {
            return categories.size();
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = ExpandableNewsFragment.newInstance(categories.get(position));
            return fragment;
        }

    }
}