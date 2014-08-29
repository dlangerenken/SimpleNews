package de.dala.simplenews.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo.ContextualUndoAdapter;

import java.util.ArrayList;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.utilities.BaseNavigation;

/**
 * Created by Daniel on 04.08.2014.
 */
public class OpmlAssignFragment extends BaseFragment implements ContextualUndoAdapter.DeleteItemCallback, ViewPager.OnPageChangeListener, BaseNavigation{

    private OpmlListAdapter adapter;
    private static final String FEED_LIST_KEY = "feeds";
    private ListView feedListView;
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
        initAdapter();
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(getActivity().getString(R.string.assigning_feeds_title));
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        return rootView;
    }

    private void initAdapter() {
        adapter = new OpmlListAdapter(getActivity(), feedsToImport);
        feedListView.setAdapter(adapter);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public String getTitle() {
        return "OpmlAssignFragment";
    }

    @Override
    public int getNavigationDrawerId() {
        return NavigationDrawerFragment.IMPORT;
    }


    private class OpmlListAdapter extends ArrayAdapter<Feed> {

        private Context context;
        private SparseBooleanArray mSelectedItemIds;

        public OpmlListAdapter(Context context, List<Feed> feeds) {
            super(feeds);
            this.context = context;
            mSelectedItemIds = new SparseBooleanArray();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Feed feed = getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.opml_list_item, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.title);
                viewHolder.link = (TextView) convertView.findViewById(R.id.url);
                viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
                convertView.setTag(viewHolder);
            }
            final ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.name.setText(feed.getTitle() == null ? context.getString(R.string.feed_title_not_found) : feed.getTitle());
            holder.link.setText(feed.getXmlUrl());

            holder.checkBox.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    onListItemCheck(position);
                }
            });

            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    holder.checkBox.toggle();
                    onListItemClicked(position);
                    return false;
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    holder.checkBox.toggle();
                    onListItemCheck(position);
                }
            });
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
            public TextView name;
            public TextView link;
            public CheckBox checkBox;
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
        }

        if (!hasCheckedItems && mActionMode != null){
            mActionMode.finish();
        }

        if (mActionMode != null) {
            mActionMode.setTitle(String.valueOf(adapter.getSelectedCount()));
        }
    }

    private void onListItemClicked(int position) {
        Feed feed = adapter.getItem(position);
        final ArrayList<Feed> selectedEntries = new ArrayList<Feed>();
        selectedEntries.add(feed);
        DialogFragment assignFeedsDialog = AssignDialogFragment.newInstance(selectedEntries);
        assignFeedsDialog.show(getFragmentManager(), "AssignFeedsDialog");
        /*assignFeedsDialog.onDismiss(new DialogInterface() {
            @Override
            public void cancel() {
                // nothing
            }

            @Override
            public void dismiss() {
                removeSelectedFeeds(selectedEntries);
            }
        });*/
    }

    private class ActionModeCallBack implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            changeOverflowIcon();
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.contextual_opml_import_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public void changeOverflowIcon() {
            getActivity().getTheme().applyStyle(R.style.ChangeOverflowToDark, true);
        }
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // retrieve selected items and print them out
            SparseBooleanArray selected = adapter.getSelectedIds();
            final ArrayList<Feed> selectedEntries = new ArrayList<Feed>();
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
                    DialogFragment assignFeedsDialog = AssignDialogFragment.newInstance(selectedEntries);
                    assignFeedsDialog.show(getFragmentManager(), "AssignFeedsDialog");
        /*assignFeedsDialog.onDismiss(new DialogInterface() {
            @Override
            public void cancel() {
                // nothing
            }

            @Override
            public void dismiss() {
                removeSelectedFeeds(selectedEntries);
            }
        });*/
                    break;
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


    private void removeSelectedFeeds(List<Feed> selectedFeeds) {
        for (Feed feed : selectedFeeds) {
            removeFeed(feed);
        }
        adapter.removeSelection();
    }

}
