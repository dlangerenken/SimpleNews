package de.dala.simplenews.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.utilities.BaseNavigation;
import recycler.ChoiceModeRecyclerAdapter;
import recycler.OpmlRecyclerAdapter;

public class OpmlAssignFragment extends BaseFragment implements BaseNavigation, ChoiceModeRecyclerAdapter.ChoiceModeListener {

    private static final String FEED_LIST_KEY = "feeds";

    private OpmlRecyclerAdapter mAdapter;
    private RecyclerView feedRecyclerView;
    private List<Feed> feedsToImport;
    private ActionMode mActionMode;

    public OpmlAssignFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feedsToImport = (List<Feed>) getArguments().getSerializable(FEED_LIST_KEY);
    }

    public static Fragment newInstance(List<Feed> feeds) {
        OpmlAssignFragment fragment = new OpmlAssignFragment();
        Bundle b = new Bundle();
        b.putSerializable(FEED_LIST_KEY, new ArrayList<>(feeds));
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.opml_list_view, container, false);
        feedRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        feedRecyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        initAdapter();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar ab = activity.getSupportActionBar();
            if (ab != null) {
                ab.setDisplayHomeAsUpEnabled(true);
            }
        }
        return rootView;
    }

    private void initAdapter() {
        mAdapter = new OpmlRecyclerAdapter(getActivity(), feedsToImport, this);
        feedRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public String getTitle() {
        Context mContext = getActivity();
        if (mContext != null) {
            return mContext.getString(R.string.opml_assign_fragment_title);
        }
        return "SimpleNews"; //should not be called
    }

    @Override
    public int getNavigationDrawerId() {
        return NavigationDrawerFragment.IMPORT;
    }

    @Override
    public void startSelectionMode() {
        mActionMode = getActivity().startActionMode(new ActionModeCallBack());
    }

    @Override
    public void updateSelectionMode(int numberOfElements) {
        if (mActionMode != null) {
            mActionMode.setTitle(String.valueOf(numberOfElements));
        }
    }

    @Override
    public void finishSelectionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    private class ActionModeCallBack implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.contextual_opml_import_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            final List<Feed> feeds = mAdapter.getSelectedItems();
            // close action mode
            switch (item.getItemId()) {
                case R.id.menu_item_remove:
                    mAdapter.remove(feeds);
                    break;
                case R.id.menu_item_assign:
                    AssignDialogFragment assignFeedsDialog = AssignDialogFragment.newInstance(feeds);
                    assignFeedsDialog.setDialogHandler(new AssignDialogFragment.IDialogHandler() {

                        @Override
                        public void assigned() {
                            mAdapter.remove(feeds);
                        }

                        @Override
                        public void canceled() {
                            mAdapter.clearSelections();
                        }
                    });
                    assignFeedsDialog.show(getFragmentManager(), "AssignFeedsDialog");
                    break;
            }

            mode.finish();
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // remove selection
            mAdapter.clearSelections();
            mActionMode = null;
        }
    }

}
