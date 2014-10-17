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

/**
 * Created by Daniel on 29.12.13.
 */
public class CategoryModifierFragment extends BaseFragment implements CategorySelectionFragment.OnCategoryClicked, BaseNavigation {
    private static final String CATEGORY_FEEDS_TAG = "feed";
    private static final String CATEGORY_SELECTION_TAG = "selection";
    private static final String FROM_RSS = "from_rss";

    private boolean fromRSS = false;
    Fragment fragment = null;

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
        ArrayList<Category> categories = new ArrayList<Category>(DatabaseHandler.getInstance().getCategories(false, true, null));
        FragmentTransaction t = getChildFragmentManager().beginTransaction();
        String rssPath = getArguments() != null ? getArguments().getString(FROM_RSS) : null;
        if (rssPath != null) {
            fromRSS = true;
            fragment = CategorySelectionFragment.newInstance(categories, fromRSS, rssPath);
        } else {
            if (!fromRSS) {
                fragment = CategorySelectionFragment.newInstance(categories, fromRSS, null);
            }
        }
        t.replace(R.id.container, fragment, CATEGORY_SELECTION_TAG);
        t.commit();
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.category_modifier, container, false);
    }

    @Override
    public void onMoreClicked(Category category) {
        fragment = CategoryFeedsFragment.newInstance(category);
        FragmentTransaction t = getChildFragmentManager().beginTransaction();
        t.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
        t.addToBackStack(null);
        t.replace(R.id.container, fragment, CATEGORY_FEEDS_TAG);
        t.commit();
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
