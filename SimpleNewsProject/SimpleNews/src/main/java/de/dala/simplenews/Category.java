package de.dala.simplenews;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 19.12.13.
 */
public class Category implements Serializable{
    private String name;
    private long id;
    private int color;
    private transient List<Feed> feeds;

    private boolean isVisible = true;

    private long lastUpdateTime;

    public Category(){
        feeds = new ArrayList<Feed>();
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }


    public String getName(){
        return name;
    }

    public int getColor(){
        return color;
    }

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    public List<Feed> getFeeds(){
        return feeds;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setFeeds(List<Feed> feeds) {
        this.feeds = feeds;
    }

    public boolean isVisible() {
        return isVisible;
    }
    public void setName(String name) {

        this.name = name;
    }
}
