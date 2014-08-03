package de.dala.simplenews.utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rosaloves.bitlyj.BitlyMethod;
import com.rosaloves.bitlyj.data.Pair;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import static com.rosaloves.bitlyj.Bitly.shorten;

/**
 * Created by Daniel on 27.12.13.
 */
public class CategoryUpdater {

    public static final int ERROR = -1;
    public static final int CANCEL = -2;
    public static final int STATUS_CHANGED = 3;
    public static final int RESULT = 4;
    public static final String IMAGE_JPEG = "image/jpeg";
    public static final String IMAGE_PNG = "image/png";
    private static final String TAG = "CategoryUpdater";
    private Handler handler;
    private Category category;
    private List<FetchingResult> results;
    private int currentWorkingThreads = 0;
    private IDatabaseHandler databaseHandler;
    private boolean updateDatabase;
    private boolean isRunning = false;
    private Context context;

    public CategoryUpdater(Handler handler, Category category, boolean updateDatabase, Context context) {
        this.handler = handler;
        this.category = category;
        this.context = context;
        databaseHandler = DatabaseHandler.getInstance();
        this.updateDatabase = updateDatabase;
    }

    public boolean start() {
        if (isRunning) {
            return false;
        }

        isRunning = true;
        String msg = context != null ? context.getString(R.string.update_news) : "";
        sendMessage(msg, STATUS_CHANGED);
        results = new ArrayList<FetchingResult>();
        currentWorkingThreads = category.getFeeds().size();
        if (currentWorkingThreads == 0) {
            sendMessage("No Feeds found", ERROR);
        }
        for (final Feed feed : category.getFeeds()) {
            NetworkCommunication.loadRSSFeed(feed.getXmlUrl(), new Response.Listener<String>() {
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

    private void dropCategory() {
        databaseHandler.removeEntries(category.getId(), null, null);
    }


    private void resultFetched(FetchingResult result) {
        currentWorkingThreads--;
        if (result != null) {
            results.add(result);
        }

        if (currentWorkingThreads <= 0) {
            // every thread has catched the information
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    if (updateDatabase) {
                        dropCategory();
                    }
                    parseInformation();
                    return null;
                }
            }.execute();
        }
    }

    private void parseInformation() {
        RSSParser parser = new RSSParser(new RSSConfig());
        List<Entry> entries = new ArrayList<Entry>();
        for (FetchingResult fetchingResult : results) {
            try {
                RSSFeed rssFeed = parser.parse(new ByteArrayInputStream(fetchingResult.stringResult.getBytes("UTF-8")));
                if (fetchingResult.feed.getTitle() == null) {
                    fetchingResult.feed.setTitle(rssFeed.getTitle());
                    databaseHandler.updateFeed(fetchingResult.feed);
                }
                String title = rssFeed.getTitle();
                for (RSSItem item : rssFeed.getItems()) {
                    Entry entry = getEntryFromRSSItem(item, fetchingResult.feed.getId(), title);
                    if (entry != null) {
                        entries.add(entry);
                    }
                }
            } catch (UnsupportedEncodingException ex) {
                Log.e(TAG, "UnsupportedEncoding", ex);
            } catch (RSSFault e) {
                Log.e(TAG, "RSSFault", e);
            }
        }

        if (updateDatabase) {
            addToDatabase(entries);
        }

        getNewItems(entries);
    }

    private void addToDatabase(List<Entry> entries) {
        Long deprecatedTime = PrefUtilities.getInstance().getDeprecatedTime();
        for (Entry entry : entries) {
            if (deprecatedTime == null || (entry.getFavoriteDate() != null && entry.getFavoriteDate() > 0) || (entry.getDate() != null && entry.getDate() > deprecatedTime)){
                databaseHandler.addEntry(category.getId(), entry.getFeedId(), entry);
            }
        }
    }

    private void getNewItems(List<Entry> entries) {
        if (entries != null && !entries.isEmpty()) {
            category.setLastUpdateTime(new Date().getTime());
            databaseHandler.updateCategory(category);

            deleteDeprecatedEntries();

            sendMessage(null, RESULT);
            if (PrefUtilities.getInstance().shouldShortenLinks()) {
                getShortenedLinks(entries);
            }
        } else {
            sendMessage(null, CANCEL);
        }
        isRunning = false;
    }

    private void deleteDeprecatedEntries() {
        Long deprecatedTime = PrefUtilities.getInstance().getDeprecatedTime();
        if (deprecatedTime != null){
            databaseHandler.deleteDeprecatedEntries(deprecatedTime);
        }
    }

    private void getShortenedLinks(final List<Entry> entries) {
        for (final Entry entry : entries) {
            if (entry.getShortenedLink() != null) {
                continue;
            }
            //shortenWithAdfly(entry);
            shortenWithBitly(entry);
        }
    }

    private void shortenWithBitly(final Entry entry) {
        if (entry != null) {
            String urlForCall = getUrlForCall(shorten(entry.getLink()));
            NetworkCommunication.loadShortenedUrl(urlForCall, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            String shortenedUrl = XmlParser.getInstance().readShortenedLink(s);
                            if (shortenedUrl != null) {
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
    }

    private void shortenWithAdfly(final Entry entry) {
        String urlForCall = String.format("http://api.adf.ly/api.php?key=86a235af637887da35e4627465b784cb&uid=6090236&advert_type=int&domain=adf.ly&url=%s", entry.getLink());

        NetworkCommunication.loadShortenedUrl(urlForCall, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String shortenedLink) {
                        if (!"error".equalsIgnoreCase(shortenedLink)) {
                            entry.setShortenedLink(shortenedLink);
                            databaseHandler.updateEntry(entry);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("CategoryUpdater", String.format("Entry with id: %s could not be shortened", entry.getId() + ""));
                    }
                }
        );
    }

    protected String getUrlForCall(BitlyMethod<?> m) {
        StringBuilder sb = new StringBuilder("http://api.bit.ly/v3/").append(m.getName()).append("?")
                .append("&login=").append(Constants.USER)
                .append("&apiKey=").append(Constants.API_KEY)
                .append("&format=xml");

        try {
            for (Pair<String, String> p : m.getParameters()) {
                sb.append("&").append(p.getOne()).append("=").append(URLEncoder.encode(p.getTwo(), "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    private Entry getEntryFromRSSItem(RSSItem item, Long feedId, String source) {
        if (item != null) {
            MediaEnclosure enclose = item.getEnclosure();
            String mediaUri = null;
            if (enclose != null) {
                if (enclose.getMimeType().equals(IMAGE_JPEG) || enclose.getMimeType().equals(IMAGE_PNG)) {
                    mediaUri = enclose.getUrl().toString();
                }
            }
            String url = item.getLink().toString();
            Date pubDate = item.getPubDate();
            Long time = null;
            if (pubDate != null) {
                time = pubDate.getTime();
            }
            String desc = item.getDescription();
            if (desc != null) {
                desc = desc.replaceAll("<.*?>", "").replace("()", "").replace("&nbsp;", "");
            }

            return new Entry(null, feedId, category.getId(), item.getTitle() != null ? item.getTitle().trim() : item.getTitle(), desc != null ? desc.trim() : desc, time, source, url, mediaUri, null, null, false);
        }
        return null;
    }

    private void sendMessage(Object message, int type) {
        Message msg = new Message();
        msg.what = type;
        msg.obj = message;
        handler.sendMessage(msg);
    }

    public boolean isRunning() {
        return isRunning;
    }

    private class FetchingResult {
        public Feed feed;
        public String stringResult;

        public FetchingResult(Feed feed, String result) {
            this.feed = feed;
            this.stringResult = result;
        }
    }
}
