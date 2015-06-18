package de.dala.simplenews.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.GINGERBREAD;

import java.util.Date;


/**
 * Created by Daniel on 09.01.14.
 */
@SuppressLint("CommitPrefEdits")
public class PrefUtilities {
    public static final String XML_LOADED = "xmlLoaded";
    public static final String ASK_FOR_RATING = "ask_for_rating";
    public static final String LAUNCH_COUNT = "launch_count";
    public static final String FIRST_DAY_OF_LAUNCH = "first_launch_time";
    public static final String TIME_FOR_REFRESH = "time_for_refresh";
    public static final String DEPRECATED_TIME = "deprecated_time";
    public static final String SHORTEN_LINKS = "shorten_links";
    public static final String MULTIPLE_COLUMNS_PORTRAIT = "multiple_columns_portrait";
    public static final String MULTIPLE_COLUMNS_LANDSCAPE = "multiple_columns_landscape";
    public static final String CATEGORY_INDEX = "category_index";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static PrefUtilities _instance;
    private SharedPreferences preferences;
    private static long DEFAULT_TIME_FOR_REFRESH = 1000 * 60 * 60; //one hour
    private static long DEFAULT_DEPRECATED_TIME = 1000 * 60 * 60 * 24 * 3; // three days


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
        save(preferences.edit().putBoolean(XML_LOADED, save));
    }

    public boolean hasUserLearnedDrawer() {
        return preferences.getBoolean(PREF_USER_LEARNED_DRAWER, false);
    }

    public void setUserLearnedDrawer(boolean b) {
        save(preferences.edit().putBoolean(PREF_USER_LEARNED_DRAWER, b));
    }

    public void shouldNotAskForRatingAnymore() {
        save(preferences.edit().putBoolean(ASK_FOR_RATING, false));
    }

    public boolean shouldAskForRatingAgain() {
        return preferences.getBoolean(ASK_FOR_RATING, true);
    }

    public boolean useMultipleColumnsLandscape() {
        return preferences.getBoolean(MULTIPLE_COLUMNS_LANDSCAPE, true);
    }

    public void setMultipleColumnsLandscape(boolean multipleColumns) {
        save(preferences.edit().putBoolean(MULTIPLE_COLUMNS_LANDSCAPE, multipleColumns));
    }

    public boolean useMultipleColumnsPortrait() {
        return preferences.getBoolean(MULTIPLE_COLUMNS_PORTRAIT, false);
    }

    public void setMultipleColumnsPortrait(boolean multipleColumns) {
        save(preferences.edit().putBoolean(MULTIPLE_COLUMNS_PORTRAIT, multipleColumns));
    }

    public boolean shouldShortenLinks() {
        return preferences.getBoolean(SHORTEN_LINKS, true);
    }

    public void increaseLaunchCountForRating() {
        int count = getLaunchCount();
        save(preferences.edit().putInt(LAUNCH_COUNT, count + 1));
    }

    public long getDateOfFirstLaunch() {
        return preferences.getLong(FIRST_DAY_OF_LAUNCH, 0);
    }

    public void setDateOfFirstLaunch(long date) {
        save(preferences.edit().putLong(FIRST_DAY_OF_LAUNCH, date));
    }

    public int getLaunchCount() {
        return preferences.getInt(LAUNCH_COUNT, 0);
    }

    public Long getTimeForRefresh() {
        return Long.parseLong(preferences.getString(TIME_FOR_REFRESH, DEFAULT_TIME_FOR_REFRESH + ""));
    }

    public void setTimeForRefresh(Long time) {
        save(preferences.edit().putString(TIME_FOR_REFRESH, time + ""));
    }

    public void setDeprecatedTime(Long time) {
        save(preferences.edit().putString(DEPRECATED_TIME, time + ""));
    }

    public Long getDeprecatedTime() {
        String value = preferences.getString(DEPRECATED_TIME, DEFAULT_DEPRECATED_TIME + "");
        if ("never".equals(value)){
            return null;
        }
        try {
            Long val = Long.parseLong(value);
            Long now = new Date().getTime();
            return now - val;
        } catch (NumberFormatException e){
            return null;
        }
    }

    private static boolean isEditorApplyAvailable() {
        return SDK_INT >= GINGERBREAD;
    }

    /**
     * Save preferences in given editor
     *
     * @param editor
     */
    public static void save(final SharedPreferences.Editor editor) {
        if (isEditorApplyAvailable()){
            editor.apply();
        }
        else {
            editor.commit();
        }
    }

    public int getCategoryIndex() {
        return preferences.getInt(CATEGORY_INDEX, 0);
    }
    public void setCategoryIndex(int id) {
        save(preferences.edit().putInt(CATEGORY_INDEX, id));
    }
}
