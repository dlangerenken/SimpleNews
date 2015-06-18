package de.dala.simplenews.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.ArrayAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.utilities.BaseNavigation;

/**
 * Created by Daniel on 04.08.2014.
 */
public class OpmlAssignFragment extends BaseFragment implements ViewPager.OnPageChangeListener, BaseNavigation{

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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        Context mContext = getActivity();
        if (mContext != null) {
            return mContext.getString(R.string.opml_assign_fragment_title);
        }
        return "SimpleNews"; //should not be called
    }

    @Override
    public int getNavigationDrawerId() {
        return NavigationDrawerFragment.IMPORT;
    }

    private class OpmlListAdapter extends ArrayAdapter<Feed> {

        private Context context;
        private Map<Feed, Boolean> mSelectedItemIds;

        public OpmlListAdapter(Context context, List<Feed> feeds) {
            super(feeds);
            this.context = context;
            mSelectedItemIds = new HashMap<>();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Feed feed = getItem(position);

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
            holder.checkBox.setChecked(Boolean.TRUE.equals(mSelectedItemIds.get(feed)));
            holder.checkBox.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    onListItemCheck(feed);
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
                    onListItemCheck(feed);
                }
            });
            int pad = getResources().getDimensionPixelSize(R.dimen.card_layout_padding);
            convertView.setPadding(pad, pad, pad, pad);
            return convertView;
        }

        public void toggleSelection(Feed feed) {
            selectView(feed, !Boolean.TRUE.equals(mSelectedItemIds.get(feed)));
        }

        public void selectView(Feed feed, Boolean value) {
            if (Boolean.TRUE.equals(value)) {
                mSelectedItemIds.put(feed, true);
            } else {
                mSelectedItemIds.remove(feed);
            }
            notifyDataSetChanged();
        }

        public int getSelectedCount() {
            return mSelectedItemIds.size();// mSelectedCount;
        }

        public Map<Feed, Boolean> getSelectedIds() {
            return mSelectedItemIds;
        }

        public void removeSelection() {
            mSelectedItemIds = new HashMap<>();
            notifyDataSetChanged();
        }

        class ViewHolder {
            public TextView name;
            public TextView link;
            public CheckBox checkBox;
        }
    }

    private void removeFeed(Feed feed){
        feedsToImport.remove(feed);
        adapter.remove(feed);
        adapter.notifyDataSetChanged();
    }

    private void onListItemCheck(Feed feedUrl) {
        adapter.toggleSelection(feedUrl);
        boolean hasCheckedItems = adapter.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null) {
            // there are some selected items, start the actionMode
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ActionModeCallBack());
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
        final ArrayList<Feed> selectedEntries = new ArrayList<>();
        selectedEntries.add(feed);
        AssignDialogFragment assignFeedsDialog = AssignDialogFragment.newInstance(selectedEntries);
        assignFeedsDialog.setDialogHandler(new AssignDialogFragment.IDialogHandler(){

            @Override
            public void assigned() {
                removeSelectedFeeds(selectedEntries);
                adapter.removeSelection();
            }

            @Override
            public void canceled() {
                adapter.removeSelection();
            }
        });

        assignFeedsDialog.show(getFragmentManager(), "AssignFeedsDialog");
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
            Map<Feed, Boolean> selected = adapter.getSelectedIds();
            final ArrayList<Feed> selectedEntries = new ArrayList<>();
            for (Map.Entry<Feed, Boolean> entry : selected.entrySet()){
                if (Boolean.TRUE.equals(entry.getValue())) {
                    selectedEntries.add(entry.getKey());
                }
            }
            // close action mode
            switch (item.getItemId()) {
                case R.id.menu_item_remove:
                    removeSelectedFeeds(selectedEntries);
                    break;
                case R.id.menu_item_assign:
                    AssignDialogFragment assignFeedsDialog = AssignDialogFragment.newInstance(selectedEntries);
                    assignFeedsDialog.setDialogHandler(new AssignDialogFragment.IDialogHandler(){

                        @Override
                        public void assigned() {
                            removeSelectedFeeds(selectedEntries);
                            adapter.removeSelection();
                        }

                        @Override
                        public void canceled() {
                            adapter.removeSelection();
                        }
                    });
                    assignFeedsDialog.show(getFragmentManager(), "AssignFeedsDialog");
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
        if (feedsToImport.size() == 0){
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.popBackStack();
        }

    }

}
