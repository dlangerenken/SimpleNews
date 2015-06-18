package recycler;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class ChoiceModeRecyclerAdapter<VH extends RecyclerView.ViewHolder, Item> extends RecyclerView.Adapter<VH> {

    private List<Item> mItems;
    private SparseBooleanArray mSelectedPositions = new SparseBooleanArray();
    private boolean mInSelectionMode = false;
    private static final int SELECTED = 1;
    private static final int NORMAL = 0;
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

    public void toggle(int position) {
        setItemChecked(position, !isItemChecked(position));
    }

    public void toggleIfActionMode(int position) {
        if (isInSelectionMode()) {
            toggle(position);
        }
    }

    private void updateSelection() {
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            if (mSelectedPositions.get(i)) {
                setSelectionMode(true);
                return;
            }
        }
        setSelectionMode(false);
        if (mListener != null) {
            mListener.updateSelectionMode(mSelectedPositions.size());
        }
    }

    public boolean isItemChecked(int position) {
        return mSelectedPositions.get(position);
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
        }
        refreshHolders();
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
        mSelectedPositions.clear();
        refreshHolders();
        updateSelection();
    }

    public void setAllItemsSelected(){
        for (int i = 0; i < mItems.size(); i++){
            setItemChecked(i, true);
        }
    }

    private void refreshHolders() {
        notifyItemRangeChanged(0, getItemCount());
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
    public int getItemViewType(int position) {
        if (mSelectedPositions.get(position)) {
            return SELECTED;
        }
        return NORMAL;
    }

    @Override
    public final VH onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == SELECTED) {
            return onCreateSelectedViewHolder(parent);
        }
        return onCreateNormalViewHolder(parent);
    }

    @Override
    public final void onBindViewHolder(VH holder, int position) {
        if (getItemViewType(position) == SELECTED) {
            onBindSelectedViewHolder(holder, position);
        }
        onBindNormalViewHolder(holder, position);
    }

    abstract void onBindSelectedViewHolder(VH holder, int position);

    abstract void onBindNormalViewHolder(VH holder, int position);

    abstract VH onCreateNormalViewHolder(ViewGroup parent);

    abstract VH onCreateSelectedViewHolder(ViewGroup parent);

}
