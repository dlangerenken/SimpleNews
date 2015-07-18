package de.dala.simplenews.utilities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import de.dala.simplenews.R;

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
}
