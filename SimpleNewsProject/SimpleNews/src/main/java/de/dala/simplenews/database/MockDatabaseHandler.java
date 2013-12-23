package de.dala.simplenews.database;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.dala.simplenews.Category;
import de.dala.simplenews.Entry;
import de.dala.simplenews.Feed;
import de.dala.simplenews.News;
import de.dala.simplenews.parser.XmlParser;

/**
 * A Mock-DatabaseHandler for the communiciation between Client and
 * Client-Databse
 * 
 * @author Daniel Langerenken
 */
public class MockDatabaseHandler implements IDatabaseHandler {

    @Override
    public List<Category> getCategories(Boolean excludeFeeds, Boolean excludeEntries) {
        return null;
    }

    @Override
    public Category getCategory(Long categoryId, Boolean excludeFeeds, Boolean excludeEntries) {
        return null;
    }

    @Override
    public long addCategory(Category category, Boolean excludeFeeds, Boolean excludeEntries) {
        return 0;
    }

    @Override
    public int removeCategory(long categoryId, Boolean excludeFeeds, Boolean excludeEntries) {
        return 0;
    }

    @Override
    public void setVisibilityCategories(Long categoryId, boolean visible, Boolean excludeFeeds, Boolean excludeEntries) {

    }

    @Override
    public List<Feed> getFeeds(Long categoryId, Boolean excludeEntries) {
        return null;
    }

    @Override
    public Feed getFeed(long feedId, Boolean excludeEntries) {
        return null;
    }

    @Override
    public long addFeed(long categoryId, Feed feed, Boolean excludeEntries) {
        return 0;
    }

    @Override
    public int removeFeeds(Long categoryId, Long feedId, Boolean excludeEntries) {
        return 0;
    }

    @Override
    public void setVisibilityFeed(Long categoryId, Long feedId, boolean visible, Boolean excludeEntries) {

    }

    @Override
    public List<Entry> getEntries(Long categoryId, Long feedId) {
        return null;
    }

    @Override
    public Entry getEntry(long entryId) {
        return null;
    }

    @Override
    public long addEntry(long categoryId, long feedId, Entry entry) {
        return 0;
    }

    @Override
    public int removeEntries(Long categoryId, Long feedId, Long entryId) {
        return 0;
    }

    @Override
    public void setVisibilityEntry(Long categoryId, Long feedId, Long entryId, boolean visible) {

    }
}
