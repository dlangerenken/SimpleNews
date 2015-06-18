package de.dala.simplenews.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.dala.simplenews.utilities.EmptyObservableRecyclerView;
import recycler.ChoiceModeRecyclerAdapter;
import recycler.ExpandableItemRecyclerAdapter;
import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Entry;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.utilities.BaseNavigation;
import de.dala.simplenews.utilities.CategoryUpdater;
import de.dala.simplenews.utilities.PrefUtilities;
import recycler.FadeInUpAnimator;


public class ExpandableNewsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, BaseNavigation, ExpandableItemRecyclerAdapter.ItemClickListener, ChoiceModeRecyclerAdapter.ChoiceModeListener {
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_ENTRY_TYPE = "entryType";
    private static final String ARG_POSITION = "position";

    private ExpandableItemRecyclerAdapter mExpandableItemRecyclerAdapter;
    private ActionMode mActionMode;
    private EmptyObservableRecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Category category;
    private CategoryUpdater updater;
    private int newsTypeMode;
    private NewsOverViewFragment parentFragment;
    private int position;
    private StaggeredGridLayoutManager mLayoutManager;

    private MenuItem refreshItem;
    private boolean isRefreshing;

    private ShareActionProvider shareActionProvider;

    public ExpandableNewsFragment() {
        //shouldn't be called
    }

