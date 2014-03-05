package de.dala.simplenews.common;

import java.io.Serializable;

/**
 * Created by Daniel on 19.12.13.
 */
public class Entry implements Comparable<Entry>, Serializable{
    private long id;
    private long feedId;
    private long categoryId;
    private String title;
    private String description;
    private Long date;
    private String srcName;
    private String link;
    private String imageLink;
    private String shortenedLink;

    private boolean visible = true;

    private Long favoriteDate;
    private Long visitedDate;



    public Entry(){
    }

    public Entry(long id, long feedId, long categoryId, String title, String description, Long date, String srcName, String link, String imageLink, Long visitedDate, Long favoriteDate){
        this.id = id;
        this.feedId = feedId;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.date = date;
        this.srcName = srcName;
        this.link = link;
        this.imageLink = imageLink;
        this.favoriteDate = favoriteDate;
        this.visitedDate = visitedDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getFeedId() {
        return feedId;
    }

    public void setFeedId(long feedId) {
        this.feedId = feedId;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getSrcName() {
        return srcName;
    }

    public void setSrcName(String srcName) {
        this.srcName = srcName;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getShortenedLink() {
        return shortenedLink;
    }

    public void setShortenedLink(String shortenedLink) {
        this.shortenedLink = shortenedLink;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }


    @Override
    public int compareTo(Entry another) {
        return getDate().compareTo(another.getDate());
    }

    @Override
    public String toString() {
        if (shortenedLink != null){
            return String.format("%s - %s", title, shortenedLink);
        }
        return String.format("%s - %s", title, link);
    }


    public Long getVisitedDate() {
        return visitedDate;
    }

    public void setVisitedDate(Long visitedDate) {
        this.visitedDate = visitedDate;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public Long getFavoriteDate() {
        return favoriteDate;
    }

    public void setFavoriteDate(Long favoriteDate) {
        this.favoriteDate = favoriteDate;
    }

/*
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Entry)){
            return false;
        }
        Entry other = (Entry) o;
        return other.getDescription().equals(description)
                && other.getTitle().equals(title);
    }

    @Override
    public int hashCode() {
        int a = description.hashCode() * 7;
        a = a + title.hashCode() * 13;
        return a;
    }
    */
}
