package de.dala.simplenews.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class News implements Serializable {
    private List<Category> categories;

    public News(List<Category> categories) {
        this.categories = categories;
    }

    public News() {
        this.categories = new ArrayList<>();
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
}
