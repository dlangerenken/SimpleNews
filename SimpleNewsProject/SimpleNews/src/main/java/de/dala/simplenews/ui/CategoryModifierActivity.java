package de.dala.simplenews.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;


import java.util.List;
import java.util.Random;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.database.DatabaseHandler;

/**
 * Created by Daniel on 29.12.13.
 */
public class CategoryModifierActivity extends FragmentActivity implements CategorySelectionFragment.OnCategoryClicked{
    private List<Category> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(this.getIntent().getDataString()!=null)
        {
            String path = this.getIntent().getDataString();
            //TODO handle this!!
            Toast.makeText(this, path + " clicked", Toast.LENGTH_SHORT).show();
        }else{
            setContentView(R.layout.category_modifier);

            categories = DatabaseHandler.getInstance().getCategories(false, true);

            FragmentTransaction t = getSupportFragmentManager().beginTransaction();
            Fragment fragment = new CategorySelectionFragment(categories);
            t.replace(R.id.container, fragment);
            t.addToBackStack(null);
            t.commit();
        }
    }

    @Override
    public void onMoreClicked(Category category) {
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        Fragment fragment = new CategoryFeedsFragment(category);
        t.replace(R.id.container, fragment);
        t.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
        t.addToBackStack(null);
        t.commit();
    }
}
