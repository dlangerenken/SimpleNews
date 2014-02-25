package de.dala.simplenews.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
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

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.ocpsoft.pretty.time.PrettyTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Entry;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.database.IDatabaseHandler;
import de.dala.simplenews.network.FadeInNetworkImageView;
import de.dala.simplenews.utilities.CategoryUpdater;
import de.dala.simplenews.utilities.ExpandableListItemAdapter;
import de.dala.simplenews.utilities.PrefUtilities;
import de.dala.simplenews.utilities.UIUtils;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.AbsDefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by Daniel on 18.12.13.
 */
public class ExpandableNewsFragment extends SherlockFragment implements OnRefreshListener{
    private MyExpandableListItemAdapter myExpandableListItemAdapter;
    public static final int ALL = 0;
    public static final int FAV = 1;
    public static final int RECENT = 2;
    private int entryType = ALL;

    private ActionMode mActionMode;
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_ENTRY_TYPE = "entryType";
    private ListView mListView;
    private PullToRefreshLayout mPullToRefreshLayout;

    private List<Feed> feeds;
    private MainActivity activity;
    private NewsOverViewFragment parentFragment;

    private IDatabaseHandler databaseHandler;

    private Category category;

    private boolean isVisible = false;

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

    private ExpandableNewsFragment(Category category) {
        this.category = category;
    }

    public static ExpandableNewsFragment newInstance(Category category,int entryType){
        ExpandableNewsFragment f = new ExpandableNewsFragment(category);
        Bundle b = new Bundle();
        b.putParcelable(ARG_CATEGORY, category);
        b.putInt(ARG_ENTRY_TYPE, entryType);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.category = getArguments().getParcelable(ARG_CATEGORY);
        this.entryType = getArguments().getInt(ARG_ENTRY_TYPE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof MainActivity){
            this.activity = (MainActivity) activity;
        }
        if (getParentFragment() instanceof NewsOverViewFragment){
            parentFragment = (NewsOverViewFragment) getParentFragment();
        }else{
            throw new ActivityNotFoundException();
        }
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        isVisible = visible;
        if (!visible) {
            if (mActionMode != null){
                mActionMode.finish();
            }
        }else{
            updateNavDrawerItems();
            if (myScrollClass != null){
                myScrollClass.fadeIn();
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);
        // Now setup the PullToRefreshLayout

        MyHeaderTransformer transformer = new MyHeaderTransformer(category);
        ActionBarPullToRefresh.from(getActivity())
                .options(Options.create()
                        .scrollDistance(.40f)
                        .headerTransformer(transformer)
                        .build())
                        // Mark All Children as pullable
                .allChildrenArePullable()
                        // Set the OnRefreshListener
                .listener(this)
                        // Finally commit the setup to our PullToRefreshLayout
                .setup(mPullToRefreshLayout);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null){
            Object feedsObject = savedInstanceState.getSerializable("feeds");
            if (feedsObject != null){
                feeds = (ArrayList<Feed>) feedsObject;
            }
        }
        databaseHandler = DatabaseHandler.getInstance();

        if (feeds == null){
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
            int scrollState;

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
                if (sliding == false) {
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
                if (sliding == false) {
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
                    }else{
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
                        if (y < mLastMotionY && mListView.getFirstVisiblePosition() == 0){
                            myScrollClass.fadeIn();
                        }
                        break;
                    case MotionEvent.ACTION_DOWN:
                        mLastMotionY = (int) event.getY();
                        return true;
                }
                return false;
            }

        });
        myScrollClass.fadeIn();
        initCardsAdapter(new ArrayList<Entry>());

        if (isVisible){
            updateNavDrawerItems();
        }
        loadEntries(true);
        return rootView;
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

