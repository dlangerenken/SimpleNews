package de.dala.simplenews.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.Collections;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.utilities.PrefUtilities;

public class NewsOverViewFragment extends BaseFragment implements ViewPager.OnPageChangeListener {
    private TabLayout tabs;
    private ViewPager pager;

    private List<Category> categories;
    private NewsActivity newsActivity;
    private int entryType = ALL;
    private MenuItem columnsMenu;

    public static final int ALL = 0;
    public static final int FAV = 1;
    public static final int RECENT = 2;

    private FloatingActionMenu newsTypeButton;
    private FloatingActionButton subactionButton1;
    private FloatingActionButton subactionButton2;

    public NewsOverViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof NewsActivity) {
            this.newsActivity = (NewsActivity) getActivity();
        } else {
            throw new ActivityNotFoundException("NewsActivity not found");
        }
        entryType = PrefUtilities.getInstance().getNewsTypeMode();
    }

    public static Fragment getInstance(int entryType) {
        Fragment fragment = new NewsOverViewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("entryType", entryType);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.news_overview_menu, menu);
        columnsMenu = menu.findItem(R.id.menu_columns);
        updateMenu();
    }

    private void updateMenu() {
        boolean useMultiple = shouldUseMultipleColumns();
        Context mContext = getActivity();
        if (mContext != null) {
            if (columnsMenu != null) {
                columnsMenu.setTitle(useMultiple ? mContext.getString(R.string.single_columns) : mContext.getString(R.string.multiple_columns));
            }
        }
    }

    private boolean shouldUseMultipleColumns() {
        boolean useMultiple = false;

        if (isAdded() && getActivity() != null && getActivity().getResources() != null) {
            android.content.res.Configuration config = getActivity().getResources().getConfiguration();
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

                if (isAdded() && getActivity() != null && getActivity().getResources() != null) {
                    android.content.res.Configuration config = getActivity().getResources().getConfiguration();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.news_overview, container, false);
        pager = (ViewPager) rootView.findViewById(R.id.pager);
        tabs = newsActivity.getTabLayout();

        if (!PrefUtilities.getInstance().xmlIsAlreadyLoaded()) {
            DatabaseHandler.getInstance().loadXmlIntoDatabase(R.raw.categories);
        }

        if (newsActivity != null) {
            ActionBar ab = newsActivity.getSupportActionBar();
            if (ab != null) {
                ab.setHomeButtonEnabled(true);
            }
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
        initNewsTypeIcon();
        return rootView;
    }

    private void initAdapterAndPager() {
        MyPagerAdapter adapter = new MyPagerAdapter(getChildFragmentManager());
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(this);
        tabs.setupWithViewPager(pager);
        int page = PrefUtilities.getInstance().getCategoryIndex();
        pager.setCurrentItem(page);
        onPageSelected(page);
    }


    private void changeColor(int primaryColor, int secondaryColor) {
        newsActivity.changeColor(primaryColor, secondaryColor);
        initNewsTypeIcon();
        setColor(subactionButton1, primaryColor, secondaryColor);
        setColor(subactionButton2, primaryColor, secondaryColor);
        setColor(newsTypeButton, primaryColor, secondaryColor);
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

    private int[] actionButtonIds;
    private boolean isInitialized;

    private void initNewsTypeIcon() {
        if (isInitialized) {
            return;
        }
        newsTypeButton = (FloatingActionMenu) getActivity().findViewById(R.id.floating_action_menu);
        subactionButton1 = (FloatingActionButton) getActivity().findViewById(R.id.menu_item_1);
        subactionButton2 = (FloatingActionButton) getActivity().findViewById(R.id.menu_item_2);

        subactionButton1.setOnClickListener(new OnSubActionButtonClickListener());
        subactionButton2.setOnClickListener(new OnSubActionButtonClickListener());

        actionButtonIds = new int[]{R.drawable.ic_home, R.drawable.ic_fav, R.drawable.ic_seen};
        isInitialized = true;
        updateNewsIcon();
    }

    private void updateNewsIcon() {
        newsTypeButton.getMenuIconView().setImageDrawable(ContextCompat.getDrawable(getActivity(), actionButtonIds[entryType % 3]));
        subactionButton1.setImageDrawable(ContextCompat.getDrawable(getActivity(), actionButtonIds[(entryType + 1) % 3]));
        subactionButton2.setImageDrawable(ContextCompat.getDrawable(getActivity(), actionButtonIds[(entryType + 2) % 3]));
        newsTypeButton.setTag((entryType));
        subactionButton1.setTag((entryType + 1) % 3);
        subactionButton2.setTag((entryType + 2) % 3);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
            // Check if this Fragment already exists.
            String name = makeFragmentName(R.id.pager, position);
            Fragment fragment = getFragmentManager().findFragmentByTag(name);
            Category category = categories.get(position);
            if (fragment == null) {
                fragment = ExpandableNewsFragment.newInstance(category, entryType, position);
            }
            return fragment;
        }

        private String makeFragmentName(int viewId, int index) {
            return "android:switcher:" + viewId + ":" + index;
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
