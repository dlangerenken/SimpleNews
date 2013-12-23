package de.dala.simplenews.database;

import java.util.List;

import de.dala.simplenews.Category;
import de.dala.simplenews.Entry;
import de.dala.simplenews.Feed;

/**
 * The Interface for the communication between Client and Client-Database
 * 
 * @author Daniel Langerenken
 */
public interface IDatabaseHandler {
    List<Category> getCategories(Boolean excludeFeeds, Boolean excludeEntries);
    Category getCategory(Long categoryId, Boolean excludeFeeds, Boolean excludeEntries);
    long addCategory(Category category, Boolean excludeFeeds, Boolean excludeEntries);
    int removeCategory(long categoryId, Boolean excludeFeeds, Boolean excludeEntries);
    void setVisibilityCategories(Long categoryId, boolean visible, Boolean excludeFeeds, Boolean excludeEntries);

    List<Feed> getFeeds(Long categoryId, Boolean excludeEntries);
    Feed getFeed(long feedId, Boolean excludeEntries);
    long addFeed(long categoryId, Feed feed, Boolean excludeEntries);
    int removeFeeds(Long categoryId, Long feedId, Boolean excludeEntries);
    void setVisibilityFeed(Long categoryId, Long feedId, boolean visible, Boolean excludeEntries);

    List<Entry> getEntries(Long categoryId, Long feedId);
    Entry getEntry(long entryId);
    long addEntry(long categoryId, long feedId, Entry entry);
    int removeEntries(Long categoryId, Long feedId, Long entryId);
    void setVisibilityEntry(Long categoryId, Long feedId, Long entryId, boolean visible);

    //TODO replace!
}
