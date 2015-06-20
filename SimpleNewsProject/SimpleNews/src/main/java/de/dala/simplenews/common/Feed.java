package de.dala.simplenews.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Feed implements Serializable, Comparable<Feed> {
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
        this.entries = new ArrayList<>();
        this.xmlUrl = xmlUrl;
    }

    public Feed() {
        this.entries = new ArrayList<>();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    @Override
    public int compareTo(Feed another) {
        if (another == null){
            return 1;
        }
        return getTitle().compareTo(another.getTitle());
    }
}