    public static ExpandableNewsFragment newInstance(Category category, int entryType, int position) {
        ExpandableNewsFragment f = new ExpandableNewsFragment();
        Bundle b = new Bundle();
        b.putParcelable(ARG_CATEGORY, category);
        b.putInt(ARG_ENTRY_TYPE, entryType);
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    public Category getCategory() {
        return category;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.category = getArguments().getParcelable(ARG_CATEGORY);
        this.newsTypeMode = getArguments().getInt(ARG_ENTRY_TYPE);
        this.position = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment newsFragment = getParentFragment();
        if (newsFragment != null && newsFragment instanceof NewsOverViewFragment) {
            this.parentFragment = (NewsOverViewFragment) newsFragment;
        } else {
            throw new ClassCastException("ParentFragment is not of type NewsOverViewFragment");
        }
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (!visible) {
            if (mActionMode != null) {
                mActionMode.finish();
            }
        } else {
            setRefresh(isRefreshing);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.news_list, container, false);

        mRecyclerView = (EmptyObservableRecyclerView) rootView.findViewById(R.id.scroll);
        mRecyclerView.setEmptyView(rootView.findViewById(R.id.emptyView));
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_layout);
        Fragment parentFragment = getParentFragment();
        ViewGroup viewGroup = (ViewGroup) parentFragment.getView();
        if (viewGroup != null) {
            mRecyclerView.setTouchInterceptionViewGroup((ViewGroup) viewGroup.findViewById(R.id.container));
            if (parentFragment instanceof ObservableScrollViewCallbacks) {
                mRecyclerView.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentFragment);
            }
        }
        setHasOptionsMenu(true);
        init();
        return rootView;
    }

    private void init() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setOnRefreshListener(this);
            mSwipeRefreshLayout.setColorSchemeColors(category.getPrimaryColor());
        }
        initCardsAdapter();
        initNewsTypeBar();
        loadEntries(false);
    }

    private void initNewsTypeBar() {
        NewsTypeButtonAnimation animation = new NewsTypeButtonAnimation();
        animation.init(mRecyclerView, parentFragment.getNewsTypeButton());
    }

    private void updateColumnCount() {
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                boolean useMultipleLandscape = PrefUtilities.getInstance().useMultipleColumnsLandscape();
                mLayoutManager.setSpanCount(useMultipleLandscape ? 2 : 1);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                boolean useMultiplePortrait = PrefUtilities.getInstance().useMultipleColumnsPortrait();
                mLayoutManager.setSpanCount(useMultiplePortrait ? 2 : 1);
                break;
        }
    }

    private void setRefresh(boolean refresh) {
        isRefreshing = refresh;
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(refresh);
        }
        setRefreshActionButtonState(refresh);
    }


    @Override
    public void onRefresh() {
        if (updater != null && updater.isRunning()) {
            setRefresh(true);
        } else {
            loadEntries(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                loadEntries(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initCardsAdapter() {
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        if (mRecyclerView != null) {
            mExpandableItemRecyclerAdapter = new ExpandableItemRecyclerAdapter(new ArrayList<Entry>(), category, getActivity(), this, mRecyclerView, this);
            mRecyclerView.setItemAnimator(new FadeInUpAnimator());
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mExpandableItemRecyclerAdapter);
        }
        updateColumnCount();
    }

    public void refreshFeeds() {
        if (updater == null) {
            updater = new CategoryUpdater(new CategoryUpdateHandler(this), category, true, getActivity());
        }
        updater.start();
        setRefresh(true);
    }

    private void receivePartResult(List<Entry> entries) {
        mExpandableItemRecyclerAdapter.addNewEntries(entries);
    }

    private void updateFinished(boolean success, List<Entry> entries) {
        if (!isDetached()) {
            if (mActionMode != null) {
                mActionMode.finish();
            }

            if (success) {
                mExpandableItemRecyclerAdapter.removeOldEntries(entries);
            }
            setRefresh(false);
        }
    }

    private void setRefreshActionButtonState(boolean refreshing) {
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.expandable_news_menu, menu);
        refreshItem = menu.findItem(R.id.menu_refresh);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void loadEntries(boolean forceRefresh) {
        long timeForRefresh = PrefUtilities.getInstance().getTimeForRefresh();
        if (forceRefresh || (category != null && category.getLastUpdateTime() != null && category.getLastUpdateTime() < new Date().getTime() - timeForRefresh)) {
            refreshFeeds();
        } else {
            new BackgroundEntryLoading().execute(newsTypeMode);
        }
    }

    private Intent getShareIntent() {
        List<Entry> entries = mExpandableItemRecyclerAdapter.getItems();
        String finalMessage = TextUtils.join("\n", entries) + " - by SimpleNews";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                finalMessage);
        return shareIntent;
    }

    private void deleteSelectedEntries(Set<Entry> selectedEntries) {
        for (Entry entry : selectedEntries) {
            entry.setVisible(false);
            DatabaseHandler.getInstance().updateEntry(entry);
        }
        mExpandableItemRecyclerAdapter.remove(selectedEntries);
    }

    private void saveSelectedEntries(Set<Entry> selectedEntries) {
        for (Entry entry : selectedEntries) {
            entry.setFavoriteDate((entry.getFavoriteDate() == null || entry.getFavoriteDate() == 0) ? new Date().getTime() : null);
            DatabaseHandler.getInstance().updateEntry(entry);
        }
        mExpandableItemRecyclerAdapter.refresh(selectedEntries);
    }

    private void markEntriesAsRead(Set<Entry> selectedEntries) {
        for (Entry entry : selectedEntries) {
            entry.setVisitedDate((entry.getVisitedDate() == null || entry.getVisitedDate() == 0) ? new Date().getTime() : null);
            DatabaseHandler.getInstance().updateEntry(entry);
        }
        mExpandableItemRecyclerAdapter.refresh(selectedEntries);
    }


    public void newsTypeModeChanged(int newsTypeMode) {
        this.newsTypeMode = newsTypeMode;
        new BackgroundEntryLoading().execute(newsTypeMode);
    }

    public void gridSettingsChanged() {
        updateColumnCount();
    }

    @Override
    public String getTitle() {
        Context mContext = getActivity();
        if (mContext != null) {
            switch (newsTypeMode) {
                case NewsOverViewFragment.ALL:
                    return getActivity().getString(R.string.home_title);
                case NewsOverViewFragment.FAV:
                    return getActivity().getString(R.string.favorite_title);
                case NewsOverViewFragment.RECENT:
                    return getActivity().getString(R.string.recent_title);
                case NewsOverViewFragment.UNREAD:
                    return getActivity().getString(R.string.unread_title);
            }
        }
        return "News"; // should not be called
    }

    @Override
    public int getNavigationDrawerId() {
        switch (newsTypeMode) {
            case NewsOverViewFragment.ALL:
                return NavigationDrawerFragment.HOME;
            case NewsOverViewFragment.FAV:
                return NavigationDrawerFragment.FAVORITE;
            case NewsOverViewFragment.RECENT:
                return NavigationDrawerFragment.RECENT;
            case NewsOverViewFragment.UNREAD:
                return NavigationDrawerFragment.UNREAD;
        }
        return NavigationDrawerFragment.HOME;
    }

    public void updateCategory(Category category) {
        this.category = category;
        if (getView() != null) {
            init();
        }
    }

    @Override
    public void onItemClick(Entry entry) {
        entry.setVisitedDate(new Date().getTime());

        DatabaseHandler.getInstance().updateEntry(entry);
        Set<Entry> updatedEntries = new HashSet<>();
        updatedEntries.add(entry);
        mExpandableItemRecyclerAdapter.refresh(updatedEntries);

        String link = entry.getShortenedLink() != null ? entry.getShortenedLink() : entry.getLink();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        try {
            startActivity(browserIntent);
        } catch (ActivityNotFoundException e) {
            Context context = getActivity();
            if (context != null) {
                Toast.makeText(context, context.getResources().getString(R.string.no_browser_found), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void startSelectionMode() {
        mActionMode = getActivity().startActionMode(new ActionModeCallBack());
    }

    @Override
    public void updateSelectionMode(int numberOfElements) {
        if (mActionMode != null) {
            mActionMode.setTitle(String.format("%d", numberOfElements));
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


    private static class CategoryUpdateHandler extends Handler {
        WeakReference<ExpandableNewsFragment> mFragment;

        public CategoryUpdateHandler(ExpandableNewsFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            ExpandableNewsFragment fragment = mFragment.get();
            if (mFragment != null) {
                switch (msg.what) {
                    case CategoryUpdater.RESULT:
                        fragment.updateFinished(true, (List<Entry>) msg.obj);
                        break;
                    case CategoryUpdater.PART_RESULT:
                        if (msg.obj != null) {
                            fragment.receivePartResult((List<Entry>) msg.obj);
                        }
                        break;
                    case CategoryUpdater.STATUS_CHANGED:
                        break;
                    case CategoryUpdater.ERROR:
                        fragment.updateFinished(false, null);
                        Context context = fragment.getActivity();
                        if (context != null && msg.obj != null) {
                            Toast.makeText(context, (String) msg.obj, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case CategoryUpdater.CANCEL:
                        fragment.updateFinished(false, null);
                        break;
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mActionMode != null) {
            mActionMode.invalidate();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mActionMode != null) {
            mActionMode.finish();
        }
        if (updater != null) {
            updater.cancel();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (updater != null) {
            updater.cancel();
        }
    }

    private class ActionModeCallBack implements ActionMode.Callback {

        public void changeOverflowIcon() {
            //getActivity().getTheme().applyStyle(R.style.ChangeOverflowToDark, true);
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            changeOverflowIcon();
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.contextual_list_view, menu);
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
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Set<Entry> selectedEntries = new HashSet<>(mExpandableItemRecyclerAdapter.getSelectedItems());

            boolean shouldFinish = false;
            switch (item.getItemId()) {
                case R.id.menu_item_save:
                    saveSelectedEntries(selectedEntries);
                    shouldFinish = true;
                    break;
                case R.id.menu_item_read:
                    markEntriesAsRead(selectedEntries);
                    shouldFinish = true;
                    break;
                case R.id.menu_item_select_all:
                    mExpandableItemRecyclerAdapter.setAllItemsSelected();
                    shouldFinish = false;
                    break;
                case R.id.menu_item_deselect_all:
                    mExpandableItemRecyclerAdapter.clearSelections();
                    shouldFinish = true;
                    break;
                case R.id.menu_item_delete:
                    deleteSelectedEntries(selectedEntries);
                    shouldFinish = true;
                    break;
            }

            if (shouldFinish) {
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mExpandableItemRecyclerAdapter.clearSelections();
        }
    }

    private class BackgroundEntryLoading extends AsyncTask<Integer, Void, List<Entry>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setRefresh(true);
        }

        @Override
        protected List<Entry> doInBackground(Integer... params) {
            List<Entry> entries = new ArrayList<>();
            if (params != null && params.length > 0) {
                int type = params[0];
                switch (type) {
                    case NewsOverViewFragment.ALL:
                        entries.addAll(DatabaseHandler.getInstance().getEntries(category.getId(), null, true));
                    case NewsOverViewFragment.FAV:
                        entries.addAll(DatabaseHandler.getInstance().getFavoriteEntries(category.getId()));
                    case NewsOverViewFragment.RECENT:
                        entries.addAll(DatabaseHandler.getInstance().getVisitedEntries(category.getId()));
                    case NewsOverViewFragment.UNREAD:
                        entries.addAll(DatabaseHandler.getInstance().getUnreadEntries(category.getId()));
                }
            }
            return entries;
        }

        @Override
        protected void onPostExecute(List<Entry> entries) {
            if (mExpandableItemRecyclerAdapter != null) {
                mExpandableItemRecyclerAdapter.addNewEntriesAndRemoveOld(entries);
            }
            setRefresh(false);
        }
    }

}