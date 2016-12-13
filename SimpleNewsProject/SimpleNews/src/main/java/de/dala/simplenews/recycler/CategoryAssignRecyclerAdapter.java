package de.dala.simplenews.recycler;

import android.content.Context;
import android.support.v4.content.ContextCompat;
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
    private final Context mContext;
    private final OnClickListener mListener;
    private final List<Category> mCategories;

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
        if (position >= mCategories.size()) {
            holder.itemView.setVisibility(View.INVISIBLE);
            return;
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
        }
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
        Utilities.setPressedColorRippleDrawable(ContextCompat.getColor(mContext, R.color.list_background), PrefUtilities.getInstance().getCurrentColor(), holder.itemView);
    }

    @Override
    public int getItemCount() {
        return mCategories
                .size() + 2;
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        public final TextView name;
        public final ImageView image;

        CategoryViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            image = (ImageView) itemView.findViewById(R.id.image);
        }
    }
}
