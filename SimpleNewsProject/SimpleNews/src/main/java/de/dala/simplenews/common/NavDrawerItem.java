package de.dala.simplenews.common;

/**
 * Created by Daniel on 19.02.14.
 * http://www.androidhive.info/2013/11/android-sliding-menu-using-navigation-drawer/
 */

public class NavDrawerItem {
    public static final int MAIN_ITEM = 0;
    public static final int SETTING_ITEM = 1;
    public static final int BORDER = 2;
    public static final int HEADER = 3;
    public static final int SETTING_ITEM_BETA = 4;

    private String title;
    private int icon;
    private String count = "0";
    private int type;
    private int id;
    // boolean to set visiblity of the counter
    private boolean isCounterVisible = false;

    public NavDrawerItem() {
    }

    public NavDrawerItem(int id, String title, int icon) {
        this.title = title;
        this.icon = icon;
        this.id = id;
        type = MAIN_ITEM;
    }

    public NavDrawerItem(int id, String title, int icon, int type){
        this.title = title;
        this.icon = icon;
        this.type = type;
        this.id = id;
    }

    public NavDrawerItem(String title, int icon, boolean isCounterVisible, String count) {
        this.title = title;
        this.icon = icon;
        this.isCounterVisible = isCounterVisible;
        this.count = count;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public int getType(){
        return type;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getCount() {
        return count;
    }

    public int getId() {
        return id;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public boolean getCounterVisibility() {
        return isCounterVisible;
    }

    public void setCounterVisibility(boolean isCounterVisible) {
        this.isCounterVisible = isCounterVisible;
    }
}