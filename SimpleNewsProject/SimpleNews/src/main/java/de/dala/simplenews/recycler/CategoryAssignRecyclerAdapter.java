package de.dala.simplenews.recycler;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.utilities.PrefUtilities;
import de.dala.simplenews.utilities.Utilities;

public class CategoryAssignRecyclerAdapter extends RecyclerView.Adapter<CategoryAssignRecyclerAdapter.CategoryViewHolder> {
    private Context mContext;
    private OnClickListener mListener;
    private List<Category> mCategories;

    public interface OnClickListener {
        void onClick(Category category);
    }

    public CategoryAssignRecyclerAdapter(Context context, List<Category> categories, OnClickListener listener) {
        mContext = context;
        mListener = listener;
        mCategories = categories;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_assign_item, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        final Category category = mCategories.get(position);
        holder.name.setText(category.getName());
        holder.image.setBackgroundColor(category.getPrimaryColor());
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(category);
                }
            }

        });
        int pad = mContext.getResources().getDimensionPixelSize(R.dimen.card_layout_padding);
        holder.itemView.setPadding(pad, pad, pad, pad);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.itemView.setBackground(Utilities.getPressedColorRippleDrawable(mContext.getResources().getColor(R.color.list_background), PrefUtilities.getInstance().getCurrentColor()));
        } else {
            holder.itemView.setBackgroundResource(mContext.getResources().getColor(R.color.list_background));
        }
    }

    @Override
    public int getItemCount() {
        return mCategories
                .size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView image;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            image = (ImageView) itemView.findViewById(R.id.image);
        }
    }
}
