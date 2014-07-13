package de.dala.simplenews.parser;

/**
 * Created by Daniel on 27.02.14.
 */

import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import de.dala.simplenews.common.Feed;

/**
 * Writes OPML documents.
 * https://github.com/danieloeh/AntennaPod/blob/master/src/de/danoeh/antennapod/opml/OpmlWriter.java
 */
public abstract class OpmlWriter {
    private static final String ENCODING = "UTF-8";
    private static final String OPML_VERSION = "2.0";
    private static final String OPML_TITLE = "AntennaPod Subscriptions";

    /**
     * Takes a list of feeds and a writer and writes those into an OPML
     * document.
     *
     * @throws IOException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    public static void writeDocument(List<Feed> feeds, Writer writer)
            throws IllegalArgumentException, IllegalStateException, IOException {
        XmlSerializer xs = Xml.newSerializer();
        xs.setOutput(writer);

        xs.startDocument(ENCODING, false);
        xs.startTag(null, OpmlSymbols.OPML);
        xs.attribute(null, OpmlSymbols.VERSION, OPML_VERSION);

        xs.startTag(null, OpmlSymbols.HEAD);
        xs.startTag(null, OpmlSymbols.TITLE);
        xs.text(OPML_TITLE);
        xs.endTag(null, OpmlSymbols.TITLE);
        xs.endTag(null, OpmlSymbols.HEAD);

        xs.startTag(null, OpmlSymbols.BODY);
        for (Feed feed : feeds) {
            xs.startTag(null, OpmlSymbols.OUTLINE);
            xs.attribute(null, OpmlSymbols.TEXT, feed.getDescription());
            xs.attribute(null, OpmlSymbols.TITLE, feed.getTitle());
            if (feed.getType() != null) {
                xs.attribute(null, OpmlSymbols.TYPE, feed.getType());
            }
            xs.attribute(null, OpmlSymbols.XMLURL, feed.getXMLUrl());
            if (feed.getUrl() != null) {
                xs.attribute(null, OpmlSymbols.HTMLURL, feed.getUrl());
            }
            xs.endTag(null, OpmlSymbols.OUTLINE);
        }
        xs.endTag(null, OpmlSymbols.BODY);
        xs.endTag(null, OpmlSymbols.OPML);
        xs.endDocument();
    }
}