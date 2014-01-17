package de.dala.simplenews.ui;

import android.app.Activity;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.haarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.ocpsoft.pretty.time.PrettyTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Entry;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.database.IDatabaseHandler;
import de.dala.simplenews.network.FadeInNetworkImageView;
import de.dala.simplenews.utilities.CategoryUpdater;
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
public class ExpandableNewsFragment extends SherlockFragment implements OnRefreshListener {
    private MyExpandableListItemAdapter myExpandableListItemAdapter;
    private ActionMode mActionMode;
    private static final String ARG_CATEGORY = "category";
    private ListView mListView;
    PullToRefreshLayout mPullToRefreshLayout;
    private ShareActionProvider myShareActionProvider;

    private List<Feed> feeds;
    private MainActivity activity;

    private IDatabaseHandler databaseHandler;
    private static long TIME_FOR_REFRESH = 1000 * 60  * 60; //one hour

    private Category category;

    public ExpandableNewsFragment(){
    }

    private ExpandableNewsFragment(Category category) {
        this.category = category;
    }

    public static ExpandableNewsFragment newInstance(Category category) {
        ExpandableNewsFragment f = new ExpandableNewsFragment(category);
        Bundle b = new Bundle();
        b.putParcelable(ARG_CATEGORY, category);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.category = getArguments().getParcelable(ARG_CATEGORY);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof MainActivity){
            this.activity = (MainActivity) activity;
        }
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (!visible) {
            if (mActionMode != null){
                mActionMode.finish();
            }
        }
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
        mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.ptr_layout);
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
        mListView = (ListView) rootView.findViewById(R.id.news_listview);
        initCardsAdapter(new ArrayList<Entry>());

        loadEntries();
        return rootView;
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

        if(mActionMode != null)
            mActionMode.setTitle(String.valueOf(myExpandableListItemAdapter.getSelectedCount()) + " selected");
    }


    private void refreshThroughPull(){
        CategoryUpdater updater = new CategoryUpdater(new CategoryPullUpdateHandler(), category, getActivity(), true);
        updater.start();
    }
    private void refreshFeeds(){
        CategoryUpdater updater = new CategoryUpdater(new CategoryUpdateHandler(), category, getActivity(), true);
        updater.start();
        activity.showLoadingNews();
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
                    List<Entry> entries = (ArrayList<Entry>) msg.obj;
                    updateFinished(entries);
                    break;
                case CategoryUpdater.STATUS_CHANGED:
                    break;
                case CategoryUpdater.ERROR:
                    break;
            }
        }
    }

    private class CategoryUpdateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CategoryUpdater.RESULT:
                    List<Entry> entries = (ArrayList<Entry>) msg.obj;
                    updateFinished(entries);
                    activity.cancelLoadingNews();
                    break;
                case CategoryUpdater.STATUS_CHANGED:
                    activity.updateNews((String)msg.obj, category.getId());
                    break;
                case CategoryUpdater.ERROR:
                    Toast.makeText(getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
                    activity.cancelLoadingNews();
                    break;
            }
        }
    }

    private void updateFinished(List<Entry> entries) {
        updateAdapter(entries);
        if (mPullToRefreshLayout != null){
            mPullToRefreshLayout.setRefreshComplete();
        }
    }

    private void updateAdapter(List<Entry> entries) {
        Collections.sort(entries);
        initCardsAdapter(entries);
    }

    private void loadEntries() {
        if (category.getLastUpdateTime() < new Date().getTime() - TIME_FOR_REFRESH){
            refreshFeeds();
        }else{
            List<Entry> entries = databaseHandler.getEntries(category.getId(), null);
            updateAdapter(entries);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("feeds", (ArrayList<Feed>) feeds);
    }


    private class MyExpandableListItemAdapter extends de.dala.simplenews.utilities.ExpandableListItemAdapter<Entry> {

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

            UIUtils.setTextMaybeHtml(titleTextView, entry.getTitle());


            String prettyTimeString = new PrettyTime().format(new Date(entry.getDate()));
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

    private class ActionModeCallBack implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.contextual_list_view, menu);
            MenuItem item = menu.findItem(R.id.menu_item_share);
            myShareActionProvider = (ShareActionProvider)item.getActionProvider();
            myShareActionProvider.setShareHistoryFileName(
                    ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
            myShareActionProvider.setShareIntent(createShareIntent());
            myShareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
                @Override
                public boolean onShareTargetSelected(ShareActionProvider shareActionProvider, Intent intent) {
                    myShareActionProvider.setShareIntent(createShareIntent());
                    return false;
                }
            });
            return true;
        }

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
            message.append("\nby SimpleNews");
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    message.toString().trim());
            return shareIntent;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // retrieve selected items and print them out
            SparseBooleanArray selected = myExpandableListItemAdapter.getSelectedIds();
            StringBuilder message = new StringBuilder();
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
        //TODO ask if download or only store
        //with ladebalken mit option "in background" :))
    }
}