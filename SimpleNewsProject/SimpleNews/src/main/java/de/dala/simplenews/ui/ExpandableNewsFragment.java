package de.dala.simplenews.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayoutExtended;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.ocpsoft.pretty.time.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Entry;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.database.IDatabaseHandler;
import de.dala.simplenews.database.PersistableEntries;
import de.dala.simplenews.database.SimpleCursorLoader;
import de.dala.simplenews.network.VolleySingleton;
import de.dala.simplenews.utilities.CategoryUpdater;
import de.dala.simplenews.utilities.ExpandableGridItemCursorAdapter;
import de.dala.simplenews.utilities.PrefUtilities;
import de.dala.simplenews.utilities.SparseBooleanArrayParcelable;
import de.dala.simplenews.utilities.UIUtils;

/**
 * Created by Daniel on 18.12.13.
 */
public class ExpandableNewsFragment extends Fragment implements SwipeRefreshLayoutExtended.OnRefreshListener, SimpleCursorLoader.OnLoadCompleteListener, NewsTypeBar.INewsTypeClicked {
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_ENTRY_TYPE = "entryType";
    private MyExpandableGridItemAdapter myExpandableListItemAdapter;
    private ActionMode mActionMode;
    private StaggeredGridView mGridView;
    private SwipeRefreshLayoutExtended mSwipeRefreshLayout;
    private AnimationAdapter swingBottomInAnimationAdapter;

    private Category category;
    private CategoryUpdater updater;
    private NewsTypeBar newsTypeBar;
    private Menu menu;
    private MenuItem columnsMenu;
    private INewsInteraction newsInteraction;
    private ShareActionProvider shareActionProvider;

    private SimpleCursorLoader simpleCursorLoader;

    private int newsTypeMode = NewsTypeBar.ALL;

    private TextView emptyText;
    private ImageView emptyImageView;
    private String noEntriesText;
    private String isLoadingText;

    public ExpandableNewsFragment() {
        //shouldn't be called
    }

    public static ExpandableNewsFragment newInstance(Category category, int entryType) {
        ExpandableNewsFragment f = new ExpandableNewsFragment();
        Bundle b = new Bundle();
        b.putParcelable(ARG_CATEGORY, category);
        b.putInt(ARG_ENTRY_TYPE, entryType);
        f.setArguments(b);
        return f;
    }

    public void setNewsInteraction(INewsInteraction newsInteraction) {
        this.newsInteraction = newsInteraction;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.category = getArguments().getParcelable(ARG_CATEGORY);
        newsTypeMode = getArguments().getInt(ARG_ENTRY_TYPE);
        noEntriesText = getResources().getString(R.string.no_entries);
        isLoadingText = getResources().getString(R.string.is_loading);
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (!visible) {
            if (mActionMode != null) {
                mActionMode.finish();
            }
        } else {
            if (newsTypeBar != null) {
                newsTypeBar.fadeIn();
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSwipeRefreshLayout = (SwipeRefreshLayoutExtended) view.findViewById(R.id.ptr_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeFromColors(category.getPrimaryColor(), getResources().getColor(R.color.background_window), category.getPrimaryColor(), getResources().getColor(R.color.background_window));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.news_list, container, false);

        mGridView = (StaggeredGridView) rootView.findViewById(R.id.news_gridview);
        mGridView.setEmptyView(rootView.findViewById(R.id.emptyView));
        emptyText = (TextView) rootView.findViewById(R.id.emptyMessage);
        emptyImageView = (ImageView) rootView.findViewById(R.id.emptyImageView);
        emptyImageView.setImageResource(R.drawable.logo_animation);

        if (savedInstanceState != null) {
            newsTypeMode = savedInstanceState.getInt("newsTypeMode");
        }
        initCardsAdapter(null);
        initNewsTypeBar(rootView);
        loadEntries(false, true);
        return rootView;
    }

    private void initNewsTypeBar(View rootView) {
        newsTypeBar = (NewsTypeBar) rootView.findViewById(R.id.news_type_bar);
        newsTypeBar.init(category.getPrimaryColor(), this, mGridView, newsTypeMode);
        newsTypeBar.fadeIn();
    }

    private void setEmptyText(boolean isLoading) {
        String textToShow = noEntriesText;
        emptyText.setText(textToShow);
        AnimationDrawable animDrawable = (AnimationDrawable)emptyImageView.getDrawable();
        animDrawable.setOneShot(!isLoading);
        animDrawable.start();
    }

    private void updateColumnCount() {
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                boolean useMultipleLandscape = PrefUtilities.getInstance().useMultipleColumnsLandscape();
                if (mGridView != null) {
                    mGridView.setColumnCountLandscape(useMultipleLandscape ? 2 : 1);
                }
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                boolean useMultiplePortrait = PrefUtilities.getInstance().useMultipleColumnsPortrait();
                if (mGridView != null) {
                    mGridView.setColumnCountPortrait(useMultiplePortrait ? 2 : 1);
                }
                break;
        }
    }

    private boolean shouldUseMultipleColumns(){
        boolean useMultiple = false;
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                useMultiple = PrefUtilities.getInstance().useMultipleColumnsLandscape();
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                useMultiple = PrefUtilities.getInstance().useMultipleColumnsPortrait();
                break;
        }
        return useMultiple;
    }

