package de.dala.simplenews.utilities;

import android.content.Context;

/**
 * Created by Daniel-L on 17.06.2015.
 */
public class Utilities {
    private static final String SMARTIES_VERSION = "smarties";

    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    public static boolean isSmartiesVersion(Context context) {
        return context.getPackageName().toLowerCase().contains(SMARTIES_VERSION);
    }

    public static boolean isFreeVersion(Context context) {
        return !isSmartiesVersion(context);
    }

}
