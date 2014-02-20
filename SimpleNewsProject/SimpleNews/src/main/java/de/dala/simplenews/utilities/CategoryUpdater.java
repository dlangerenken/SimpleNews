package de.dala.simplenews.utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidrss.MediaEnclosure;
import androidrss.RSSConfig;
import androidrss.RSSFault;
import androidrss.RSSFeed;
import androidrss.RSSItem;
import androidrss.RSSParser;
import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Entry;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.database.IDatabaseHandler;
import de.dala.simplenews.network.NetworkCommunication;
import de.dala.simplenews.parser.XmlParser;
import de.dala.toasty.Toasty;

import com.rosaloves.bitlyj.BitlyMethod;
import com.rosaloves.bitlyj.data.Pair;

import org.w3c.dom.Document;

import static com.rosaloves.bitlyj.Bitly.*;

/**
 * Created by Daniel on 27.12.13.
 */
public class CategoryUpdater {

    public static final int ERROR = -1;
    public static final int STATUS_CHANGED = 3;
    public static final int RESULT = 4;

    private Handler handler;
    private Category category;

    private List<FetchingResult> results;
    private int currentWorkingThreads = 0;
    private IDatabaseHandler databaseHandler;
    private boolean updateDatabase;
    private boolean isRunning = false;


    public boolean start() {
        if (!isRunning){
            isRunning = true;
            sendMessage(context.getString(R.string.update_news), STATUS_CHANGED);
            results = new ArrayList<FetchingResult>();
            currentWorkingThreads = category.getFeeds().size();
            if (currentWorkingThreads == 0){
                sendMessage("No Feeds found", ERROR);
            }
            for(final Feed feed : category.getFeeds()){
                NetworkCommunication.loadRSSFeed(feed.getUrl(), new Response.Listener<String>() {
                            @Override
                            public void onResponse(String feedStringResult) {
                                resultFetched(new FetchingResult(feed, feedStringResult));
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                resultFetched(null);
                            }
                        }
                );
            }
            return true;
        }
        return false;
    }

    private class FetchingResult {
        public Feed feed;
        public String stringResult;

        public FetchingResult(Feed feed, String result){
            this.feed = feed;
            this.stringResult = result;
        }
    }
    private Context context;

    public CategoryUpdater(Handler handler, Category category, Context context, boolean updateDatabase){
        this.handler = handler;
        this.category = category;
        this.context = context;
        databaseHandler = DatabaseHandler.getInstance();
        this.updateDatabase = updateDatabase;
    }

    private void dropCategory() {
        databaseHandler.removeEntries(category.getId(), null, null);
    }


    private void resultFetched(FetchingResult result) {
        currentWorkingThreads--;
        if (result != null){
            results.add(result);
        }

        if (currentWorkingThreads <= 0){
            //every thread has catched the information
            new AsyncTask<Void, Void, Void>(){

                @Override
                protected Void doInBackground(Void... params) {
                    if (updateDatabase){
                        dropCategory();
                    }
                    parseInformation();
                    return null;
                }
            }.execute();
        }
    }

    private void parseInformation() {
        //sendMessage("Parsing Information", STATUS_CHANGED);
        RSSParser parser = new RSSParser(new RSSConfig());
        List<Entry> entries = new ArrayList<Entry>();
        for(FetchingResult fetchingResult : results){
            try {
                RSSFeed rssFeed = parser.parse(new ByteArrayInputStream(fetchingResult.stringResult.getBytes("UTF-8")));
                if (fetchingResult.feed.getTitle() == null){
                    fetchingResult.feed.setTitle(rssFeed.getTitle());
                    databaseHandler.updateFeed(fetchingResult.feed);
                }
                String title = rssFeed.getTitle();
                for (RSSItem item : rssFeed.getItems()){
                    entries.add(getEntryFromRSSItem(item, fetchingResult.feed.getId(), title));
                }
            } catch (UnsupportedEncodingException ex){
            } catch (RSSFault e){
            }

        }
        //sleep(250);
        if (updateDatabase){
            addToDatabase(entries);
        }
        //sleep(250);
        getNewItems(entries);
    }

    private void addToDatabase(List<Entry> entries) {
        //sendMessage("Adding to database", STATUS_CHANGED);
        for (Entry entry : entries){
            databaseHandler.addEntry(category.getId(), entry.getFeedId(), entry);
        }
    }

    private void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getNewItems(List<Entry> entries) {
        //sleep(500);
        category.setLastUpdateTime(new Date().getTime());
        databaseHandler.updateCategory(category);

        sendMessage(entries, RESULT);
        getShortenedLinks(entries);
    }

    private void getShortenedLinks(final List<Entry> entries) {
        for (final Entry entry : entries){
            if (entry.getShortenedLink() != null){
                continue;
            }
            //shortenWithAdfly(entry);
            shortenWithBitly(entry);
        }
        isRunning = false;
    }

    private void shortenWithBitly(final Entry entry){
        String urlForCall = getUrlForCall(shorten(entry.getLink()));
            NetworkCommunication.loadShortenedUrl(urlForCall, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            String shortenedUrl = new XmlParser(context).readShortenedLink(s);
                            if (shortenedUrl != null){
                                //Toasty.toastI(shortenedUrl);
                                entry.setShortenedLink(shortenedUrl);
                                databaseHandler.updateEntry(entry);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    }
            );
    }

    private void shortenWithAdfly(final Entry entry) {
        String urlForCall = String.format("http://api.adf.ly/api.php?key=86a235af637887da35e4627465b784cb&uid=6090236&advert_type=int&domain=adf.ly&url=%s", entry.getLink());

        NetworkCommunication.loadShortenedUrl(urlForCall, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String shortenedLink) {
                        if (!"error".equalsIgnoreCase(shortenedLink)){
                            entry.setShortenedLink(shortenedLink);
                            databaseHandler.updateEntry(entry);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("CategoryUpdater", String.format("Entry with id: %s could not be shortened", entry.getId()+""));
                    }
                }
        );
    }

    protected String getUrlForCall(BitlyMethod<?> m) {
        StringBuilder sb = new StringBuilder("http://api.bit.ly/v3/")
                .append(m.getName() + "?")
                .append("&login=").append(context.getString(R.string.api_user))
                .append("&apiKey=").append(context.getString(R.string.api_key))
                .append("&format=xml");

        try {
            for(Pair<String, String> p : m.getParameters()) {
                sb.append("&" + p.getOne() + "=" + URLEncoder.encode(p.getTwo(), "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }


    public static final String IMAGE_JPEG = "image/jpeg";


    private Entry getEntryFromRSSItem(RSSItem item, long feedId, String source) {
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
            desc = desc.replaceAll("\\<.*?>","").replace("()", "").replace("&nbsp;", "");
        }

        return new Entry(-1, feedId, category.getId(), item.getTitle(), desc, time, source, url, mediaUri, null, null);
    }

    private void sendMessage(Object message, int type){
        Message msg = new Message();
        msg.what = type;
        msg.obj = message;
        handler.sendMessage(msg);
    }
}
