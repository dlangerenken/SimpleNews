package de.dala.simplenews.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import recycler.AlphaAnimationAdapter;
import recycler.ExpandableItemRecyclerAdapter;
import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Entry;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.utilities.BaseNavigation;
import de.dala.simplenews.utilities.CategoryUpdater;
import de.dala.simplenews.utilities.PrefUtilities;
import recycler.FadeInUpAnimator;
import recycler.SlideInBottomAnimationAdapter;


public class ExpandableNewsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, BaseNavigation, ExpandableItemRecyclerAdapter.ItemClickListener {
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_ENTRY_TYPE = "entryType";
    private static final String ARG_POSITION = "position";

    private ExpandableItemRecyclerAdapter mExpandableItemRecyclerAdapter;
    private ActionMode mActionMode;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Category category;
    private CategoryUpdater updater;
    private Menu menu;
    private int newsTypeMode;
    private NewsOverViewFragment parentFragment;

    private View emptyView;
    private TextView emptyText;
    private ImageView emptyImageView;
    private String noEntriesText;
    private String isLoadingText;
    private int position;
    StaggeredGridLayoutManager mLayoutManager;

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

    public Category getCategory(){
        return category;
    }

    public int getPosition(){
        return position;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.category = getArguments().getParcelable(ARG_CATEGORY);
        this.newsTypeMode = getArguments().getInt(ARG_ENTRY_TYPE);
        this.position = getArguments().getInt(ARG_POSITION);
        noEntriesText = getResources().getString(R.string.no_entries);
        isLoadingText = getResources().getString(R.string.is_loading);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment newsFragment = getParentFragment();
        if (newsFragment != null && newsFragment instanceof NewsOverViewFragment){
            this.parentFragment = (NewsOverViewFragment) newsFragment;
        }else{
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
        }
    }

    private void showEmptyView(boolean visible){
        mRecyclerView.setVisibility(visible ? View.VISIBLE :  View.INVISIBLE);
        emptyView.setVisibility(!visible? View.INVISIBLE :  View.VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.news_list, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.news_gridview);
        emptyView = rootView.findViewById(R.id.emptyView);
        emptyText = (TextView) rootView.findViewById(R.id.emptyMessage);
        emptyImageView = (ImageView) rootView.findViewById(R.id.emptyImageView);
        emptyImageView.setImageResource(R.drawable.logo_animation);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.ptr_layout);

