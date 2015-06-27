package recycler;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ChoiceModeRecyclerAdapter<VH extends RecyclerView.ViewHolder, Item extends Comparable> extends RecyclerView.Adapter<VH> {

    private List<Item> mItems;
    private SparseBooleanArray mSelectedPositions = new SparseBooleanArray();
    private boolean mInSelectionMode = false;
    private static final String SELECTED_POSITIONS = "positions";
    private static final String SELECTING_MODE = "inSelectionMode";
    private ChoiceModeListener mListener;

    public interface ChoiceModeListener {
        void startSelectionMode();

        void updateSelectionMode(int numberOfElements);

        void finishSelectionMode();
    }

    public ChoiceModeRecyclerAdapter(List<Item> items, ChoiceModeListener listener) {
        mItems = items;
        mListener = listener;
    }

    public void setItemChecked(int position, boolean isChecked) {
        mSelectedPositions.put(position, isChecked);
        notifyItemChanged(position);
        updateSelection();
    }

    public void toggle(Item item) {
        int index = indexOf(item);
        setItemChecked(index, !isItemChecked(index));
    }

    public void toggleIfActionMode(Item item) {
        if (isInSelectionMode()) {
            toggle(item);
        }
    }

    private void updateSelection() {
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            if (mSelectedPositions.valueAt(i)) {
                setSelectionMode(true);
                if (mListener != null) {
                    mListener.updateSelectionMode(mSelectedPositions.size());
                }
                return;
            }
        }
        setSelectionMode(false);
    }

    public boolean isItemChecked(int key) {
        return mSelectedPositions.get(key);
    }

    public void setSelectionMode(boolean selectionMode) {
        if (mListener != null) {
            if (selectionMode && !mInSelectionMode) {
                mListener.startSelectionMode();
            }
            if (!selectionMode && mInSelectionMode) {
                mListener.finishSelectionMode();
            }
        }
        mInSelectionMode = selectionMode;
    }

    public boolean isInSelectionMode() {
        return mInSelectionMode;
    }

    public List<Integer> getSelectedPositions() {
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

    public int indexOf(Item item) {
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
