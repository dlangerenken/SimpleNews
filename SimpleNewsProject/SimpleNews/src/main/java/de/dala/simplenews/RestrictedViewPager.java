package de.dala.simplenews;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Daniel on 19.12.13.
 */
public class RestrictedViewPager extends ViewPager{

    public RestrictedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RestrictedViewPager(Context context) {
        super(context);
    }

    /*
    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        // Never allow swiping to switch between pages
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }
    */
}
