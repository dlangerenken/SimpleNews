package de.dala.simplenews.parser;

/**
 * Created by Daniel on 27.02.14.
 * https://github.com/danieloeh/AntennaPod/blob/master/src/de/danoeh/antennapod/opml/OpmlElement.java
 */
public class OpmlElement {
    private String description;
    private String title;
    private String xmlUrl;
    private String htmlUrl;
    private String type;

    public OpmlElement() {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getXmlUrl() {
        return xmlUrl;
    }

    public void setXmlUrl(String xmlUrl) {
        this.xmlUrl = xmlUrl;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}