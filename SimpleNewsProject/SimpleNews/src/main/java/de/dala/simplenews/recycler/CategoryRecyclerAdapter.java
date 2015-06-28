package de.dala.simplenews.recycler;

import android.content.Context;
import android.graphics.Color;
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
import de.dala.simplenews.utilities.EmptyObservableRecyclerView;
import de.dala.simplenews.utilities.GripView;
import de.dala.simplenews.utilities.ItemTouchHelperAdapter;
import de.dala.simplenews.utilities.ItemTouchHelperCallback;
import de.dala.simplenews.utilities.ItemTouchHelperViewHolder;
import de.dala.simplenews.utilities.Movement;

public class CategoryRecyclerAdapter extends ChoiceModeRecyclerAdapter<CategoryRecyclerAdapter.CategoryViewHolder, Category> implements ItemTouchHelperAdapter {
    private Context mContext;
    private String mRssPath;
    private OnCategoryClicked mListener;
    private ItemTouchHelper mItemTouchHelper;
    private List<Movement> movements;

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Category category = getItems().remove(fromPosition);
       // getItems().add(toPosition > fromPosition ? toPosition - 1 : toPosition, category);
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
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(this, false, false);
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
    void onBindNormalViewHolder(final CategoryViewHolder holder, int position) {
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
        holder.itemView.setBackgroundColor(Color.WHITE);
        holder.itemView.setOnLongClickListener(new CategoryItemLongClickListener(category));
        holder.itemView.setOnClickListener(new CategoryItemRSSClickListener(category));
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
            if (mListener != null && mRssPath != null) {
                mListener.onRSSSavedClick(category, mRssPath);
            } else {
                toggleIfActionMode(category);
            }
        }
    }

    class CategoryItemLongClickListener implements View.OnLongClickListener {
        private Category category;

        public CategoryItemLongClickListener(Category category) {
            this.category = category;
        }

        @Override
        public boolean onLongClick(View v) {
            toggle(category);
            return true;
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
        TextView name;
        ImageView color;
        ImageView more;
        CheckBox show;
        ImageView edit;
        GripView drag;

        public CategoryViewHolder(View itemView) {
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
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(Color.WHITE);
        }
    }
}