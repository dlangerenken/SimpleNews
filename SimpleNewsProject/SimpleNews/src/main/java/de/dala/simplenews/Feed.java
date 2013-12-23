package de.dala.simplenews;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 19.12.13.
 */
public class Feed implements Serializable{
    private long id;
    private String url;
    private long categoryId;
    private long lastUpdateTime;
    private List<Entry> entries;
    private String title;
    private String description;
    private boolean visible = true;

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public boolean isVisible(){
        return visible;
    }

    public void setVisible(boolean visible){
        this.visible = visible;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }
    public Feed (String url){
        this.entries = new ArrayList<Entry>();
        this.url = url;
    }

    public Feed(){
        this.entries = new ArrayList<Entry>();
    }

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
