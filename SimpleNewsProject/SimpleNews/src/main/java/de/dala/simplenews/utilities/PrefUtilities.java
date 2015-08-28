package de.dala.simplenews.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Date;

import de.dala.simplenews.ui.NewsActivity;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.GINGERBREAD;


@SuppressLint("CommitPrefEdits")
public class PrefUtilities {
    public static final String MULTIPLE_COLUMNS_PORTRAIT = "multiple_columns_portrait";
    public static final String MULTIPLE_COLUMNS_LANDSCAPE = "multiple_columns_landscape";
    public static final String CURRENT_NEWS_TYPE_MODE = "news_type_mode";

    private static final String XML_LOADED = "xmlLoaded";
    private static final String ASK_FOR_RATING = "ask_for_rating";
    private static final String LAUNCH_COUNT = "launch_count";
    private static final String FIRST_DAY_OF_LAUNCH = "first_launch_time";
    private static final String TIME_FOR_REFRESH = "time_for_refresh";
    private static final String DEPRECATED_TIME = "deprecated_time";
    private static final String SHORTEN_LINKS = "shorten_links";
    private static final String CATEGORY_INDEX = "category_index";
    private static final String CURRENT_COLOR = "current_color";
    private static final String USE_INTERNAL_BROWSER = "use_internal_browser";
    private static final String FONT_STYLE = "font_style";

    private static final String MARK_UNREAD_ENTRIES = "mark_unread_entries";
    private static final String SORT_BY_FEED = "sorting_order";


    private static PrefUtilities _instance;
    private static final long DEFAULT_TIME_FOR_REFRESH = 1000 * 60 * 60; //one hour
    private static final long DEFAULT_DEPRECATED_TIME = 1000 * 60 * 60 * 24 * 3; // three days

    private final SharedPreferences preferences;

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

    public void shouldNotAskForRatingAnymore() {
        save(preferences.edit().putBoolean(ASK_FOR_RATING, false));
    }

    public boolean shouldAskForRatingAgain() {
        return preferences.getBoolean(ASK_FOR_RATING, true);
    }

    public boolean shouldMarkUnreadEntries() {
        return preferences.getBoolean(MARK_UNREAD_ENTRIES, false);
    }

    public boolean shouldSortByFeeds() {
        return preferences.getBoolean(SORT_BY_FEED, false);
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
        return preferences.getBoolean(SHORTEN_LINKS, false);
    }

    public boolean useInternalBrowser() {
        return preferences.getBoolean(USE_INTERNAL_BROWSER, false);
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

    public void saveCurrentColor(int color) {
        save(preferences.edit().putInt(CURRENT_COLOR, color));
    }

    public int getCurrentColor() {
        return preferences.getInt(CURRENT_COLOR, 0);
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
        if ("never".equals(value)) {
            return null;
        }
        try {
            Long val = Long.parseLong(value);
            Long now = new Date().getTime();
            return now - val;
        } catch (NumberFormatException e) {
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
    private static void save(final SharedPreferences.Editor editor) {
        if (isEditorApplyAvailable()) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public FontStyle getFontStyle() {
        try {
            return FontStyle.valueOf(preferences.getString(FONT_STYLE,
                    FontStyle.Medium.name()));
        } catch (Exception e) {
            return FontStyle.Medium;
        }
    }

    public void setFontStyle(FontStyle style) {
        save(preferences.edit().putString(FONT_STYLE, style.name()));
    }

    public int getCategoryIndex() {
        return preferences.getInt(CATEGORY_INDEX, 0);
    }

    public void setCategoryIndex(int id) {
        save(preferences.edit().putInt(CATEGORY_INDEX, id));
    }

    public void setNewsTypeMode(int entryType) {
        save(preferences.edit().putInt(CURRENT_NEWS_TYPE_MODE, entryType));
    }

    public int getNewsTypeMode() {
        return preferences.getInt(CURRENT_NEWS_TYPE_MODE, NewsActivity.ALL);
    }

    public void addListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void removeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
