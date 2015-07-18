package de.dala.simplenews.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Feed;

import static de.dala.simplenews.database.DatabaseHandler.*;

/**
 * Created by Daniel on 01.08.2014.
 */
public class PersistableCategories implements IPersistableObject<Category> {

    private final Long mCategoryId;
    private final Boolean mExcludeFeeds;
    private final Boolean mExcludeEntries;
    private final Boolean mOnlyVisible;
    private final SQLiteDatabase db;

    public PersistableCategories(Long categoryId, Boolean excludeFeeds, Boolean excludeEntries, Boolean onlyVisible){
        mExcludeFeeds = excludeFeeds;
        mExcludeEntries = excludeEntries;
        mCategoryId = categoryId;
        mOnlyVisible = onlyVisible;
        db = DatabaseHandler.getDbInstance();
    }

    @Override
    public Cursor getCursor() {
        String query = null;
        if (mCategoryId != null){
            query = concatenateQueries(null, CATEGORY_ID + " = " + mCategoryId);
        }
        if (mOnlyVisible != null){
            query = concatenateQueries(query, CATEGORY_VISIBLE + "=" + (mOnlyVisible ? "1" : "0"));
        }
        return db.query(TABLE_CATEGORY, null, query, null, null, null, null);
    }

    @Override
    public Category loadFrom(Cursor cursor) {
        Category category = new Category();
        category.setId(cursor.getLong(0));
        category.setColorId(cursor.getInt(1));
        category.setName(cursor.getString(2));
        category.setLastUpdateTime(cursor.getLong(3));
        category.setVisible(cursor.getInt(4) == 1);
        category.setOrder(cursor.getInt(5));

        if (mExcludeFeeds == null || !mExcludeFeeds) {
            PersistableFeeds mPersistableFeeds = getPersistableFeeds(category.getId());
            Cursor feedCursor = mPersistableFeeds.getCursor();
            try {
                if (feedCursor.moveToFirst()){
                    List<Feed> cached = new ArrayList<>();
                    do {
                        cached.add(mPersistableFeeds.loadFrom(feedCursor));
                    }
                    while (feedCursor.moveToNext());
                    category.setFeeds(cached);
                }
            } finally {
                feedCursor.close();
            }
        }
        return category;
    }

    private PersistableFeeds getPersistableFeeds(Long categoryId) {
        return new PersistableFeeds(categoryId, null, mExcludeEntries, mOnlyVisible);
    }

    @Override
    public long[] store(List<Category> items) {
        if (items == null){
            return null;
        }
        long[] ids = new long[items.size()];
        int current = 0;
        for(Category category : items){
            ContentValues values = new ContentValues();
            values.put(CATEGORY_ID, category.getId());
            values.put(CATEGORY_COLOR, category.getColorId());
            values.put(CATEGORY_NAME, category.getName());
            values.put(CATEGORY_LAST_UPDATE, category.getLastUpdateTime());
            values.put(CATEGORY_VISIBLE, category.isVisible() ? 1 : 0);
            if (category.getOrder() != null) {
                values.put(CATEGORY_ORDER, category.getOrder());
            }

            Long categoryId = db.replace(TABLE_CATEGORY, null, values);
            ids[current++] = categoryId;
            category.setId(categoryId);

            if (category.getOrder() == null || category.getOrder() == -1) {
                values = new ContentValues();
                values.put(CATEGORY_ORDER, categoryId);
                db.update(TABLE_CATEGORY, values, CATEGORY_ID + " = " + categoryId, null);
            }
            if (mExcludeFeeds == null || !mExcludeFeeds) {
                PersistableFeeds mPersistableFeeds = getPersistableFeeds(categoryId);
                mPersistableFeeds.store(category.getFeeds());
            }
        }
        return ids;
    }

    @Override
    public void delete() {
        String query = null;
        if (mCategoryId != null) {
            query = concatenateQueries(null, CATEGORY_ID + "=" + mCategoryId);
        }
        db.delete(TABLE_CATEGORY, query, null);

        if (mExcludeFeeds == null || !mExcludeFeeds) {
            PersistableFeeds mPersistableFeeds = getPersistableFeeds(mCategoryId);
            mPersistableFeeds.delete();
        }
    }

}
