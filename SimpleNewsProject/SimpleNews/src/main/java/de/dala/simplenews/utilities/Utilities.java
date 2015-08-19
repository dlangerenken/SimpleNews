package de.dala.simplenews.utilities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.view.View;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.dala.simplenews.common.Entry;

/**
 * Created by Daniel-L on 17.06.2015.
 */
public class Utilities {
    private static final String SMARTIES_VERSION = "smarties";

    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    private static boolean isSmartiesVersion(Context context) {
        return context.getPackageName().toLowerCase().contains(SMARTIES_VERSION);
    }

    public static boolean isFreeVersion(Context context) {
        return !isSmartiesVersion(context);
    }

    public static <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<>();
        for (T t : list1) {
            if (list2.contains(t)) {
                list.add(t);
            }
        }
        return list;
    }

    public static <T> List<T> nonIntersection(List<T> list1, List<T> list2) {
        if (list1 == null) {
            list1 = new ArrayList<>();
        }
        if (list2 == null) {
            list2 = new ArrayList<>();
        }
        List<T> list = new ArrayList<>();
        for (T t : list1) {
            if (!list2.contains(t)) {
                list.add(t);
            }
        }
        return list;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Drawable getPressedColorRippleDrawable(int normalColor, int pressedColor) {
        Drawable drawable = new RippleDrawable(getPressedColorSelector(normalColor, pressedColor), getColorDrawableFromColor(normalColor), null);
        drawable.setAlpha(70);
        return drawable;
    }


    public static void setPressedColorRippleDrawable(int normalColor, int pressedColor, View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setBackground(Utilities.getPressedColorRippleDrawable(normalColor, pressedColor));
        } else {
            view.setBackgroundColor(normalColor);
        }
    }

    private static ColorStateList getPressedColorSelector(int normalColor, int pressedColor) {
        return new ColorStateList(
                new int[][]
                        {
                                new int[]{android.R.attr.state_pressed},
                                new int[]{android.R.attr.state_focused},
                                new int[]{android.R.attr.state_activated},
                                new int[]{}
                        },
                new int[]
                        {
                                pressedColor,
                                pressedColor,
                                pressedColor,
                                normalColor
                        }
        );
    }

    private static ColorDrawable getColorDrawableFromColor(int color) {
        return new ColorDrawable(color);
    }

    public static Entry getEntryFromRSSItem(SyndEntry item, Long feedId, String source, long categoryId) {
        if (item != null) {
            if (item.getTitle() == null) {
                return null;
            }

            Object url = item.getLink();
            if (url == null) {
                return null;
            }

            Date pubDate = item.getPublishedDate();
            Long time = null;
            if (pubDate != null) {
                time = pubDate.getTime();
            }

            SyndContent desc = item.getDescription();
            String description = null;
            if (desc != null && desc.getValue() != null) {
                description = desc.getValue();
                description = description.replaceAll("<.*?>", "").replace("()", "").replace("&nbsp;", "").trim();
            }

            return new Entry(null, feedId, categoryId, item.getTitle().trim(), description, time, source, url.toString(), null, null, null, null,
                    false);
        }
        return null;
    }

    /**
     * Returns a string containing the tokens joined by delimiters.
     *
     * @param tokens an array objects to be joined. Strings will be formed from
     *               the objects by calling object.toString().
     */
    public static String join(CharSequence delimiter, Iterable<Entry> tokens, boolean shorten) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Entry token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token.toString(shorten));
        }
        return sb.toString();
    }
}
