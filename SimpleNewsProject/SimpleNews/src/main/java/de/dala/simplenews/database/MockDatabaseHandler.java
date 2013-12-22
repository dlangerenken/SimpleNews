package de.dala.simplenews.database;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidrss.RSSFeed;
import androidrss.RSSItem;
import de.dala.simplenews.Category;
import de.dala.simplenews.Entry;
import de.dala.simplenews.Feed;

/**
 * A Mock-DatabaseHandler for the communiciation between Client and
 * Client-Databse
 * 
 * @author Daniel Langerenken
 */
public class MockDatabaseHandler implements IDatabaseHandler {

    List<Category> categories;
    List<Feed> feeds;
    List<Entry> entries;

    public MockDatabaseHandler(){
        categories = new ArrayList<Category>();
        categories.add(new Category("Sport", null, 0, Color.parseColor("#FF666666"), true));
        categories.add(new Category("Wirtschaft", null, 1, Color.parseColor("#FF96AA39"), true));
        categories.add(new Category("Politik", null, 2, Color.parseColor("#FFC74B46"), true));
        categories.add(new Category("Studium", null, 3, Color.parseColor("#FFF4842D"), true));
        categories.add(new Category("International", null, 4, Color.parseColor("#FF3F9FE0"), false));
        categories.add(new Category("Technologie", null, 5, Color.parseColor("#FFBA55D3"), false));

        feeds = new ArrayList<Feed>();
        feeds.add(new Feed(0, "http://www.spiegel.de/sport/index.rss", 0, 0, null));
        feeds.add(new Feed(1, "http://www.spiegel.de/wirtschaft/index.rss", 1, 0, null));
        feeds.add(new Feed(2, "http://www.spiegel.de/politik/index.rss", 2, 0, null));
        feeds.add(new Feed(3, "http://www.spiegel.de/unispiegel/studium/index.rss", 3, 0, null));
        feeds.add(new Feed(4, "http://www.spiegel.de/international/index.rss", 4, 0, null));
        feeds.add(new Feed(5, "http://rss.golem.de/rss.php?feed=RSS2.0", 5, 0, null));
        feeds.add(new Feed(6, "http://www.spiegel.de/wissenschaft/technik/index.rss", 5, 0, null));
        feeds.add(new Feed(7, "http://www.heise.de/newsticker/heise-atom.xml", 5, 0, null));



        entries = new ArrayList<Entry>();
    }

    @Override
    public List<Category> getCategories() {



        return categories;
    }

    @Override
    public Category getCategory(long categoryId) {
        for (int i = 0; i < categories.size(); i++){
            if (categories.get(i).getId() == categoryId){
                return categories.get(i);
            }
        }
        return null;
    }

    @Override
    public long addCategory(Category category) {
        categories.add(category);
        category.setId(categories.size()-1);
        return category.getId();
    }

    @Override
    public Category removeCategory(long categoryId) {
        for (int i = 0; i < categories.size(); i++){
            if (categories.get(i).getId() == categoryId){
                return categories.remove(i);
            }
        }
        return null;
    }

    @Override
    public List<Feed> getFeeds(long categoryId) {
        List<Feed> categoryFeeds = new ArrayList<Feed>();
        for(int i = 0; i < feeds.size(); i++){
            if (feeds.get(i).getCategoryId() == categoryId){
                categoryFeeds.add(feeds.get(i));
            }
        }
        return categoryFeeds;
    }

    @Override
    public List<Entry> getEntries(long feedId) {
        List<Entry> tempEntries = new ArrayList<Entry>();
        for(int i = 0; i < entries.size(); i++){
            if (entries.get(i).getFeedId() == feedId){
                tempEntries.add(entries.get(i));
            }
        }
        return tempEntries;
    }


    @Override
    public void setVisibilityCategory(long categoryId, boolean visible) {
        Category category = getCategory(categoryId);
        if (category != null){
            category.setVisible(visible);
        }
    }

    @Override
    public void setEntryDeleted(long entryId, boolean visible) {
        for(int i = 0; i < entries.size(); i++){
            Entry entry = entries.get(i);
            if (entry.getId() == entryId){
                entry.setDeleted(visible);
            }
        }
    }

    @Override
    public long addEntry(Entry entry) {
        entries.add(entry);
        entry.setId(entries.size()-1);
        return entry.getId();
    }
}
