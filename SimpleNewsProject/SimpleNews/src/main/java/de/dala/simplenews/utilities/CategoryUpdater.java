package de.dala.simplenews.utilities;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.squareup.okhttp.Request;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Entry;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.database.IDatabaseHandler;
import de.dala.simplenews.network.NetworkCommunication;
import de.dala.simplenews.network.StringCallback;

public class CategoryUpdater {

    public static final int EMPTY = 0;
    public static final int ERROR = -1;
    public static final int CANCEL = -2;
    public static final int RESULT = 4;
    public static final int PART_RESULT = 5;

    private Handler handler;
    private Category category;
    private IDatabaseHandler databaseHandler;
    private boolean updateDatabase;
    private boolean isRunning = false;
    private Context context;
    private int finishedUpdates = 0;
    private int newEntries = 0;
    private UpdatingTask task;
    private long startTime;

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
            if (deprecatedTime == null || (entry.getFavoriteDate() != null && entry.getFavoriteDate() > 0) || (entry.getDate() != null && entry.getDate() > deprecatedTime)) {
                databaseHandler.addEntry(category.getId(), entry.getFeedId(), entry);
            }
        }
    }

    private void getNewItems(List<Entry> entries) {
        finishedUpdates++;
        if (entries != null && !entries.isEmpty()) {
            sendMessage(entries, PART_RESULT);
        }

        if (finishedUpdates == category.getFeeds().size()) {
            boolean success;
            //done here
            if (newEntries > 0) {
                // change things only, when category is not empty, otherwise keep links
                databaseHandler.updateCategory(category);
                category.setLastUpdateTime(new Date().getTime());
                deleteDeprecatedEntries();
                if (PrefUtilities.getInstance().shouldShortenLinks()) {
                    getShortenedLinks(entries);
                }
                success = true;
            } else {
                success = false;
            }
            // wait at least 750 ms to have a better user-feedback
            long endTime = new Date().getTime();
            if (endTime - startTime < 750) {
                try {
                    Thread.sleep(750 - (endTime - startTime));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            sendMessage(null, success ? RESULT : CANCEL);
            isRunning = false;
            startTime = 0;
        }
    }

    private void deleteDeprecatedEntries() {
        databaseHandler.deleteDeprecatedEntries(PrefUtilities.getInstance().getDeprecatedTime());
    }

    private void getShortenedLinks(final List<Entry> entries) {
        for (final Entry entry : entries) {
            if (entry.getShortenedLink() != null) {
                continue;
            }
            shortenWithAdfly(entry);
        }
    }

    private void shortenWithAdfly(final Entry entry) {
        String link = entry.getLink();
        if (link != null) {
            String urlForCall = String.format("http://api.adf.ly/api.php?key=86a235af637887da35e4627465b784cb&uid=6090236&advert_type=int&domain=adf.ly&url=%s", Uri.encode(link));
            Log.d("CategoryUpdater", String.format("Shorten started for: %s", urlForCall));
            NetworkCommunication.loadShortenedUrl(urlForCall, new StringCallback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.e("CategoryUpdater", String.format("Entry with id: %s could not be shortened", entry.getId() + ""));
                }

                @Override
                public void success(String result) {
                    Log.d("CategoryUpdater", "Success in shorten: " + result);
                    if (!"error".equalsIgnoreCase(result)) {
                        entry.setShortenedLink(result);
                        databaseHandler.updateEntry(entry);
                    } else {
                        Log.e("CategoryUpdater", String.format("Entry with id: %s could not be shortened", entry.getId() + ""));
                    }
                }
            });
        }
    }


    private Entry getEntryFromRSSItem(SyndEntry item, Long feedId, String source) {
        if (item != null) {
            if (item.getTitle() == null) {
                return null;
            }

            Object url = item.getLink();
            if (url == null) {
                return null;
            }

            Date pubDate = item.getPublishedDate();
            Long time = null;
            if (pubDate != null) {
                time = pubDate.getTime();
            }

            SyndContent desc = item.getDescription();
            String description = null;
            if (desc != null && desc.getValue() != null) {
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
        if (isRunning) {
            isRunning = false;
            if (task != null) {
                task.cancel(true);
            }
            sendMessage(null, CANCEL);
        }
    }

    private class UpdatingTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startTime = new Date().getTime();
            finishedUpdates = 0;
            newEntries = 0;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (category == null || category.getFeeds() == null) {
                sendMessage(null, ERROR);
                return null;
            }
            if (category.getFeeds().size() == 0) {
                sendMessage(context.getString(R.string.no_feeds_found), ERROR);
                return null;
            }

            List<Entry> entries = new ArrayList<>();
            List<Callable<List<Entry>>> futures = new ArrayList<>();
            for (final Feed feed : category.getFeeds()) {
                futures.add(new FeedFutureTask(feed));
            }
            ExecutorService executor = Executors.newFixedThreadPool(4);
            try {
                List<Future<List<Entry>>> results = executor.invokeAll(futures, 10, TimeUnit.SECONDS);
                for (Future<List<Entry>> future : results) {
                    List<Entry> receivedEntries = null;
                    try {
                        if (isCancelled()) {
                            sendMessage(null, CANCEL);
                            return null;
                        }
                        receivedEntries = future.get();
                        entries.addAll(receivedEntries);
                    } catch (CancellationException e) {
                    }
                    getNewItems(receivedEntries);
                }
            } catch (Exception e) {
                sendMessage(null, ERROR);
                return null;
            }

            if (entries.isEmpty()) {
                sendMessage(null, EMPTY);
                return null;
            }

            if (updateDatabase) {
                dropCategory();
                addToDatabase(entries);
            }
            return null;
        }
    }

    private class FeedFutureTask implements Callable<List<Entry>> {
        SyndFeedInput input = new SyndFeedInput();
        Feed mFeed;

        public FeedFutureTask(Feed feed) {
            mFeed = feed;
        }

        @Override
        public List<Entry> call() throws Exception {
            List<Entry> feedEntries = new ArrayList<>();
            try {
                SyndFeed syndFeed = input.build(new XmlReader(new URL(mFeed.getXmlUrl()), context));
                String title = syndFeed.getTitle();
                if (mFeed.getTitle() == null) {
                    mFeed.setTitle(title);
                    databaseHandler.updateFeed(mFeed);
                }
                for (SyndEntry item : syndFeed.getEntries()) {
                    Entry entry = getEntryFromRSSItem(item, mFeed.getId(), title);
                    if (entry != null) {
                        feedEntries.add(entry);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return feedEntries;
        }
    }
}