    private void updateMenu(){
        boolean useMultiple = shouldUseMultipleColumns();
        if (columnsMenu != null){
            columnsMenu.setTitle(useMultiple ? getString(R.string.single_columns) : getString(R.string.multiple_columns));
        }
    }

    @Override
    public void onRefresh() {
        if (updater != null && updater.isRunning()) {
            mSwipeRefreshLayout.setRefreshing(false);
        } else {
            loadEntries(true, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                loadEntries(true, true);
                return true;
            case R.id.menu_columns:
                updateMenu();
                updateColumnCount();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initCardsAdapter(Cursor cursor) {
        myExpandableListItemAdapter = new MyExpandableGridItemAdapter(getActivity(), cursor);
        swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(myExpandableListItemAdapter);
        swingBottomInAnimationAdapter.setAbsListView(mGridView);
        swingBottomInAnimationAdapter.setInitialDelayMillis(300);

        if (mGridView != null) {
            mGridView.setAdapter(swingBottomInAnimationAdapter);
        }
        simpleCursorLoader = new SimpleCursorLoader(getActivity()) {
            @Override
            public Cursor loadInBackground() {
                return getCursorByNewsType(newsTypeMode);
            }
        };
        simpleCursorLoader.registerListener(0, this);
        updateColumnCount();
    }

    private Cursor getCursorByNewsType(int type){
        switch (type) {
            case NewsTypeBar.ALL:
                return DatabaseHandler.getInstance().getEntriesCursor(category.getId());
            case NewsTypeBar.FAV:
                return DatabaseHandler.getInstance().getFavoriteEntriesCursor(category.getId());
            case NewsTypeBar.RECENT:
                return DatabaseHandler.getInstance().getRecentEntriesCursor(category.getId());
            case NewsTypeBar.UNREAD:
                return DatabaseHandler.getInstance().getUnreadEntriesCursor(category.getId());
        }
        return null;
    }
    private void onListItemCheck(int position, boolean value) {
        myExpandableListItemAdapter.selectView(position, value);
    }

    private void onListItemCheck(int position) {
        myExpandableListItemAdapter.toggleSelection(position);
        OpenActionModeIfNecessary();

        if (mActionMode != null) {
            mActionMode.setTitle(String.valueOf(myExpandableListItemAdapter.getSelectedCount()));
        }
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(createShareIntent());
        }
    }

    private void OpenActionModeIfNecessary(){
        boolean hasCheckedItems = myExpandableListItemAdapter.getSelectedCount() > 0;
        if (hasCheckedItems && mActionMode == null) {
            // there are some selected items, start the actionMode
            mActionMode = ((ActionBarActivity) getActivity()).startSupportActionMode(new ActionModeCallBack());
        } else if (!hasCheckedItems && mActionMode != null) {
            // there no selected items, finish the actionMode
            mActionMode.finish();
        }
    }

    public void refreshFeeds(boolean showNewsInteraction) {
        if (updater == null) {
            updater = new CategoryUpdater(new CategoryUpdateHandler(), category, true, getActivity());
        }
        if (updater.start()) {
            if (newsInteraction != null && showNewsInteraction) {
                newsInteraction.showLoadingNews();
            }
            setRefreshActionButtonState(true);
        }
    }

    private void updateFinished(boolean success) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        if (mActionMode != null) {
            mActionMode.finish();
        }
        if (newsInteraction != null) {
            newsInteraction.cancelLoadingNews();
        }
        setRefreshActionButtonState(false);

        if (success){
            simpleCursorLoader.startLoading();
        }
        setEmptyText(false);
    }

    private void setRefreshActionButtonState(boolean refreshing) {
        if (menu == null) {
            return;
        }

        final MenuItem refreshItem = menu.findItem(R.id.menu_refresh);
        if (refreshItem != null) {
            if (refreshing) {
                MenuItemCompat.setActionView(refreshItem, R.layout.actionbar_indeterminate_progress);
            } else {
                MenuItemCompat.setActionView(refreshItem, null);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        menu.clear();
        inflater.inflate(R.menu.expandable_news_menu, menu);
        columnsMenu = menu.findItem(R.id.menu_columns);
        updateMenu();
        super.onCreateOptionsMenu(menu, inflater);
    }


    private void loadEntries(boolean forceRefresh, boolean showNewsInteraction) {
        setEmptyText(true);
        long timeForRefresh = PrefUtilities.getInstance().getTimeForRefresh();
        if (forceRefresh || category.getLastUpdateTime() < new Date().getTime() - timeForRefresh) {
            refreshFeeds(showNewsInteraction);
        }else{
            simpleCursorLoader.startLoading();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("newsTypeMode", newsTypeMode);
        super.onSaveInstanceState(outState);
    }

    private Intent createShareIntent() {
        // retrieve selected items and print them out
        SparseBooleanArrayParcelable selected = myExpandableListItemAdapter.getSelectedIds();
        ArrayList<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < selected.size(); i++) {
            if (selected.valueAt(i)) {
                Entry selectedItem = GetEntryByCursor((Cursor) myExpandableListItemAdapter.getItem(selected.keyAt(i)));
                entries.add(selectedItem);
            }
        }

        String finalMessage = TextUtils.join("\n", entries) + " - by SimpleNews";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                finalMessage);
        return shareIntent;
    }

    private void saveSelectedEntries(List<Entry> selectedEntries) {
        for (Entry entry : selectedEntries) {
            entry.setFavoriteDate((entry.getFavoriteDate() == null || entry.getFavoriteDate() == 0) ? new Date().getTime() : null);
            DatabaseHandler.getInstance().updateEntry(entry);
        }
    }

    private void markEntriesAsRead(List<Entry> selectedEntries) {
        for (Entry entry : selectedEntries) {
            entry.setVisitedDate((entry.getVisitedDate() == null || entry.getVisitedDate() == 0) ? new Date().getTime() : null);
            DatabaseHandler.getInstance().updateEntry(entry);
        }
    }

    @Override
    public void onLoadComplete(Loader loader, Object data) {
        if (data instanceof  Cursor){
            Cursor cursor = (Cursor) data;
            swingBottomInAnimationAdapter.reset();
            myExpandableListItemAdapter.changeCursor(cursor);
        }
        setEmptyText(false);
    }

    @Override
    public void newsTypeClicked(int type) {
        newsTypeMode = type;
        loadEntries(false, false);
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    public interface INewsInteraction {
        void showLoadingNews();
        void cancelLoadingNews();
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
                    Toast.makeText(getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case CategoryUpdater.CANCEL:
                    updateFinished(false);
                    break;
            }
        }
    }

    public Entry GetEntryByCursor(Cursor cursor){
        return PersistableEntries.loadFromCursor(cursor);
    }

    private class MyExpandableGridItemAdapter extends ExpandableGridItemCursorAdapter {

        private Context mContext;
        private SparseBooleanArrayParcelable mSelectedItemIds;

        /**
         * Creates a new ExpandableListItemAdapter with the specified list, or an empty list if
         * items == null.
         */
        private MyExpandableGridItemAdapter(Context context, Cursor cursor) {
            super(context, R.layout.expandable_card, R.id.card_title, R.id.expandable_card_content, cursor);
            mContext = context;
            mSelectedItemIds = new SparseBooleanArrayParcelable();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onListItemCheck(position);
                    return false;
                }
            });
            view.setBackgroundResource(mSelectedItemIds.get(position) ? R.drawable.card_background_blue : R.drawable.card_background_white);
            int pad = getResources().getDimensionPixelSize(R.dimen.card_layout_padding);
            view.setPadding(pad, pad, pad, pad);
            return view;
        }

