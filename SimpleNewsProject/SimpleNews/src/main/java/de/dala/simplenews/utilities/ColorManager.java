package de.dala.simplenews.utilities;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;

public class ColorManager {

    private final int[] colors;
    private final int[] darkColors;
    private static ColorManager _instance;

    private ColorManager(Context context) {
        final TypedArray typedArrayDefault = context.getResources().obtainTypedArray(R.array.colors);
        colors = new int[typedArrayDefault.length()];
        darkColors = new int[typedArrayDefault.length()];
        for (int i = 0; i < typedArrayDefault.length(); i++) {
            colors[i] = typedArrayDefault.getColor(i, 0);
            darkColors[i] = shiftColor(colors[i]);
        }
        typedArrayDefault.recycle();
    }

    public static int shiftColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f; // value component
        return Color.HSVToColor(hsv);
    }

    public int[] getColors() {
        return colors;
    }

    public int[] getDarkColors() {
        return colors;
    }


    public int getColorByCategory(Category category) {
        return getColorById(category.getColorId());
    }

    public int getDarkColorByCategory(Category category) {
        return getDarkColorById(category.getColorId());
    }

    private int getColorById(int id) {
        if (id < colors.length && id >= 0) {
            return colors[id];
        }
        return id;
    }

    private int getDarkColorById(int id) {
        if (id < darkColors.length && id >= 0) {
            return darkColors[id];
        }
        return id;
    }

    public int getIdByColor(int color) {
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] == color) {
                return i;
            }
        }
        return color;
    }

    public static void init(Context context) {
        _instance = new ColorManager(context);
    }

    public static ColorManager getInstance() {
        return _instance;
    }

    public static int moreAlpha(int currentColor, int alpha) {
        int red = Color.red(currentColor);
        int green = Color.green(currentColor);
        int blue = Color.blue(currentColor);
        return Color.argb(alpha, red, green, blue);
    }
}
