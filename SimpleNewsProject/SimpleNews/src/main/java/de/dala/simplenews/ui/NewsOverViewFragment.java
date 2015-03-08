package de.dala.simplenews.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.astuetz.PagerSlidingTabStrip;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import circularmenu.FloatingActionButton;
import circularmenu.FloatingActionMenu;
import circularmenu.SubActionButton;
import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.utilities.BaseNavigation;
import de.dala.simplenews.utilities.PrefUtilities;
import de.keyboardsurfer.android.widget.crouton.Crouton;

/**
 * Created by Daniel on 20.02.14.
 */
public class NewsOverViewFragment extends BaseFragment implements ViewPager.OnPageChangeListener, BaseNavigation {

    private PagerSlidingTabStrip tabs;

    private List<Category> categories;
    private int loadingNews = -1;
    private MainActivity mainActivity;
    private int entryType = ALL;
    private FloatingActionMenu newsTypeButton;
    private MenuItem columnsMenu;

    public static final int ALL = 1;
    public static final int FAV = 2;
    public static final int RECENT = 3;
    public static final int UNREAD = 4;

    public FloatingActionMenu getNewsTypeButton() {
        return newsTypeButton;
    }
    ViewPager pager;

    @Override
    public String getTitle() {
        Context mContext = getActivity();
        if (mContext != null){
            switch (entryType) {
                case ALL:
                    return mContext.getString(R.string.home_title);
                case FAV:
                    return mContext.getString(R.string.favorite_title);
                case RECENT:
                    return mContext.getString(R.string.recent_title);
                case UNREAD:
                    return mContext.getString(R.string.unread_title);
            }
        }
        return "News"; // should not be called
    }

    @Override
    public int getNavigationDrawerId() {
        switch (entryType){
            case ALL:
                return NavigationDrawerFragment.HOME;
            case FAV:
                return NavigationDrawerFragment.FAVORITE;
            case RECENT:
                return NavigationDrawerFragment.RECENT;
            case UNREAD:
                return NavigationDrawerFragment.UNREAD;
        };
        return NavigationDrawerFragment.HOME;
    }

    public void onBackStackChanged() {
        categories = DatabaseHandler.getInstance().getCategories(null, null, true);
        Collections.sort(categories);
        orderChanged();
    }

    private ArrayList<String> newsTypeTags;

    public NewsOverViewFragment() {
    }

