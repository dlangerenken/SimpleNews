package de.dala.simplenews.utilities;


import de.dala.simplenews.R;

/**
 * Created by Daniel-L on 18.08.2015.
 */
public enum FontStyle {
    XSmall(R.style.FontStyle_XSmall, "XSmall"),
    Small(R.style.FontStyle_Small, "Small"),
    Medium(R.style.FontStyle_Medium, "Medium"),
    Large(R.style.FontStyle_Large, "Large"),
    XLarge(R.style.FontStyle_XLarge, "XLarge");

    private final int resId;
    private final String title;

    public int getResId() {
        return resId;
    }

    public String getTitle() {
        return title;
    }

    FontStyle(int resId, String title) {
        this.resId = resId;
        this.title = title;
    }
}
