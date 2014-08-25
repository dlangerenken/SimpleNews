package de.dala.simplenews.parser;

/**
 * Created by Daniel on 27.02.14.
 */


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import de.dala.simplenews.common.Feed;

/**
 * Reads OPML documents.
 */
public abstract class OpmlReader {

    public static List<Feed> importFile(Reader reader) throws IOException, XmlPullParserException {
        return convertOpmlListToFeedList(readDocument(reader));
    }

    /**
     * Reads an Opml document and returns a list of all OPML elements it can
     * find
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static List<OpmlElement> readDocument(Reader reader)
            throws XmlPullParserException, IOException {
        boolean isInOpml = false;
        List<OpmlElement> elementList = new ArrayList<OpmlElement>();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(reader);
        int eventType = xpp.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    if (xpp.getName().equals(OpmlSymbols.OPML)) {
                        isInOpml = true;
                    } else if (isInOpml && xpp.getName().equals(OpmlSymbols.OUTLINE)) {
                        OpmlElement element = new OpmlElement();
                        element.setTitle(xpp.getAttributeValue(null, OpmlSymbols.TITLE));
                        element.setDescription(xpp.getAttributeValue(null, OpmlSymbols.TEXT));
                        element.setXmlUrl(xpp.getAttributeValue(null, OpmlSymbols.XMLURL));
                        element.setHtmlUrl(xpp.getAttributeValue(null, OpmlSymbols.HTMLURL));
                        element.setUrl(xpp.getAttributeValue(null, OpmlSymbols.URL));
                        element.setType(xpp.getAttributeValue(null, OpmlSymbols.TYPE));
                        elementList.add(element);
                    }
                    break;
            }
            eventType = xpp.next();
        }
        return elementList;
    }

    public static List<Feed> convertOpmlListToFeedList(List<OpmlElement> elements) {
        List<Feed> feeds = new ArrayList<Feed>();
        for (OpmlElement element : elements) {
            if (IsValidFeed(element)) {
                feeds.add(convertOpmlToFeed(element));
            }
        }
        return feeds;
    }

    private static boolean IsValidFeed(OpmlElement element) {
        if (element == null){
            return false;
        }
        if (element.getXmlUrl() == null || "".equals(element.getXmlUrl())){
            if (element.getUrl() != null && !"".equals(element.getUrl())){
                element.setXmlUrl(element.getUrl());
                return true;
            }
        }
        return true;
    }

    public static Feed convertOpmlToFeed(OpmlElement element) {
        Feed feed = new Feed();
        feed.setDescription(element.getDescription());
        feed.setTitle(element.getTitle());
        feed.setXmlUrl(element.getXmlUrl());
        return feed;
    }

}