package de.dala.simplenews.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


/**
 * Created by Daniel on 09.01.14.
 */
public class PrefUtilities {
    private SharedPreferences preferences;
    private static PrefUtilities _instance;

    public static final String XML_LOADED = "xmlLoaded";

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    private PrefUtilities(Context context){
        preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
    }

    public boolean xmlIsAlreadyLoaded() {
        return preferences.getBoolean(XML_LOADED, false);
    }

    public void saveLoading(boolean save) {
        preferences.edit().putBoolean(XML_LOADED, save).commit();
    }


    public boolean hasUserLearnedDrawer(){
        return preferences.getBoolean(PREF_USER_LEARNED_DRAWER, false);
    }

    public static void init(Context context) {
        _instance = new PrefUtilities(context);
    }

    public static PrefUtilities getInstance(){
        return _instance;
    }

    public void setUserLearnedDrawer(boolean b) {
        preferences.edit().putBoolean(PREF_USER_LEARNED_DRAWER, b).commit();
    }

}
