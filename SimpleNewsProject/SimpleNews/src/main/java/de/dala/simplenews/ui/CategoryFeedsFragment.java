package de.dala.simplenews.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ShareActionProvider;

import com.rometools.rome.feed.opml.Opml;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.impl.OPML20Generator;

import org.jdom2.output.XMLOutputter;

import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.utilities.BaseNavigation;
import de.dala.simplenews.utilities.LightAlertDialog;
import de.dala.simplenews.utilities.OpmlConverter;
import de.dala.simplenews.utilities.UpdatingFeedTask;
import recycler.ChoiceModeRecyclerAdapter;
import recycler.FeedRecyclerAdapter;

public class CategoryFeedsFragment extends BaseFragment implements BaseNavigation, ChoiceModeRecyclerAdapter.ChoiceModeListener {

    private static final String CATEGORY_KEY = "mCategory";
    private RecyclerView feedListView;
    private FeedRecyclerAdapter mRecyclerAdapter;
    private Category mCategory;
    private UpdatingFeedTask feedTask;
    private ActionMode mActionMode;
    private ShareActionProvider shareActionProvider;

    public CategoryFeedsFragment() {
    }

    public static CategoryFeedsFragment newInstance(Category category) {
        CategoryFeedsFragment fragment = new CategoryFeedsFragment();
        Bundle b = new Bundle();
        b.putParcelable(CATEGORY_KEY, category);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.feed_selection, container, false);
        feedListView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        initAdapter();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar ab = activity.getSupportActionBar();
            if (ab != null) {
                ab.setDisplayHomeAsUpEnabled(true);
            }
        }
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("ActionMode")) {
                Bundle actionModeBundle = savedInstanceState.getBundle("ActionMode");
                mRecyclerAdapter.restoreSelectionStates(actionModeBundle);
            }
        }
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.mCategory = getArguments().getParcelable(CATEGORY_KEY);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.feed_selection_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_feed:
                createFeedClicked();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initAdapter() {
        mRecyclerAdapter = new FeedRecyclerAdapter(getActivity(), mCategory, this);
        feedListView.setAdapter(mRecyclerAdapter);
    }

    private void createFeedClicked() {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.check_valid_rss_dialog, null);
        final ViewGroup inputLayout = (ViewGroup) view.findViewById(R.id.inputLayout);
        final View progress = view.findViewById(R.id.m_progress);
        final Button positive = (Button) inputLayout.findViewById(R.id.positive);
        final Button negative = (Button) inputLayout.findViewById(R.id.negative);
        final EditText input = (EditText) inputLayout.findViewById(R.id.input);

        final AlertDialog dialog = LightAlertDialog.Builder.create(getActivity()).setView(view).setTitle(R.string.new_feed_url_header).create();

        View.OnClickListener dialogClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.positive:
                        feedTask = new UpdatingFeedTask(getActivity(), view, inputLayout, progress, dialog, null, mRecyclerAdapter, mCategory);
                        feedTask.execute(input.getText().toString());
                        break;
                    case R.id.negative:
                        dialog.dismiss();
                        break;
                }
            }
        };

        positive.setOnClickListener(dialogClickListener);
        negative.setOnClickListener(dialogClickListener);

        dialog.show();
    }

    @Override
    public String getTitle() {
        Context context = getActivity();
        if (context != null) {
            return String.format(context.getString(R.string.category_feeds_fragment_title), mCategory.getName());
        }
        return "SimpleNews"; //default
    }

    @Override
    public int getNavigationDrawerId() {
        return NavigationDrawerFragment.CATEGORIES;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (feedTask != null) {
            feedTask.cancel(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("ActionMode", mRecyclerAdapter.saveSelectionStates());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (feedTask != null) {
            feedTask.cancel(true);
        }
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
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(getShareIntent());
        }
    }

    @Override
    public void finishSelectionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
        shareActionProvider = null;
    }

    private Intent getShareIntent() {
        List<Feed> feeds = mRecyclerAdapter.getSelectedItems();
        Opml opml = OpmlConverter.convertFeedsToOpml("SimpleNews - OPML", feeds);
        String finalMessage = null;
        try {
            finalMessage = new XMLOutputter().outputString(new OPML20Generator().generate(opml));
        } catch (FeedException e) {
            e.printStackTrace();
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/xml");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                finalMessage);
        return shareIntent;
    }

    private class ActionModeCallBack implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.contextual_feed_selection_menu, menu);
            MenuItem item = menu.findItem(R.id.menu_item_share);
            if (item != null) {
                shareActionProvider = (ShareActionProvider) item.getActionProvider();
                if (shareActionProvider != null) {
                    String shareHistoryFileName = ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME;
                    shareActionProvider.setShareHistoryFileName(shareHistoryFileName);
                    shareActionProvider.setShareIntent(getShareIntent());
                    shareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
                        @Override
                        public boolean onShareTargetSelected(ShareActionProvider shareActionProvider, Intent intent) {
                            return false;
                        }
                    });
                }
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            List<Feed> feeds = mRecyclerAdapter.getSelectedItems();
            if (shareActionProvider != null) {
                shareActionProvider.setShareIntent(getShareIntent());
            }
            // close action mode
            switch (item.getItemId()) {
                case R.id.menu_item_remove:
                    mRecyclerAdapter.removeFeeds(feeds);
                    break;
                case R.id.menu_item_share:

                    break;
            }
            mode.finish();
            mRecyclerAdapter.clearSelections();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mRecyclerAdapter.clearSelections();
        }
    }
}
