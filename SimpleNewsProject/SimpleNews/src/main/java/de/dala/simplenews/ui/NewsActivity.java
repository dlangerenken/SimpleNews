package de.dala.simplenews.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.Collections;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.utilities.PrefUtilities;

public class NewsActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    private TabLayout mTabLayout;
    private ViewPager pager;

    private List<Category> categories;
    private int entryType = ALL;
    private MenuItem columnsMenu;

    public static final int ALL = 0;
    public static final int FAV = 1;
    public static final int RECENT = 2;
    private int[] actionButtonIds;

    private FloatingActionMenu newsTypeButton;
    private FloatingActionButton subactionButton1;
    private FloatingActionButton subactionButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            if (getIntent().getDataString() != null) {
                String path = getIntent().getDataString();
                startCategory(path);
                return;
            }
        }

        entryType = PrefUtilities.getInstance().getNewsTypeMode();
        setContentView(R.layout.activity_news);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
        }

        onCreateNewsView(savedInstanceState);
        RateMyApp.appLaunched(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.open_scale, R.anim.close_translate);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        columnsMenu = menu.findItem(R.id.menu_columns);
        updateMenu();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void changeTabColor(Drawable drawable) {
        if (mTabLayout != null) {
            mTabLayout.setBackground(drawable);
        }
    }

    private void updateMenu() {
        boolean useMultiple = shouldUseMultipleColumns();
        if (columnsMenu != null) {
            columnsMenu.setTitle(useMultiple ? getString(R.string.single_columns) : getString(R.string.multiple_columns));
        }
    }

    private boolean shouldUseMultipleColumns() {
        boolean useMultiple = false;
        if (getResources() != null) {
            android.content.res.Configuration config = getResources().getConfiguration();
            if (config != null) {
                switch (config.orientation) {
                    case android.content.res.Configuration.ORIENTATION_LANDSCAPE:
                        useMultiple = PrefUtilities.getInstance().useMultipleColumnsLandscape();
                        break;
                    case android.content.res.Configuration.ORIENTATION_PORTRAIT:
                        useMultiple = PrefUtilities.getInstance().useMultipleColumnsPortrait();
                        break;
                }
            }
        } else {
            useMultiple = PrefUtilities.getInstance().useMultipleColumnsPortrait();
        }

        return useMultiple;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_columns:
                if (getResources() != null) {
                    android.content.res.Configuration config = getResources().getConfiguration();
                    if (config != null) {
                        switch (config.orientation) {
                            case android.content.res.Configuration.ORIENTATION_LANDSCAPE:
                                PrefUtilities.getInstance().setMultipleColumnsLandscape(!PrefUtilities.getInstance().useMultipleColumnsLandscape());
                                break;
                            case android.content.res.Configuration.ORIENTATION_PORTRAIT:
                                PrefUtilities.getInstance().setMultipleColumnsPortrait(!PrefUtilities.getInstance().useMultipleColumnsPortrait());
                                break;
                        }
                    }
                }

                updateMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onCreateNewsView(Bundle savedInstanceState) {
        pager = (ViewPager) findViewById(R.id.pager);
        initNewsTypeIcon();

        if (!PrefUtilities.getInstance().xmlIsAlreadyLoaded()) {
            DatabaseHandler.getInstance().loadXmlIntoDatabase(R.raw.categories);
        }

        categories = DatabaseHandler.getInstance().getCategories(null, null, true);
        if (categories.isEmpty()) {
            categories.add(new Category());
        }
        Collections.sort(categories);
        if (savedInstanceState != null) {
            entryType = savedInstanceState.getInt("entryType", ALL);
            newsTypeModeChanged();
        }

        if (categories != null && !categories.isEmpty()) {
            initAdapterAndPager();
        }
    }

    private void initAdapterAndPager() {
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(this);
        mTabLayout.setupWithViewPager(pager);
        int page = PrefUtilities.getInstance().getCategoryIndex();
        pager.setCurrentItem(page);
        onPageSelected(page);
    }


    private void setColor(FloatingActionButton button, int primaryColor, int secondaryColor) {
        button.setColorNormal(primaryColor);
        button.setColorPressed(secondaryColor);
        button.setColorRipple(secondaryColor);
    }

    private void setColor(FloatingActionMenu menu, int primaryColor, int secondaryColor) {
        menu.setMenuButtonColorNormal(primaryColor);
        menu.setMenuButtonColorPressed(secondaryColor);
        menu.setMenuButtonColorRipple(secondaryColor);
    }


    private void initNewsTypeIcon() {
        newsTypeButton = (FloatingActionMenu) findViewById(R.id.floating_action_menu);
        subactionButton1 = (FloatingActionButton) findViewById(R.id.menu_item_1);
        subactionButton2 = (FloatingActionButton) findViewById(R.id.menu_item_2);

        subactionButton1.setOnClickListener(new OnSubActionButtonClickListener());
        subactionButton2.setOnClickListener(new OnSubActionButtonClickListener());

        actionButtonIds = new int[]{R.drawable.ic_home, R.drawable.ic_fav, R.drawable.ic_seen};
        updateNewsIcon();
    }

    private void updateNewsIcon() {
        newsTypeButton.getMenuIconView().setImageDrawable(ContextCompat.getDrawable(this, actionButtonIds[entryType % 3]));
        subactionButton1.setImageDrawable(ContextCompat.getDrawable(this, actionButtonIds[(entryType + 1) % 3]));
        subactionButton2.setImageDrawable(ContextCompat.getDrawable(this, actionButtonIds[(entryType + 2) % 3]));
        newsTypeButton.setTag((entryType));
        subactionButton1.setTag((entryType + 1) % 3);
        subactionButton2.setTag((entryType + 2) % 3);
    }

    @Override
    protected void onStop() {
        super.onStop();
        newsTypeButton.close(false);
        if (pager != null) {
            PrefUtilities.getInstance().setCategoryIndex(pager.getCurrentItem());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("entryType", entryType);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }

    @Override
    public void onPageSelected(int i) {
        if (categories != null && categories.size() > i) {
            Category category = categories.get(i);
            changeColor(category.getPrimaryColor(), category.getSecondaryColor());
        }
    }

    @Override
    public void changeColor(int primaryColor, int secondaryColor) {
        super.changeColor(primaryColor, secondaryColor);
        setColor(subactionButton1, primaryColor, secondaryColor);
        setColor(subactionButton2, primaryColor, secondaryColor);
        setColor(newsTypeButton, primaryColor, secondaryColor);
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {

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
            Category category = categories.get(position);
            return ExpandableNewsFragment.newInstance(category, entryType, position);
        }

    }

    private class OnSubActionButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            entryType = (Integer) v.getTag();
            updateNewsIcon();
            newsTypeModeChanged();
            newsTypeButton.close(true);
        }
    }

    private void newsTypeModeChanged() {
        PrefUtilities.getInstance().setNewsTypeMode(entryType);
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }
}