    public static Fragment getInstance(int entryType) {
        Fragment fragment = new NewsOverViewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("entryType", entryType);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof MainActivity) {
            this.mainActivity = (MainActivity) getActivity();
        } else {
            throw new ActivityNotFoundException("MainActivity not found");
        }
        entryType = getArguments().getInt("entryType", ALL);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.news_overview_menu, menu);
        columnsMenu = menu.findItem(R.id.menu_columns);
        updateMenu();
    }

    private void updateMenu(){
        boolean useMultiple = shouldUseMultipleColumns();
        Context mContext = getActivity();
        if (mContext != null){
            if (columnsMenu != null) {
                columnsMenu.setTitle(useMultiple ? mContext.getString(R.string.single_columns) : mContext.getString(R.string.multiple_columns));
            }
        }
    }

    private boolean shouldUseMultipleColumns(){
        boolean useMultiple = false;

        if (isAdded() && getActivity() != null && getActivity().getResources() != null){
            android.content.res.Configuration config = getActivity().getResources().getConfiguration();
            if (config != null){
                switch (config.orientation) {
                    case android.content.res.Configuration.ORIENTATION_LANDSCAPE:
                        useMultiple = PrefUtilities.getInstance().useMultipleColumnsLandscape();
                        break;
                    case android.content.res.Configuration.ORIENTATION_PORTRAIT:
                        useMultiple = PrefUtilities.getInstance().useMultipleColumnsPortrait();
                        break;
                }
            }
        }else{
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
                    if (config != null){
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
                updateColumnCount();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateColumnCount() {
        for (ExpandableNewsFragment newsTypeButton: getActiveINewsTypeFragments()){
            newsTypeButton.gridSettingsChanged();
        }
    }

    private List<ExpandableNewsFragment> getActiveINewsTypeFragments(){
        List<ExpandableNewsFragment> fragments = new ArrayList<ExpandableNewsFragment>();
        if (newsTypeTags != null) {
            for (Iterator<String> iterator = newsTypeTags.iterator();
                 iterator.hasNext(); ) {
                Fragment fragment = getChildFragmentManager().findFragmentByTag(iterator.next());
                if (fragment != null && fragment instanceof ExpandableNewsFragment) {
                    fragments.add((ExpandableNewsFragment) fragment);
                } else {
                    iterator.remove();
                }
            }
        }
        return fragments;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.news_overview, container, false);
        pager = (ViewPager) rootView.findViewById(R.id.pager);
        tabs = (PagerSlidingTabStrip) rootView.findViewById(R.id.tabs);

        if (!PrefUtilities.getInstance().xmlIsAlreadyLoaded()) {
            DatabaseHandler.getInstance().loadXmlIntoDatabase(R.raw.categories);
        }

        mainActivity.getSupportActionBar().setHomeButtonEnabled(true);
        categories = DatabaseHandler.getInstance().getCategories(null, null, true);
        Collections.sort(categories);
        if (savedInstanceState != null){
            //probably orientation change
            Serializable newsTypeTagsSerializable = savedInstanceState.getSerializable("newsTypeTags");
            if (newsTypeTagsSerializable != null && newsTypeTagsSerializable instanceof ArrayList<?>){
                newsTypeTags = (ArrayList<String>) newsTypeTagsSerializable;
            }
            entryType = savedInstanceState.getInt("entryType", ALL);
            newsTypeModeChanged();
            orderChanged();
        }else{
            if (newsTypeTags != null){

                //returning from backstack, data is fine, do nothing
            }else{
                newsTypeTags = new ArrayList<String>();
            }
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
        tabs.setViewPager(pager);
        tabs.setOnPageChangeListener(this);

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);
        int page = PrefUtilities.getInstance().getCategoryIndex();
        pager.setCurrentItem(page);
        onPageSelected(page);
    }

    private void changeColor(Category category) {
        tabs.setIndicatorColor(category.getSecondaryColor());
        mainActivity.changeColor(category.getPrimaryColor());

        // http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    private SubActionButton subactionButton1;
    private SubActionButton subactionButton2;
    private SubActionButton subactionButton3;
    private ImageView mainIcon;
    private FloatingActionButton button;

    private void initNewsTypeIcon(){
        mainIcon = new ImageView(getActivity());
        button = new FloatingActionButton.Builder(getActivity())
                .setContentView(mainIcon)
                .build();

        int subButtonSize = getResources().getDimensionPixelSize(R.dimen.sub_action_button_size_medium);
        int actionMenuRadius = getResources().getDimensionPixelSize(R.dimen.action_menu_radius);
        SubActionButton.Builder rLSubBuilder = new SubActionButton.Builder(getActivity()).setLayoutParams(new FrameLayout.LayoutParams(subButtonSize, subButtonSize));
        ImageView icon1 = new ImageView(getActivity());
        ImageView icon2 = new ImageView(getActivity());
        ImageView icon3 = new ImageView(getActivity());

        subactionButton1 = rLSubBuilder.setContentView(icon1).build();
        subactionButton2 = rLSubBuilder.setContentView(icon2).build();
        subactionButton3 = rLSubBuilder.setContentView(icon3).build();
        subactionButton1.setOnClickListener(new OnSubActionButtonClickListener());
        subactionButton2.setOnClickListener(new OnSubActionButtonClickListener());
        subactionButton3.setOnClickListener(new OnSubActionButtonClickListener());

        // Build the menu with default options: light theme, 90 degrees, 72dp radius.
        // Set 4 default SubActionButtons
        newsTypeButton =  new FloatingActionMenu.Builder(getActivity())
                .addSubActionView(subactionButton1)
                .addSubActionView(subactionButton2)
                .addSubActionView(subactionButton3)
                .setRadius(actionMenuRadius)
                .attachTo(button)
                .build();
        button.setScaleX(0);
        button.setScaleY(0);

        subactionButton1.setAlpha(0);
        subactionButton2.setAlpha(0);
        subactionButton3.setAlpha(0);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(button, "scaleX", 1),
                ObjectAnimator.ofFloat(button, "scaleY", 1)
        );
        set.setDuration(500);
        set.setStartDelay(1200);
        set.start();

        set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(subactionButton1, "alpha", 1),
                ObjectAnimator.ofFloat(subactionButton2, "alpha", 1),
                ObjectAnimator.ofFloat(subactionButton3, "alpha", 1)
        );
        set.setDuration(0);
        set.setStartDelay(1200);
        set.start();

        updateNewsIcon();
    }

    private void updateNewsIcon(){
        switch (entryType){
            case ALL:
                subactionButton1.setTag(UNREAD);
                subactionButton2.setTag(FAV);
                subactionButton3.setTag(RECENT);
                ((ImageView)subactionButton1.getContentView()).setImageDrawable(getResources().getDrawable(R.drawable.ic_unread));
                ((ImageView)subactionButton2.getContentView()).setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite));
                ((ImageView)subactionButton3.getContentView()).setImageDrawable(getResources().getDrawable(R.drawable.ic_recent));
                mainIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_home));
                break;
            case UNREAD:
                subactionButton1.setTag(ALL);
                subactionButton2.setTag(FAV);
                subactionButton3.setTag(RECENT);
                ((ImageView)subactionButton1.getContentView()).setImageDrawable(getResources().getDrawable(R.drawable.ic_home));
                ((ImageView)subactionButton2.getContentView()).setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite));
                ((ImageView)subactionButton3.getContentView()).setImageDrawable(getResources().getDrawable(R.drawable.ic_recent));
                mainIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_unread));
                break;
            case FAV:
                subactionButton1.setTag(UNREAD);
                subactionButton2.setTag(ALL);
                subactionButton3.setTag(RECENT);
                ((ImageView)subactionButton1.getContentView()).setImageDrawable(getResources().getDrawable(R.drawable.ic_unread));
                ((ImageView)subactionButton2.getContentView()).setImageDrawable(getResources().getDrawable(R.drawable.ic_home));
                ((ImageView)subactionButton3.getContentView()).setImageDrawable(getResources().getDrawable(R.drawable.ic_recent));
                mainIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite));
                break;
            case RECENT:
                subactionButton1.setTag(UNREAD);
                subactionButton2.setTag(FAV);
                subactionButton3.setTag(ALL);
                ((ImageView)subactionButton1.getContentView()).setImageDrawable(getResources().getDrawable(R.drawable.ic_unread));
                ((ImageView)subactionButton2.getContentView()).setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite));
                ((ImageView)subactionButton3.getContentView()).setImageDrawable(getResources().getDrawable(R.drawable.ic_home));
                mainIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_recent));
                break;
        }
        mainActivity.updateNavigation(getNavigationDrawerId(), getTitle());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        newsTypeButton.close(false);
        button.detach();
        if (pager != null) {
            PrefUtilities.getInstance().setCategoryIndex(pager.getCurrentItem());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("newsTypeTags", newsTypeTags);
        outState.putInt("entryType", entryType);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }

    @Override
    public void onPageSelected(int i) {
        if (categories != null && categories.size() > i) {
            changeColor(categories.get(i));
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }


    public class MyPagerAdapter extends FragmentPagerAdapter {

        private FragmentManager mFragmentManager;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;
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
            if (newsTypeTags == null){
                newsTypeTags = new ArrayList<>();
            }
            if (!newsTypeTags.contains(name)){
                newsTypeTags.add(name);
            }

            Fragment fragment = mFragmentManager.findFragmentByTag(name);
            Category category = categories.get(position);
            if(fragment == null){
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
        for (ExpandableNewsFragment newsTypeButton: getActiveINewsTypeFragments()){
            newsTypeButton.newsTypeModeChanged(entryType);
        }
    }

    private void orderChanged() {
        for (ExpandableNewsFragment newsTypeButton: getActiveINewsTypeFragments()){
            int newsTypeButtonPosition = newsTypeButton.getPosition();
            if (categories.size() > newsTypeButtonPosition){
                newsTypeButton.updateCategory(categories.get(newsTypeButtonPosition));
            }
        }
    }

}
