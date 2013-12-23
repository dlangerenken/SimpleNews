package de.dala.simplenews;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.ocpsoft.pretty.time.PrettyTime;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import androidrss.MediaEnclosure;
import androidrss.RSSConfig;
import androidrss.RSSFeed;
import androidrss.RSSItem;
import androidrss.RSSParser;
import de.dala.simplenews.database.IDatabaseHandler;
import de.dala.simplenews.network.AnimatedNetworkImageView;
import de.dala.simplenews.network.NetworkCommunication;
import de.dala.simplenews.network.VolleySingleton;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardListView;

/**
 * Created by Daniel on 18.12.13.
 */
public class NewsCardFragment extends Fragment {
    //TODO fragment performance
    private static final String ARG_POSITION = "position";
    private static final String ARG_CATEGORY = "category";
    public static final String IMAGE_JPEG = "image/jpeg";
    private CardListView mListView;
    private View undobar;
    private MyCardArrayAdapter mCardArrayAdapter;
    private int position;
    private Category category;

    private List<Feed> feeds;
    private MainActivity activity;

    private IDatabaseHandler databaseHandler;
    private static long TIME_FOR_REFRESH = 1000 * 60  * 60; //one hour

    public static NewsCardFragment newInstance(int position, Category category) {
        NewsCardFragment f = new NewsCardFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_CATEGORY, category);
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
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
        position = getArguments().getInt(ARG_POSITION);
        category = (Category) getArguments().getSerializable(ARG_CATEGORY);
        databaseHandler = ((MainActivity)getActivity()).getDatabaseHandler();
        feeds = databaseHandler.getFeeds(category.getId());

        View rootView = inflater.inflate(R.layout.list_base_different_inner, container, false);
        mListView = (CardListView) rootView.findViewById(R.id.card_list_base);
        undobar = rootView.findViewById(R.id.list_card_undobar);
        initCardsAdapter();

