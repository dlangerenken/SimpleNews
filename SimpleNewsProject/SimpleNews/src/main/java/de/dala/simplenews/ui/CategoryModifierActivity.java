package de.dala.simplenews.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;


import java.util.ArrayList;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.toasty.Toasty;

/**
 * Created by Daniel on 29.12.13.
 */
public class CategoryModifierActivity extends FragmentActivity implements CategorySelectionFragment.OnCategoryClicked{
    private ArrayList<Category> categories;
    private int currentFragment = CATEGORY_SELECTION;
    private static final String LAST_FRAG = "CURRENT_FRAGMENT";
    private static final int CATEGORY_SELECTION = 0;
    private static final int CATEGORY_FEEDS = 1;
    private static final String CATEGORY_FEEDS_TAG = "feed";
    private static final String CATEGORY_SELECTION_TAG = "selection";

    private boolean fromRSS = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(this.getIntent().getDataString()!=null)
        {
            String path = this.getIntent().getDataString();
            Toasty.toast(path + " clicked");
            fromRSS = true;
        }else{
            setContentView(R.layout.category_modifier);

            categories = new ArrayList<Category>(DatabaseHandler.getInstance().getCategories(false, true));


            if (savedInstanceState == null){
                FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                Fragment fragment = CategorySelectionFragment.newInstance(categories);
                t.replace(R.id.container, fragment, CATEGORY_SELECTION_TAG);
                t.addToBackStack(CATEGORY_SELECTION_TAG);
                t.commit();
            }
        }
        overridePendingTransition(R.anim.open_translate,R.anim.close_scale);
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() == 0){
            startActivity(new Intent(CategoryModifierActivity.this, MainActivity.class));
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
