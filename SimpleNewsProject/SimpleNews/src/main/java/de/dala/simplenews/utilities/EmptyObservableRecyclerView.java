package de.dala.simplenews.utilities;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class EmptyObservableRecyclerView extends RecyclerView {
    private static final int EMPTY = 0;
    private View emptyView;
    final private RecyclerView.AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    };

    public EmptyObservableRecyclerView(Context context) {
        super(context);
    }

    public EmptyObservableRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmptyObservableRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void checkIfEmpty() {
        if (emptyView != null) {
            if (getAdapter().getItemCount() == EMPTY) {
                emptyView.animate().alpha(1).setDuration(500).start();
            } else {
                emptyView.animate().alpha(0).setDuration(100).start();
            }
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }
        checkIfEmpty();
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
    }
}
