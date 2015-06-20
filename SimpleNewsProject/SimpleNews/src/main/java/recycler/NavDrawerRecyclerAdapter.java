package recycler;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.NavDrawerItem;
import de.dala.simplenews.utilities.UIUtils;


public class NavDrawerRecyclerAdapter extends RecyclerView.Adapter<NavDrawerRecyclerAdapter.NavDrawerViewHolder> {
    private List<NavDrawerItem> mItems;
    private ColorStateList colorStateList;
    private Drawable colorDrawable;
    private OnItemClicked mItemClickListener;

    public void setCategoryDrawable(Drawable categoryDrawable) {
        this.colorDrawable = categoryDrawable;
        notifyItemRangeChanged(0, mItems.size());
    }

    public interface OnItemClicked {
        void onClick(NavDrawerItem item);
    }

    public NavDrawerRecyclerAdapter(List<NavDrawerItem> items, OnItemClicked itemClickListener) {
        mItems = items;
        mItemClickListener = itemClickListener;
        colorDrawable = new ColorDrawable(Color.BLACK);
        colorStateList = UIUtils.getColorTextStateList();
    }

    @Override
    public NavDrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case NavDrawerItem.MAIN_ITEM:
                return new MainItemViewHolder(inflater.inflate(R.layout.drawer_list_main_item, parent, false));
            case NavDrawerItem.BORDER:
                return new BorderItemViewHolder(inflater.inflate(R.layout.drawer_list_border_item, parent, false));
            case NavDrawerItem.HEADER:
                return new HeaderItemViewHolder(inflater.inflate(R.layout.drawer_list_header_item, parent, false));
            case NavDrawerItem.SETTING_ITEM:
                return new SettingItemViewHolder(inflater.inflate(R.layout.drawer_list_setting_item, parent, false));
            case NavDrawerItem.SETTING_ITEM_BETA:
                return new SettingBetaItemViewHolder(inflater.inflate(R.layout.drawer_list_setting_item_beta, parent, false));
        }
        return new MainItemViewHolder(inflater.inflate(R.layout.drawer_list_main_item, parent, false));

    }

    @Override
    public int getItemViewType(int position) {
        NavDrawerItem item = mItems.get(position);
        return item.getType();
    }

    @Override
    public void onBindViewHolder(NavDrawerViewHolder holder, int position) {
        final NavDrawerItem item = mItems.get(position);
        switch (item.getType()) {
            case NavDrawerItem.MAIN_ITEM:
            case NavDrawerItem.SETTING_ITEM:
            case NavDrawerItem.SETTING_ITEM_BETA:
                MainItemViewHolder mainViewHolder = (MainItemViewHolder) holder;
                StateListDrawable stateList = UIUtils.getStateListDrawableByColorDrawable(colorDrawable);
                UIUtils.setBackground(mainViewHolder.itemView, stateList);
                mainViewHolder.title.setTextColor(colorStateList);
                mainViewHolder.image.setImageResource(item.getIcon());
                mainViewHolder.title.setText(item.getTitle());
                break;
            case NavDrawerItem.BORDER:
                BorderItemViewHolder borderViewHolder = (BorderItemViewHolder) holder;
                borderViewHolder.itemView.setEnabled(false);
                UIUtils.setBackground(borderViewHolder.itemView, colorDrawable);
                break;
            case NavDrawerItem.HEADER:
                HeaderItemViewHolder headerViewHolder = (HeaderItemViewHolder) holder;
                headerViewHolder.title.setText(item.getTitle());
                headerViewHolder.itemView.setEnabled(false);
                break;
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onClick(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    abstract class NavDrawerViewHolder extends RecyclerView.ViewHolder {

        public NavDrawerViewHolder(View itemView) {
            super(itemView);
        }
    }

    class MainItemViewHolder extends NavDrawerViewHolder {
        ImageView image;
        TextView title;

        public MainItemViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.icon);
            title = (TextView) itemView.findViewById(R.id.title);
        }
    }

    class BorderItemViewHolder extends NavDrawerViewHolder {
        View border;

        public BorderItemViewHolder(View itemView) {
            super(itemView);
            border = itemView.findViewById(R.id.view);
        }
    }

    class SettingItemViewHolder extends MainItemViewHolder {
        public SettingItemViewHolder(View itemView) {
            super(itemView);
        }
    }

    class SettingBetaItemViewHolder extends MainItemViewHolder {
        public SettingBetaItemViewHolder(View itemView) {
            super(itemView);
        }
    }

    class HeaderItemViewHolder extends NavDrawerViewHolder {
        TextView title;

        public HeaderItemViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
        }
    }

}
