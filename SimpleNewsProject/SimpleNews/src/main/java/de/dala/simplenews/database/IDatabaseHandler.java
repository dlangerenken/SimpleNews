package de.dala.simplenews.database;

import android.database.Cursor;

import java.util.List;

import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Entry;
import de.dala.simplenews.common.Feed;

/**
 * The Interface for the communication between Client and Client-Database
 *
 * @author Daniel Langerenken
 */
public interface IDatabaseHandler {
    List<Category> getCategories(Boolean excludeFeeds, Boolean excludeEntries, Boolean onlyVisible);
    Category getCategory(Long categoryId, Boolean excludeFeeds, Boolean excludeEntries);
    long[] addCategories(List<Category> categories, Boolean excludeFeeds, Boolean excludeEntries);
    void addCategory(Category category, Boolean excludeFeeds, Boolean excludeEntries);
    void removeCategory(long categoryId, Boolean excludeFeeds, Boolean excludeEntries);
    void updateCategory(Category category);
    void removeAllCategories();

    List<Feed> getFeeds(Long categoryId, Boolean excludeEntries);
    Feed getFeed(long feedId, Boolean excludeEntries);
    long addFeed(long categoryId, Feed feed, Boolean excludeEntries);
    long[] addFeeds(long categoryId, List<Feed> feeds, Boolean excludeEntries);
    void removeFeeds(Long categoryId, Long feedId, Boolean excludeEntries);
    void updateFeed(Feed feed);

    List<Entry> getEntries(Long categoryId, Long feedId, Boolean onlyVisible);
    Entry getEntry(long entryId);
    void addEntry(long categoryId, long feedId, Entry entry);
    long[] addEntries(Long categoryId, Long feedId, List<Entry> entries);
    void updateEntry(Entry entry);
    void removeEntries(Long categoryId, Long feedId, Long entryId);
    List<Entry> getFavoriteEntries(long categoryId);
    List<Entry> getVisitedEntries(long categoryId);

    List<Entry> getUnreadEntries(long categoryId);

    Cursor getEntriesCursor(Long categoryId, Long feedId, Boolean onlyVisible);
    Cursor getFavoriteEntriesCursor(Long categoryId, Long feedId);
    Cursor getRecentEntriesCursor(Long categoryId, Long feedId);
    Cursor getUnreadEntriesCursor(Long categoryId, Long feedId);
    Cursor getEntriesCursor(Long categoryId, Boolean onlyVisible);
    Cursor getFavoriteEntriesCursor(Long categoryId);
    Cursor getRecentEntriesCursor(Long categoryId);
    Cursor getUnreadEntriesCursor(Long categoryId);
    void deleteDeprecatedEntries(Long deprecatedTime);

    void loadXmlIntoDatabase(int xml);
}
