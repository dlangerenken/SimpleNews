package de.dala.simplenews.ui;

import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;

import de.dala.simplenews.R;
import de.dala.simplenews.dialog.ChangeLogDialog;
import de.dala.simplenews.parser.XmlParser;

public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static String TAG = "MainActivity";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        setupDrawer();

        getSupportActionBar().setTitle(getString(R.string.simple_news_title));
        overridePendingTransition(R.anim.open_translate, R.anim.close_scale);

        RateMyApp.appLaunched(this);

        if (savedInstanceState == null) {
            if (getIntent().getDataString() != null) {
                String path = getIntent().getDataString();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                Fragment categoryModifierFrag = CategoryModifierFragment.getInstance(path);
                transaction.replace(R.id.container, categoryModifierFrag).commit();
            } else {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                Fragment newsFrag = NewsOverViewFragment.getInstance(NewsTypeBar.ALL);
                transaction.replace(R.id.container, newsFrag).commit();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        //This method is called when the up button is pressed. Just the pop back stack.
        getSupportFragmentManager().popBackStack();
        return true;
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
                clearBackStack();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, NewsOverViewFragment.getInstance(NewsTypeBar.ALL)).commit();
                break;
            case NavigationDrawerFragment.CATEGORIES:
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
                transaction.replace(R.id.container, CategoryModifierFragment.getInstance()).addToBackStack(null).commit();
                break;
            case NavigationDrawerFragment.SETTINGS:
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, PrefFragment.getInstance()).addToBackStack(null).commit();
                break;
            case NavigationDrawerFragment.CHANGELOG:
                DialogFragment dialog = new ChangeLogDialog();
                dialog.show(getSupportFragmentManager(), "ChangeLog");
                break;
            case NavigationDrawerFragment.RATING:
                RateMyApp.showRateDialog(this);
                break;
        }
    }


    private void clearBackStack(){
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    protected void changeDrawerColor(LayerDrawable ld, int newColor) {
        mNavigationDrawerFragment.changeColor(ld, newColor);
    }

}