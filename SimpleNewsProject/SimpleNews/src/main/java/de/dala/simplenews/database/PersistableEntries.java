package de.dala.simplenews.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import de.dala.simplenews.common.Entry;

import static de.dala.simplenews.database.DatabaseHandler.*;

/**
 * Created by Daniel on 01.08.2014.
 */
public class PersistableEntries implements IPersistableObject<Entry>{

    private Long mCategoryId;
    private Long mFeedId;
    private Long mEntryId;
    private Boolean mOnlyVisible;
    private SQLiteDatabase db;

    private static final String entryTableShortName = "entryTable";
    private static final String feedTableShortName = "cursorTable";


    public PersistableEntries(Long categoryId, Long feedId, Long entryId, Boolean onlyVisible){
        mCategoryId = categoryId;
        mFeedId = feedId;
        mEntryId = entryId;
        mOnlyVisible = onlyVisible;
        db = DatabaseHandler.getDbInstance();
    }

    protected String getQuery(){
        String query = null;
        if (mCategoryId != null) {
            query = concatenateQueries(query, entryTableShortName + "." + ENTRY_CATEGORY_ID + "=" + mCategoryId);
        }
        if (mFeedId != null){
            query = concatenateQueries(query, entryTableShortName + "." + ENTRY_FEED_ID + "=" + mFeedId);
        }
        if (mEntryId != null){
            query = concatenateQueries(query, entryTableShortName + "." + ENTRY_ID + "=" + mEntryId);
        }
        if (mOnlyVisible != null){
            query = concatenateQueries(query, ENTRY_VISIBLE + "=" + (mOnlyVisible ? "1" : "0"));
        }
        return query;
    }

    @Override
    public Cursor getCursor() {
        String entryQuery = "SELECT * FROM " + TABLE_ENTRY + " " + entryTableShortName
                + " INNER JOIN "
                + TABLE_FEED + " " + feedTableShortName + " " +
                " ON " + entryTableShortName+ "." + ENTRY_FEED_ID
                +"=" + feedTableShortName +"." + FEED_ID
                +" WHERE " + getQuery()
                + " ORDER BY " + ENTRY_DATE + " DESC";

        return db.rawQuery(entryQuery, null);
    }

    @Override
    public Entry loadFrom(Cursor cursor) {
        return loadFromCursor(cursor);
    }

    public static Entry loadFromCursor(Cursor cursor){
        Entry entry = new Entry();
        entry.setId(cursor.getLong(0));
        entry.setCategoryId(cursor.getLong(1));
        entry.setFeedId(cursor.getLong(2));
        entry.setTitle(cursor.getString(3));
        entry.setDescription(cursor.getString(4));
        entry.setDate(cursor.getLong(5));
        entry.setSrcName(cursor.getString(6));
        entry.setLink(cursor.getString(7));
        entry.setShortenedLink(cursor.getString(8));
        entry.setImageLink(cursor.getString(9));
        entry.setVisible(cursor.getInt(10) == 1);
        entry.setVisitedDate(cursor.getLong(11));
        entry.setFavoriteDate(cursor.getLong(12));
        entry.setExpanded(cursor.getInt(13) == 1);
        return entry;
    }

    @Override
    public void store(List<Entry> items) {
        for (Entry entry : items) {
            List<Entry> similarEntries = getSimilarEntries(entry);
            if (similarEntries == null || similarEntries.isEmpty()) {
                ContentValues values = new ContentValues();
                if (entry.getId() != null) {
                    values.put(ENTRY_ID, entry.getId());
                }
                Long categoryId = entry.getCategoryId() != null ? entry.getCategoryId() : mCategoryId;
                Long feedId = entry.getFeedId() != null ? entry.getFeedId() : mFeedId;
                values.put(ENTRY_CATEGORY_ID, categoryId);
                values.put(ENTRY_FEED_ID, feedId);
                values.put(ENTRY_TITLE, entry.getTitle());
                values.put(ENTRY_DESCRIPTION, entry.getDescription());
                values.put(ENTRY_DATE, entry.getDate());
                values.put(ENTRY_SRC_NAME, entry.getSrcName());
                values.put(ENTRY_URL, entry.getLink());
                values.put(ENTRY_SHORTENED_URL, entry.getShortenedLink());
                values.put(ENTRY_IMAGE_URL, entry.getImageLink());
                values.put(ENTRY_VISIBLE, entry.isVisible() ? 1 : 0);
                values.put(ENTRY_VISITED_DATE, entry.getVisitedDate());
                values.put(ENTRY_FAVORITE_DATE, entry.getFavoriteDate());
                values.put(ENTRY_IS_EXPANDED, entry.isExpanded() ? 1 : 0);

                /*
                 * Inserting Row
                 */
                long rowId = db.replace(TABLE_ENTRY, null, values);
                entry.setId(rowId);
            }
        }
    }

    @Override
    public void delete() {
        String query = null;
        if (mCategoryId != null) {
            query = concatenateQueries(query, ENTRY_CATEGORY_ID + "=" + mCategoryId);
        }
        if (mFeedId != null) {
            query = concatenateQueries(query, ENTRY_FEED_ID + "=" + mFeedId);
        }
        if (mEntryId != null) {
            query = concatenateQueries(query, ENTRY_ID + "=" + mEntryId);
        }
        query = concatenateQueries(query, ENTRY_FAVORITE_DATE + " is null");
        query = concatenateQueries(query, ENTRY_VISITED_DATE + " is null");
        db.delete(TABLE_ENTRY, query, null);
    }

    public List<Entry> getSimilarEntries(Entry oldEntry) {
        List<Entry> entries = new ArrayList<Entry>();
        if (oldEntry != null) {
            String desc = oldEntry.getLink() == null ? "" : oldEntry.getLink();
            Cursor cursor = db.rawQuery("SELECT * FROM " +
                            TABLE_ENTRY + " WHERE " +
                            ENTRY_URL + "=?",
                    new String[]{desc}
            );
            /*
             * looping through all rows and adding to list
             */
            if (cursor.moveToFirst()) {
                do {
                    Entry entry = loadFrom(cursor);
                    if (entry.getId() != oldEntry.getId()) {
                        entries.add(entry);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return entries;
    }
}
