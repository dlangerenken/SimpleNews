package recycler;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ocpsoft.pretty.time.PrettyTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Entry;
import de.dala.simplenews.utilities.ExpandCollapseHelper;
import de.dala.simplenews.utilities.UIUtils;


public class ExpandableItemRecyclerAdapter extends RecyclerView.Adapter<ExpandableItemRecyclerAdapter.EntryViewHolder>{
    private List<Entry> mEntries;
    private Category mCategory;
    private Context mContext;
    private RecyclerView mRecyclerView;

    public int getCount() {
        return mEntries.size();
    }

    public void updateEntries(List<Entry> entries) {
        if (mEntries == null) {
            mEntries = new ArrayList<>();
        }

        Iterator<Entry> iterator = mEntries.iterator();
        while (iterator.hasNext()) {
            Entry next = iterator.next();
            if (!entries.contains(next)) {
                int index = mEntries.indexOf(next);
                iterator.remove();
                notifyItemRemoved(index);
            }
        }
        iterator = entries.iterator();
        while (iterator.hasNext()) {
            Entry next = iterator.next();
            if (!mEntries.contains(next)) {
                mEntries.add(next);
                Collections.sort(mEntries);
                int index = mEntries.indexOf(next);
                notifyItemInserted(index);
            }
        }
    }

    public void remove(Set<Entry> selectedEntries) {
        List<Entry> diff = new ArrayList<>(mEntries);
        diff.removeAll(selectedEntries);
        mSelectedItemIds.removeAll(selectedEntries);
        updateEntries(diff);
    }

    public void refresh(Set<Entry> selectedEntries) {
        for(Entry entry : selectedEntries){
            notifyItemChanged(mEntries.indexOf(entry));
        }
    }


    public interface ItemClickListener {
        void onItemClick(Entry entry);
        void updateActionMode();
    }

    private ItemClickListener mItemClickListener;
    private Set<Entry> mSelectedItemIds;
    private Set<Entry> mExpandedItemIds;

    public ExpandableItemRecyclerAdapter(List<Entry> entries, Category category, Context context, ItemClickListener itemClickListener, RecyclerView recyclerView){
        mEntries = entries;
        mCategory = category;
        mContext = context;
        mItemClickListener = itemClickListener;
        mRecyclerView = recyclerView;
        mSelectedItemIds = new HashSet<>();
        mExpandedItemIds = new HashSet<>();
    }

