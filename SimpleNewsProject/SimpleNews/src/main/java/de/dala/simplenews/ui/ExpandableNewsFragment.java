package de.dala.simplenews.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.ViewAnimator;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.ocpsoft.pretty.time.PrettyTime;

import java.util.ArrayList;
import java.util.Date;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Entry;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.database.PersistableEntries;
import de.dala.simplenews.database.SimpleCursorLoader;
import de.dala.simplenews.utilities.BaseNavigation;
import de.dala.simplenews.utilities.CategoryUpdater;
import de.dala.simplenews.utilities.ExpandableGridItemCursorAdapter;
import de.dala.simplenews.utilities.PrefUtilities;
import de.dala.simplenews.utilities.SparseBooleanArrayParcelable;
import de.dala.simplenews.utilities.UIUtils;

/**
 * Created by Daniel on 18.12.13.
 */
public class ExpandableNewsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, SimpleCursorLoader.OnLoadCompleteListener, BaseNavigation {
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_ENTRY_TYPE = "entryType";
    private static final String ARG_POSITION = "position";
    private static final int INITIAL_DELAY_MILLIS = 300;

    private MyExpandableGridItemAdapter myExpandableListItemAdapter;
    private ActionMode mActionMode;
    private StaggeredGridView mGridView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AnimationAdapter swingBottomInAnimationAdapter;

    private Category category;
    private CategoryUpdater updater;
    private Menu menu;
    private int newsTypeMode;
    private NewsOverViewFragment parentFragment;

    private SimpleCursorLoader simpleCursorLoader;

