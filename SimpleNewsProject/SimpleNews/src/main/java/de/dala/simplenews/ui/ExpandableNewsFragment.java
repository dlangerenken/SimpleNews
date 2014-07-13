package de.dala.simplenews.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import de.dala.simplenews.database.SimpleCursorLoader;
import de.dala.simplenews.network.FadeInNetworkImageView;
import de.dala.simplenews.utilities.CategoryUpdater;
import de.dala.simplenews.utilities.ExpandableListItemCursorAdapter;
import de.dala.simplenews.utilities.PrefUtilities;
import de.dala.simplenews.utilities.UIUtils;

/**
 * Created by Daniel on 18.12.13.
 */
public class ExpandableNewsFragment extends Fragment implements SwipeRefreshLayoutExtended.OnRefreshListener, SimpleCursorLoader.OnLoadCompleteListener {
    public static final int ALL = 0;
    private int entryType = ALL;
    public static final int FAV = 1;
    public static final int RECENT = 2;
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_ENTRY_TYPE = "entryType";
    private MyExpandableListItemAdapter myExpandableListItemAdapter;
    private ActionMode mActionMode;
    private ListView mListView;
    private SwipeRefreshLayoutExtended mSwipeRefreshLayout;
    private AnimationAdapter animCardArrayAdapter;

    private List<Feed> feeds;

    private IDatabaseHandler databaseHandler;

    private Category category;

    private CategoryUpdater pullUpdater;
    private CategoryUpdater autoUpdater;
    private LinearLayout categoryView;
    private View allEntryButton;
    private View favEntryButton;
    private View recentEntryButton;
    private TextView allEntryTextView;
    private TextView favEntryTextView;
    private TextView recentEntryTextView;
    private ScrollClass myScrollClass;

    private Menu menu;
    private INewsInteraction newsInteraction;
    private ShareActionProvider shareActionProvider;

