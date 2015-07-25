package de.dala.simplenews.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Entry;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.utilities.CategoryUpdater;
import de.dala.simplenews.utilities.EmptyObservableRecyclerView;
import de.dala.simplenews.utilities.PrefUtilities;
import de.dala.simplenews.recycler.ExpandableItemRecyclerAdapter;
import de.dala.simplenews.recycler.FadeInUpAnimator;
import de.dala.simplenews.utilities.Utilities;


public class ExpandableNewsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, ExpandableItemRecyclerAdapter.ItemClickListener {
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_ENTRY_TYPE = "entryType";
    private static final String ARG_POSITION = "position";

    private ExpandableItemRecyclerAdapter mExpandableItemRecyclerAdapter;
    private EmptyObservableRecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView emptyTextView;

    private Category category;
    private CategoryUpdater updater;
    private int newsTypeMode;
    private StaggeredGridLayoutManager mLayoutManager;

    private boolean isRefreshing;
    private MenuItem refreshItem;

    public ExpandableNewsFragment() {
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

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            setRefresh(isRefreshing);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.category = getArguments().getParcelable(ARG_CATEGORY);
        this.newsTypeMode = getArguments().getInt(ARG_ENTRY_TYPE);
    }

    @Override
    public void onResume() {
        super.onResume();
        addListener();
    }

    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private void addListener() {
        if (listener == null) {
            listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    switch (key) {
                        case PrefUtilities.CURRENT_NEWS_TYPE_MODE:
                            newsTypeModeChanged(PrefUtilities.getInstance().getNewsTypeMode());
                            break;
                        case PrefUtilities.MULTIPLE_COLUMNS_LANDSCAPE:
                            gridSettingsChanged();
                            break;
                        case PrefUtilities.MULTIPLE_COLUMNS_PORTRAIT:
                            gridSettingsChanged();
                            break;
                    }
                }
            };
            PrefUtilities.getInstance().addListener(listener);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.news_list, container, false);

        mRecyclerView = (EmptyObservableRecyclerView) rootView.findViewById(R.id.scroll);
        mRecyclerView.setEmptyView(rootView.findViewById(R.id.emptyView));
        emptyTextView = (TextView) rootView.findViewById(R.id.emptyMessage);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_layout);
        setHasOptionsMenu(true);
        init();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        loadEntries(false);
    }

    private void init() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setOnRefreshListener(this);
            mSwipeRefreshLayout.setColorSchemeColors(category.getPrimaryColor());
            mSwipeRefreshLayout.setProgressViewOffset(false, 0,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));

        }
        emptyTextView.setTextColor(category.getPrimaryColor());
        initCardsAdapter();
    }

    private void updateColumnCount() {
        if (mLayoutManager != null && isAdded()) {
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
        assert mRecyclerView != null;
        mExpandableItemRecyclerAdapter = new ExpandableItemRecyclerAdapter(new ArrayList<Entry>(), category, getActivity(), this, mRecyclerView, null);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new FadeInUpAnimator());
        mRecyclerView.setAdapter(mExpandableItemRecyclerAdapter);
        mRecyclerView.setHasFixedSize(true);
        updateColumnCount();
    }

    private void refreshFeeds() {
        if (updater == null) {
            updater = new CategoryUpdater(new CategoryUpdateHandler(this), category, getActivity());
        }
        setRefresh(true);
        updater.start();
    }

    private void receivePartResult(List<Entry> entries) {
        mExpandableItemRecyclerAdapter.add(entries);
    }

    private void updateFinished(boolean success, List<Entry> entries) {
        if (!isDetached()) {
            if (success) {
                mExpandableItemRecyclerAdapter.removeOldEntries(entries);
            } else {
                refreshFeedsByDatabase();
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
            refreshFeedsByDatabase();
        }
    }

    private void refreshFeedsByDatabase() {
        new BackgroundEntryLoading().execute(newsTypeMode);
    }


    private void deleteSelectedEntries(List<Entry> selectedEntries) {
        for (Entry entry : selectedEntries) {
            entry.setVisible(false);
            DatabaseHandler.getInstance().updateEntry(entry);
        }
        mExpandableItemRecyclerAdapter.remove(selectedEntries);
    }

    private void saveEntry(Entry entry) {
        entry.setFavoriteDate((entry.getFavoriteDate() == null || entry.getFavoriteDate() == 0) ? new Date().getTime() : null);
        DatabaseHandler.getInstance().updateEntry(entry);
        mExpandableItemRecyclerAdapter.update(entry);
    }

    private void saveSelectedEntries(List<Entry> selectedEntries) {
        for (Entry entry : selectedEntries) {
            saveEntry(entry);
        }
    }

    private void markEntryAsRead(Entry entry) {
        entry.setVisitedDate((entry.getVisitedDate() == null || entry.getVisitedDate() == 0) ? new Date().getTime() : null);
        DatabaseHandler.getInstance().updateEntry(entry);
        mExpandableItemRecyclerAdapter.update(entry);
    }

    private void markEntriesAsRead(List<Entry> selectedEntries) {
        for (Entry entry : selectedEntries) {
            markEntryAsRead(entry);
        }
    }


    private void newsTypeModeChanged(int newsTypeMode) {
        this.newsTypeMode = newsTypeMode;
        refreshFeedsByDatabase();
    }

    private void gridSettingsChanged() {
        updateColumnCount();
    }

    @Override
    public void onOpenClick(Entry entry) {
        markEntryAsRead(entry);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        //String link = entry.getShortenedLink() != null ? entry.getShortenedLink() : entry.getLink();
        browserIntent.setData(Uri.parse(entry.getLink()));
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
    public void onLongClick(Entry entry) {
        final List<Entry> entries = new ArrayList<>();
        entries.add(entry);
        new MaterialDialog.Builder(getActivity())
                .items(R.array.entry_options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int which, CharSequence charSequence) {
                        switch (which) {
                            case 0: //Share
                                share(entries);
                                break;
                            case 1: // Save
                                saveSelectedEntries(entries);
                                break;
                            case 2: // Read
                                markEntriesAsRead(entries);
                                break;
                            case 3: // Delete
                                deleteSelectedEntries(entries);
                                break;
                        }
                    }
                }).show();
    }

    private void share(List<Entry> entries) {
        String finalMessage = Utilities.join("\n", entries, PrefUtilities.getInstance().shouldShortenLinks()) + " - by SimpleNews";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                finalMessage);
        startActivity(shareIntent);
    }

    private static class CategoryUpdateHandler extends Handler {
        final WeakReference<ExpandableNewsFragment> mFragment;

        public CategoryUpdateHandler(ExpandableNewsFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        private List<Entry> entriesFromObject(Object obj){
            if (obj == null || !(obj instanceof List<?>)){
                return null;
            }
            List<?> castedList = (List<?>) obj;
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < castedList.size(); i++) {
                Object elem = castedList.get(i);
                if (elem instanceof Entry) {
                    entries.add((Entry) elem);
                }
            }
            return entries;
        }

        @Override
        public void handleMessage(Message msg) {
            ExpandableNewsFragment fragment = mFragment.get();
            if (fragment != null) {
                switch (msg.what) {
                    case CategoryUpdater.RESULT:
                        fragment.updateFinished(true, entriesFromObject(msg.obj));
                        break;
                    case CategoryUpdater.PART_RESULT:
                        if (msg.obj != null) {
                            fragment.receivePartResult(entriesFromObject(msg.obj));
                        }
                        break;
                    case CategoryUpdater.ERROR:
                        fragment.updateFinished(false, null);
                        break;
                    case CategoryUpdater.CANCEL:
                        fragment.updateFinished(false, null);
                        break;
                    case CategoryUpdater.EMPTY:
                        fragment.updateFinished(false, null);
                        break;
                }
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (updater != null) {
            updater.cancel();
        }
        removeListener();
    }

    private void removeListener() {
        if (listener != null) {
            PrefUtilities.getInstance().removeListener(listener);
        }
        listener = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (updater != null) {
            updater.cancel();
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
            if (category.getId() == null || category.getId() == -1) {
                return entries;
            }
            if (params != null && params.length > 0) {
                int type = params[0];
                switch (type) {
                    case NewsActivity.ALL:
                        entries.addAll(DatabaseHandler.getInstance().getEntries(category.getId(), null, true));
                        break;
                    case NewsActivity.FAV:
                        entries.addAll(DatabaseHandler.getInstance().getFavoriteEntries(category.getId()));
                        break;
                    case NewsActivity.RECENT:
                        entries.addAll(DatabaseHandler.getInstance().getVisitedEntries(category.getId()));
                        break;
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