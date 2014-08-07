package de.dala.simplenews.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo.ContextualUndoAdapter;

import java.util.ArrayList;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.database.IDatabaseHandler;

/**
 * Created by Daniel on 04.08.2014.
 */
public class OpmlAssignFragment extends Fragment implements ContextualUndoAdapter.DeleteItemCallback{

    private IDatabaseHandler databaseHandler;
    private OpmlListAdapter adapter;
    private static final String FEED_LIST_KEY = "feeds";
    private ContextualUndoAdapter undoAdapter;
    private ListView feedListView;
    private List<Category> categories;
    private List<Feed> feedsToImport;
    private ActionMode mActionMode;

    public OpmlAssignFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feedsToImport = (List<Feed>) getArguments().getSerializable(FEED_LIST_KEY);
    }

    public static Fragment newInstance(List<Feed> feeds) {
        OpmlAssignFragment fragment = new OpmlAssignFragment();
        Bundle b = new Bundle();
        b.putSerializable(FEED_LIST_KEY, new ArrayList<Feed>(feeds));
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.opml_list_view, container, false);
        feedListView = (ListView) rootView.findViewById(R.id.listView);
        feedListView.setDivider(null);

        databaseHandler = DatabaseHandler.getInstance();
        categories = databaseHandler.getCategories(null, null, null);
        initAdapter();
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle("Assigning Feeds");
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return rootView;
    }

    private void initAdapter() {
        adapter = new OpmlListAdapter(getActivity(), feedsToImport);
        //buggy because of handler
        undoAdapter = new ContextualUndoAdapter(adapter, R.layout.undo_row, R.id.undo_row_undobutton, 3000, R.id.undo_row_texttv, this, new MyFormatCountDownCallback());
        undoAdapter.setAbsListView(feedListView);
        feedListView.setAdapter(undoAdapter);
    }



    private class OpmlListAdapter extends ArrayAdapter<Feed> {

        private Context context;
        private IDatabaseHandler database;
        private SparseBooleanArray mSelectedItemIds;

        public OpmlListAdapter(Context context, List<Feed> feeds) {
            super(feeds);
            this.context = context;
            this.database = DatabaseHandler.getInstance();
            mSelectedItemIds = new SparseBooleanArray();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getId();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Feed feed = getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.opml_list_item, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.category = (ImageView) convertView.findViewById(R.id.category);
                viewHolder.name = (TextView) convertView.findViewById(R.id.title);
                viewHolder.link = (TextView) convertView.findViewById(R.id.url);
                convertView.setTag(viewHolder);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.name.setText(feed.getTitle() == null ? context.getString(R.string.feed_title_not_found) : feed.getTitle());
            holder.link.setText(feed.getXmlUrl());

            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onListItemCheck(position);
                    return false;
                }
            });
            convertView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    onListItemCheck(position);
                }
            });
            convertView.setBackgroundResource(mSelectedItemIds.get(position) ? R.drawable.card_background_blue : R.drawable.card_background_white);
            int pad = getResources().getDimensionPixelSize(R.dimen.card_layout_padding);
            convertView.setPadding(pad, pad, pad, pad);
            return convertView;
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

        class ViewHolder {
            public ImageView category;
            public TextView name;
            public TextView link;
        }
    }

    private class MyFormatCountDownCallback implements ContextualUndoAdapter.CountDownFormatter {

        @Override
        public String getCountDownString(long millisUntilFinished) {
            if (getActivity() == null) {
                return "";
            }
            int seconds = (int) Math.ceil((millisUntilFinished / 1000.0));
            if (seconds > 0) {
                return getResources().getQuantityString(R.plurals.countdown_seconds, seconds, seconds);
            }
            return getString(R.string.countdown_dismissing);
        }
    }

    public void deleteItem(int position) {
        Feed feed = adapter.getItem(position);
        removeFeed(feed);
    }

    private void removeFeed(Feed feed){
        feedsToImport.remove(feed);
        adapter.remove(feed);
        adapter.notifyDataSetChanged();
    }

    private void onListItemCheck(int position) {
        adapter.toggleSelection(position);
        boolean hasCheckedItems = adapter.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null) {
            // there are some selected items, start the actionMode
            mActionMode = ((ActionBarActivity) getActivity()).startSupportActionMode(new ActionModeCallBack());
        } else if (!hasCheckedItems && mActionMode != null) {
            // there no selected items, finish the actionMode
            mActionMode.finish();
        }

        if (mActionMode != null) {
            mActionMode.setTitle(String.valueOf(adapter.getSelectedCount()));
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
            // retrieve selected items and print them out
            SparseBooleanArray selected = adapter.getSelectedIds();
            List<Feed> selectedEntries = new ArrayList<Feed>();
            for (int i = 0; i < selected.size(); i++) {
                if (selected.valueAt(i)) {
                    Feed selectedItem = adapter.getItem(selected.keyAt(i));
                    selectedEntries.add(selectedItem);
                }
            }
            // close action mode
            switch (item.getItemId()) {
                case R.id.menu_item_remove:
                    removeSelectedFeeds(selectedEntries);
                    break;
                case R.id.menu_item_assign:
                    assignSelectedEntries(selectedEntries);
            }
            mode.finish();
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // remove selection
            adapter.removeSelection();
            mActionMode = null;
        }
    }

    private void assignSelectedEntries(List<Feed> selectedEntries) {
        /*PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.actions, popup.getMenu());
        popup.show();

        importFeeds(selectedEntries);
        for(Feed feed : selectedEntries){
            adapter.remove(feed); //TODO animation
            feedsToImport.remove(feed);
        }
        adapter.notifyDataSetChanged();
        */
    }

    private void removeSelectedFeeds(List<Feed> selectedFeeds) {
        for (Feed feed : selectedFeeds) {
            removeFeed(feed);
        }
    }

    private void importFeeds(List<Feed> feeds){
        for(Feed feed : feeds){
            if (feed.getCategoryId() > 0){
                databaseHandler.addFeed(feed.getCategoryId(), feed, true);
            }
        }
    }
}
