package de.dala.simplenews.utilities;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.etsy.android.grid.StaggeredGridView;

/**
 * Created by Daniel on 16.07.2014.
 */
public class MyStaggeredGridView extends StaggeredGridView{

    public MyStaggeredGridView(final Context context) {
        this(context, null);
    }

    public MyStaggeredGridView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyStaggeredGridView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        return false;
    }

    boolean columnCountChanged = false;
    int columnCountPortrait = 1;
    int columnCountLandscape = 1;

    @Override
    public void setColumnCountPortrait(int columnCountPortrait) {
        if (columnCountChanged) {
            super.setColumnCountPortrait(columnCountPortrait);
        }else{
            this.columnCountPortrait = columnCountPortrait;
        }
    }

    @Override
    public void setColumnCountLandscape(int columnCountLandscape) {
        if (columnCountChanged) {
            super.setColumnCountLandscape(columnCountLandscape);
        }else{
            this.columnCountLandscape = columnCountLandscape;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h) {
        super.onSizeChanged(w, h);
        if(!columnCountChanged && getWidth() > 0) {
            columnCountChanged = true;
            this.setColumnCountLandscape(columnCountLandscape);
            this.setColumnCountPortrait(columnCountPortrait);
        }
    }
}
