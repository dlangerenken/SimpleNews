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
    List<Category> getCategories();
    Category getCategory(long categoryId);
    long addCategory(Category category);
    Category removeCategory(long categoryId);
    List<Feed> getFeeds(long categoryId);
    List<Entry> getEntries(long feedId);
    void setVisibilityCategory(long categoryId, boolean visible);
    void setEntryDeleted(long entryId, boolean visible);

    long addEntry(Entry entry);
}
