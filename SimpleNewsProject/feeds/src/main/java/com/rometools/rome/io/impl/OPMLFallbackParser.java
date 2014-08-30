package com.rometools.rome.io.impl;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Created by Daniel on 30.08.2014.
 */
public class OPMLFallbackParser extends OPML20Parser{
    /**
     * Inspects an XML Document (JDOM) to check if it can parse it.
     * <p>
     * It checks if the given document if the type of feeds the parser understands.
     * <p>
     *
     * @param document XML Document (JDOM) to check if it can be parsed by this parser.
     * @return <b>true</b> if the parser know how to parser this feed, <b>false</b> otherwise.
     */
    @Override
    public boolean isMyType(final Document document) {
        //check if any other opmlparser can handle this
        if (new OPML20Parser().isMyType(document) || new OPML10Parser().isMyType(document)){
            return false;
        }

        //check if something without <xml or <opml is a valid document


        return false;
    }
}
