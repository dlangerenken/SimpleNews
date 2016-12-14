package de.dala.simplenews.ui;

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

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.recycler.ChoiceModeRecyclerAdapter;
import de.dala.simplenews.utilities.EmptyObservableRecyclerView;
import de.dala.simplenews.recycler.CategoryAssignRecyclerAdapter;
import de.dala.simplenews.recycler.OpmlRecyclerAdapter;

public class OpmlAssignFragment extends BaseFragment implements ChoiceModeRecyclerAdapter.ChoiceModeListener {

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
        Serializable feedListSerializable = getArguments().getSerializable(FEED_LIST_KEY);
        assert feedListSerializable != null;
        if (feedListSerializable instanceof List<?>) {
            feedsToImport = new ArrayList<>();
            List<?> serializable = (List<?>) feedListSerializable;
            for (int i = 0; i < serializable.size(); i++) {
                Object elem = serializable.get(i);
                if (elem instanceof Feed) {
                    feedsToImport.add((Feed) elem);
                }
            }
        }
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
                    openAssign(feeds);
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

    private void openAssign(final List<Feed> feedsToAssign) {
        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.choose_category_for_feeds)
                .customView(R.layout.assign_category_list, false).build();
        View customView = dialog.getCustomView();
        if (customView != null) {
            EmptyObservableRecyclerView assignCategoryView = (EmptyObservableRecyclerView) customView;
            assignCategoryView.setLayoutManager(new LinearLayoutManager(getActivity()));
            List<Category> categories = DatabaseHandler.getInstance().getCategories(true, true, null);
            CategoryAssignRecyclerAdapter adapter = new CategoryAssignRecyclerAdapter(getActivity(), categories, new CategoryAssignRecyclerAdapter.OnClickListener() {
                @Override
                public void onClick(Category category) {
                    assignSelectedEntries(category, feedsToAssign);
                    mAdapter.remove(feedsToAssign);
                    dialog.dismiss();
                }
            });
            assignCategoryView.setAdapter(adapter);
            dialog.show();
        }
    }

    private void assignSelectedEntries(Category category, List<Feed> feeds) {
        for (Feed feed : feeds) {
            feed.setCategoryId(category.getId());
            if (feed.getCategoryId() != null && feed.getCategoryId() > 0) {
                DatabaseHandler.getInstance().addFeed(feed.getCategoryId(), feed, true);
            }
        }
    }
}
