package de.dala.simplenews.common;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 19.12.13.
 */
public class Category implements Serializable, Parcelable {
    private String name;
    private long id;
    private int color;
    private int order;
    private transient List<Feed> feeds;

    private boolean isVisible = true;

    private long lastUpdateTime;

    public Category(){
        feeds = new ArrayList<Feed>();
    }

    public Category(Parcel in){
        this.name = in.readString();
        this.id = in.readLong();
        this.color = in.readInt();
        this.order = in.readInt();
        this.isVisible = in.readInt() > 0;
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

    public int getOrder(){
        return order;
    }

    public void setOrder(int order){
        this.order = order;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeLong(id);
        dest.writeInt(color);
        dest.writeInt(order);
        dest.writeInt(isVisible ? 1 : 0);
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
}