    private SimpleCursorLoader simpleCursorLoader;

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
        this.entryType = getArguments().getInt(ARG_ENTRY_TYPE);
    }


    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (!visible) {
            if (mActionMode != null) {
                mActionMode.finish();
            }
        } else {
            if (myScrollClass != null) {
                myScrollClass.fadeIn();
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSwipeRefreshLayout = (SwipeRefreshLayoutExtended) view.findViewById(R.id.ptr_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeFromColors(category.getColor(), getResources().getColor(R.color.background_window), category.getColor(), getResources().getColor(R.color.background_window));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            Object feedsObject = savedInstanceState.getSerializable("feeds");
            if (feedsObject != null && feedsObject instanceof ArrayList<?>) {
                feeds = (ArrayList<Feed>) feedsObject;
            }
        }
        databaseHandler = DatabaseHandler.getInstance();

        if (feeds == null) {
            feeds = databaseHandler.getFeeds(category.getId(), null);
        }
        category.setFeeds(feeds);

        View rootView = inflater.inflate(R.layout.news_list, container, false);
        categoryView = (LinearLayout) rootView.findViewById(R.id.entry_types);

        initButtons(rootView);

        mListView = (ListView) rootView.findViewById(R.id.news_listview);

        myScrollClass = new ScrollClass() {
            int mLastFirstVisibleItem = 0;
            boolean sliding = false;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (view.getId() == mListView.getId()) {
                    final int currentFirstVisibleItem = mListView.getFirstVisiblePosition();
                    if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                        fadeIn();
                    } else if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                        fadeOut();
                    }
                    mLastFirstVisibleItem = currentFirstVisibleItem;
                }
            }

            @Override
            public void fadeOut() {
                if (!sliding) {
                    final int height = categoryView.getMeasuredHeight();
                    if (categoryView.getVisibility() == View.VISIBLE) {
                        Animation animation = new TranslateAnimation(0, 0, 0,
                                height);
                        animation.setInterpolator(new AccelerateInterpolator(1.0f));
                        animation.setDuration(400);

                        categoryView.startAnimation(animation);
                        animation.setAnimationListener(new Animation.AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation animation) {
                                sliding = true;
                                interrupt();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                sliding = false;
                                categoryView.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }
            }

            @Override
            public void fadeIn() {
                if (!sliding) {
                    final int height = categoryView.getMeasuredHeight();
                    if (categoryView.getVisibility() == View.INVISIBLE) {

                        Animation animation = new TranslateAnimation(0, 0,
                                height, 0);

                        animation.setInterpolator(new AccelerateInterpolator(1.0f));
                        animation.setDuration(400);
                        categoryView.startAnimation(animation);
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                sliding = true;
                                categoryView.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                sliding = false;
                                disappear();
                            }
                        });
                    } else {
                        disappear();
                    }
                }
            }

        };

        mListView.setOnScrollListener(myScrollClass);
        mListView.setOnTouchListener(new View.OnTouchListener() {
            private int mLastMotionY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int y = (int) event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        if (y < mLastMotionY && mListView.getFirstVisiblePosition() == 0) {
                            myScrollClass.fadeIn();
                        }
                        break;
                    case MotionEvent.ACTION_DOWN:
                        mLastMotionY = (int) event.getY();
                        break;
                }
                return false;
            }

        });
        myScrollClass.fadeIn();
        initCardsAdapter();
        loadEntries(true);
        return rootView;
    }

    @Override
    public void onRefresh() {
        if (autoUpdater != null && autoUpdater.isRunning()) {
            mSwipeRefreshLayout.setRefreshing(false);
        } else {
            refreshThroughPull();
        }
    }

    private void initButtons(View rootView) {
        allEntryButton = rootView.findViewById(R.id.entry_type_all);
        favEntryButton = rootView.findViewById(R.id.entry_type_fav);
        recentEntryButton = rootView.findViewById(R.id.entry_type_recently);

        allEntryTextView = (TextView) rootView.findViewById(R.id.entry_type_text_all);
        favEntryTextView = (TextView) rootView.findViewById(R.id.entry_type_text_fav);
        recentEntryTextView = (TextView) rootView.findViewById(R.id.entry_type_text_recently);

        allEntryTextView.setTextColor(UIUtils.getColorTextStateList());
        favEntryTextView.setTextColor(UIUtils.getColorTextStateList());
        recentEntryTextView.setTextColor(UIUtils.getColorTextStateList());

        allEntryButton.setBackgroundDrawable(UIUtils.getStateListDrawableByColor(category.getColor()));
        favEntryButton.setBackgroundDrawable(UIUtils.getStateListDrawableByColor(category.getColor()));
        recentEntryButton.setBackgroundDrawable(UIUtils.getStateListDrawableByColor(category.getColor()));
        switch (entryType) {
            case ALL:
                allEntryButton.setSelected(true);
                allEntryTextView.setSelected(true);
                break;
            case FAV:
                favEntryButton.setSelected(true);
                favEntryTextView.setSelected(true);
                break;
            case RECENT:
                recentEntryButton.setSelected(true);
                recentEntryTextView.setSelected(true);
                break;
        }
        allEntryButton.setOnClickListener(new EntryTypeClickListener(ALL));
        favEntryButton.setOnClickListener(new EntryTypeClickListener(FAV));
        recentEntryButton.setOnClickListener(new EntryTypeClickListener(RECENT));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refreshFeeds();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initCardsAdapter() {
        myExpandableListItemAdapter = new MyExpandableListItemAdapter(getActivity(), null);
        animCardArrayAdapter = new SwingBottomInAnimationAdapter(myExpandableListItemAdapter);
        animCardArrayAdapter.setAbsListView(mListView);
        if (mListView != null) {
            mListView.setAdapter(animCardArrayAdapter);
        }
        simpleCursorLoader = new SimpleCursorLoader(getActivity()) {
            @Override
            public Cursor loadInBackground() {
                switch (entryType) {
                    case ALL:
                        return DatabaseHandler.getInstance().getEntriesCursor(category.getId());
                    case FAV:
                        return DatabaseHandler.getInstance().getFavoriteEntriesCursor(category.getId());
                    case RECENT:
                        return DatabaseHandler.getInstance().getRecentEntriesCursor(category.getId());
                }
                return null;
            }
        };
        simpleCursorLoader.registerListener(0, this);

    }

    private void onListItemCheck(int position) {
        myExpandableListItemAdapter.toggleSelection(position);
        boolean hasCheckedItems = myExpandableListItemAdapter.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null) {
            // there are some selected items, start the actionMode
            mActionMode = ((ActionBarActivity) getActivity()).startSupportActionMode(new ActionModeCallBack());
        } else if (!hasCheckedItems && mActionMode != null) {
            // there no selected items, finish the actionMode
            mActionMode.finish();
        }

        if (mActionMode != null) {
            mActionMode.setTitle(String.valueOf(myExpandableListItemAdapter.getSelectedCount()));
        }
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(createShareIntent());
        }
    }

    private void refreshThroughPull() {
        if (pullUpdater == null) {
            pullUpdater = new CategoryUpdater(new CategoryPullUpdateHandler(), category, true, getActivity());
        }
        if (pullUpdater.start()) {
            setRefreshActionButtonState(true);
        }
    }

    public void refreshFeeds() {
        if (autoUpdater == null) {
            autoUpdater = new CategoryUpdater(new CategoryUpdateHandler(), category, true, getActivity());
        }
        if (autoUpdater.start()) {
            if (newsInteraction != null) {
                newsInteraction.showLoadingNews();
            }
            setRefreshActionButtonState(true);
        } else {
            //already running
        }
    }

    private void updateFinished() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        if (mActionMode != null) {
            mActionMode.finish();
        }
        setRefreshActionButtonState(false);
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
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void loadEntries(boolean withRefresh) {
        long timeForRefresh = PrefUtilities.getInstance().getTimeForRefresh();
        if (withRefresh && category.getLastUpdateTime() < new Date().getTime() - timeForRefresh) {
            refreshFeeds();
        }
        simpleCursorLoader.startLoading();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("feeds", (ArrayList<Feed>) feeds);
    }

    private Intent createShareIntent() {
        // retrieve selected items and print them out
        SparseBooleanArray selected = myExpandableListItemAdapter.getSelectedIds();
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

    @Override
    public void onLoadComplete(Loader loader, Object data) {
        if (data instanceof  Cursor){
            Cursor cursor = (Cursor) data;
            animCardArrayAdapter.reset();
            myExpandableListItemAdapter.changeCursor(cursor);
        }
    }


    public interface INewsInteraction {
        void showLoadingNews();

        void cancelLoadingNews();

        void updateNews(String msg, long id);
    }

    private abstract class ScrollClass implements AbsListView.OnScrollListener {
        Handler mHandler = new Handler();
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                fadeOut();
            }
        };

        abstract void fadeIn();

        public void disappear() {
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, 5 * 1000);
        }

        public void interrupt() {
            mHandler.removeCallbacks(mRunnable);
        }

        abstract void fadeOut();
    }

    private class CategoryPullUpdateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CategoryUpdater.RESULT:
                    loadEntries(false);
                    updateFinished();
                    break;
                case CategoryUpdater.STATUS_CHANGED:
                    break;
                case CategoryUpdater.ERROR:
                    updateFinished();
                    break;
                case CategoryUpdater.CANCEL:
                    updateFinished();
                    break;
            }
        }
    }

    private class CategoryUpdateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CategoryUpdater.RESULT:
                    loadEntries(false);
                    if (newsInteraction != null) {
                        newsInteraction.cancelLoadingNews();
                    }
                    setRefreshActionButtonState(false);
                    break;
                case CategoryUpdater.STATUS_CHANGED:
                    if (newsInteraction != null) {
                        newsInteraction.updateNews((String) msg.obj, category.getId());
                    }
                    break;
                case CategoryUpdater.ERROR:
                    Toast.makeText(getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
                    if (newsInteraction != null) {
                        newsInteraction.cancelLoadingNews();
                    }
                    setRefreshActionButtonState(false);
                    break;
                case CategoryUpdater.CANCEL:
                    updateFinished();
                    break;
            }
        }
    }

    public Entry GetEntryByCursor(Cursor cursor){
        return DatabaseHandler.getEntryByCursor(cursor);
    }

    private class MyExpandableListItemAdapter extends ExpandableListItemCursorAdapter {

        private Context mContext;
        private SparseBooleanArray mSelectedItemIds;

        /**
         * Creates a new ExpandableListItemAdapter with the specified list, or an empty list if
         * items == null.
         */
        private MyExpandableListItemAdapter(Context context, Cursor cursor) {
            super(context, R.layout.expandable_card, R.id.card_title, R.id.expandable_card_content, cursor);
            mContext = context;
            mSelectedItemIds = new SparseBooleanArray();
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
            FadeInNetworkImageView imageView = (FadeInNetworkImageView) layout.findViewById(R.id.card_header_imageView);
            imageView.setVisibility(View.GONE);
            TextView titleTextView = (TextView) layout.findViewById(R.id.title);
            TextView infoTextView = (TextView) layout.findViewById(R.id.info);
            ImageView entryType = (ImageView) layout.findViewById(R.id.image);

            UIUtils.setTextMaybeHtml(titleTextView, entry.getTitle());

            if (entry.getFavoriteDate() != null && entry.getFavoriteDate() > 0) {
                entryType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_nav_fav));
            } else if (entry.getVisitedDate() != null && entry.getVisitedDate() > 0) {
                entryType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_nav_recently_used));
            }

            long current = new Date().getTime();
            if (current > entry.getDate()) {
                current = entry.getDate();
            }
            String prettyTimeString = new PrettyTime().format(new Date(current));
            infoTextView.setText(String.format("%s - %s", entry.getSrcName(), prettyTimeString));
            infoTextView.setTextColor(category.getColor());
            infoTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

            return layout;
        }

        @Override
        public View getContentView(final int position, View convertView, ViewGroup parent) {
            final Entry entry = GetEntryByCursor((Cursor) getItem(position));

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.news_card_expand, null);

            View colorBorder = layout.findViewById(R.id.colorBorder);
            colorBorder.setBackgroundColor(category.getColor());

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

        public SparseBooleanArray getSelectedIds() {
            return mSelectedItemIds;
        }

        public void removeSelection() {
            mSelectedItemIds = new SparseBooleanArray();
            notifyDataSetChanged();
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
            return false;
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
            // close action mode
            switch (item.getItemId()) {
                case R.id.menu_item_save:
                    saveSelectedEntries(selectedEntries);
                    break;
            }
            mode.finish();
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // remove selection
            myExpandableListItemAdapter.removeSelection();
            mActionMode = null;
        }
    }

    private class EntryTypeClickListener implements View.OnClickListener {
        private int type;

        public EntryTypeClickListener(int type) {
            this.type = type;
        }

        @Override
        public void onClick(View v) {
            allEntryButton.setSelected(ALL == type);
            allEntryTextView.setSelected(ALL == type);

            favEntryButton.setSelected(FAV == type);
            favEntryTextView.setSelected(FAV == type);

            recentEntryButton.setSelected(RECENT == type);
            recentEntryTextView.setSelected(RECENT == type);

            entryType = type;
            loadEntries(true);
            if (mActionMode != null) {
                mActionMode.finish();
            }
        }
    }
}