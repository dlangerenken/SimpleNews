package de.dala.simplenews;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.os.Build;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.astuetz.PagerSlidingTabStrip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidrss.*;
import androidrss.RSSFeed;
import de.dala.simplenews.database.IDatabaseHandler;
import de.dala.simplenews.database.MockDatabaseHandler;
import de.dala.simplenews.dialog.ChangeLogDialog;
import de.dala.simplenews.network.NetworkCommunication;

public class MainActivity extends ActionBarActivity implements ViewPager.OnPageChangeListener{

    private IDatabaseHandler databaseHandler;

    private final Handler handler = new Handler();

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private MyPagerAdapter adapter;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private LinearLayout linearDrawer;
    public ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListLeft;
    private NavDrawerAdapter navDrawAdapter;
    private View verticalLine;

    private Drawable oldBackground = null;
    private int currentColor = 0xFF666666;

    private List<Category> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupDrawer(savedInstanceState);

        databaseHandler = new MockDatabaseHandler();
        categories = databaseHandler.getCategories();

        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);
        tabs.setOnPageChangeListener(this);

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);
        changeColor(currentColor);
    }


    private void setupDrawer(Bundle savedInstanceState) {
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListLeft = (ListView) findViewById(R.id.left_drawer);
        verticalLine = findViewById(R.id.vertical_line);
        linearDrawer = (LinearLayout) findViewById(R.id.linear_drawer);
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        // set up the drawer's list view with items and click listener
        navDrawAdapter = new NavDrawerAdapter(this);
        mDrawerListLeft.setAdapter(navDrawAdapter);
        addItemsToAdapter();

        mDrawerListLeft.setOnItemClickListener(new DrawerItemClickListener());

        // ActionBarDrawerToggle ties together the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.drawable.ic_navigation_drawer, /*
										 * nav drawer image to replace 'Up'
										 * caret
										 */
                R.string.drawer_open, /* "open drawer" description for accessibility */
                R.string.drawer_close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                // getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                // getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();
        if (savedInstanceState == null) {
            // selectItem(0);
        }
    }


    private void addItemsToAdapter() {
        navDrawAdapter.addItem(new NavDrawItem("Coming soon", NavDrawItem.Type.CATEGORY));
        /*navDrawAdapter.addItem(new NavDrawItem("Tees", NavDrawItem.Type.CATEGORY));
        navDrawAdapter.addItem(new NavDrawItem("Today's Tee", NavDrawItem.Type.ENTRY));
        navDrawAdapter.addItem(new NavDrawItem("Last Chance Tee", NavDrawItem.Type.ENTRY));
        navDrawAdapter.addItem(new NavDrawItem("Previous Tees", NavDrawItem.Type.ENTRY));
        navDrawAdapter.addItem(new NavDrawItem("Free Tees", NavDrawItem.Type.ENTRY));
        */
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		/*
		 * If the nav drawer is open, hide action items related to the content
		 * view
		 */
        @SuppressWarnings("unused")
          boolean leftDrawerOpen = mDrawerLayout.isDrawerOpen(linearDrawer);
         //menu.findItem(R.id.action_settings).setVisible(!leftDrawerOpen); //TODO

        return super.onPrepareOptionsMenu(menu);
    }

    private void selectItem(int position) {
        // update selected item then close the drawer

        FragmentManager manager = getSupportFragmentManager();

        switch (position) {
            case 0:
              //  manager.beginTransaction()
              //          .replace(R.id.content, new BuyableTeesViewFragment())
              //          .commit();
                break;

        }

        // .setItemChecked(position, true);
        // setTitle(categories[position]);
        mDrawerLayout.closeDrawer(linearDrawer);
    }

    public IDatabaseHandler getDatabaseHandler() {
        return databaseHandler;
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            selectItem(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DialogFragment dialog;
        switch (item.getItemId()) {

            //case R.id.action_contact:
            //    dialog = new QuickContactFragment();
            //    dialog.show(getSupportFragmentManager(), "QuickContactFragment");
            //    return true;
            case R.id.changelog:
                dialog = new ChangeLogDialog();
                dialog.show(getSupportFragmentManager(), "ChangeLog");
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void changeColor(int newColor) {
        tabs.setIndicatorColor(newColor);

        // change ActionBar color just if an ActionBar is available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            Drawable colorDrawable = new ColorDrawable(newColor);
            Drawable bottomDrawable = getResources().getDrawable(R.drawable.actionbar_bottom);
            LayerDrawable ld = new LayerDrawable(new Drawable[] { colorDrawable, bottomDrawable });

            if (oldBackground == null) {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    ld.setCallback(drawableCallback);
                } else {
                  //  getActionBar().setBackgroundDrawable(ld);
                }
            } else {
                TransitionDrawable td = new TransitionDrawable(new Drawable[] { oldBackground, ld });

                // workaround for broken ActionBarContainer drawable handling on
                // pre-API 17 builds
                // https://github.com/android/platform_frameworks_base/commit/a7cc06d82e45918c37429a59b14545c6a57db4e4
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    td.setCallback(drawableCallback);
                } else {
                  getActionBar().setBackgroundDrawable(td);
                }
                td.startTransition(200);
            }
            navDrawAdapter.setCategoryDrawable(colorDrawable);
            verticalLine.setBackgroundColor(newColor);
            oldBackground = ld;

            // http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
            getActionBar().setDisplayShowTitleEnabled(false);
            getActionBar().setDisplayShowTitleEnabled(true);
        }
        currentColor = newColor;
    }

    public void onColorClicked(View v) {
        int color = Color.parseColor(v.getTag().toString());
        changeColor(color);
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

    private Drawable.Callback drawableCallback = new Drawable.Callback() {
        @Override
        public void invalidateDrawable(Drawable who) {
            getActionBar().setBackgroundDrawable(who);
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
            handler.postAtTime(what, when);
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
            handler.removeCallbacks(what);
        }
    };

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
            Fragment fragment = NewsCardFragment.newInstance(position, categories.get(position));
            return fragment;
        }

    }



}