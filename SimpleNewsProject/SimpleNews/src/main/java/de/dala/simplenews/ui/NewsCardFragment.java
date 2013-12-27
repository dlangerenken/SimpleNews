package de.dala.simplenews.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import androidrss.MediaEnclosure;
import androidrss.RSSConfig;
import androidrss.RSSFeed;
import androidrss.RSSItem;
import androidrss.RSSParser;
import de.dala.simplenews.R;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.database.IDatabaseHandler;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Entry;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.common.NewsCard;
import de.dala.simplenews.utilities.CategoryUpdater;
import de.dala.simplenews.utilities.MyCardArrayAdapter;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardListView;

/**
 * Created by Daniel on 18.12.13.
 */
public class NewsCardFragment extends SherlockFragment {
    //TODO fragment performance
    private static final String ARG_CATEGORY = "category";
    public static final String IMAGE_JPEG = "image/jpeg";
    private CardListView mListView;
    private View undoBar;
    private MyCardArrayAdapter mCardArrayAdapter;
    private Category category;

    private List<Feed> feeds;
    private MainActivity activity;

    private IDatabaseHandler databaseHandler;
    private static long TIME_FOR_REFRESH = 1000 * 60  * 60; //one hour


    public static NewsCardFragment newInstance(Category category) {
        NewsCardFragment f = new NewsCardFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_CATEGORY, category);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof MainActivity){
            this.activity = (MainActivity) activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        category = (Category) getArguments().getSerializable(ARG_CATEGORY);

        if (savedInstanceState != null){
            Object feedsObject = savedInstanceState.getSerializable("feeds");
            if (feedsObject != null){
                feeds = (ArrayList<Feed>) feedsObject;
            }
        }
        databaseHandler = DatabaseHandler.getInstance(getActivity());

        if (feeds == null){
            feeds = databaseHandler.getFeeds(category.getId(), null);
        }
        category.setFeeds(feeds);
        View rootView = inflater.inflate(R.layout.list_base_different_inner, container, false);
        mListView = (CardListView) rootView.findViewById(R.id.card_list_base);
        undoBar = rootView.findViewById(R.id.list_card_undobar);
        initCardsAdapter(new ArrayList<Card>());

        loadEntries();
        return rootView;
    }

    private void initCardsAdapter(List<Card> cards) {
        // Provide a custom adapter.
        // It is important to set the viewTypeCount
        // You have to provide in your card the type value with {@link Card#setType(int)} method.
        mCardArrayAdapter = new MyCardArrayAdapter(activity, cards);
        //mCardArrayAdapter.setInnerViewTypeCount(3);
        mCardArrayAdapter.setEnableUndo(true, undoBar);

        if (mListView!=null){
            mListView.setAdapter(mCardArrayAdapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                refreshFeeds();
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.news, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void refreshFeeds(){
        CategoryUpdater updater = new CategoryUpdater(new CategoryUpdateHandler(), category, getActivity(), true);
        updater.start();
        activity.showLoadingNews();
    }

    private class CategoryUpdateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CategoryUpdater.RESULT:
                    List<Entry> entries = (ArrayList<Entry>) msg.obj;
                    updateAdapter(entries);
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

    private void updateAdapter(List<Entry> entries) {
        Collections.sort(entries);
        List<Card> cards = new ArrayList<Card>();
        for (Entry entry : entries){
            final Card newsCard = new NewsCard(getActivity(), entry, category);
            newsCard.setId(entry.getTitle());
            cards.add(newsCard);
        }
        initCardsAdapter(cards);
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

}