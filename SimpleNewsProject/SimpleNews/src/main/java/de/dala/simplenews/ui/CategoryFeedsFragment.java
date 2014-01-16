package de.dala.simplenews.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.haarman.listviewanimations.ArrayAdapter;
import com.haarman.listviewanimations.itemmanipulation.contextualundo.ContextualUndoAdapter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import androidrss.RSSConfig;
import androidrss.RSSFault;
import androidrss.RSSFeed;
import androidrss.RSSParser;
import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.network.NetworkCommunication;
import de.dala.simplenews.utilities.UIUtils;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by Daniel on 29.12.13.
 */
public class CategoryFeedsFragment extends Fragment implements ContextualUndoAdapter.DeleteItemCallback {


    private ListView feedListView;
    private FeedListAdapter adapter;
    private ContextualUndoAdapter undoAdapter;
    private static final String CATEGORY_KEY = "category";
    private Category category;

    public CategoryFeedsFragment(){
    }

    public static CategoryFeedsFragment newInstance(Category category){
        CategoryFeedsFragment fragment = new CategoryFeedsFragment();
        Bundle b = new Bundle();
        b.putParcelable(CATEGORY_KEY, category);
        fragment.setArguments(b);
        return fragment;
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
        this.category = getArguments().getParcelable(CATEGORY_KEY);
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
        //buggy because of handler
        undoAdapter = new ContextualUndoAdapter(adapter, R.layout.undo_row, R.id.undo_row_undobutton, 5000, R.id.undo_row_texttv, new MyFormatCountDownCallback());
        //undoAdapter = new ContextualUndoAdapter(adapter, R.layout.undo_row, R.id.undo_row_undobutton);
        undoAdapter.setAbsListView(feedListView);
        undoAdapter.setDeleteItemCallback(this);
        feedListView.setAdapter(undoAdapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
            if (getActivity() == null)
            {
                return "";
            }
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
        final View view =  LayoutInflater.from(getActivity()).inflate(R.layout.check_valid_rss_dialog, null);
        final ViewGroup inputLayout = (ViewGroup) view.findViewById(R.id.inputLayout);
        final View progress = view.findViewById(R.id.m_progress);
        final Button positive = (Button) inputLayout.findViewById(R.id.positive);
        final Button negative = (Button) inputLayout.findViewById(R.id.negative);
        final EditText input = (EditText) inputLayout.findViewById(R.id.input);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(view).setTitle(R.string.new_feed_url_header).create();

        View.OnClickListener dialogClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.positive:
                        String feedUrl = input.getText().toString().toLowerCase();
                        if (!feedUrl.startsWith("http://")){
                            feedUrl = "http://" + feedUrl;
                        }
                        final String formattedFeedUrl = feedUrl;

                        if (UIUtils.isValideUrl(feedUrl)){
                            crossfade(progress, inputLayout);

                            NetworkCommunication.loadRSSFeed(feedUrl, new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String result) {
                                            try {
                                                RSSFeed rssFeed = new RSSParser(new RSSConfig()).parse(new ByteArrayInputStream(result.getBytes("UTF-8")));
                                                if (rssFeed.getItems() == null || rssFeed.getItems().isEmpty()){
                                                    invalidFeedUrl(true);
                                                }else{
                                                    Feed feed = new Feed();
                                                    feed.setCategoryId(category.getId());
                                                    feed.setUrl(formattedFeedUrl); //TODO check if valid
                                                    long id = DatabaseHandler.getInstance().addFeed(category.getId(), feed, true);
                                                    feed.setId(id);
                                                    adapter.add(feed);
                                                    adapter.notifyDataSetChanged();
                                                    dialog.dismiss();
                                                }
                                            } catch (RSSFault ex){
                                                ex.printStackTrace();
                                                invalidFeedUrl(true);
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                                invalidFeedUrl(true);
                                            }
                                        }


                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError volleyError) {
                                            invalidFeedUrl(true);
                                        }
                                    });
                        }else{
                            invalidFeedUrl(false);
                        }
                        break;
                    case R.id.negative:
                        dialog.dismiss();
                        break;
                }
            }
            private void invalidFeedUrl(final boolean hideProgressBar) {
                Animation shake = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.shake);
                view.startAnimation(shake);
                shake.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (hideProgressBar){
                            crossfade(inputLayout, progress);
                            Crouton.makeText(getActivity(),getActivity().getString(R.string.not_valid_format), Style.ALERT, inputLayout).show();
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
            }
        };

        positive.setOnClickListener(dialogClickListener);
        negative.setOnClickListener(dialogClickListener);

        dialog.show();
    }


    private void crossfade(final View firstView, final View secondView) {
        int mShortAnimationDuration = getActivity().getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        ViewPropertyAnimator.animate(firstView).alpha(0f);
        firstView.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        ViewPropertyAnimator.animate(firstView).alpha(1f).setDuration(mShortAnimationDuration).setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        ViewPropertyAnimator.animate(secondView).alpha(0f).setDuration(mShortAnimationDuration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                secondView.setVisibility(View.GONE);
            }
        });

    }

    private void editClicked(final Feed feed) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.check_valid_rss_dialog, null);
        final ViewGroup inputLayout = (ViewGroup) view.findViewById(R.id.inputLayout);
        final View progress = view.findViewById(R.id.m_progress);
        final Button positive = (Button) inputLayout.findViewById(R.id.positive);
        final Button negative = (Button) inputLayout.findViewById(R.id.negative);
        final EditText input = (EditText) inputLayout.findViewById(R.id.input);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(view).setTitle(R.string.rename_feed).create();

        input.setText(feed.getUrl());
        View.OnClickListener dialogClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.positive:

                        String feedUrl = input.getText().toString().toLowerCase();
                        if (!feedUrl.startsWith("http://")){
                            feedUrl = "http://" + feedUrl;
                        }
                        final String formattedFeedUrl = feedUrl;

                        if (UIUtils.isValideUrl(feedUrl)){
                            crossfade(progress, inputLayout);

                            NetworkCommunication.loadRSSFeed(formattedFeedUrl, new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String result) {
                                            try {
                                                RSSFeed rssFeed = new RSSParser(new RSSConfig()).parse(new ByteArrayInputStream(result.getBytes("UTF-8")));
                                                if (rssFeed.getItems() == null || rssFeed.getItems().isEmpty()){
                                                    invalidFeedUrl(true);
                                                }else{
                                                    feed.setUrl(formattedFeedUrl);
                                                    adapter.notifyDataSetChanged();
                                                    DatabaseHandler.getInstance().updateFeedUrl(feed.getId(), formattedFeedUrl);
                                                    dialog.dismiss();
                                                }
                                            } catch (RSSFault ex){
                                                ex.printStackTrace();
                                                invalidFeedUrl(true);
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                                invalidFeedUrl(true);
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError volleyError) {
                                            invalidFeedUrl(true);
                                        }
                                    });
                        }else{
                            invalidFeedUrl(false);
                        }
                        break;
                    case R.id.negative:
                        dialog.dismiss();
                        break;
                }
            }

            private void invalidFeedUrl(final boolean hideProgressBar) {
                Animation shake = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.shake);
                view.startAnimation(shake);
                shake.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (hideProgressBar){
                            crossfade(inputLayout, progress);
                            Crouton.makeText(getActivity(),getActivity().getString(R.string.not_valid_format), Style.ALERT, inputLayout).show();
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
            }
        };

        positive.setOnClickListener(dialogClickListener);
        negative.setOnClickListener(dialogClickListener);
        dialog.show();
    }

}
