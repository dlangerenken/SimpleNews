package de.dala.simplenews.common;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.dala.simplenews.utilities.ColorManager;

/**
 * Created by Daniel on 19.12.13.
 */
public class Category implements Serializable, Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
    private String name;
    private Long id;
    private int colorId;
    private int order;
    private transient List<Feed> feeds;
    private boolean isVisible = true;
    private Long lastUpdateTime;

    public Category() {
        feeds = new ArrayList<Feed>();
    }

    public Category(Parcel in) {
        this.name = in.readString();
        this.id = in.readLong();
        this.colorId = in.readInt();
        this.order = in.readInt();
        this.isVisible = in.readInt() > 0;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public int getPrimaryColor(){
        return ColorManager.getInstance().getColorByCategory(this);
    }

    public int getSecondaryColor(){
        return ColorManager.getInstance().getDarkColorByCategory(this);
    }

    public int getColorId() {
        return colorId;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Feed> getFeeds() {
        return feeds;
    }

    public void setFeeds(List<Feed> feeds) {
        this.feeds = feeds;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
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
        dest.writeInt(colorId);
        dest.writeInt(order);
        dest.writeInt(isVisible ? 1 : 0);
    }
}
