package de.dala.simplenews.ui;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

import de.dala.simplenews.R;
import de.dala.simplenews.common.NavDrawerItem;
import de.dala.simplenews.utilities.NavDrawerListAdapter;
import de.dala.simplenews.utilities.PrefUtilities;


/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment implements FragmentManager.OnBackStackChangedListener {
    public static final int HOME = 0;
    public static final int CATEGORIES = 1;
    public static final int CHANGELOG = 2;
    public static final int SETTINGS = 3;
    public static final int RATING = 4;
    public static final int IMPORT = 5;

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;
    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;
    private LinearLayout mDrawerView;
    private View verticalLine;
    private ListView mDrawerList;
    private NavDrawerListAdapter navDrawAdapter;

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    private ArrayList<NavDrawerItem> navDrawerItems;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getSupportFragmentManager().removeOnBackStackChangedListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDrawerView = (LinearLayout) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        mDrawerList = (ListView) mDrawerView.findViewById(R.id.left_drawer);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NavDrawerItem item = (NavDrawerItem) parent.getItemAtPosition(position);
                switch (item.getType()){
                    case NavDrawerItem.MAIN_ITEM:
                        selectItem(item);
                        break;
                    case NavDrawerItem.SETTING_ITEM:
                        selectItem(item);
                        break;
                }
            }
        });
        verticalLine = mDrawerView.findViewById(R.id.vertical_line);
        return mDrawerView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }


    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        initNavDrawerAdapter();

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // set up the drawer's list view with items and click listener
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_navigation_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }
                getActivity().supportInvalidateOptionsMenu();// calls onPrepareOptionsMenu()
                setActionBarArrowDependingOnFragmentsBackStack();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!PrefUtilities.getInstance().hasUserLearnedDrawer()) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    PrefUtilities.getInstance().setUserLearnedDrawer(true);
                }
                getActivity().supportInvalidateOptionsMenu();// calls onPrepareOptionsMenu()
                mDrawerToggle.setDrawerIndicatorEnabled(true);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!PrefUtilities.getInstance().hasUserLearnedDrawer()) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
            PrefUtilities.getInstance().setUserLearnedDrawer(true);
        }
        updateDrawerToggle();
    }

    // Defer code dependent on restoration of previous instance state.
    public void updateDrawerToggle(boolean drawerIndicatorEnabled) {
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        mDrawerToggle.setDrawerIndicatorEnabled(drawerIndicatorEnabled);
    }

    // Defer code dependent on restoration of previous instance state.
    public void updateDrawerToggle() {
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }


    private void initNavDrawerAdapter() {
        if (isAdded()) {
            // load slide menu items
            navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
            // nav drawer icons from resources
            navMenuIcons = getResources()
                    .obtainTypedArray(R.array.nav_drawer_icons);
            navDrawerItems = new ArrayList<NavDrawerItem>();
            navDrawerItems.add(new NavDrawerItem(HOME, navMenuTitles[HOME], navMenuIcons.getResourceId(HOME, -1)));
            navDrawerItems.add(new NavDrawerItem(CATEGORIES, navMenuTitles[CATEGORIES], navMenuIcons.getResourceId(CATEGORIES, -1)));
            navDrawerItems.add(new NavDrawerItem(RATING, navMenuTitles[RATING], navMenuIcons.getResourceId(RATING, -1)));

            // navDrawerItems.add(new NavDrawerItem(-1, navMenuTitles[SETTINGS], -1, NavDrawerItem.HEADER));
            navDrawerItems.add(new NavDrawerItem(CHANGELOG, navMenuTitles[CHANGELOG], navMenuIcons.getResourceId(CHANGELOG, -1), NavDrawerItem.SETTING_ITEM));
            navDrawerItems.add(new NavDrawerItem(SETTINGS, navMenuTitles[SETTINGS], navMenuIcons.getResourceId(SETTINGS, -1), NavDrawerItem.SETTING_ITEM));
            navDrawerItems.add(new NavDrawerItem(IMPORT, navMenuTitles[IMPORT], navMenuIcons.getResourceId(IMPORT, -1), NavDrawerItem.SETTING_ITEM));

            navMenuIcons.recycle();
            // set up the drawer's list view with items and click listener
            navDrawAdapter = new NavDrawerListAdapter(getActivity(), navDrawerItems);
            mDrawerList.setAdapter(navDrawAdapter);
        }
    }

    private void selectItem(NavDrawerItem item) {
        //checkItem(position, true);

        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(item.getId());
        }
    }

    public void checkItem(int position, boolean checkPosition) {
        if (mDrawerList != null) {
            mDrawerList.setItemChecked(position, checkPosition);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.main, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.isDrawerIndicatorEnabled() &&
                mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()){
            case R.id.home:
                boolean popBackSuccessful = getActivity().getSupportFragmentManager().popBackStackImmediate();
                if (popBackSuccessful){
                    return true;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    public void changeColor(int color) {
        Drawable mColorDrawable = new ColorDrawable(color);
        verticalLine.setBackgroundColor(color);
        mDrawerList.setDivider(new ColorDrawable(color));
        mDrawerList.setDividerHeight(1);
        navDrawAdapter.setCategoryDrawable(mColorDrawable);
    }

    public void lock(boolean shouldLock) {
        mDrawerLayout.setDrawerLockMode(shouldLock ?  DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         *
         * @param item
         */
        void onNavigationDrawerItemSelected(int item);
    }

    @Override
    public void onBackStackChanged() {
        setActionBarArrowDependingOnFragmentsBackStack();
    }
    private void setActionBarArrowDependingOnFragmentsBackStack() {
        int backStackEntryCount = getActivity().getSupportFragmentManager().getBackStackEntryCount();
        mDrawerToggle.setDrawerIndicatorEnabled(backStackEntryCount == 0);
    }


}