        loadEntries();
        return rootView;
    }

    private void initCardsAdapter() {
        // Provide a custom adapter.
        // It is important to set the viewTypeCount
        // You have to provide in your card the type value with {@link Card#setType(int)} method.
        mCardArrayAdapter = new MyCardArrayAdapter(getActivity(),new ArrayList<Card>());
        //mCardArrayAdapter.setInnerViewTypeCount(3);
        mCardArrayAdapter.setEnableUndo(true, undobar);
        // An alternative is to write a own CardArrayAdapter
        // MyCardArrayAdapter mCardArrayAdapter = new MyCardArrayAdapter(getActivity(),cards);

        if (mListView!=null){
            mListView.setAdapter(mCardArrayAdapter);
        }
    }

    private void loadEntries() {
        for(Feed feed : feeds){
            if (feed.getLastUpdateTime() < new Date().getTime() - TIME_FOR_REFRESH){
                updateFeed(feed);
            }else{
                List<Entry> entries = databaseHandler.getEntries(feed.getId());
                for (Entry entry : entries){
                    addEntryToCardAndDatabase(entry, false);
                }
            }
        }
    }

    private void updateFeed(final Feed feed) {
        if (activity != null){
            activity.showLoadingNews();
        }
        NetworkCommunication.loadRSSFeed(feed.getUrl(),new Response.Listener<String>() {
            @Override
            public void onResponse(String feedStringResult) {
                feed.setLastUpdateTime(new Date().getTime()); //TODO update database
                RSSParser parser = new RSSParser(new RSSConfig());
                try {
                    RSSFeed rssFeed = parser.parse(new ByteArrayInputStream(feedStringResult.getBytes("UTF-8")));
                    String title = rssFeed.getTitle();
                    if (rssFeed.getItems() != null){
                        for (RSSItem item : rssFeed.getItems()){
                            addRSSItemToEntry(item, feed.getId(), title);
                        }
                    }
                }catch (UnsupportedEncodingException ex){}
                finally {
                    activity.cancelLoadingNews();
                }
            }
        }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    activity.cancelLoadingNews();
                }
            });
    }


    /**
     * Created by Daniel on 18.12.13.
     */
    public class NewsCard extends Card{
        protected String mTitleHeader;
        protected String mTitleMain;
        protected Entry entry;

        public NewsCard(Context context, Entry entry) {
            super(context, R.layout.news_card);
            this.mTitleHeader=entry.getTitle();
            this.mTitleMain=entry.getDescription();
            this.entry = entry;
            init();
        }

        public Entry getEntry(){
            return entry;
        }

        private void init(){
            //Create a CardHeader
            CardHeader header = new CardHeader(getContext(), R.layout.my_inner_base_header){

                @Override
                public void setupInnerViewElements(ViewGroup parent,View view){

                    //Add simple title to header
                    if (view!=null){
                        TextView mTitleView=(TextView) view.findViewById(R.id.card_header_inner_simple_title);
                        if (mTitleView!=null){
                            mTitleView.setText(mTitle);
                        }

                        AnimatedNetworkImageView mImageView = (AnimatedNetworkImageView) view.findViewById(R.id.card_header_imageView);
                        if ( entry.getImageLink() !=  null && entry.getImageLink() != ""  ){
                            mImageView.setImageUrl(entry.getImageLink(), VolleySingleton.getImageLoader(), category.getColor());
                        }else{
                            mImageView.setVisibility(View.GONE);
                        }
                    }

                }
            };

            //Set the header title
            header.setTitle(mTitleHeader);

            //Add a popup menu. This method set OverFlow button to visible
            /*header.setPopupMenu(R.menu.popupmain, new CardHeader.OnClickCardHeaderPopupMenuListener() {
                @Override
                public void onMenuItemClick(BaseCard card, MenuItem item) {
                    //Toast.makeText(getContext(), "Click on card menu" + mTitleHeader + " item=" + item.getTitle(), Toast.LENGTH_SHORT).show();
                }
            });
            */
            addCardHeader(header);
            setShadow(true);

            setOnLongClickListener(new OnLongCardClickListener() {
                @Override
                public boolean onLongClick(Card card, View view) {
                    if (entry.getLink() != null){
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(entry.getLink()));
                        startActivity(browserIntent);
                    }
                    return true;
                }
            });

            //Set the card inner text
            setTitle(mTitleMain);
            //setSwipeable(false);


            //Set visible the expand/collapse button
            header.setButtonExpandVisible(true);
            if (mTitleMain != null && !mTitleMain.equals("")){
                CardExpand expand = new NewsCardExpand(getContext(), mTitleMain);
                addCardExpand(expand);
            }
        }

        /**
         * This method sets values to header elements and customizes view.
         * <p/>
         * Override this method to set your elements inside InnerView.
         *
         * @param parent parent view (Inner Frame)
         * @param view   Inner View
         */
        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {

            //Add simple title to header
            if (view != null) {
                TextView mTitleView = (TextView) view.findViewById(R.id.card_main_inner_simple_title);
                if (mTitleView != null){
                    String prettyTimeString = new PrettyTime().format(new Date(entry.getDate()));
                    mTitleView.setText(String.format("%s - %s",entry.getSrcName(), prettyTimeString));
                    mTitleView.setTextColor(category.getColor());
                    mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                }
            }
        }

        @Override
        public int getType() {
            //Very important with different inner layouts
            return 0;
        }
    }

    public class NewsCardExpand extends CardExpand {

        private String mTitleMain;

        public NewsCardExpand(Context context, String mTitleMain) {
            super(context, R.layout.news_card_expand);
            this.mTitleMain = mTitleMain;
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            if (view == null) return;
            view.setBackgroundColor(Color.WHITE);

            TextView mTitleView = (TextView) view.findViewById(R.id.expand_card_main_inner_simple_title);
            if (mTitleView != null){
                mTitleView.setText(mTitleMain);
            }
            View mView = view.findViewById(R.id.colorBorder);
            if (mView != null){
                mView.setBackgroundColor(category.getColor());
            }
        }
    }


    private void addRSSItemToEntry(RSSItem item, long feedId, String source) {
        MediaEnclosure enclose = item.getEnclosure();
        String mediaUri = null;
        if (enclose != null){
            if (enclose.getMimeType().equals(IMAGE_JPEG)){
                 mediaUri = enclose.getUrl().toString();
            }
        }
        String url = item.getLink().toString();
        Date pubDate = item.getPubDate();
        Long time = null;
        if (pubDate != null){
            time = pubDate.getTime();
        }
        String desc = item.getDescription();
        if (desc != null) {
            desc = desc.replaceAll("\\<.*?>","").replaceAll("()", "");
        }

        Entry entry = new Entry(-1, feedId, category.getId(), item.getTitle(), desc, time, source, url, mediaUri);
        addEntryToCardAndDatabase(entry, true);
    }

    private void addEntryToCardAndDatabase(final Entry entry, boolean toDatabase){
        final NewsCard newsCard = new NewsCard(getActivity(), entry);
        newsCard.setId(entry.getTitle());
        mCardArrayAdapter.add(newsCard);
        if (toDatabase){
            databaseHandler.addEntry(entry);
        }
    }
}
