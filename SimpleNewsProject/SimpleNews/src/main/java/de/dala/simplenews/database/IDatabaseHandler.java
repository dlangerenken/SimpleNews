package de.dala.simplenews.database;


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
    long[] addCategories(List<Category> categories, Boolean excludeFeeds, Boolean excludeEntries);
    void addCategory(Category category, Boolean excludeFeeds, Boolean excludeEntries);
    void removeCategory(long categoryId, Boolean excludeFeeds, Boolean excludeEntries);
    void updateCategory(Category category);
    void removeAllCategories();

    long addFeed(long categoryId, Feed feed, Boolean excludeEntries);
    void removeFeeds(Long categoryId, Long feedId, Boolean excludeEntries);
    void updateFeed(Feed feed);

    List<Entry> getEntries(Long categoryId, Long feedId, Boolean onlyVisible);
    void addEntries(Long categoryId, Long feedId, List<Entry> entries);
    void updateEntry(Entry entry);
    void removeEntries(Long categoryId, Long feedId, Long entryId);
    List<Entry> getFavoriteEntries(long categoryId);
    List<Entry> getVisitedEntries(long categoryId);

    void deleteDeprecatedEntries(Long deprecatedTime);
    void loadXmlIntoDatabase(int xml);
}
