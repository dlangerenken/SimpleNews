
package de.dala.simplenews.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.dala.simplenews.Category;
import de.dala.simplenews.Entry;
import de.dala.simplenews.Feed;

/**
* The DatabaseHandler for the communication between Client and Client-Database
*
* @author Daniel Langerenken based on
*         http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
*/
public class DatabaseHandler extends SQLiteOpenHelper implements
		IDatabaseHandler {

    private static final String TAG = "DatabaseHandler";

	/**
	 * Database Name and Version
	 */
	private static final int DATABASE_VERSION = 10;
	private static final String DATABASE_NAME = "news_database";

	/**
	 * Table names
	 */
	private static final String TABLE_CATEGORY = "category";
    private static final String TABLE_FEED = "feed";
    private static final String TABLE_ENTRY = "entry";

    private static final String CATEGORY_ID = "id";
    private static final String CATEGORY_COLOR = "color";
    private static final String CATEGORY_NAME = "name";
    private static final String CATEGORY_VISIBLE = "visible";

    private static final String FEED_ID = "id";
    private static final String FEED_CATEGORY_ID = "category_id";
    private static final String FEED_TITLE = "title";
    private static final String FEED_DESCRIPTION = "description";
    private static final String FEED_URL = "url";
    private static final String FEED_LAST_UPDATE = "last_update";
    private static final String FEED_VISIBLE = "visible";

    private static final String ENTRY_ID = "id";
    private static final String ENTRY_CATEGORY_ID = "category_id";
    private static final String ENTRY_FEED_ID = "feed_id";
    private static final String ENTRY_TITLE = "title";
    private static final String ENTRY_DESCRIPTION = "description";
    private static final String ENTRY_DATE = "date";
    private static final String ENTRY_SRC_NAME = "src_name";
    private static final String ENTRY_URL = "url";
    private static final String ENTRY_IMAGE_URL = "image_url";
    private static final String ENTRY_VISIBLE = "visible";




	// Ways = List of Nodes, Date, unique id, pedestrian/wheelchair type
	// In case of node/edge-manipulation. If way is affected (blocked or new type) ->
	// way into revision (with revision date). node/edge into revision (with date). new node/edge into list of nodes
	// if node is removed -> node into revision. corresponding edges into revision, removed of "edges"
	// if building changed, move all nodes,edges,routegraphs,messages,ways into revision.
	// if routegraph changed, move routegraph into revision
	// if messages reseted by server (e.g.), move all messages into revision
	// if messages "expire" -> older then some hours? move into revision
	// if node/edge changes, recalculate map.

    private Context context;

    private static SQLiteDatabase db;
    private static DatabaseHandler instance;

    /**
     * Retrieves a thread-safe instance of the singleton object {@link DatabaseHandler} and opens the database
     * with writing permissions.
     *
     * @param context the context to set.
     * @return the singleton instance.
     */
    public static synchronized DatabaseHandler getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHandler(context);
            db = instance.getWritableDatabase();
        }
        return instance;
    }

    /*
     * (non-Javadoc)
     * @see android.database.sqlite.SQLiteOpenHelper#close()
     */
    @Override
    public synchronized void close() {
        if (instance != null){
            db.close();
        }
    }

	private DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

	/*
	 * Creating Tables(non-Javadoc)
	 *
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 * .SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		 String createCategoryTable = "CREATE TABLE "
                 + TABLE_CATEGORY + "("
                 + CATEGORY_ID + " INTEGER PRIMARY KEY, "
		         + CATEGORY_COLOR + " INTEGER,"
                 + CATEGORY_NAME + " TEXT,"
                 + CATEGORY_VISIBLE + " INTEGER" +")";
        String createFeedTable = "CREATE TABLE "
                + TABLE_FEED + "("
                + FEED_ID + " INTEGER PRIMARY KEY, "
                + FEED_CATEGORY_ID + " LONG,"
                + FEED_TITLE + " TEXT,"
                + FEED_DESCRIPTION + " TEXT,"
                + FEED_URL + " TEXT,"
                + FEED_LAST_UPDATE + " LONG,"
                + FEED_VISIBLE + " INTEGER" +")";
        String createEntryTable = "CREATE TABLE "
                + TABLE_ENTRY + "("
                + ENTRY_ID + " INTEGER PRIMARY KEY, "
                + ENTRY_CATEGORY_ID + " LONG,"
                + ENTRY_FEED_ID + " LONG,"
                + ENTRY_TITLE + " TEXT,"
                + ENTRY_DESCRIPTION + " TEXT,"
                + ENTRY_DATE + " LONG,"
                + ENTRY_SRC_NAME + " TEXT,"
                + ENTRY_URL + " TEXT,"
                + ENTRY_IMAGE_URL + " TEXT,"
                + ENTRY_VISIBLE + " INTEGER" +")";

		db.execSQL(createCategoryTable);
        db.execSQL(createFeedTable);
        db.execSQL(createEntryTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FEED);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRY);

		/*
		 * Create tables again
		 */
		onCreate(db);
	}

    public List<Category> getCategories(Boolean excludeFeeds, Boolean excludeEntries) {
        List<Category> categories = new ArrayList<Category>();
        Cursor cursor = db.query(TABLE_CATEGORY, null,
                null, null, null, null, null);

		/*
		 * looping through all rows and adding to list
		 */
        if (cursor.moveToFirst()) {
            do {
                Category category = getCategoryByCursor(cursor);
                if (excludeFeeds != null && !excludeFeeds){
                    category.setFeeds(getFeeds(category.getId(), excludeEntries));
                }
                categories.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    @Override
    public Category getCategory(Long categoryId, Boolean excludeFeeds, Boolean excludeEntries) {
        Cursor cursor = db.query(TABLE_CATEGORY, null,
                CATEGORY_ID + " = " + categoryId, null, null, null, null);

        Category category = null;
        if (cursor.moveToFirst()) {
            category = getCategoryByCursor(cursor);
            if (excludeFeeds != null && !excludeFeeds){
                category.setFeeds(getFeeds(categoryId, excludeEntries));
            }
        }
        cursor.close();
        return category;
    }

    @Override
    public long addCategory(Category category, Boolean excludeFeeds, Boolean excludeEntries) {
        ContentValues values = new ContentValues();
        values.put(CATEGORY_COLOR, category.getColor());
        values.put(CATEGORY_NAME, category.getName());
        values.put(CATEGORY_VISIBLE, category.isVisible() ? 1 : 0);

        long rowId = db.insert(TABLE_CATEGORY, null, values);

        if (excludeFeeds != null && !excludeFeeds){
            for(Feed feed : category.getFeeds()){
                addFeed(rowId, feed, excludeEntries);
            }
        }
        return rowId;
    }


    @Override
    public int removeCategory(long categoryId, Boolean excludeFeeds, Boolean excludeEntries) {
        int rowsAffected = db.delete(TABLE_CATEGORY, CATEGORY_ID + "=" + categoryId, null);

        if (excludeFeeds != null && !excludeFeeds){
            removeFeeds(categoryId, null, excludeEntries);
        }
        return rowsAffected;
    }

    @Override
    public List<Feed> getFeeds(Long categoryId, Boolean excludeEntries) {
        String query = null;
        if (categoryId != null){
            query = concatenateQueries(query, FEED_CATEGORY_ID + "=" + categoryId);
        }
        Cursor cursor = db.query(TABLE_FEED, null,
                query, null, null, null, null);

		/*
		 * looping through all rows and adding to list
		 */
        List<Feed> feeds = new ArrayList<Feed>();
        if (cursor.moveToFirst()) {
            do {
                Feed feed = getFeedByCursor(cursor);
                    if (excludeEntries != null && !excludeEntries){
                        feed.setEntries(getEntries(categoryId, feed.getId()));
                    }
                feeds.add(feed);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return feeds;
    }

    @Override
    public Feed getFeed(long feedId, Boolean excludeEntries) {
        Cursor cursor = db.query(TABLE_FEED, null,
                FEED_ID + " = " + feedId, null, null, null, null);

        Feed feed = null;
        if (cursor.moveToFirst()) {
            feed = getFeedByCursor(cursor);
            if (excludeEntries != null && !excludeEntries){
                feed.setEntries(getEntries(null, feed.getId()));
            }
        }
        cursor.close();
        return feed;
    }

    @Override
    public long addFeed(long categoryId, Feed feed, Boolean excludeEntries) {
        ContentValues values = new ContentValues();
        values.put(FEED_CATEGORY_ID, categoryId);
        values.put(FEED_TITLE, feed.getTitle());
        values.put(FEED_DESCRIPTION, feed.getDescription());
        values.put(FEED_URL, feed.getUrl());
        values.put(FEED_LAST_UPDATE, feed.getLastUpdateTime());
        values.put(FEED_VISIBLE, feed.isVisible() ? 1 : 0);
		/*
		 * Inserting Row
		 */
        long rowId = db.insert(TABLE_FEED, null, values);

        if (excludeEntries != null && !excludeEntries){
            if (feed.getEntries() != null){
                for(Entry entry : feed.getEntries()){
                    addEntry(categoryId, rowId, entry);
                }
            }
        }
        return rowId;
    }

    @Override
    public int removeFeeds(Long categoryId, Long feedId, Boolean excludeEntries) {
        String query = null;
        if (categoryId != null){
            query = concatenateQueries(query, FEED_CATEGORY_ID + "=" + categoryId);
        }
        if (feedId != null){
            query = concatenateQueries(query, FEED_ID + "=" + feedId);
        }

        int rowsAffected = db.delete(TABLE_FEED, query, null);

        if (excludeEntries != null && !excludeEntries){
            removeEntries(categoryId, feedId, null);
        }
        return rowsAffected;
    }


    @Override
    public void setVisibilityFeed(Long categoryId, Long feedId, boolean visible, Boolean excludeEntries) {
        String query = null;
        if (categoryId != null){
            query = concatenateQueries(query, FEED_CATEGORY_ID + "=" + categoryId);
        }
        if (feedId != null){
            query = concatenateQueries(query, FEED_ID + "=" + feedId);
        }
        ContentValues values = new ContentValues();
        values.put(FEED_VISIBLE, visible);
        db.update(TABLE_FEED, values, query, null);

        if (excludeEntries != null && !excludeEntries){
            setVisibilityEntry(categoryId, feedId, null, visible);
        }
    }

    @Override
    public List<Entry> getEntries(Long categoryId, Long feedId) {
        String query = null;
        if (categoryId != null){
            query = concatenateQueries(query, ENTRY_CATEGORY_ID + "=" + categoryId);
        }
        if (feedId != null){
            query = concatenateQueries(query, ENTRY_FEED_ID + "=" + feedId);
        }

        Cursor cursor = db.query(TABLE_ENTRY, null,
                query, null, null, null, null);

		/*
		 * looping through all rows and adding to list
		 */
        List<Entry> entries = new ArrayList<Entry>();
        if (cursor.moveToFirst()) {
            do {
                Entry entry = getEntryByCursor(cursor);
                entries.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return entries;
    }

    @Override
    public Entry getEntry(long entryId) {
        Cursor cursor = db.query(TABLE_ENTRY, null,
                ENTRY_ID + " = " + entryId, null, null, null, null);

        Entry entry = null;
        if (cursor.moveToFirst()) {
            entry = getEntryByCursor(cursor);
        }
        cursor.close();
        return entry;
    }


    @Override
    public void setVisibilityCategories(Long categoryId, boolean visible, Boolean excludeFeeds, Boolean excludeEntries) {
        ContentValues values = new ContentValues();
        values.put(CATEGORY_VISIBLE, visible);
        db.update(TABLE_CATEGORY, values, CATEGORY_ID + " = " + categoryId, null);

        if (excludeFeeds != null && !excludeFeeds){
            setVisibilityFeed(categoryId, null, visible, excludeEntries);
        }
    }


    @Override
    public long addEntry(long categoryId, long feedId, Entry entry) {
        ContentValues values = new ContentValues();
        values.put(ENTRY_CATEGORY_ID, categoryId);
        values.put(ENTRY_FEED_ID, feedId);
        values.put(ENTRY_TITLE, entry.getTitle());
        values.put(ENTRY_DESCRIPTION, entry.getDescription());
        values.put(ENTRY_DATE, entry.getDate());
        values.put(ENTRY_SRC_NAME, entry.getSrcName());
        values.put(ENTRY_URL, entry.getLink());
        values.put(ENTRY_IMAGE_URL, entry.getImageLink());
        values.put(ENTRY_VISIBLE, entry.isVisible() ? 1 : 0);
		/*
		 * Inserting Row
		 */
        long rowId = db.insert(TABLE_ENTRY, null, values);
        return rowId;
    }

    @Override
    public int removeEntries(Long categoryId, Long feedId, Long entryId) {
        String query = null;
        if (categoryId != null){
            query = concatenateQueries(query, ENTRY_CATEGORY_ID + "=" + categoryId);
        }
        if (feedId != null){
            query = concatenateQueries(query, ENTRY_FEED_ID + "=" + feedId);
        }
        if (entryId != null){
            query = concatenateQueries(query, ENTRY_ID + "=" + entryId);
        }

        int rowsAffected = db.delete(TABLE_FEED, query, null);
        return rowsAffected;
    }

    @Override
    public void setVisibilityEntry(Long categoryId, Long feedId, Long entryId, boolean visible) {
        String query = null;
        if (categoryId != null){
            query = concatenateQueries(query, ENTRY_CATEGORY_ID + "=" + categoryId);
        }
        if (feedId != null){
            query = concatenateQueries(query, ENTRY_FEED_ID + "=" + feedId);
        }
        if (entryId != null){
            query = concatenateQueries(query, ENTRY_ID + "=" + entryId);
        }

        ContentValues values = new ContentValues();
        values.put(ENTRY_VISIBLE, visible);
        db.update(TABLE_ENTRY, values, query, null);
    }

    @Override
    public void updateFeedTime(long feedId, long lastUpdateTime){
        ContentValues values = new ContentValues();
        values.put(FEED_LAST_UPDATE, lastUpdateTime);
        db.update(TABLE_FEED, values, FEED_ID + "=" + feedId, null);
    }

    private Category getCategoryByCursor(Cursor cursor) {
        long id = cursor.getLong(0);
        int color = cursor.getInt(1);
        String name = cursor.getString(2);
        int visible = cursor.getInt(3);

        Category category = new Category();
        category.setId(id);
        category.setColor(color);
        category.setName(name);
        category.setVisible(visible > 0);
        return category;
    }
    private Feed getFeedByCursor(Cursor cursor) {
        long id = cursor.getLong(0);
        long categoryId = cursor.getLong(1);
        String title = cursor.getString(2);
        String description = cursor.getString(3);
        String url = cursor.getString(4);
        long lastUpdate = cursor.getLong(5);
        int visible = cursor.getInt(6);

        Feed feed = new Feed();
        feed.setId(id);
        feed.setCategoryId(categoryId);
        feed.setTitle(title);
        feed.setDescription(description);
        feed.setUrl(url);
        feed.setLastUpdateTime(lastUpdate);
        feed.setVisible(visible > 0);
        return feed;
    }
    private Entry getEntryByCursor(Cursor cursor) {
        long id = cursor.getLong(0);
        long categoryId = cursor.getLong(1);
        long feedId = cursor.getLong(2);
        String title = cursor.getString(3);
        String description = cursor.getString(4);
        long date = cursor.getLong(5);
        String src = cursor.getString(6);
        String url = cursor.getString(7);
        String imageUrl = cursor.getString(8);
        int visible = cursor.getInt(9);

        Entry entry = new Entry();
        entry.setId(id);
        entry.setCategoryId(categoryId);
        entry.setFeedId(feedId);
        entry.setTitle(title);
        entry.setDescription(description);
        entry.setDate(date);
        entry.setSrcName(src);
        entry.setLink(url);
        entry.setImageLink(imageUrl);
        entry.setVisible(visible > 0);
        return entry;
    }


    public static String concatenateQueries(String query, String additionalQuery){
        if (query == null){
            return additionalQuery;
        }
        return query + " AND " + additionalQuery;
    }
}
