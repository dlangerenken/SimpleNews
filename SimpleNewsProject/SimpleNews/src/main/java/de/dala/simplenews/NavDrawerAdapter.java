package de.dala.simplenews;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by Daniel on 18.12.13.
 */
public class NavDrawerAdapter extends BaseAdapter {

    private ArrayList<NavDrawItem> mData = new ArrayList<NavDrawItem>();
    private LayoutInflater mInflater;
    private Drawable categoryDrawable;

    public NavDrawerAdapter(Context context) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int addItem(final NavDrawItem item) {
        int id = -1;
        if (mData.add(item)) {
            id = mData.size() - 1;
        }
        notifyDataSetChanged();
        return id;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public NavDrawItem getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        NavDrawItem item = mData.get(position);
        if (convertView == null) {
            holder = new ViewHolder();
            switch (item.type) {
                case CATEGORY:
                    convertView = mInflater.inflate(R.layout.category, null);
                    holder.textView = (TextView) convertView
                            .findViewById(R.id.textCategory);
                    holder.view = convertView.findViewById(R.id.underline);
                    break;
                case ENTRY:
                    convertView = mInflater.inflate(R.layout.element, null);
                    holder.textView = (TextView) convertView
                            .findViewById(R.id.textEntry);
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
            if (item.type == NavDrawItem.Type.CATEGORY){
                if (categoryDrawable != null){
                holder.view.setBackground(categoryDrawable);
            }
        }
        holder.textView.setText(mData.get(position).text);
        return convertView;
    }

    public void setCategoryDrawable(Drawable colorDrawable) {
        this.categoryDrawable = colorDrawable;
        this.notifyDataSetChanged();
    }

    public static class ViewHolder {
        TextView textView;
        View view;
    }
}
