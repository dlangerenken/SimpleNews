package de.dala.simplenews.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 23.12.13.
 */
public class News implements Serializable {
    private List<Category> categories;

    public News(List<Category> categories) {
        this.categories = categories;
    }

    public News() {
        this.categories = new ArrayList<Category>();
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
}
