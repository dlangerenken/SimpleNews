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
    private Long id;
    private String xmlUrl;
    private String htmlUrl;
    private Long categoryId;
    private List<Entry> entries;
    private String title;
    private String description;
    private boolean visible = true;
    /**
     * Feed type, for example RSS 2 or Atom
     */
    private String type;

    public Feed(String xmlUrl) {
        this.entries = new ArrayList<Entry>();
        this.xmlUrl = xmlUrl;
    }

    public Feed() {
        this.entries = new ArrayList<Entry>();
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getXmlUrl() {
        return xmlUrl;
    }

    public void setXmlUrl(String xmlUrl) {
        this.xmlUrl = xmlUrl;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl){
        this.htmlUrl = htmlUrl;
    }
}
