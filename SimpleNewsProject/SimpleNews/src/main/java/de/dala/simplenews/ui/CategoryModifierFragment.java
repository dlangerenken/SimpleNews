package de.dala.simplenews.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;


import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import java.util.ArrayList;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.toasty.Toasty;

/**
 * Created by Daniel on 29.12.13.
 */
public class CategoryModifierFragment extends SherlockFragment implements CategorySelectionFragment.OnCategoryClicked{
    private static final String CATEGORY_FEEDS_TAG = "feed";
    private static final String CATEGORY_SELECTION_TAG = "selection";
    private static final String FROM_RSS =  "from_rss";
    private ArrayList<Category> categories;
    private boolean fromRSS = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categories = new ArrayList<Category>(DatabaseHandler.getInstance().getCategories(false, true, false));
        Fragment fragment = null;
        FragmentTransaction t = getChildFragmentManager().beginTransaction();

        String rssPath = getArguments() != null ? getArguments().getString(FROM_RSS) : null;
        if (rssPath != null){
            fromRSS = true;
            fragment = CategorySelectionFragment.newInstance(categories, fromRSS, rssPath);
        }else{
            if (!fromRSS){
                fragment = CategorySelectionFragment.newInstance(categories, fromRSS, null);
            }
        }
        t.replace(R.id.container, fragment, CATEGORY_SELECTION_TAG);
        t.addToBackStack(null);
        t.commit();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.category_modifier, container, false);
    }


    @Override
    public void onMoreClicked(Category category) {
        FragmentTransaction t = getChildFragmentManager().beginTransaction();
        Fragment fragment = CategoryFeedsFragment.newInstance(category);
        t.replace(R.id.container, fragment, CATEGORY_FEEDS_TAG);
        t.addToBackStack(null);
        t.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
        t.commit();
    }

    @Override
    public void onRSSSavedClick(Category category, String rssPath) {
        Feed feed = new Feed();
        feed.setUrl(rssPath);
        feed.setCategoryId(category.getId());
        DatabaseHandler.getInstance().addFeed(category.getId(), feed, true);
        getActivity().finish();
    }

    @Override
    public void onRestore() {
        //restore-clicked
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
}
