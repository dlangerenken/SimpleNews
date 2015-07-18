package de.dala.simplenews.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Entry;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.common.News;
import de.dala.simplenews.parser.XmlParser;
import de.dala.simplenews.utilities.PrefUtilities;

/**
 * The DatabaseHandler for the communication between Client and Client-Database
 *
 * @author Daniel Langerenken based on
 *         http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
 */
public class DatabaseHandler extends SQLiteOpenHelper implements
        IDatabaseHandler {

    /**
     * Database Name and Version
     */
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "simple_db";

    /**
     * Table names
     */
    public static final String TABLE_CATEGORY = "category";
    public static final String TABLE_FEED = "feed";
    public static final String TABLE_ENTRY = "entry";

    public static final String CATEGORY_ID = "_id";
    public static final String CATEGORY_COLOR = "color";
    public static final String CATEGORY_NAME = "name";
    public static final String CATEGORY_VISIBLE = "visible";
    public static final String CATEGORY_LAST_UPDATE = "last_update";
    public static final String CATEGORY_ORDER = "_order";

    public static final String FEED_ID = "_id";
    public static final String FEED_CATEGORY_ID = "category_id";
    public static final String FEED_TITLE = "title";
    public static final String FEED_DESCRIPTION = "description";
    public static final String FEED_URL = "url";
    public static final String FEED_HTML_URL = "html_url";
    public static final String FEED_VISIBLE = "visible";
    public static final String FEED_TYPE = "type";

    public static final String ENTRY_ID = "_id";
    public static final String ENTRY_CATEGORY_ID = "category_id";
    public static final String ENTRY_FEED_ID = "feed_id";
    public static final String ENTRY_TITLE = "title";
    public static final String ENTRY_DESCRIPTION = "description";
    public static final String ENTRY_DATE = "date";
    public static final String ENTRY_SRC_NAME = "src_name";
    public static final String ENTRY_URL = "url";
    public static final String ENTRY_SHORTENED_URL = "shortened_url";
    public static final String ENTRY_IMAGE_URL = "image_url";
    public static final String ENTRY_VISIBLE = "visible";
    public static final String ENTRY_VISITED_DATE = "visited";
    public static final String ENTRY_FAVORITE_DATE = "favorite";
    public static final String ENTRY_IS_EXPANDED = "expanded";

    private static SQLiteDatabase db;
    private static DatabaseHandler instance;

    private DatabaseHandler(Context context, String databasePath) {
        super(context, databasePath, null, DATABASE_VERSION);
    }

    /**
     * @return the singleton instance.
     */
    public static synchronized IDatabaseHandler getInstance() {
        return instance;
    }

    public static synchronized SQLiteDatabase getDbInstance() {
        return db;
    }


    public static String concatenateQueries(String query, String additionalQuery) {
        if (query == null) {
            return additionalQuery;
        }
        return query + " AND " + additionalQuery;
    }

    /*
    * Retrieves a thread-safe instance of the singleton object {@link DatabaseHandler} and opens the database
    * with writing permissions.
    *
    * @param context the context to set.
    */
    public static void init(Context context, String databasePath) {
        if (instance == null) {
            instance = new DatabaseHandler(context, databasePath);
            db = instance.getWritableDatabase();
        }
    }

    /*
     * (non-Javadoc)
     * @see android.database.sqlite.SQLiteOpenHelper#close()
     */
    @Override
    public synchronized void close() {
        if (instance != null) {
            db.close();
        }
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
                + CATEGORY_LAST_UPDATE + " LONG,"
                + CATEGORY_VISIBLE + " INTEGER,"
                + CATEGORY_ORDER + " INTEGER" + ");";
        String createFeedTable = "CREATE TABLE "
                + TABLE_FEED + "("
                + FEED_ID + " INTEGER PRIMARY KEY, "
                + FEED_CATEGORY_ID + " LONG,"
                + FEED_TITLE + " TEXT,"
                + FEED_DESCRIPTION + " TEXT,"
                + FEED_URL + " TEXT,"
                + FEED_VISIBLE + " INTEGER,"
                + FEED_HTML_URL + " TEXT,"
                + FEED_TYPE + " TEXT" + ");";
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
                + ENTRY_SHORTENED_URL + " TEXT,"
                + ENTRY_IMAGE_URL + " TEXT,"
                + ENTRY_VISIBLE + " INTEGER,"
                + ENTRY_VISITED_DATE + " LONG,"
                + ENTRY_FAVORITE_DATE + " LONG,"
                + ENTRY_IS_EXPANDED + " INTEGER" + ");";
        db.execSQL(createCategoryTable);
        db.execSQL(createFeedTable);
        db.execSQL(createEntryTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            switch (oldVersion) {
                case -1:
                    break;
                default:
                    db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
                    db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRY);
                    db.execSQL("DROP TABLE IF EXISTS " + TABLE_FEED);
                    onCreate(db);
            }
        }
    }

    public List<Category> getCategories(Boolean excludeFeeds, Boolean excludeEntries, Boolean visible) {
        IPersistableObject<Category> persistence = new PersistableCategories(null, excludeFeeds, excludeEntries, visible);
        return load(persistence);
    }

    @Override
    public Category getCategory(Long categoryId, Boolean excludeFeeds, Boolean excludeEntries) {
        IPersistableObject<Category> persistence = new PersistableCategories(categoryId, excludeFeeds, excludeEntries, null);
        List<Category> result = load(persistence);
        if (result != null && result.size() == 1) {
            return result.get(0);
        }
        return null;
    }

    @Override
    public void addCategory(Category category, Boolean excludeFeeds, Boolean excludeEntries) {
        IPersistableObject<Category> persistence = new PersistableCategories(null, excludeFeeds, excludeEntries, null);
        List<Category> result = new ArrayList<>();
        result.add(category);
        persistence.store(result);
    }

    @Override
    public long[] addCategories(List<Category> categories, Boolean excludeFeeds, Boolean excludeEntries) {
        IPersistableObject<Category> persistence = new PersistableCategories(null, excludeFeeds, excludeEntries, null);
        return persistence.store(categories);
    }

    @Override
    public void removeCategory(long categoryId, Boolean excludeFeeds, Boolean excludeEntries) {
        IPersistableObject<Category> persistence = new PersistableCategories(categoryId, excludeFeeds, excludeEntries, null);
        persistence.delete();
    }

    @Override
    public List<Feed> getFeeds(Long categoryId, Boolean excludeEntries) {
        IPersistableObject<Feed> persistence = new PersistableFeeds(categoryId, null, excludeEntries, null);
        return load(persistence);
    }

    @Override
    public Feed getFeed(long feedId, Boolean excludeEntries) {
        IPersistableObject<Feed> persistence = new PersistableFeeds(null, feedId, excludeEntries, null);
        List<Feed> result = load(persistence);
        if (result != null && result.size() == 1) {
            return result.get(0);
        }
        return null;
    }

    @Override
    public long addFeed(long categoryId, Feed feed, Boolean excludeEntries) {
        IPersistableObject<Feed> persistence = new PersistableFeeds(categoryId, null, excludeEntries, null);
        List<Feed> result = new ArrayList<>();
        result.add(feed);
        persistence.store(result);
        return feed.getId();
    }

    @Override
    public long[] addFeeds(long categoryId, List<Feed> feeds, Boolean excludeEntries) {
        IPersistableObject<Feed> persistence = new PersistableFeeds(categoryId, null, excludeEntries, null);
        return persistence.store(feeds);
    }

    @Override
    public void removeFeeds(Long categoryId, Long feedId, Boolean excludeEntries) {
        IPersistableObject<Feed> persistence = new PersistableFeeds(categoryId, feedId, excludeEntries, null);
        persistence.delete();
    }

    @Override
    public List<Entry> getEntries(Long categoryId, Long feedId, Boolean onlyVisible) {
        IPersistableObject<Entry> persistence = new PersistableEntries(categoryId, feedId, null, onlyVisible);
        return load(persistence);
    }

    @Override
    public Entry getEntry(long entryId) {
        IPersistableObject<Entry> persistence = new PersistableEntries(null, null, entryId, null);
        List<Entry> result = load(persistence);
        if (result != null && result.size() == 1) {
            return result.get(0);
        }
        return null;
    }

    @Override
    public void addEntry(long categoryId, long feedId, Entry entry) {
        IPersistableObject<Entry> persistence = new PersistableEntries(categoryId, feedId, null, null);
        List<Entry> result = new ArrayList<>();
        result.add(entry);
        persistence.store(result);
    }

    @Override
    public long[] addEntries(Long categoryId, Long feedId, List<Entry> entries) {
        IPersistableObject<Entry> persistence = new PersistableEntries(categoryId, feedId, null, null);
        return persistence.store(entries);
    }

    @Override
    public void updateEntry(Entry entry) {
        IPersistableObject<Entry> persistence = new PersistableEntries(entry.getCategoryId(), entry.getFeedId(), entry.getId(), null);
        long[] ids = update(persistence, entry);
    }

    @Override
    public void removeEntries(Long categoryId, Long feedId, Long entryId) {
        IPersistableObject<Entry> persistence = new PersistableEntries(categoryId, feedId, entryId, null);
        persistence.delete();
    }

    @Override
    public List<Entry> getFavoriteEntries(long categoryId) {
        IPersistableObject<Entry> persistence = new PersistableFavoriteEntries(categoryId, null, null, null);
        return load(persistence);
    }

    @Override
    public List<Entry> getVisitedEntries(long categoryId) {
        IPersistableObject<Entry> persistence = new PersistableVisibleEntries(categoryId, null, null, null);
        return load(persistence);
    }

    @Override
    public List<Entry> getUnreadEntries(long categoryId) {
        IPersistableObject<Entry> persistence = new PersistableUnreadEntries(categoryId, null, null, null);
        return load(persistence);
    }

    @Override
    public Cursor getEntriesCursor(Long categoryId, Long feedId, Boolean onlyVisible) {
        IPersistableObject<Entry> persistence = new PersistableEntries(categoryId, feedId, null, onlyVisible);
        return persistence.getCursor();
    }

    @Override
    public Cursor getFavoriteEntriesCursor(Long categoryId, Long feedId) {
        IPersistableObject<Entry> persistence = new PersistableFavoriteEntries(categoryId, feedId, null, null);
        return persistence.getCursor();
    }

    @Override
    public Cursor getRecentEntriesCursor(Long categoryId, Long feedId) {
        IPersistableObject<Entry> persistence = new PersistableVisibleEntries(categoryId, feedId, null, null);
        return persistence.getCursor();
    }

    @Override
    public Cursor getUnreadEntriesCursor(Long categoryId, Long feedId) {
        IPersistableObject<Entry> persistence = new PersistableUnreadEntries(categoryId, feedId, null, null);
        return persistence.getCursor();
    }

    @Override
    public Cursor getEntriesCursor(Long categoryId, Boolean onlyVisible) {
        return getEntriesCursor(categoryId, null, onlyVisible);
    }

    @Override
    public Cursor getFavoriteEntriesCursor(Long categoryId) {
        return getFavoriteEntriesCursor(categoryId, null);
    }

    @Override
    public Cursor getRecentEntriesCursor(Long categoryId) {
        return getRecentEntriesCursor(categoryId, null);
    }

    @Override
    public Cursor getUnreadEntriesCursor(Long categoryId) {
        return getUnreadEntriesCursor(categoryId, null);
    }

    @Override
    public void deleteDeprecatedEntries(Long deprecatedTime) {
        if (deprecatedTime != null) {
            String query = concatenateQueries(null, "(" + ENTRY_FAVORITE_DATE + " < " + 1 + " OR " + ENTRY_FAVORITE_DATE + " IS NULL)");
            query = concatenateQueries(query, ENTRY_DATE + " < " + deprecatedTime);
            db.delete(TABLE_ENTRY, query, null);
        }
    }

    @Override
    public void removeAllCategories() {
        IPersistableObject<Category> persistence = new PersistableCategories(null, null, null, null);
        persistence.delete();
    }

    @Override
    public void updateCategory(Category category) {
        IPersistableObject<Category> persistence = new PersistableCategories(category.getId(), null, null, null);
        long[] ids = update(persistence, category);
    }

    @Override
    public void updateFeed(Feed feed) {
        IPersistableObject<Feed> persistence = new PersistableFeeds(feed.getCategoryId(), feed.getId(), null, null);
        long[] ids = update(persistence, feed);
    }

    private <E> long[] update(final IPersistableObject<E> persistableResource, E resource) {
        List<E> result = new ArrayList<>();
        result.add(resource);
        return persistableResource.store(result);
    }

    private <E> List<E> load(final IPersistableObject<E> persistableResource) {
        Cursor cursor = persistableResource.getCursor();
        List<E> cached = new ArrayList<>();
        try {
            if (!cursor.moveToFirst()) {
                return cached;
            }
            do {
                cached.add(persistableResource.loadFrom(cursor));
            }
            while (cursor.moveToNext());
            return cached;
        } finally {
            cursor.close();
        }
    }

    public void loadXmlIntoDatabase(int xml) {
        try {
            News news = XmlParser.getInstance().readDefaultNewsFile(xml);
            addCategories(news.getCategories(), false, false);
            PrefUtilities.getInstance().saveLoading(true);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }
}
