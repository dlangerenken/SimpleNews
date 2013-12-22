package de.dala.simplenews;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidrss.RSSFeed;
import androidrss.RSSItem;

/**
 * Created by Daniel on 19.12.13.
 */
public class Category implements Serializable{
    private String name;
    private long id;
    private int color;
    private transient List<Feed> feeds;

    private boolean isDisplayed;

    public Category(String name, List<Feed> feeds, long id, int color, boolean isDisplayed){
        this.name = name;
        this.feeds = feeds;
        if (feeds==null){
            feeds = new ArrayList<Feed>();
        }
        this.id = id;
        this.color = color;
        this.isDisplayed = isDisplayed;
    }

    public boolean isDisplay(){
        return isDisplayed;
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
        this.isDisplayed = visible;
    }
}
