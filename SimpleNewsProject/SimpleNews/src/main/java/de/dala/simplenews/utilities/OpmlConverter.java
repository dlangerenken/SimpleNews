package de.dala.simplenews.utilities;


import com.rometools.rome.feed.opml.Opml;
import com.rometools.rome.feed.opml.Outline;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Feed;

/**
    * Created by Daniel on 29.08.2014.
    */
    public class OpmlConverter {

    public static List<Feed> convertOpmlListToFeedList(Opml opml) {
        List<Feed> feeds = new ArrayList<>();
        for (Outline element : opml.getOutlines()) {
            feeds.addAll(convertOutlineToFeedList(element));
        }
        return feeds;
    }

    private static List<Feed> convertOutlineToFeedList(Outline outline) {
        List<Feed> feeds = new ArrayList<>();
        if (outline != null && outline.getChildren() != null && outline.getChildren().size() > 0) {
            for (Outline childrenOutline : outline.getChildren()) {
                feeds.addAll(convertOutlineToFeedList(childrenOutline));
            }
        } else {
            feeds.add(convertOutlineToFeed(outline));
        }
        return feeds;
    }

    private static Feed convertOutlineToFeed(Outline element) {
        Feed feed = new Feed();
        feed.setDescription(element.getText());
        feed.setTitle(element.getTitle());
        String url = element.getXmlUrl();
        if (url == null || "".equals(url)) {
            url = element.getUrl();
        }
        feed.setXmlUrl(url);
        feed.setHtmlUrl(element.getHtmlUrl());

        return feed;
    }

    private static Outline convertFeedsToOutline(String name, List<Feed> feeds) {
        Outline outline = new Outline();
        outline.setTitle(name);
        List<Outline> children = new ArrayList<>();
        for (Feed feed : feeds) {
            Outline childOutline;
            URL htmlUrl = null;
            URL xmlUrl = null;
            try {
                xmlUrl = new URL(feed.getXmlUrl());
                htmlUrl = new URL(feed.getHtmlUrl()); //reihenfolge wichtig
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            childOutline = new Outline(feed.getTitle(), xmlUrl, htmlUrl);
            childOutline.setText(feed.getDescription());
            if (feed.getType() != null && !"".equals(feed.getType())) {
                childOutline.setType(feed.getType());
            }
            children.add(childOutline);
        }
        outline.setChildren(children);
        return outline;
    }

    public static Opml convertFeedsToOpml(String name, List<Feed> feeds) {
        Opml opml = new Opml();
        opml.setTitle("SimpleNews - OPML");
        opml.getOutlines().add(convertFeedsToOutline(name, feeds));
        return opml;
    }


    public static Opml convertCategoriesToOpml(List<Category> categories) {
        List<Outline> children = new ArrayList<>();
        for (Category category : categories){
            children.add(convertFeedsToOutline(category.getName(), category.getFeeds()));
        }
        Opml opml = new Opml();
        opml.setTitle("SimpleNews - OPML");
        opml.setOutlines(children);
        return opml;
    }

}
