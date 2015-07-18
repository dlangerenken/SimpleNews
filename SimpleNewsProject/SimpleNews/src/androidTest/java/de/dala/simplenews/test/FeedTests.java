package de.dala.simplenews.test;

import android.test.InstrumentationTestCase;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import junit.framework.Assert;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.dala.simplenews.common.Entry;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.common.News;
import de.dala.simplenews.parser.XmlParser;


public class FeedTests extends InstrumentationTestCase {

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        XmlParser.Init(getInstrumentation().getContext());
    }


    /**
     * testFeeds
     */
    public final void testFeeds() throws IOException, XmlPullParserException, FeedException {
        News news = XmlParser.getInstance().readDefaultNewsFile(R.raw.categories_test);
        List<Feed> invalidFeeds = new ArrayList<>();
        for (Feed feed : news.getCategories().get(0).getFeeds()) {
            try {
                testFeed(feed);
            } catch (Exception e) {
                invalidFeeds.add(feed);
            }
        }
        if (!invalidFeeds.isEmpty()){
            StringBuilder builder = new StringBuilder();
            builder.append("Following feeds are not valid:\n");
            for(Feed feed : invalidFeeds){
                builder.append(feed.getXmlUrl()).append("\n");
            }
            Assert.fail(builder.toString());
        }
    }

    private void testFeed(Feed feed) throws IOException, FeedException {
        SyndFeedInput input = new SyndFeedInput();
        Assert.assertTrue(feed.getXmlUrl() != null);
        SyndFeed syndFeed = input.build(new XmlReader(new URL(feed.getXmlUrl()), null));
        String title = syndFeed.getTitle();
        List<Entry> entries = new ArrayList<>();
        for (SyndEntry item : syndFeed.getEntries()) {
            Entry entry = getEntryFromRSSItem(item, null, title);
            if (entry != null) {
                entries.add(entry);
            }
        }
        Assert.assertTrue(!entries.isEmpty());
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

            return new Entry(null, feedId, null, item.getTitle().trim(), description, time, source, url.toString(), null, null, null, false);
        }
        return null;
    }
}
