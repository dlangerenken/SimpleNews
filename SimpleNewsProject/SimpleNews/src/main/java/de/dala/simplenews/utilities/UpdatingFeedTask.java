package de.dala.simplenews.utilities;

import android.os.AsyncTask;
import android.webkit.URLUtil;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.net.URL;

import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;

public class UpdatingFeedTask extends AsyncTask<String, String, Feed> {
    private final Category mCategory;
    private final UpdatingFeedListener mListener;
    private final Long mFeedId;

    public interface UpdatingFeedListener {
        void success(Feed feed);

        void loading();

        void fail();
    }

    public UpdatingFeedTask(Category category, UpdatingFeedListener listener, Long feedId) {
        mCategory = category;
        mListener = listener;
        mFeedId = feedId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mListener != null) {
            mListener.loading();
        }
    }

    @Override
    protected Feed doInBackground(String... params) {
        String feedUrl = params[0];
        if (!feedUrl.startsWith("http://") && !feedUrl.startsWith("https://")) {
            feedUrl = "http://" + feedUrl;
        }
        if (!URLUtil.isValidUrl(feedUrl)) {
            return null;
        }
        /* Check whether or not the feed-url is already added to the category*/
        if (mFeedId == null) {
            for (Feed feed : mCategory.getFeeds()) {
                if (Utilities.equals(feed.getXmlUrl(), feedUrl)) {
                    return feed;
                }
            }
        }

        try {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed syndFeed = input.build(new XmlReader(new URL(feedUrl)));
            if (syndFeed.getEntries() == null || syndFeed.getEntries().isEmpty()) {
                return null;
            } else {
                Feed feed = new Feed();
                feed.setId(mFeedId);
                feed.setCategoryId(mCategory.getId());
                feed.setTitle(syndFeed.getTitle());
                feed.setDescription(syndFeed.getDescription());
                feed.setXmlUrl(feedUrl);
                feed.setType(syndFeed.getFeedType());
                long id = DatabaseHandler.getInstance().addFeed(mCategory.getId(), feed, true);
                feed.setId(id);
                return feed;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Feed feed) {
        super.onPostExecute(feed);
        if (mListener != null) {
            if (feed != null) {
                mListener.success(feed);
            } else {
                mListener.fail();
            }
        }
    }

}
