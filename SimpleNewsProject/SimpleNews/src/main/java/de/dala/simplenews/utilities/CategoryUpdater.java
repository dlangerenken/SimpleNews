package de.dala.simplenews.utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Entry;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.database.IDatabaseHandler;

public class CategoryUpdater {

    public static final int EMPTY = 0;
    public static final int ERROR = -1;
    public static final int CANCEL = -2;
    public static final int RESULT = 4;
    public static final int PART_RESULT = 5;

    private final Handler handler;
    private final Category category;
    private final IDatabaseHandler databaseHandler;
    private boolean isRunning = false;
    private final Context context;
    private UpdatingTask task;
    private final Long deprecatedTime;

    public CategoryUpdater(Handler handler, Category category, Context context) {
        this.handler = handler;
        this.category = category;
        this.context = context;
        databaseHandler = DatabaseHandler.getInstance();
        deprecatedTime = PrefUtilities.getInstance().getDeprecatedTime();
    }

    public void start() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        task = new UpdatingTask();
        task.execute();
    }

    private void getPartResult(List<Entry> entries) {
        if (entries != null && !entries.isEmpty()) {
            sendMessage(entries, PART_RESULT);
        }
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
            if (task != null) {
                task.cancel(true);
            }
            isRunning = false;
            sendMessage(null, CANCEL);
        }
    }

    private class UpdatingTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (error(category)) {
                return null;
            }

            List<Callable<List<Entry>>> futures = new ArrayList<>();
            for (final Feed feed : category.getFeeds()) {
                futures.add(new FeedFutureTask(feed));
            }
            try {
                List<Entry> entries = new ArrayList<>();
                List<Future<List<Entry>>> results = Executors
                        .newFixedThreadPool(category.getFeeds().size())
                        .invokeAll(futures, 10, TimeUnit.SECONDS);

                for (Future<List<Entry>> future : results) {
                    if (isCancelled()) {
                        return null;
                    }
                    try {
                        if (!future.isCancelled()) {
                            List<Entry> result = future.get(5, TimeUnit.SECONDS);
                            if (result != null) {
                                entries.addAll(result);
                            }
                        }
                    } catch (CancellationException ignored) {
                    }
                }
                finishUpdate(entries);
            } catch (Exception e) {
                if (!isCancelled()) {
                    sendMessage(null, ERROR);
                }
            }
            return null;
        }

        private boolean error(Category category) {
            if (category == null || category.getFeeds() == null) {
                sendMessage(null, ERROR);
                return true;
            }
            if (category.getFeeds().isEmpty()) {
                sendMessage(null, EMPTY);
                return true;
            }
            return false;
        }
    }

    private void finishUpdate(List<Entry> entries) {
        if (!entries.isEmpty()) {
            category.setLastUpdateTime(new Date().getTime());
            databaseHandler.updateCategory(category);
            NetworkUtils.shortenIfNecessary(entries);
            sendMessage(entries, RESULT);
        } else {
            sendMessage(null, EMPTY);
        }
        isRunning = false;
    }

    private class FeedFutureTask implements Callable<List<Entry>> {
        final SyndFeedInput input = new SyndFeedInput();
        final Feed mFeed;

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
                    Entry entry = Utilities.getEntryFromRSSItem(item, mFeed.getId(), title, category.getId());
                    if (entry == null) {
                        continue;
                    }
                    if (deprecatedTime == null || (entry.getDate() != null && entry.getDate() > deprecatedTime)) {
                        feedEntries.add(entry);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            databaseHandler.removeEntries(category.getId(), mFeed.getId(), null);
            databaseHandler.addEntries(category.getId(), mFeed.getId(), feedEntries);
            getPartResult(feedEntries);
            return feedEntries;
        }
    }
}
