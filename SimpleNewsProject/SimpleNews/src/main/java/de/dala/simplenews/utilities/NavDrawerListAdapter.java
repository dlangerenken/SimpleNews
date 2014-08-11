package de.dala.simplenews.utilities;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.dala.simplenews.R;
import de.dala.simplenews.common.NavDrawerItem;

/**
 * Created by Daniel on 18.12.13.
 */
public class NavDrawerListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private ColorStateList colorStateList;
    private Drawable colorDrawable;

    public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems) {
        this.context = context;
        this.navDrawerItems = navDrawerItems;
        colorDrawable = new ColorDrawable(Color.BLACK);
        colorStateList = UIUtils.getColorTextStateList();
    }

    @Override
    public int getCount() {
        return navDrawerItems.size();
    }

    @Override
    public Object getItem(int position) {
        return navDrawerItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NavDrawerItem item = navDrawerItems.get(position);
        switch (item.getType()){
            case NavDrawerItem.MAIN_ITEM:
                return getMainItemView(position, convertView, parent, item);
            case NavDrawerItem.BORDER:
                return getBorderView(position, convertView, parent, item);
            case NavDrawerItem.SETTING_ITEM:
                return getSettingItemView(position, convertView, parent, item);
            case NavDrawerItem.HEADER:
                return getHeaderView(position, convertView, parent, item);
            default: return getMainItemView(position, convertView, parent, item);
        }
    }


    private View getHeaderView(int position, View convertView, ViewGroup parent, NavDrawerItem item) {
        if (convertView == null) {
            LayoutInflater mInflater = LayoutInflater.from(context);
            convertView = mInflater.inflate(R.layout.drawer_list_header_item, null);
        }

        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
        View leftBorder = convertView.findViewById(R.id.left_border);
        //View rightBorder = convertView.findViewById(R.id.right_border);
        UIUtils.setBackground(leftBorder, colorDrawable);
        //UIUtils.setBackground(rightBorder, colorDrawable);
        txtTitle.setText(item.getTitle());

        convertView.setEnabled(false);
        return convertView;
    }

    private View getBorderView(int position, View convertView, ViewGroup parent, NavDrawerItem item) {
        if (convertView == null) {
            LayoutInflater mInflater = LayoutInflater.from(context);
            convertView = mInflater.inflate(R.layout.drawer_list_border_item, null);
        }
        View borderView = convertView.findViewById(R.id.view);
        convertView.setEnabled(false);
        UIUtils.setBackground(borderView, colorDrawable);
        return convertView;
    }

    private View getMainItemView(int position, View convertView, ViewGroup parent, NavDrawerItem item) {
        if (convertView == null) {
            LayoutInflater mInflater = LayoutInflater.from(context);
            convertView = mInflater.inflate(R.layout.drawer_list_main_item, null);
        }

        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);

        StateListDrawable stateList = UIUtils.getStateListDrawableByColorDrawable(colorDrawable);

        UIUtils.setBackground(convertView, stateList);
        txtTitle.setTextColor(colorStateList);

        imgIcon.setImageResource(item.getIcon());
        txtTitle.setText(item.getTitle());

        return convertView;
    }

    private View getSettingItemView(int position, View convertView, ViewGroup parent, NavDrawerItem item) {
        if (convertView == null) {
            LayoutInflater mInflater = LayoutInflater.from(context);
            convertView = mInflater.inflate(R.layout.drawer_list_setting_item, null);
        }
        return getMainItemView(position, convertView, parent, item);
    }

    public void setCategoryDrawable(Drawable colorDrawable) {
        this.notifyDataSetChanged();
        this.colorDrawable = colorDrawable;
    }

}