package de.dala.simplenews.recycler;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseRecyclerAdapter<VH extends RecyclerView.ViewHolder, Item extends Comparable> extends RecyclerView.Adapter<VH> {

    private List<Item> mItems;


    BaseRecyclerAdapter(List<Item> items) {
        super();
        mItems = items;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public Item get(int index) {
        return getItems().get(index);
    }

    private int indexOf(Item item) {
        return getItems().indexOf(item);
    }

    public List<Item> getItems() {
        return mItems;
    }

    public void setItems(List<Item> items) {
        mItems = items;
    }

    public void update(List<Item> updateItems) {
        for (Item item : updateItems) {
            update(item);
        }
    }

    public void update(Item item) {
        notifyItemChanged(indexOf(item));
    }


    public void remove(List<Item> oldItems) {
        for (Item item : oldItems) {
            remove(item);
        }
    }

    public void remove(Item removingItem) {
        int index = indexOf(removingItem);
        if (index >= 0) {
            mItems.remove(removingItem);
            notifyItemRemoved(index);
        }
    }

    public void add(List<Item> newItems) {
        List<Item> addedItems = new ArrayList<>();
        for (Item item : newItems) {
            if (!mItems.contains(item)) {
                addedItems.add(item);
                mItems.add(item);
            }
        }
        Collections.sort(mItems);
        for (Item addedItem : addedItems) {
            int index = indexOf(addedItem);
            notifyItemInserted(index);
        }
    }

    public void add(Item newItem) {
        if (!mItems.contains(newItem)) {
            mItems.add(newItem);
            Collections.sort(mItems);
            int index = indexOf(newItem);
            notifyItemInserted(index);
        } else {
            int index = indexOf(newItem);
            notifyItemChanged(index);
        }
    }
}
