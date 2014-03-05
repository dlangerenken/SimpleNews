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
    long addCategory(Category category, Boolean excludeFeeds, Boolean excludeEntries);
    int removeCategory(long categoryId, Boolean excludeFeeds, Boolean excludeEntries);
    int updateCategory(Category category);
    void removeAllCategories();

    List<Feed> getFeeds(Long categoryId, Boolean excludeEntries);
    Feed getFeed(long feedId, Boolean excludeEntries);
    long addFeed(long categoryId, Feed feed, Boolean excludeEntries);
    int removeFeeds(Long categoryId, Long feedId, Boolean excludeEntries);
    int updateFeed(Feed feed);

    List<Entry> getEntries(Long categoryId, Long feedId);
    Entry getEntry(long entryId);
    long addEntry(long categoryId, long feedId, Entry entry);
    int updateEntry(Entry entry);
    int removeEntries(Long categoryId, Long feedId, Long entryId);
    Cursor getEntriesCursor(Long categoryId, Long feedId);

    List<Entry> getFavoriteEntries(long categoryId);

    List<Entry> getSimilarEntries(Entry oldEntry);

    List<Entry> getVisitedEntries(long categoryId);
}
