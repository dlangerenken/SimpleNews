package de.dala.simplenews.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.dialog.ChangeLogDialog;
import de.dala.simplenews.utilities.BaseNavigation;
import de.dala.simplenews.utilities.ColorManager;

public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, FragmentManager.OnBackStackChangedListener {
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setTitle(getString(R.string.simple_news_title));
        }
        setupDrawer();
        overridePendingTransition(R.anim.open_translate, R.anim.close_scale);

        RateMyApp.appLaunched(this);

        if (savedInstanceState == null) {
            if (getIntent().getDataString() != null) {
                String path = getIntent().getDataString();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                currentFragment = CategoryModifierFragment.getInstance(path);
                transaction.replace(R.id.container, currentFragment).commit();
            } else {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                currentFragment = NewsOverViewFragment.getInstance(NewsOverViewFragment.ALL);
                transaction.replace(R.id.container, currentFragment).commit();
            }
        }else{
            if (savedInstanceState.containsKey("actionbar_color")) {
                int color = savedInstanceState.getInt("actionbar_color");
                changeColor(color);
            }
        }
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        updateNavigation();
    }

    @Override
    public boolean onSupportNavigateUp() {
        currentFragment = getVisibleFragment();
        if (currentFragment != null && currentFragment.getChildFragmentManager().getBackStackEntryCount() > 0){
            currentFragment.getChildFragmentManager().popBackStackImmediate();
        }else{
            //This method is called when the up button is pressed. Just the pop back stack.
            getSupportFragmentManager().popBackStackImmediate();
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        currentFragment = getVisibleFragment();
        boolean popped;
        if (currentFragment != null && currentFragment.getChildFragmentManager().getBackStackEntryCount() > 0){
            popped = currentFragment.getChildFragmentManager().popBackStackImmediate();
        }else{
            //This method is called when the up button is pressed. Just the pop back stack.
            popped = getSupportFragmentManager().popBackStackImmediate();
        }
        if (!popped) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //closing transition animations
        overridePendingTransition(R.anim.open_scale, R.anim.close_translate);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected void setupDrawer() {
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int item) {
        switch (item) {
            case NavigationDrawerFragment.HOME:
                clearBackStackKeep(0);
                currentFragment = NewsOverViewFragment.getInstance(NewsOverViewFragment.ALL);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, currentFragment).commit();
                break;
            case NavigationDrawerFragment.FAVORITE:
                clearBackStackKeep(0);
                currentFragment = NewsOverViewFragment.getInstance(NewsOverViewFragment.FAV);
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, currentFragment).commit();
                break;
            case NavigationDrawerFragment.RECENT:
                clearBackStackKeep(0);
                currentFragment = NewsOverViewFragment.getInstance(NewsOverViewFragment.RECENT);
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, currentFragment).commit();
                break;
            case NavigationDrawerFragment.UNREAD:
                clearBackStackKeep(0);
                currentFragment = NewsOverViewFragment.getInstance(NewsOverViewFragment.UNREAD);
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, currentFragment).commit();
                break;
            case NavigationDrawerFragment.CATEGORIES:
                clearBackStackKeep(1);
                currentFragment = CategoryModifierFragment.getInstance();
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
                transaction.replace(R.id.container, currentFragment).addToBackStack(null).commit();
                break;
            case NavigationDrawerFragment.SETTINGS:
                clearBackStackKeep(1);
                currentFragment = PrefFragment.getInstance();
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
                transaction.replace(R.id.container, currentFragment).addToBackStack(null).commit();
                break;
            case NavigationDrawerFragment.CHANGELOG:
                ChangeLogDialog dialog = new ChangeLogDialog();
                dialog.setDialogInterface(new DialogInterface() {
                    @Override
                    public void cancel() {
                        updateNavigation();
                    }

                    @Override
                    public void dismiss() {
                        updateNavigation();
                    }
                });
                dialog.show(getSupportFragmentManager(), "ChangeLog");
                break;
            case NavigationDrawerFragment.RATING:
                RateMyApp.showRateDialog(this);
                // TODO ondismiss
                break;
            case NavigationDrawerFragment.IMPORT:
                clearBackStackKeep(1);
                currentFragment = OpmlFragment.getInstance();
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
                transaction.replace(R.id.container, currentFragment).addToBackStack(null).commit();
                break;
        }
        updateNavigation();
    }


    private void clearBackStackKeep(int toKeep){
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > toKeep) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(toKeep);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        updateNavigation();
    }

    private void clearBackStack(){
        clearBackStackKeep(0);
    }



    public void updateNavigation() {
        currentFragment = getVisibleFragment();
        if (currentFragment != null && currentFragment instanceof BaseNavigation){
            BaseNavigation navigation = (BaseNavigation) currentFragment;
            mNavigationDrawerFragment.checkItem(navigation.getNavigationDrawerId());
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(navigation.getTitle());
            }
        }
    }

    public Fragment getVisibleFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for(Fragment fragment : fragments){
            if(fragment != null && fragment.isAdded() && !(fragment instanceof NavigationDrawerFragment)) {
                return fragment;
            }
        }
        return null;
    }

    @Override
    public void onBackStackChanged() {
        Fragment visibleFragment = getVisibleFragment();
        if (visibleFragment instanceof NewsOverViewFragment){
            ((NewsOverViewFragment)visibleFragment).onBackStackChanged();
        }
        updateNavigation();
    }

    public void updateNavigation(int navigationDrawerId, String title) {
        mNavigationDrawerFragment.checkItem(navigationDrawerId);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("actionbar_color", lastColor);
    }

    private Drawable oldBackground = null;
    private int lastColor;

    public void changeColor(int color) {
        ColorDrawable colorDrawable = new ColorDrawable(color);
        lastColor = color;
        if (oldBackground == null) {
            getSupportActionBar().setBackgroundDrawable(colorDrawable);
        } else {
            //getSupportActionBar().setBackgroundDrawable(ld); //BUG otherwise
            TransitionDrawable td = new TransitionDrawable(new Drawable[]{oldBackground, colorDrawable});
            getSupportActionBar().setBackgroundDrawable(td);
            td.startTransition(400);
        }
        oldBackground = colorDrawable;
        changeDrawerColor(color);
    }

    private void changeDrawerColor(int newColor) {
        mNavigationDrawerFragment.changeColor(newColor);
    }

}