package recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;

public class CategoryRecyclerAdapter extends ChoiceModeRecyclerAdapter<CategoryRecyclerAdapter.CategoryViewHolder, Category> {
    private Context mContext;
    private String mRssPath;
    private OnCategoryClicked mListener;


    public interface OnCategoryClicked {
        void onMoreClicked(Category category);

        void onRSSSavedClick(Category category, String rssPath);

        void onRestore();

        void onShowClicked(Category category);

        void onColorClicked(Category category);

        void editClicked(Category category);
    }

    public CategoryRecyclerAdapter(Context context, List<Category> categories, String rssPath, ChoiceModeListener listener, OnCategoryClicked categoryClicked) {
        super(categories, listener);
        mContext = context;
        mRssPath = rssPath;
        mListener = categoryClicked;
    }


    @Override
    void onBindSelectedViewHolder(CategoryViewHolder holder, int position) {
        onBindNormalViewHolder(holder, position);
        holder.itemView.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_blue_light));
    }

    @Override
    void onBindNormalViewHolder(CategoryViewHolder holder, int position) {
        Category category = get(position);
        holder.name.setText(category.getName());
        holder.color.setBackgroundColor(category.getPrimaryColor());
        holder.show.setChecked(category.isVisible());
        if (mRssPath == null) {
            holder.edit.setOnClickListener(new CategoryItemClickListener(category));
            holder.color.setOnClickListener(new CategoryItemClickListener(category));
            holder.show.setOnClickListener(new CategoryItemClickListener(category));
            holder.more.setOnClickListener(new CategoryItemClickListener(category));
        } else {
            holder.edit.setVisibility(View.GONE);
            holder.show.setVisibility(View.GONE);
            holder.more.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(new CategoryItemRSSClickListener(category));
        }
        holder.itemView.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_modify_item, parent, false);
        return new CategoryViewHolder(itemView);
    }

    class CategoryItemRSSClickListener implements View.OnClickListener {
        private Category category;

        public CategoryItemRSSClickListener(Category category) {
            this.category = category;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onRSSSavedClick(category, mRssPath);
            }
        }
    }

    class CategoryItemClickListener implements View.OnClickListener {
        private Category category;

        public CategoryItemClickListener(Category category) {
            this.category = category;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                switch (v.getId()) {
                    case R.id.color:
                        mListener.onColorClicked(category);
                        break;
                    case R.id.edit:
                        mListener.editClicked(category);
                        break;
                    case R.id.show:
                        mListener.onShowClicked(category);
                        break;
                    case R.id.more:
                        mListener.onMoreClicked(category);
                        break;
                }
            }

        }
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView color;
        ImageView more;
        CheckBox show;
        ImageView edit;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            color = (ImageView) itemView.findViewById(R.id.color);
            more = (ImageView) itemView.findViewById(R.id.more);
            show = (CheckBox) itemView.findViewById(R.id.show);
            edit = (ImageView) itemView.findViewById(R.id.edit);
        }
    }
}