        @Override
        public View getTitleView(final int position, View convertView, ViewGroup parent) {
            Entry entry = GetEntryByCursor((Cursor) getItem(position));

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.news_card, null);

            TextView titleTextView = (TextView) layout.findViewById(R.id.title);
            TextView infoTextView = (TextView) layout.findViewById(R.id.info);
            ImageView entryType = (ImageView) layout.findViewById(R.id.image);

            UIUtils.setTextMaybeHtml(titleTextView, entry.getTitle());

            if (entry.getFavoriteDate() != null && entry.getFavoriteDate() > 0) {
                entryType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_nav_favorite));
            } else if (entry.getVisitedDate() != null && entry.getVisitedDate() > 0) {
                entryType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_nav_recently_used));
            }

            long current = new Date().getTime();
            if (current > entry.getDate()) {
                current = entry.getDate();
            }
            String prettyTimeString = new PrettyTime().format(new Date(current));

            infoTextView.setText(String.format("%s - %s", entry.getSrcName(), prettyTimeString));
            infoTextView.setTextColor(category.getSecondaryColor());
            infoTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

            return layout;
        }

        @Override
        public View getContentView(final int position, View convertView, ViewGroup parent) {
            final Entry entry = GetEntryByCursor((Cursor) getItem(position));

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.news_card_expand, null);

            View colorBorder = layout.findViewById(R.id.colorBorder);
            colorBorder.setBackgroundColor(category.getSecondaryColor());

            final TextView description = (TextView) layout.findViewById(R.id.expand_card_main_inner_simple_title);
            UIUtils.setTextMaybeHtml(description, entry.getDescription());

            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    entry.setVisitedDate(new Date().getTime());
                    DatabaseHandler.getInstance().updateEntry(entry);
                    myExpandableListItemAdapter.notifyDataSetChanged();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(entry.getLink())); //TODO link or shortenedlink
                    startActivity(browserIntent);
                }
            });
            return layout;
        }

        @Override
        public void selectAllIds() {
            for(int i = 0; i < mCursor.getCount(); i++){
                onListItemCheck(i, true);
            }
            OpenActionModeIfNecessary();

            if (mActionMode != null) {
                mActionMode.setTitle(String.valueOf(myExpandableListItemAdapter.getSelectedCount()));
            }
            if (shareActionProvider != null) {
                shareActionProvider.setShareIntent(createShareIntent());
            }
            notifyDataSetChanged();
        }

        @Override
        public void deselectAllIds() {
            for(int i = 0; i < mCursor.getCount(); i++){
                onListItemCheck(i, false);
            }
            OpenActionModeIfNecessary();

            if (mActionMode != null) {
                mActionMode.setTitle(String.valueOf(myExpandableListItemAdapter.getSelectedCount()));
            }
            if (shareActionProvider != null) {
                shareActionProvider.setShareIntent(createShareIntent());
            }
            notifyDataSetChanged();
        }

        public void toggleSelection(int position) {
            selectView(position, !mSelectedItemIds.get(position));
        }

        public void selectView(int position, boolean value) {
            if (value) {
                mSelectedItemIds.put(position, value);
            } else {
                mSelectedItemIds.delete(position);
            }
            notifyDataSetChanged();
        }

        public int getSelectedCount() {
            return mSelectedItemIds.size();// mSelectedCount;
        }

        public SparseBooleanArrayParcelable getSelectedIds() {
            return mSelectedItemIds;
        }

        public void removeSelection() {
            mSelectedItemIds = new SparseBooleanArrayParcelable();
            notifyDataSetChanged();
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
    }

    private class ActionModeCallBack implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.contextual_list_view, menu);
            MenuItem item = menu.findItem(R.id.menu_item_share);

            shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
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
            // retrieve selected items and print them out
            SparseBooleanArray selected = myExpandableListItemAdapter.getSelectedIds();
            List<Entry> selectedEntries = new ArrayList<Entry>();
            for (int i = 0; i < selected.size(); i++) {
                if (selected.valueAt(i)) {
                    Entry selectedEntry = GetEntryByCursor((Cursor) myExpandableListItemAdapter.getItem(selected.keyAt(i)));
                    selectedEntries.add(selectedEntry);
                }
            }
            boolean shouldFinish = true;
            // close action mode
            switch (item.getItemId()) {
                case R.id.menu_item_save:
                    saveSelectedEntries(selectedEntries);
                    break;
                case R.id.menu_item_read:
                    markEntriesAsRead(selectedEntries);
                    break;
                case R.id.menu_item_select_all:
                    myExpandableListItemAdapter.selectAllIds();
                    shouldFinish = false;
                    break;
                case R.id.menu_item_deselect_all:
                    myExpandableListItemAdapter.deselectAllIds();
                    break;

            }
            if (shouldFinish) {
                mode.finish();
                myExpandableListItemAdapter.removeSelection();
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    }


}