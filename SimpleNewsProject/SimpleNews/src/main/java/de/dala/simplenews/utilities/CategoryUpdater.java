package de.dala.simplenews.utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.rosaloves.bitlyj.BitlyMethod;
import com.rosaloves.bitlyj.data.Pair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private Handler handler;
    private Category category;
    private IDatabaseHandler databaseHandler;
    private boolean updateDatabase;
    private boolean isRunning = false;
    private Context context;
    private UpdatingTask task;

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

        task = new UpdatingTask();
        task.execute();
        return true;
    }

    private void dropCategory() {
        databaseHandler.removeEntries(category.getId(), null, null);
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
        databaseHandler.deleteDeprecatedEntries(PrefUtilities.getInstance().getDeprecatedTime());
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

    private Entry getEntryFromRSSItem(SyndEntry item, Long feedId, String source) {
        if (item != null) {
            if (item.getTitle() == null){
                return null;
            }

            Object url = item.getLink();
            if (url == null){
                return null;
            }

            Date pubDate = item.getPublishedDate();
            Long time = null;
            if (pubDate != null) {
                time = pubDate.getTime();
            }

            SyndContent desc = item.getDescription();
            String description = null;
            if (desc != null && desc.getValue() != null){
                description = desc.getValue();
                description = description.replaceAll("<.*?>", "").replace("()", "").replace("&nbsp;", "").trim();
            }

            return new Entry(null, feedId, category.getId(), item.getTitle().trim(), description, time, source, url.toString(), null, null, null, false);
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

    public void cancel() {
        if (task != null){
            sendMessage(null, CANCEL);
            task.cancel(true);
        }
    }

    private class UpdatingTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void[] params) {
            if (category  == null || category.getFeeds() == null){
                sendMessage(null, CANCEL);
                return null;
            }
            String msg = context != null ? context.getString(R.string.update_news) : "";
            sendMessage(msg, STATUS_CHANGED);
            if (category.getFeeds().size() == 0) {
                sendMessage(context.getString(R.string.no_feeds_found), ERROR);
            }

            SyndFeedInput input = new SyndFeedInput();
            List<Entry> entries = new ArrayList<Entry>();
            for (final Feed feed : category.getFeeds()) {
                try {
                    SyndFeed syndFeed = input.build(new XmlReader(new URL(feed.getXmlUrl())));
                    String title = syndFeed.getTitle();
                    if (feed.getTitle() == null) {
                        feed.setTitle(title);
                        databaseHandler.updateFeed(feed);
                    }
                    for (SyndEntry item : syndFeed.getEntries()) {
                        Entry entry = getEntryFromRSSItem(item, feed.getId(), title);
                        if (entry != null) {
                            entries.add(entry);
                        }
                    }
                } catch (FeedException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e){
                    e.printStackTrace();
                }
            }

            if (updateDatabase) {
                dropCategory();
                addToDatabase(entries);
            }

            getNewItems(entries);
            return null;
        }
    }
}
