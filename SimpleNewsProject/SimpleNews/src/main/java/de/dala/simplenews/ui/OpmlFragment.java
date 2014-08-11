package de.dala.simplenews.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;

/**
 * Created by Daniel on 29.12.13.
 */
public class OpmlFragment extends Fragment implements OpmlImportFragment.OnFeedsLoaded {
    private static final String OPML_IMPORT_TAG = "import";
    private static final String OPML_ASSIGN_TAG = "assign";

    Fragment fragment = null;

    public OpmlFragment() {
    }

    public static Fragment getInstance() {
        return new OpmlFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentTransaction t = getChildFragmentManager().beginTransaction();
        fragment = OpmlImportFragment.newInstance();
        t.replace(R.id.container, fragment, OPML_IMPORT_TAG);
        t.commit();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.opml_view, container, false);
    }

    @Override
    public void assignFeeds(List<Feed> feeds) {
        FragmentTransaction t = getChildFragmentManager().beginTransaction();
        t.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
        t.replace(R.id.container, OpmlAssignFragment.newInstance(feeds), OPML_ASSIGN_TAG);
        t.addToBackStack(null);
        t.commit();
        getActivity().supportInvalidateOptionsMenu();
    }

}