        public void disappear(){
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, 5 * 1000);
        }

        public void interrupt(){
            mHandler.removeCallbacks(mRunnable);
        }

        abstract void fadeOut();
    }

    private void initButtons(View rootView) {
        allEntryButton = rootView.findViewById(R.id.entry_type_all);
        favEntryButton =  rootView.findViewById(R.id.entry_type_fav);
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
        switch (entryType){
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

    private void updateNavDrawerItems() {
        if (activity != null && databaseHandler != null && category != null){
            activity.setNavDrawerInformation(databaseHandler.getFavoriteEntries(category.getId()), databaseHandler.getVisitedEntries(category.getId()));
        }
    }

    private void initCardsAdapter(List<Entry> entries) {
        myExpandableListItemAdapter = new MyExpandableListItemAdapter(getActivity(), entries);
        AnimationAdapter animCardArrayAdapter = new SwingBottomInAnimationAdapter(myExpandableListItemAdapter);
        animCardArrayAdapter.setAbsListView(mListView);
        if (mListView != null) {
            mListView.setAdapter(animCardArrayAdapter);
        }
    }

    private void onListItemCheck(int position) {
        myExpandableListItemAdapter.toggleSelection(position);
        boolean hasCheckedItems = myExpandableListItemAdapter.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null){
            // there are some selected items, start the actionMode
            mActionMode = getSherlockActivity().startActionMode(new ActionModeCallBack());
        }
        else if (!hasCheckedItems && mActionMode != null){
            // there no selected items, finish the actionMode
            mActionMode.finish();
        }

        if(mActionMode != null){
            mActionMode.setTitle(String.valueOf(myExpandableListItemAdapter.getSelectedCount()));
        }
        if (shareActionProvider != null){
            shareActionProvider.setShareIntent(createShareIntent());
        }
    }


    private void refreshThroughPull(){
        if (pullUpdater == null){
            pullUpdater = new CategoryUpdater(new CategoryPullUpdateHandler(), category, getActivity(), true);
        }
        pullUpdater.start();
    }
    private void refreshFeeds(){
        if (autoUpdater == null){
            autoUpdater = new CategoryUpdater(new CategoryUpdateHandler(), category, getActivity(), true);
        }
        if (autoUpdater.start()){
            parentFragment.showLoadingNews();
        }else{
            //already running
        }
    }

    @Override
    public void onRefreshStarted(View view) {
        refreshThroughPull();
    }

    private class CategoryPullUpdateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CategoryUpdater.RESULT:
                    loadEntries(false);
                    updateFinished();
                    break;
                case CategoryUpdater.STATUS_CHANGED:
                    break;
                case CategoryUpdater.ERROR:
                    updateFinished();
                    break;
            }
        }
    }

    private class CategoryUpdateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CategoryUpdater.RESULT:
                    loadEntries(false);
                    parentFragment.cancelLoadingNews();
                    break;
                case CategoryUpdater.STATUS_CHANGED:
                    parentFragment.updateNews((String)msg.obj, category.getId());
                    break;
                case CategoryUpdater.ERROR:
                    Toast.makeText(getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
                    parentFragment.cancelLoadingNews();
                    updateAdapter(new ArrayList<Entry>());
                    break;
            }
        }
    }

    private void updateFinished() {
        if (mPullToRefreshLayout != null){
            mPullToRefreshLayout.setRefreshComplete();
        }
        if (mActionMode != null){
            mActionMode.finish();
        }
    }

    private void updateAdapter(List<Entry> entries) {
        Collections.sort(entries, Collections.reverseOrder());
        initCardsAdapter(entries);
        updateNavDrawerItems();
    }

    private void loadEntries(boolean withRefresh) {
        switch (entryType){
            case ALL:
                List<Feed> feeds = databaseHandler.getFeeds(category.getId(), false);
                List<Entry> entries = new ArrayList<Entry>();
                if (feeds != null){
                    for (Feed feed : feeds){
                        entries.addAll(feed.getEntries());
                    }
                }
                updateAdapter(entries);
                long timeForRefresh = PrefUtilities.getInstance().getTimeForRefresh();
                if (withRefresh && category.getLastUpdateTime() < new Date().getTime() - timeForRefresh){
                    refreshFeeds();
                }
                break;
            case FAV:
                updateAdapter(databaseHandler.getFavoriteEntries(category.getId()));
                break;
            case RECENT:
                updateAdapter(databaseHandler.getVisitedEntries(category.getId()));
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("feeds", (ArrayList<Feed>) feeds);
    }

    private class MyExpandableListItemAdapter extends ExpandableListItemAdapter<Entry> {

        private Context mContext;
        private SparseBooleanArray mSelectedItemIds;

        /**
         * Creates a new ExpandableListItemAdapter with the specified list, or an empty list if
         * items == null.
         */
        private MyExpandableListItemAdapter(Context context, List<Entry> items) {
            super(context, R.layout.expandable_card, R.id.card_title, R.id.expandable_card_content, items);
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
            view.setPadding(pad,pad,pad,pad);
            return view;
        }

        @Override
        public View getTitleView(final int position, View convertView, ViewGroup parent) {
            Entry entry = getItem(position);
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.news_card, null);
            FadeInNetworkImageView imageView = (FadeInNetworkImageView) layout.findViewById(R.id.card_header_imageView);
            imageView.setVisibility(View.GONE);
            TextView titleTextView = (TextView) layout.findViewById(R.id.title);
            TextView infoTextView = (TextView) layout.findViewById(R.id.info);
            ImageView entryType = (ImageView) layout.findViewById(R.id.image);

            UIUtils.setTextMaybeHtml(titleTextView, entry.getTitle());

            if (entry.getFavoriteDate() != null && entry.getFavoriteDate() > 0){
                entryType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_nav_fav));
            }else if (entry.getVisitedDate() != null && entry.getVisitedDate() > 0){
                entryType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_nav_recently_used));
            }

            long current = new Date().getTime();
            if (current > entry.getDate()){
                current = entry.getDate();
            }
            String prettyTimeString = new PrettyTime().format(new Date(current));
            infoTextView.setText(String.format("%s - %s",entry.getSrcName(), prettyTimeString));
            infoTextView.setTextColor(category.getColor());
            infoTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

            return layout;
        }

        @Override
        public View getContentView(final int position, View convertView, ViewGroup parent) {
            final Entry entry = getItem(position);
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
                    updateNavDrawerItems();
                    myExpandableListItemAdapter.notifyDataSetChanged();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(entry.getLink()));
                    startActivity(browserIntent);
                }
            });
            return layout;
        }

        public void toggleSelection(int position) {
            selectView(position, !mSelectedItemIds.get(position));
        }

        public void selectView(int position, boolean value)
        {
            if(value){
                mSelectedItemIds.put(position, value);
            }else{
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
    private class MyHeaderTransformer extends AbsDefaultHeaderTransformer {
        private Category category;
        private TextView mHeaderTextView;
        private SmoothProgressBar mHeaderProgressBar;


        public MyHeaderTransformer(Category category) {
            super();
            this.category = category;
        }

        @Override
        public void onViewCreated(Activity activity, View headerView) {
            super.onViewCreated(activity, headerView);
            mHeaderTextView = (TextView) headerView.findViewById(R.id.ptr_text);
            mHeaderTextView.setBackgroundColor(category.getColor());
            mHeaderProgressBar = (SmoothProgressBar) headerView.findViewById(R.id.ptr_progress);
            mHeaderProgressBar.setBackgroundColor(category.getColor());
            mHeaderProgressBar.setPadding(0,0,0,5);
            setProgressBarColor(Color.WHITE);
        }
    }

    private ShareActionProvider shareActionProvider;

    private Intent createShareIntent() {
        // retrieve selected items and print them out
        SparseBooleanArray selected = myExpandableListItemAdapter.getSelectedIds();
        ArrayList<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < selected.size(); i++){
            if (selected.valueAt(i)) {
                Entry selectedItem = myExpandableListItemAdapter.getItem(selected.keyAt(i));
                entries.add(selectedItem);
            }
        }

        StringBuilder message = new StringBuilder();
        message.append(TextUtils.join("\n", entries));
        message.append(" - by SimpleNews");
        String finalMessage = message.toString();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                finalMessage);
        return shareIntent;
    }

    private class ActionModeCallBack implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.contextual_list_view, menu);
            MenuItem item = menu.findItem(R.id.menu_item_share);

            shareActionProvider = (ShareActionProvider)item.getActionProvider();
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
            for (int i = 0; i < selected.size(); i++){
                if (selected.valueAt(i)) {
                    Entry selectedItem = myExpandableListItemAdapter.getItem(selected.keyAt(i));
                    selectedEntries.add(selectedItem);
                }
            }
            // close action mode
            switch (item.getItemId()){
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

    private void saveSelectedEntries(List<Entry> selectedEntries) {
        for(Entry entry : selectedEntries){
            entry.setFavoriteDate((entry.getFavoriteDate() == null || entry.getFavoriteDate() == 0) ? new Date().getTime() : null);
            DatabaseHandler.getInstance().updateEntry(entry);
        }
        updateNavDrawerItems();
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
            if (mActionMode != null){
                mActionMode.finish();
            }
        }
    }
}