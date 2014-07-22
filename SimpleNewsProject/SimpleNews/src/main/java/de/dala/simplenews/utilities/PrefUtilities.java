package de.dala.simplenews.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


/**
 * Created by Daniel on 09.01.14.
 */
public class PrefUtilities {
    public static final String XML_LOADED = "xmlLoaded";
    public static final String ASK_FOR_RATING = "ask_for_rating";
    public static final String LAUNCH_COUNT = "launch_count";
    public static final String FIRST_DAY_OF_LAUNCH = "first_launch_time";
    public static final String TIME_FOR_REFRESH = "time_for_refresh";
    public static final String SHORTEN_LINKS = "shorten_links";
    public static final String MULTIPLE_COLUMNS_PORTRAIT = "multiple_columns_portrait";
    public static final String MULTIPLE_COLUMNS_LANDSCAPE = "multiple_columns_landscape";
    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static PrefUtilities _instance;
    private static long DEFAULT_TIME_FOR_REFRESH = 1000 * 60 * 60; //one hour
    private SharedPreferences preferences;

    private PrefUtilities(Context context) {
        preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
    }

    public static void init(Context context) {
        _instance = new PrefUtilities(context);
    }

    public static PrefUtilities getInstance() {
        return _instance;
    }

    public boolean xmlIsAlreadyLoaded() {
        return preferences.getBoolean(XML_LOADED, false);
    }

    public void saveLoading(boolean save) {
        preferences.edit().putBoolean(XML_LOADED, save).commit();
    }

    public boolean hasUserLearnedDrawer() {
        return preferences.getBoolean(PREF_USER_LEARNED_DRAWER, false);
    }

    public void setUserLearnedDrawer(boolean b) {
        preferences.edit().putBoolean(PREF_USER_LEARNED_DRAWER, b).commit();
    }

    public void shouldNotAskForRatingAnymore() {
        preferences.edit().putBoolean(ASK_FOR_RATING, false).commit();
    }

    public boolean shouldAskForRatingAgain() {
        return preferences.getBoolean(ASK_FOR_RATING, true);
    }

    public boolean useMultipleColumnsLandscape() {
        return preferences.getBoolean(MULTIPLE_COLUMNS_LANDSCAPE, true);
    }

    public void setMultipleColumnsLandscape(boolean multipleColumns) {
        preferences.edit().putBoolean(MULTIPLE_COLUMNS_LANDSCAPE, multipleColumns).commit();
    }

    public boolean useMultipleColumnsPortrait() {
        return preferences.getBoolean(MULTIPLE_COLUMNS_PORTRAIT, false);
    }

    public void setMultipleColumnsPortrait(boolean multipleColumns) {
        preferences.edit().putBoolean(MULTIPLE_COLUMNS_PORTRAIT, multipleColumns).commit();
    }

    public boolean shouldShortenLinks() {
        return preferences.getBoolean(SHORTEN_LINKS, true);
    }

    public void increaseLaunchCountForRating() {
        int count = getLaunchCount();
        preferences.edit().putInt(LAUNCH_COUNT, count + 1).commit();
    }

    public long getDateOfFirstLaunch() {
        return preferences.getLong(FIRST_DAY_OF_LAUNCH, 0);
    }

    public void setDateOfFirstLaunch(long date) {
        preferences.edit().putLong(FIRST_DAY_OF_LAUNCH, date).commit();
    }

    public int getLaunchCount() {
        return preferences.getInt(LAUNCH_COUNT, 0);
    }

    public long getTimeForRefresh() {
        return Long.parseLong(preferences.getString(TIME_FOR_REFRESH, DEFAULT_TIME_FOR_REFRESH + ""));
    }

    public void setTimeForRefresh(long time) {
        preferences.edit().putString(TIME_FOR_REFRESH, time + "").commit();
    }

}
