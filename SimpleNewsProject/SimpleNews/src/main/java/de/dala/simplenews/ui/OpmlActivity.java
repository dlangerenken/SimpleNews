package de.dala.simplenews.ui;

import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Feed;

public class OpmlActivity extends BaseActivity implements OpmlImportFragment.OnFeedsLoaded {

    private static final String OPML_IMPORT_TAG = "import";
    private static final String OPML_ASSIGN_TAG = "assign";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opml);
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.container, OpmlImportFragment.newInstance(), OPML_IMPORT_TAG);
        t.commit();
    }

    @Override
    public void assignFeeds(List<Feed> feeds) {
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
        t.replace(R.id.container, OpmlAssignFragment.newInstance(feeds), OPML_ASSIGN_TAG);
        t.addToBackStack(null);
        t.commit();
        supportInvalidateOptionsMenu();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_opml, menu);
        return true;
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
}
