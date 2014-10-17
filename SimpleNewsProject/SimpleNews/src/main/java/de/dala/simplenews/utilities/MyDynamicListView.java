package de.dala.simplenews.utilities;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;

/**
 * The DynamicListView is an extension of {@link ListView} that supports cell dragging
 * and swapping.
 * </p>
 * Make sure your adapter has stable ids, and override {@link ListAdapter#hasStableIds()} to return true.</br>
 * </p>
 * This layout is in charge of positioning the hover cell in the correct location
 * on the screen in response to user touch events. It uses the position of the
 * hover cell to determine when two cells should be swapped. If two cells should
 * be swapped, all the corresponding data set and layout changes are handled here.
 * </p>
 * If no cell is selected, all the touch events are passed down to the ListView
 * and behave normally. If one of the items in the ListView experiences a
 * long press event, the contents of its current visible state are captured as
 * a bitmap and its visibility is set to INVISIBLE. A hover cell is then created and
 * added to this layout as an overlaying BitmapDrawable above the ListView. Once the
 * hover cell is translated some distance to signify an item swap, a data set change
 * accompanied by animation takes place. When the user releases the hover cell,
 * it animates into its corresponding position in the ListView.
 * </p>
 * When the hover cell is either above or below the bounds of the ListView, this
 * ListView also scrolls on its own so as to reveal additional content.
 * </p>
 * See http://youtu.be/_BZIvjMgH-Q
 */

/**
 * Modified by Daniel on 18.01.14.
 */
public class MyDynamicListView extends DynamicListView {


    private OnItemLongClickListener first;
    private OnItemLongClickListener mAdditionalOnLongItemClickListener;
    /**
     * Listens for long clicks on any items in the listview. When a cell has
     * been selected, the hover cell is created and set up.
     */
    private OnItemLongClickListener mOnItemLongClickListener = new OnItemLongClickListener() {
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
            if (mAdditionalOnLongItemClickListener != null) {
                boolean result = mAdditionalOnLongItemClickListener.onItemLongClick(arg0, arg1, pos, id);
                if (result) {
                    return true;
                }
            }
            if (first != null) {
                return first.onItemLongClick(arg0, arg1, pos, id);
            }
            return true;
        }
    };

    public MyDynamicListView(Context context) {
        super(context);
    }

    public MyDynamicListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MyDynamicListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAdditionalOnLongItemClickListener(OnItemLongClickListener additionalListener) {
        first = getOnItemLongClickListener();
        this.mAdditionalOnLongItemClickListener = additionalListener;
        setOnItemLongClickListener(mOnItemLongClickListener);
    }

}
