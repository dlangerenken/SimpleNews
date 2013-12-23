package de.dala.simplenews;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 23.12.13.
 */
public class News implements Serializable{
    private List<Category> categories;

    public News(List<Category> categories){
        this.categories = categories;
    }

    public News(){
        this.categories = new ArrayList<Category>();
    }

    public void setCategories(List<Category> categories){
        this.categories = categories;
    }

    public List<Category> getCategories(){
        return categories;
    }
}
