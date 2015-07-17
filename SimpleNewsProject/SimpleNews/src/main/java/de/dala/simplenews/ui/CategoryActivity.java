package de.dala.simplenews.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;

import java.util.ArrayList;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;

public class CategoryActivity extends BaseActivity implements CategorySelectionFragment.OnCategorySelectionFragmentAction {
    public static final String PATH = "from_rss";
    private Fragment fragment = null;
    private long lastCategoryClick = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Intent intent = getIntent();
        ArrayList<Category> categories = new ArrayList<>(DatabaseHandler.getInstance().getCategories(false, true, null));
        String rssPath = intent.getStringExtra(PATH);
        if (savedInstanceState != null) {
            boolean isCategoryFeedsFragment = savedInstanceState.getBoolean("isCategoryFeedsFragment");
            if (isCategoryFeedsFragment) {
                lastCategoryClick = savedInstanceState.getLong("lastCategoryClick");
                for (Category category : categories) {
                    if (category.getId() == lastCategoryClick) {
                        fragment = CategoryFeedsFragment.newInstance(category);
                        break;
                    }
                }
            } else {
                fragment = CategorySelectionFragment.newInstance(categories, rssPath);
            }
        }
        if (fragment == null) {
            fragment = CategorySelectionFragment.newInstance(categories, rssPath);
        }

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.container, fragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.category, menu);
        return super.onCreateOptionsMenu(menu);
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
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
        t.replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onRSSSavedClick(Category category, String rssPath) {
        Feed feed = new Feed();
        feed.setXmlUrl(rssPath);
        feed.setCategoryId(category.getId());
        DatabaseHandler.getInstance().addFeed(category.getId(), feed, true);
        startNewsActivity();
    }

    @Override
    public void onRestore() {
        DatabaseHandler.getInstance().removeAllCategories();
        DatabaseHandler.getInstance().loadXmlIntoDatabase(R.raw.categories);
        startNewsActivity();
    }

    @Override
    public void onShowClicked(Category category) {
        boolean visible = !category.isVisible();
        category.setVisible(visible);
        DatabaseHandler.getInstance().updateCategory(category);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!getSupportFragmentManager().popBackStackImmediate()) {
            startNewsActivity();
        }
    }

    private void startNewsActivity() {
        Intent intent = new Intent(CategoryActivity.this, NewsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
