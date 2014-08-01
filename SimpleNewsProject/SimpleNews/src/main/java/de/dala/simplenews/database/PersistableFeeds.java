package de.dala.simplenews.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import static de.dala.simplenews.database.DatabaseHandler.*;

import de.dala.simplenews.common.Entry;
import de.dala.simplenews.common.Feed;

/**
 * Created by Daniel on 01.08.2014.
 */
public class PersistableFeeds implements IPersistableObject<Feed>{

    private Boolean mExcludeEntries;
    private Long mCategoryId;
    private Long mFeedId;
    private Boolean mOnlyVisible;

    private SQLiteDatabase db;

    public PersistableFeeds(Long categoryId, Long feedId, Boolean excludeEntries, Boolean onlyVisible){
        mCategoryId = categoryId;
        mFeedId = feedId;
        mExcludeEntries = excludeEntries;
        mOnlyVisible = onlyVisible;
        db = DatabaseHandler.getDbInstance();
    }

    @Override
    public Cursor getCursor() {
        String query = null;
        if (mCategoryId != null){
            query = concatenateQueries(query, FEED_CATEGORY_ID + " = " + mCategoryId);
        }
        if (mFeedId != null){
            query = concatenateQueries(query, FEED_ID + " = " + mFeedId);
        }
        if (mOnlyVisible != null){
            query = concatenateQueries(query, FEED_VISIBLE + "=" + (mOnlyVisible ? "1" : "0"));
        }
        return db.query(TABLE_FEED, null,
                query, null, null, null, null);
    }

    @Override
    public Feed loadFrom(Cursor cursor) {
        Feed feed = new Feed();
        feed.setId(cursor.getLong(0));
        feed.setCategoryId(cursor.getLong(1));
        feed.setTitle(cursor.getString(2));
        feed.setDescription(cursor.getString(3));
        feed.setXmlUrl(cursor.getString(4));
        feed.setVisible(cursor.getInt(5) == 1);
        feed.setHtmlUrl(cursor.getString(6));

        if (mExcludeEntries != null && !mExcludeEntries) {
            PersistableEntries mPersistableEntries = getPersistableEntries(feed.getCategoryId(), feed.getId());
            Cursor entryCursor = mPersistableEntries.getCursor();
            try {
                if (entryCursor.moveToFirst()){
                    List<Entry> cached = new ArrayList<Entry>();
                    do {
                        cached.add(mPersistableEntries.loadFrom(cursor));
                    }
                    while (entryCursor.moveToNext());
                    feed.setEntries(cached);
                }
            } finally {
                entryCursor.close();
            }
        }
        return feed;
    }

    @Override
    public void store(List<Feed> items) {
        for (Feed feed : items){
            if (feed.getCategoryId() == null){
                feed.setCategoryId(mCategoryId);
            }
            ContentValues values = new ContentValues();
            if (feed.getId() != null) {
                values.put(FEED_ID, feed.getId());
            }
            values.put(FEED_CATEGORY_ID, feed.getCategoryId());
            values.put(FEED_TITLE, feed.getTitle());
            values.put(FEED_DESCRIPTION, feed.getDescription());
            values.put(FEED_URL, feed.getXmlUrl());
            values.put(FEED_VISIBLE, feed.isVisible() ? 1 : 0);
            values.put(FEED_HTML_URL, feed.getHtmlUrl());
            /*
		     * Inserting Row
		     */
            long id = db.replace(TABLE_FEED, null, values);
            feed.setId(id);

            if (mExcludeEntries == null || !mExcludeEntries) {
                PersistableEntries mPersistableEntries = getPersistableEntries(feed.getCategoryId(), id);
                mPersistableEntries.store(feed.getEntries());
            }
        }
    }

    @Override
    public void delete() {
        String query = null;
        if (mCategoryId != null) {
            query = concatenateQueries(query, FEED_CATEGORY_ID + "=" + mCategoryId);
        }
        if (mFeedId != null) {
            query = concatenateQueries(query, FEED_ID + "=" + mFeedId);
        }

        db.delete(TABLE_FEED, query, null);

        if (mExcludeEntries == null || !mExcludeEntries) {
            PersistableEntries entries = getPersistableEntries(mCategoryId, mFeedId);
            entries.delete();
        }
    }

    private PersistableEntries getPersistableEntries(Long categoryId, Long feedId){
        return new PersistableEntries(categoryId, feedId, null, mOnlyVisible);
    }
}
