package de.dala.simplenews.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 19.12.13.
 */
public class Feed implements Serializable {
    public static final int FEEDFILETYPE_FEED = 0;
    public static final String TYPE_RSS2 = "rss";
    public static final String TYPE_RSS091 = "rss";
    public static final String TYPE_ATOM1 = "atom";
    private long id;
    private String url;
    private long categoryId;
    private List<Entry> entries;
    private String title;
    private String description;
    private boolean visible = true;
    private String xmlUrl;
    /**
     * Feed type, for example RSS 2 or Atom
     */
    private String type;

    public Feed(String url) {
        this.entries = new ArrayList<Entry>();
        this.url = url;
    }

    public Feed() {
        this.entries = new ArrayList<Entry>();
    }

    public Feed(long id, String url, long categoryId, List<Entry> entries) {
        this.id = id;
        this.categoryId = categoryId;
        this.entries = entries;
        if (this.entries == null) {
            this.entries = new ArrayList<Entry>();
        }
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public String getXMLUrl() {
        return xmlUrl;
    }

    public void setXmlUrl(String xmlUrl) {
        this.xmlUrl = xmlUrl;
    }
}
