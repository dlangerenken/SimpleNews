package de.dala.simplenews.recycler;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.dala.simplenews.utilities.EmptyObservableRecyclerView;

public abstract class ChoiceModeRecyclerAdapter<VH extends RecyclerView.ViewHolder, Item extends Comparable> extends RecyclerView.Adapter<VH> {

    private List<Item> mItems;
    private final SparseBooleanArray mSelectedPositions = new SparseBooleanArray();
    private boolean mInSelectionMode = false;
    private static final String SELECTED_POSITIONS = "positions";
    private static final String SELECTING_MODE = "inSelectionMode";
    private final ChoiceModeListener mListener;

    public interface ChoiceModeListener {
        void startSelectionMode();

        void updateSelectionMode(int numberOfElements);

        void finishSelectionMode();
    }

    ChoiceModeRecyclerAdapter(List<Item> items, ChoiceModeListener listener) {
        super();
        mItems = items;
        mListener = listener;
    }

    private void setItemChecked(int position, boolean isChecked) {
        mSelectedPositions.put(position, isChecked);
        notifyItemChanged(position);
        updateSelection();
    }

    void toggle(Item item) {
        int index = indexOf(item);
        setItemChecked(index, !isItemChecked(index));
    }

    void toggleIfActionMode(Item item) {
        if (isInSelectionMode()) {
            toggle(item);
        }
    }

    private void updateSelection() {
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            if (mSelectedPositions.valueAt(i)) {
                setSelectionMode(true);
                return;
            }
        }
        setSelectionMode(false);
    }

    boolean isItemChecked(int key) {
        return mSelectedPositions.get(key);
    }

    private void setSelectionMode(boolean selectionMode) {
        if (mListener != null) {
            if (selectionMode && !mInSelectionMode) {
                mListener.startSelectionMode();
            }
            if (!selectionMode && mInSelectionMode) {
                mListener.finishSelectionMode();
            }
        }
        mInSelectionMode = selectionMode;
        if (mListener != null) {
            mListener.updateSelectionMode(getSelectedPositionsSize());
        }
    }

    private int getSelectedPositionsSize() {
        int sum = 0;
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            if (mSelectedPositions.valueAt(i)) {
                sum++;
            }
        }
        return sum;
    }

    boolean isInSelectionMode() {
        return mInSelectionMode;
    }

    private List<Integer> getSelectedPositions() {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            if (mSelectedPositions.valueAt(i)) {
                positions.add(mSelectedPositions.keyAt(i));
            }
        }
        return positions;
    }

    public List<Item> getSelectedItems() {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            if (mSelectedPositions.valueAt(i)) {
                items.add(mItems.get(mSelectedPositions.keyAt(i)));
            }
        }
        return items;
    }

    private void restoreSelections(List<Integer> selected) {
        if (selected == null) return;
        int position;
        mSelectedPositions.clear();
        for (int i = 0; i < selected.size(); i++) {
            position = selected.get(i);
            mSelectedPositions.put(position, true);
            update(get(position));
        }
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

    public void clearSelections() {
        List<Item> selectedItems = getSelectedItems();
        mSelectedPositions.clear();
        for (Item item : selectedItems) {
            update(item);
        }
        updateSelection();
    }

    public void setAllItemsSelected() {
        for (int i = 0; i < mItems.size(); i++) {
            setItemChecked(i, true);
        }
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
        List<Item> selectedItems = getSelectedItems();
        clearSelections();
        int index = indexOf(removingItem);
        if (index >= 0) {
            mItems.remove(removingItem);
            notifyItemRemoved(index);
        }
        for (Item item : mItems) {
            if (selectedItems.contains(item)) {
                toggle(item);
            }
        }
    }

    public void add(List<Item> newItems) {
        List<Item> selectedItems = getSelectedItems();
        clearSelections();
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
        for (Item item : mItems) {
            if (selectedItems.contains(item)) {
                toggle(item);
            }
        }
    }

    public void add(Item newItem) {
        List<Item> selectedItems = getSelectedItems();
        clearSelections();
        if (!mItems.contains(newItem)) {
            mItems.add(newItem);
            Collections.sort(mItems);
            int index = indexOf(newItem);
            notifyItemInserted(index);
        } else {
            int index = indexOf(newItem);
            notifyItemChanged(index);
        }
        for (Item item : mItems) {
            if (selectedItems.contains(item)) {
                toggle(item);
            }
        }
    }

    public Bundle saveSelectionStates() {
        Bundle states = new Bundle();
        states.putIntegerArrayList(SELECTED_POSITIONS, (ArrayList<Integer>) getSelectedPositions());
        states.putBoolean(SELECTING_MODE, mInSelectionMode);
        return states;
    }

    public void restoreSelectionStates(Bundle savedStates) {
        List<Integer> selectedPositions = savedStates.getIntegerArrayList(SELECTED_POSITIONS);
        mInSelectionMode = savedStates.getBoolean(SELECTING_MODE);
        restoreSelections(selectedPositions);
    }

    @Override
    public final void onBindViewHolder(VH holder, int position) {
        if (isItemChecked(position)) {
            onBindSelectedViewHolder(holder, position);
        } else {
            onBindNormalViewHolder(holder, position);
        }
    }

    abstract void onBindSelectedViewHolder(VH holder, int position);

    abstract void onBindNormalViewHolder(VH holder, int position);
}