    private TextView emptyText;
    private ImageView emptyImageView;
    private String noEntriesText;
    private String isLoadingText;
    private int position;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.news_list, container, false);

        mGridView = (StaggeredGridView) rootView.findViewById(R.id.news_gridview);
        mGridView.setEmptyView(rootView.findViewById(R.id.emptyView));
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
        initCardsAdapter(null);
        initNewsTypeBar();
        loadEntries(false, true);
    }

    private void initNewsTypeBar() {
        NewsTypeButtonAnimation animation = new NewsTypeButtonAnimation();
        animation.init(mGridView, parentFragment.getNewsTypeButton());
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void initCardsAdapter(Cursor cursor) {
        myExpandableListItemAdapter = new MyExpandableGridItemAdapter(getActivity(), cursor);
        swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(myExpandableListItemAdapter);
        swingBottomInAnimationAdapter.setAbsListView(mGridView);
        ViewAnimator animator = swingBottomInAnimationAdapter.getViewAnimator();
        if (animator != null){
            animator.setInitialDelayMillis(INITIAL_DELAY_MILLIS);
        }
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
            case NewsOverViewFragment.ALL:
                return DatabaseHandler.getInstance().getEntriesCursor(category.getId(), true);
            case NewsOverViewFragment.FAV:
                return DatabaseHandler.getInstance().getFavoriteEntriesCursor(category.getId());
            case NewsOverViewFragment.RECENT:
                return DatabaseHandler.getInstance().getRecentEntriesCursor(category.getId());
            case NewsOverViewFragment.UNREAD:
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
    }

    private void OpenActionModeIfNecessary(){
        boolean hasCheckedItems = myExpandableListItemAdapter.getSelectedCount() > 0;
        if (hasCheckedItems && mActionMode == null) {
            // there are some selected items, start the actionMode
            mActionMode = ((ActionBarActivity) getActivity()).startActionMode(new ActionModeCallBack());
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
            if (showNewsInteraction) {
                parentFragment.showLoadingNews();
            }
            setRefreshActionButtonState(true);
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
            if (!parentFragment.isDetached()) {
                parentFragment.cancelLoadingNews();
            }
            setRefreshActionButtonState(false);

            if (success || myExpandableListItemAdapter != null && myExpandableListItemAdapter.getCount() == 0) {
                simpleCursorLoader.startLoading();
            }
            setEmptyText(false);
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

    private void loadEntries(boolean forceRefresh, boolean showNewsInteraction) {
        boolean loaded = false;
        setEmptyText(true);
        long timeForRefresh = PrefUtilities.getInstance().getTimeForRefresh();
        if (forceRefresh || (category != null && category.getLastUpdateTime() != null && category.getLastUpdateTime() < new Date().getTime() - timeForRefresh)) {
            refreshFeeds(showNewsInteraction);
        }else {
            simpleCursorLoader.startLoading();
            loaded = true;
        }
        if (myExpandableListItemAdapter != null && myExpandableListItemAdapter.getCount() == 0 && !loaded){
            simpleCursorLoader.startLoading();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
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

    private void deleteSelectedEntries(SparseArray<Entry> selectedEntries) {
        for (int i = 0; i < selectedEntries.size(); i++){
            int key = selectedEntries.keyAt(i);
            Entry entry = selectedEntries.get(key);
            entry.setVisible(false);
            DatabaseHandler.getInstance().updateEntry(entry);
        }
    }

    private void saveSelectedEntries(SparseArray<Entry> selectedEntries) {
        for (int i = 0; i < selectedEntries.size(); i++){
            int key = selectedEntries.keyAt(i);
            Entry entry = selectedEntries.get(key);
            entry.setFavoriteDate((entry.getFavoriteDate() == null || entry.getFavoriteDate() == 0) ? new Date().getTime() : null);
            DatabaseHandler.getInstance().updateEntry(entry);
        }
    }

    private void markEntriesAsRead(SparseArray<Entry> selectedEntries) {
        for (int i = 0; i < selectedEntries.size(); i++){
            int key = selectedEntries.keyAt(i);
            Entry entry = selectedEntries.get(key);
            entry.setVisitedDate((entry.getVisitedDate() == null || entry.getVisitedDate() == 0) ? new Date().getTime() : null);
            DatabaseHandler.getInstance().updateEntry(entry);
        }
    }

    @Override
    public void onLoadComplete(Loader loader, Object data) {
        if (isAdded()) {
            if (data instanceof Cursor) {
                Cursor cursor = (Cursor) data;
                swingBottomInAnimationAdapter.reset();
                myExpandableListItemAdapter.changeCursor(cursor);
            }
            setEmptyText(false);
        }
    }

    public void newsTypeModeChanged(int newsTypeMode) {
        this.newsTypeMode = newsTypeMode;
        if (simpleCursorLoader != null){
            simpleCursorLoader.startLoading();
        }
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
        };
        return NavigationDrawerFragment.HOME;
    }

    public void updateCategory(Category category) {
        this.category = category;
        if (getView() != null) {
            init();
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
            view.setBackgroundResource(mSelectedItemIds.get(position) ? R.color.list_background_selected : R.color.list_background);
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

            setImageDrawable(entryType, entry);

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
                    //String link = entry.getShortenedLink() != null ? entry.getShortenedLink() : entry.getLink();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(entry.getLink()));
                    try{
                        startActivity(browserIntent);
                    } catch (ActivityNotFoundException e){
                        Toast.makeText(mContext, mContext.getString(R.string.no_browser_found), Toast.LENGTH_LONG).show();

                    }
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
            notifyDataSetChanged();
        }

        @Override
        public void deselectAllIds() {
            for(int i = 0; i < mCursor.getCount(); i++){
                onListItemCheck(i, false);
            }

            if (mActionMode != null) {
                mActionMode.setTitle(String.valueOf(myExpandableListItemAdapter.getSelectedCount()));
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

    private void setImageDrawable(ImageView entryType, Entry entry) {
        Drawable drawable = null;
        if (entry.getFavoriteDate() != null && entry.getFavoriteDate() > 0) {
            drawable = getResources().getDrawable(R.drawable.ic_favorite);
        } else if (entry.getVisitedDate() != null && entry.getVisitedDate() > 0) {
            drawable = getResources().getDrawable(R.drawable.ic_recent);
        }
        entryType.setImageDrawable(drawable);
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
            // retrieve selected items and print them out
            SparseBooleanArray selected = myExpandableListItemAdapter.getSelectedIds();
            SparseArray<Entry> selectedEntries = new SparseArray<Entry>();
            for (int i = 0; i < selected.size(); i++) {
                if (selected.valueAt(i)) {
                    Entry selectedEntry = GetEntryByCursor((Cursor) myExpandableListItemAdapter.getItem(selected.keyAt(i)));
                    selectedEntries.put(i, selectedEntry);
                }
            }

            boolean shouldFinish = false;
            // close action mode
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
                    myExpandableListItemAdapter.selectAllIds();
                    shouldFinish = false;
                    break;
                case R.id.menu_item_deselect_all:
                    myExpandableListItemAdapter.deselectAllIds();
                    OpenActionModeIfNecessary();
                    shouldFinish = true;
                    break;
                case R.id.menu_item_delete:
                    deleteSelectedEntries(selectedEntries);
                    shouldFinish = true;
                    break;
            }

            if (shouldFinish) {
                mode.finish();
                myExpandableListItemAdapter.removeSelection();
                simpleCursorLoader.forceLoad();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            myExpandableListItemAdapter.deselectAllIds();
        }
    }
}