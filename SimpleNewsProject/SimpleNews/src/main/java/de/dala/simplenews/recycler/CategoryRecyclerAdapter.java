package de.dala.simplenews.recycler;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.utilities.ColorManager;
import de.dala.simplenews.utilities.EmptyObservableRecyclerView;
import de.dala.simplenews.utilities.GripView;
import de.dala.simplenews.utilities.ItemTouchHelperAdapter;
import de.dala.simplenews.utilities.ItemTouchHelperCallback;
import de.dala.simplenews.utilities.ItemTouchHelperViewHolder;
import de.dala.simplenews.utilities.Movement;
import de.dala.simplenews.utilities.PrefUtilities;
import de.dala.simplenews.utilities.Utilities;

public class CategoryRecyclerAdapter extends BaseRecyclerAdapter<CategoryRecyclerAdapter.CategoryViewHolder, Category> implements ItemTouchHelperAdapter {
    private final Context mContext;
    private final String mRssPath;
    private final OnCategoryClicked mListener;
    private ItemTouchHelper mItemTouchHelper;
    private List<Movement> movements;

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (toPosition >= getItems().size()){
            return;
        }
        Category category = getItems().remove(fromPosition);
        getItems().add(toPosition, category);
        notifyItemMoved(fromPosition, toPosition);
        if (movements == null) {
            movements = new ArrayList<>();
        }
        movements.add(new Movement(fromPosition, toPosition));
    }

    @Override
    public void onItemDismiss(int position) {
        remove(get(position));
    }

    public void initTouch(EmptyObservableRecyclerView recyclerView) {
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(this);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public List<Movement> getMovements() {
        return movements;
    }


    public interface OnCategoryClicked {
        void onMoreClicked(Category category);

        void onRSSSavedClick(Category category, String rssPath);

        void onRestore();

        void onShowClicked(Category category);

        void onColorClicked(Category category);

        void editClicked(Category category);

        void saveMovement(List<Movement> movements);

        void onLongClick(Category category);
    }

    public CategoryRecyclerAdapter(Context context, List<Category> categories, String rssPath, OnCategoryClicked categoryClicked) {
        super(categories);
        mContext = context;
        mRssPath = rssPath;
        mListener = categoryClicked;
    }


    @Override
    public void onBindViewHolder(final CategoryViewHolder holder, int position) {
        if (position >= getItems().size()) {
            holder.itemView.setVisibility(View.INVISIBLE);
            return;
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
        }
        Category category = get(position);
        holder.name.setText(category.getName());
        holder.color.setBackgroundColor(category.getPrimaryColor());
        holder.show.setChecked(category.isVisible());
        if (mRssPath == null) {
            holder.edit.setOnClickListener(new CategoryItemClickListener(category));
            holder.color.setOnClickListener(new CategoryItemClickListener(category));
            holder.show.setOnClickListener(new CategoryItemClickListener(category));
            holder.more.setOnClickListener(new CategoryItemClickListener(category));
            holder.drag.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        mItemTouchHelper.startDrag(holder);
                    }
                    return false;
                }
            });
        } else {
            holder.edit.setVisibility(View.GONE);
            holder.drag.setVisibility(View.GONE);
            holder.show.setVisibility(View.GONE);
            holder.more.setVisibility(View.GONE);
        }
        holder.itemView.setOnLongClickListener(new CategoryItemLongClickListener(category));
        holder.itemView.setOnClickListener(new CategoryItemRSSClickListener(category));
        setBackground(holder.itemView);
    }

    private void setBackground(View view) {
        Utilities.setPressedColorRippleDrawable(ContextCompat.getColor(mContext, R.color.list_background), PrefUtilities.getInstance().getCurrentColor(), view);
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_modify_item, parent, false);
        return new CategoryViewHolder(itemView);
    }

    private class CategoryItemRSSClickListener implements View.OnClickListener {
        private final Category category;

        CategoryItemRSSClickListener(Category category) {
            this.category = category;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null && mRssPath != null) {
                mListener.onRSSSavedClick(category, mRssPath);
            }
        }
    }

    private class CategoryItemLongClickListener implements View.OnLongClickListener {
        private final Category category;

        CategoryItemLongClickListener(Category category) {
            this.category = category;
        }

        @Override
        public boolean onLongClick(View v) {
            mListener.onLongClick(category);
            return true;
        }
    }

    private class CategoryItemClickListener implements View.OnClickListener {
        private final Category category;

        CategoryItemClickListener(Category category) {
            this.category = category;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.saveMovement(movements);
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

    class CategoryViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        final TextView name;
        final ImageView color;
        final ImageView more;
        final CheckBox show;
        final ImageView edit;
        final GripView drag;

        CategoryViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            color = (ImageView) itemView.findViewById(R.id.color);
            more = (ImageView) itemView.findViewById(R.id.more);
            show = (CheckBox) itemView.findViewById(R.id.show);
            edit = (ImageView) itemView.findViewById(R.id.edit);
            drag = (GripView) itemView.findViewById(R.id.drag);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(ColorManager.moreAlpha(PrefUtilities.getInstance().getCurrentColor(), 90));
        }

        @Override
        public void onItemClear() {
            setBackground(itemView);
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 2;
    }
}