package de.dala.simplenews.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.utilities.BaseNavigation;
import recycler.CategoryRecyclerAdapter;

public class CategoryModifierFragment extends BaseFragment implements CategoryRecyclerAdapter.OnCategoryClicked, BaseNavigation {
    private static final String FROM_RSS = "from_rss";
    private Fragment fragment = null;
    private long lastCategoryClick = 0;

    public CategoryModifierFragment() {
    }

    public static Fragment getInstance() {
        return new CategoryModifierFragment();
    }

    public static Fragment getInstance(String path) {
        Bundle bundle = new Bundle();
        bundle.putString(FROM_RSS, path);
        CategoryModifierFragment frag = new CategoryModifierFragment();
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<Category> categories = new ArrayList<>(DatabaseHandler.getInstance().getCategories(false, true, null));
        String rssPath = getArguments() != null ? getArguments().getString(FROM_RSS) : null;
        if (savedInstanceState != null) {
            boolean isCategoryFeedsFragment = savedInstanceState.getBoolean("isCategoryFeedsFragment");
            if (isCategoryFeedsFragment){
                lastCategoryClick = savedInstanceState.getLong("lastCategoryClick");
                for (Category category : categories){
                    if (category.getId() == lastCategoryClick){
                        fragment = CategoryFeedsFragment.newInstance(category);
                        break;
                    }
                }
            }else{
                fragment = CategorySelectionFragment.newInstance(categories, rssPath);
            }
        }
        if (fragment == null) {
            fragment = CategorySelectionFragment.newInstance(categories, rssPath);
        }
        FragmentTransaction t = getChildFragmentManager().beginTransaction();
        t.replace(R.id.container, fragment).commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.category_modifier, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isCategoryFeedsFragment", fragment instanceof CategoryFeedsFragment);
        outState.putLong("lastCategoryClick", lastCategoryClick);
    }

    @Override
    public void onMoreClicked(Category category) {
        lastCategoryClick = category.getId();
        fragment = CategoryFeedsFragment.newInstance(category);
        FragmentTransaction t = getChildFragmentManager().beginTransaction();
        t.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
        t.replace(R.id.container, fragment).commit();
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onRSSSavedClick(Category category, String rssPath) {
        Feed feed = new Feed();
        feed.setXmlUrl(rssPath);
        feed.setCategoryId(category.getId());
        DatabaseHandler.getInstance().addFeed(category.getId(), feed, true);
        getActivity().finish();
    }

    @Override
    public void onRestore() {
        DatabaseHandler.getInstance().removeAllCategories();
        DatabaseHandler.getInstance().loadXmlIntoDatabase(R.raw.categories);
    }

    @Override
    public void onShowClicked(Category category) {
        boolean visible = !category.isVisible();
        category.setVisible(visible);
        DatabaseHandler.getInstance().updateCategory(category);
    }

    @Override
    public void onColorClicked(Category category) {

    }

    @Override
    public void editClicked(Category category) {

    }

    @Override
    public String getTitle() {
        if (fragment == null){
            fragment = getVisibleFragment();
        }
        if (fragment != null && fragment instanceof BaseNavigation && fragment.isAdded()) {
            return ((BaseNavigation) fragment).getTitle();
        }
        return getActivity().getString(R.string.category_modifier_fragment);
    }

    @Override
    public int getNavigationDrawerId() {
        return NavigationDrawerFragment.CATEGORIES;
    }
}