    @Override
    public EntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_card_layout, parent, false);
        EntryViewHolder viewHolder = new EntryViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final EntryViewHolder holder, final int position) {
        final Entry currentEntry = mEntries.get(position);

        long current = Math.min(new Date().getTime(), currentEntry.getDate());
        String formattedDate = new PrettyTime().format(new Date(current));

        /* title */
        holder.infoTextView.setText(String.format("%s - %s", currentEntry.getSrcName(), formattedDate));
        holder.infoTextView.setTextColor(mCategory.getSecondaryColor());
        holder.infoTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        UIUtils.setTextMaybeHtml(holder.titleTextView, currentEntry.getTitle());
        setImageDrawable(holder.imageView, currentEntry);

        /* content */
        holder.colorBorderView.setBackgroundColor(mCategory.getSecondaryColor());
        UIUtils.setTextMaybeHtml(holder.descriptionTextView, currentEntry.getDescription());

        /* general */
        holder.itemView.setBackgroundResource(mSelectedItemIds.contains(currentEntry) ? R.color.list_background_selected : R.color.list_background);
        int pad = mContext.getResources().getDimensionPixelSize(R.dimen.card_layout_padding);
        holder.itemView.setPadding(pad, pad, pad, pad);

        /* click listener */
        holder.clickListener = new EntryViewHolder.ClickListener() {
            @Override
            public void onSelectItem() {
                onListItemCheck(currentEntry);
                mItemClickListener.updateActionMode();
            }

            @Override
            public void onTitleClicked() {
                if (mSelectedItemIds.isEmpty()) {
                    toggle(currentEntry, holder.contentLayout);
                }else{
                    onListItemCheck(currentEntry);
                    mItemClickListener.updateActionMode();
                }
            }

            @Override
            public void onDescriptionClicked() {
                if (mSelectedItemIds.isEmpty()) {
                    mItemClickListener.onItemClick(currentEntry);
                }else{
                    onListItemCheck(currentEntry);
                    mItemClickListener.updateActionMode();
                }
            }
        };
    }

    @Override
    public int getItemCount() {
        return mEntries.size();
    }

    static class EntryViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout titleLayout;
        public LinearLayout contentLayout;

        /* Content */
        public View colorBorderView;
        public TextView descriptionTextView;

        /* Title */
        public ImageView imageView;
        public TextView titleTextView;
        public TextView infoTextView;

        interface ClickListener {
            void onSelectItem();
            void onTitleClicked();
            void onDescriptionClicked();
        }

        public ClickListener clickListener;


        public EntryViewHolder(View itemView) {
            super(itemView);

            titleLayout = (LinearLayout) itemView.findViewById(R.id.card_title);
            contentLayout = (LinearLayout) itemView.findViewById(R.id.card_content);

            imageView = (ImageView) titleLayout.findViewById(R.id.image);
            titleTextView = (TextView) titleLayout.findViewById(R.id.title);
            infoTextView = (TextView) titleLayout.findViewById(R.id.info);

            colorBorderView = contentLayout.findViewById(R.id.color_border);
            descriptionTextView = (TextView) contentLayout.findViewById(R.id.content_description);

            titleLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (clickListener != null){
                        clickListener.onSelectItem();
                        return true;
                    }
                    return false;
                }
            });
            titleLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null){
                        clickListener.onTitleClicked();
                    }
                }
            });
            contentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null){
                        clickListener.onDescriptionClicked();
                    }
                }
            });
        }
    }

    private void setImageDrawable(ImageView entryType, Entry entry) {
        Drawable drawable = null;
        if (entry.getFavoriteDate() != null && entry.getFavoriteDate() > 0) {
            drawable = mContext.getResources().getDrawable(R.drawable.ic_favorite);
        } else if (entry.getVisitedDate() != null && entry.getVisitedDate() > 0) {
            drawable = mContext.getResources().getDrawable(R.drawable.ic_recent);
        }
        entryType.setImageDrawable(drawable);
    }

    private void onListItemCheck(Entry entry) {
        toggleSelection(entry);
    }

    public void selectAllIds(){
        for (int i = 0; i < mEntries.size(); i++){
            onListItemCheck(i, true);
        }
        mItemClickListener.updateActionMode();
    }

    public void deselectAllIds(){
        List<Entry> entriesToRemove = new ArrayList<>(mSelectedItemIds);
        for (Entry entry : entriesToRemove){
            mSelectedItemIds.remove(entry);
            notifyItemChanged(mEntries.indexOf(entry));
        }
        mItemClickListener.updateActionMode();
    }

    private void onListItemCheck(int position, boolean value) {
        selectView(mEntries.get(position), value);
    }

    public void toggleSelection(Entry entry) {
        selectView(entry, !mSelectedItemIds.contains(entry));
    }


    public void selectView(Entry entry, boolean value) {
        if (value) {
            mSelectedItemIds.add(entry);
        } else {
            mSelectedItemIds.remove(entry);
        }
        notifyItemChanged(mEntries.indexOf(entry));
    }

    public int getSelectedCount() {
        return mSelectedItemIds.size();
    }

    public Set<Entry> getSelectedIds() {
        return mSelectedItemIds;
    }

    private void toggle(Entry entry, final View contentParent) {
        if (mRecyclerView != null) {
            boolean isVisible = contentParent.getVisibility() == View.VISIBLE;
            if (isVisible) {
                collapse(entry, contentParent);
            } else {
                expand(entry, contentParent);
            }
        }
    }

    private void expand(Entry entry, final View contentParent){
        ExpandCollapseHelper.animateExpanding(contentParent, mRecyclerView);
        mExpandedItemIds.add(entry);
    }

    private void collapse(Entry entry, final View contentParent){
        ExpandCollapseHelper.animateCollapsing(contentParent);
        mExpandedItemIds.remove(entry);
    }
}
