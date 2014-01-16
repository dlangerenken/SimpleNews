package de.dala.simplenews.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RelativeLayout;


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
public class CategoryModifierActivity extends SherlockFragmentActivity implements CategorySelectionFragment.OnCategoryClicked{
    private static final String LAST_FRAG = "CURRENT_FRAGMENT";
    private static final String CATEGORY_FEEDS_TAG = "feed";
    private static final String CATEGORY_SELECTION_TAG = "selection";
    private static final int CATEGORY_SELECTION = 0;
    private static final int CATEGORY_FEEDS = 1;

    private ArrayList<Category> categories;
    private boolean fromRSS = false;
    private int currentFragment = CATEGORY_SELECTION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_modifier);
        getSupportActionBar().setTitle(getString(R.string.categories_title));
        categories = new ArrayList<Category>(DatabaseHandler.getInstance().getCategories(false, true));

        Fragment fragment = null;

        if(this.getIntent().getDataString()!=null)
        {
            String path = this.getIntent().getDataString();
            fromRSS = true;
            fragment = CategorySelectionFragment.newInstance(categories, fromRSS, path);
        }
        if (savedInstanceState == null){
            FragmentTransaction t = getSupportFragmentManager().beginTransaction();
            if (!fromRSS){
                fragment = CategorySelectionFragment.newInstance(categories, fromRSS, null);
            }
            t.replace(R.id.container, fragment, CATEGORY_SELECTION_TAG);
            t.commit();
        }
        overridePendingTransition(R.anim.open_translate,R.anim.close_scale);
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() == 0 && fromRSS){
            startActivity(new Intent(CategoryModifierActivity.this, MainActivity.class));
            finish();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void onMoreClicked(Category category) {
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        Fragment fragment = CategoryFeedsFragment.newInstance(category);
        t.replace(R.id.container, fragment, CATEGORY_FEEDS_TAG);
        t.addToBackStack(null);
        t.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
        t.commit();
        currentFragment = CATEGORY_FEEDS;
    }

    @Override
    public void onRSSSavedClick(Category category, String rssPath) {
        Feed feed = new Feed();
        feed.setUrl(rssPath);
        feed.setCategoryId(category.getId());
        DatabaseHandler.getInstance().addFeed(category.getId(), feed, true);
        finish();
    }


    @Override
    protected void onPause() {
        super.onPause();
        //closing transition animations
        overridePendingTransition(R.anim.open_scale,R.anim.close_translate);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(LAST_FRAG, currentFragment);
        super.onSaveInstanceState(outState);
    }

}
