package de.dala.simplenews.utilities;

import android.content.Context;
import android.graphics.Color;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;

/**
 * Created by Daniel on 23.07.2014.
 */
public class ColorManager {

    private int[] colors;
    private int[] darkColors;
    private static ColorManager _instance;

    public ColorManager(Context context){
        String[] colorArray = context.getResources().getStringArray(R.array.default_color_choice_values);
        String[] darkColorArray = context.getResources().getStringArray(R.array.default_color_choice_values_dark);

        colors = initColorsArray(colorArray);
        darkColors = initColorsArray(darkColorArray);
    }

    private int[] initColorsArray(String[] colorArray) {
        int[] mColors = new int[colorArray.length];
        for (int i = 0; i < colorArray.length; i++){
            mColors[i] = Color.parseColor(colorArray[i]);
        }
        return mColors;
    }

    public int getColorByCategory(Category category){
        return getColorById(category.getColorId());
    }

    public int getDarkColorByCategory(Category category){
        return getDarkColorById(category.getColorId());
    }

    public int getColorById(int id){
        if (id < colors.length && id >= 0){
            return colors[id];
        }
        return id;
    }

    public int getDarkColorById(int id){
        if (id < darkColors.length && id >= 0){
            return darkColors[id];
        }
        return id;
    }

    public int getIdByColor(int color){
        for(int i = 0; i < colors.length; i++){
            if (colors[i] == color){
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

}