        init();
        return rootView;
    }

    private void init(){
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

    private void setEmptyText(boolean isLoading) {
        if (!isDetached()) {
            String textToShow = noEntriesText;
            if (isLoading) {
                textToShow = isLoadingText;
            }
            emptyText.setText(textToShow);
            AnimationDrawable animDrawable = (AnimationDrawable) emptyImageView.getDrawable();
            animDrawable.setOneShot(!isLoading);
            animDrawable.stop();
            if (isLoading) {
                animDrawable.start();
            }
        }
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

    private void setRefresh(boolean refresh){
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
        mRecyclerView.setItemAnimator(new FadeInUpAnimator());
        mExpandableItemRecyclerAdapter = new ExpandableItemRecyclerAdapter(new ArrayList<Entry>(), category,getActivity(), this, mRecyclerView);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(mExpandableItemRecyclerAdapter);
            //mRecyclerView.setAdapter(new AlphaAnimationAdapter(new SlideInBottomAnimationAdapter(mExpandableItemRecyclerAdapter)));
        }
        updateColumnCount();
    }

    public void refreshFeeds() {
        if (updater == null) {
            updater = new CategoryUpdater(new CategoryUpdateHandler(), category, true, getActivity());
        }
        if (updater.start()) {
            setRefresh(true);
        }
    }

    private void updateFinished(boolean success) {
        if (!isDetached()) {
            if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
            if (mActionMode != null) {
                mActionMode.finish();
            }
            setRefresh(false);
            setEmptyText(false);

            if (success || mExpandableItemRecyclerAdapter != null && mExpandableItemRecyclerAdapter.getCount() == 0) {
                new BackgroundEntryLoading().execute(newsTypeMode);
            }
        }
    }

    private void setRefreshActionButtonState(boolean refreshing) {
        if (menu == null) {
            return;
        }

        final MenuItem refreshItem = menu.findItem(R.id.menu_refresh);
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
        super.onCreateOptionsMenu(menu, inflater);
        this.menu = menu;
        if (menu.findItem(R.id.menu_refresh) != null){
            return;
        }
        inflater.inflate(R.menu.expandable_news_menu, menu);
    }

    private boolean loadingEntries = false;

    private void loadEntries(boolean forceRefresh) {
        setEmptyText(true);
        long timeForRefresh = PrefUtilities.getInstance().getTimeForRefresh();
        if (forceRefresh || (category != null && category.getLastUpdateTime() != null && category.getLastUpdateTime() < new Date().getTime() - timeForRefresh)) {
            refreshFeeds();
        }else {
            loadingEntries = true;
            new BackgroundEntryLoading().execute(newsTypeMode);
        }
        if (mExpandableItemRecyclerAdapter != null && mExpandableItemRecyclerAdapter.getCount() == 0 && !loadingEntries){
            loadingEntries = true;
            new BackgroundEntryLoading().execute(newsTypeMode);
        }
    }

    private Intent createShareIntent() {
        // retrieve selected items and print them out
        Set<Entry> entries = mExpandableItemRecyclerAdapter.getSelectedIds();

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
        switch (newsTypeMode){
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
        List<Entry> updatedEntries = new ArrayList<>();
        updatedEntries.add(entry);
        mExpandableItemRecyclerAdapter.updateEntries(updatedEntries);

        //String link = entry.getShortenedLink() != null ? entry.getShortenedLink() : entry.getLink();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(entry.getLink()));
        try{
            startActivity(browserIntent);
        } catch (ActivityNotFoundException e){
            Context context = getActivity();
            if (context != null) {
                Toast.makeText(context, context.getResources().getString(R.string.no_browser_found), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void updateActionMode() {
        boolean hasCheckedItems = mExpandableItemRecyclerAdapter.getSelectedCount() > 0;
        if (hasCheckedItems && mActionMode == null) {
            mActionMode = getActivity().startActionMode(new ActionModeCallBack());
        } else if (!hasCheckedItems && mActionMode != null) {
            mActionMode.finish();
        }
        if (hasCheckedItems && mActionMode != null){
            mActionMode.setTitle(String.format("%d", mExpandableItemRecyclerAdapter.getSelectedCount()));
        }
    }


    private class CategoryUpdateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CategoryUpdater.RESULT:
                    updateFinished(true);
                    break;
                case CategoryUpdater.STATUS_CHANGED:
                    break;
                case CategoryUpdater.ERROR:
                    updateFinished(false);
                    Context context = getActivity();
                    if (context != null && msg.obj != null){
                        Toast.makeText(context, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case CategoryUpdater.CANCEL:
                    updateFinished(false);
                    break;
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
    public void onPause()
    {
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
            getActivity().getTheme().applyStyle(R.style.ChangeOverflowToDark, true);
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            changeOverflowIcon();
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.contextual_list_view, menu);
            MenuItem item = menu.findItem(R.id.menu_item_share);

            ShareActionProvider shareActionProvider = new ShareActionProvider(getActivity());
            item.setActionProvider(shareActionProvider);
            shareActionProvider.setShareHistoryFileName(
                    ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
            shareActionProvider.setShareIntent(createShareIntent());
            shareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
                @Override
                public boolean onShareTargetSelected(ShareActionProvider shareActionProvider, Intent intent) {
                    return false;
                }
            });
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Set<Entry> selectedEntries = mExpandableItemRecyclerAdapter.getSelectedIds();

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
                    mExpandableItemRecyclerAdapter.selectAllIds();
                    shouldFinish = false;
                    break;
                case R.id.menu_item_deselect_all:
                    mExpandableItemRecyclerAdapter.deselectAllIds();
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
            mExpandableItemRecyclerAdapter.deselectAllIds();
        }
    }

    private class BackgroundEntryLoading extends AsyncTask<Integer, Void, List<Entry>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingEntries = true;
        }

        @Override
        protected List<Entry> doInBackground(Integer... params) {
            List<Entry> entries = new ArrayList<>();
            if (params != null && params.length > 0){
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
            loadingEntries = false;
            mExpandableItemRecyclerAdapter.updateEntries(entries);
        }

    }
}