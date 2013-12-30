package de.dala.simplenews.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.haarman.listviewanimations.ArrayAdapter;
import com.haarman.listviewanimations.itemmanipulation.contextualundo.ContextualUndoAdapter;
import com.haarman.listviewanimations.view.DynamicListView;

import java.util.List;

import colorpicker.ColorPickerDialog;
import colorpicker.ColorUtils;
import colorpicker.OnColorSelectedListener;
import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.utilities.UIUtils;

/**
 * Created by Daniel on 29.12.13.
 */
public class CategoryFeedsFragment extends Fragment implements ContextualUndoAdapter.DeleteItemCallback {


    private ListView feedListView;
    private FeedListAdapter adapter;

    private Category category;

    public CategoryFeedsFragment(Category category) {
        this.category = category;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.feed_selection, container, false);
        feedListView = (ListView) rootView.findViewById(R.id.listView);
        feedListView.setDivider(null);
        initAdapter();
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.feed_selection_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.new_feed:
                createFeedClicked();
                return true;
        }
        return false;
    }

    private void initAdapter() {
        adapter = new FeedListAdapter(getActivity(), category.getFeeds());
        ContextualUndoAdapter undoAdapter = new ContextualUndoAdapter(adapter, R.layout.undo_row, R.id.undo_row_undobutton, 5000, R.id.undo_row_texttv, new MyFormatCountDownCallback());
        undoAdapter.setAbsListView(feedListView);
        undoAdapter.setDeleteItemCallback(this);
        feedListView.setAdapter(undoAdapter);
    }

    @Override
    public void deleteItem(int position) {
        Feed feed = adapter.getItem(position);
        adapter.remove(position);
        adapter.notifyDataSetChanged();

        DatabaseHandler.getInstance().removeFeeds(null, feed.getId(), false);
        category.getFeeds().remove(feed);
    }

    private class MyFormatCountDownCallback implements ContextualUndoAdapter.CountDownFormatter {

        @Override
        public String getCountDownString(long millisUntilFinished) {
            int seconds = (int) Math.ceil((millisUntilFinished / 1000.0));
            if (seconds > 0) {
                return getResources().getQuantityString(R.plurals.countdown_seconds, seconds, seconds);
            }
            return getString(R.string.countdown_dismissing);
        }
    }

    private class FeedListAdapter extends ArrayAdapter<Feed> {

        private Context context;
        private DatabaseHandler database;

        public FeedListAdapter(Context context, List<Feed> feeds) {
            super(feeds);
            this.context = context;
            this.database = DatabaseHandler.getInstance();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            Feed feed = getItem(position);

            if (convertView == null){
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.feed_modify_item, null, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.show = (ImageView) convertView.findViewById(R.id.show);
                viewHolder.edit = (ImageView) convertView.findViewById(R.id.edit);
                convertView.setTag(viewHolder);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.name.setText(feed.getUrl());
            holder.show.setOnClickListener(new FeedItemClickListener(feed));
            holder.edit.setOnClickListener(new FeedItemClickListener(feed));

            return convertView;
        }

        @Override
        public void swapItems(int positionOne, int positionTwo) {
            Feed temp = getItem(positionOne);
            set(positionOne, getItem(positionTwo));
            set(positionTwo, temp);
            database.updateCategoryOrder(getItem(positionOne).getId(), positionOne);
            database.updateCategoryOrder(getItem(positionTwo).getId(), positionTwo);
        }

        class ViewHolder {
            public TextView name;
            public ImageView show;
            public ImageView edit;
        }
    }

    class FeedItemClickListener implements View.OnClickListener {
        private Feed feed;

        public FeedItemClickListener(Feed feed) {
            this.feed = feed;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.edit:
                    editClicked(feed);
                    break;
                case R.id.show:
                    break;
            }
        }
    }

    private void createFeedClicked(){
            final EditText input = new EditText(getActivity());
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            String feedUrl = input.getText().toString(); //TODO validate
                            Feed feed = new Feed();
                            feed.setCategoryId(category.getId());
                            feed.setUrl(feedUrl); //TODO check if valid
                            long id = DatabaseHandler.getInstance().addFeed(category.getId(), feed, true);
                            feed.setId(id);
                            adapter.add(feed);
                            adapter.notifyDataSetChanged();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            };

            new AlertDialog.Builder(getActivity()).
                    setPositiveButton("Ok", dialogClickListener).setNegativeButton("Cancel", dialogClickListener).setTitle(getActivity().getString(R.string.create_category_1_2))
                    .setMessage("New Feed Url:").setView(input).show();
    }

    private void editClicked(final Feed feed) {
        final EditText input = new EditText(getActivity());
        input.setText(feed.getUrl());
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        String newUrl = input.getText().toString(); //TODO validate
                        feed.setUrl(newUrl);
                        adapter.notifyDataSetChanged();
                        DatabaseHandler.getInstance().updateFeedUrl(feed.getId(), newUrl);
                        Toast.makeText(getActivity(), "New url: " + newUrl, Toast.LENGTH_SHORT).show();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        new AlertDialog.Builder(getActivity()).
                setPositiveButton("Rename", dialogClickListener).setNegativeButton("Cancel", dialogClickListener).setTitle("Feed")
                .setMessage("Change the url of the feed").setView(input).show();
    }

}
