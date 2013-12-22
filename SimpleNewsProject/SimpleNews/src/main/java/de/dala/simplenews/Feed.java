package de.dala.simplenews;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidrss.RSSFeed;

/**
 * Created by Daniel on 19.12.13.
 */
public class Feed {
    private long id;
    private String url;
    private long categoryId;
    private long lastUpdateTime;
    private List<Entry> entries;

    public Feed(long id, String url, long categoryId, long lastUpdateTime, List<Entry> entries){
        this.id = id;
        this.categoryId =categoryId;
        this.lastUpdateTime =lastUpdateTime;
        this.entries = entries;
        if (entries==null){
            entries = new ArrayList<Entry>();
        }
        this.url = url;
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

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }
}
