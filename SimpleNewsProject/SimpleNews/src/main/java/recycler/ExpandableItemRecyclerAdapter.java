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


public class ExpandableItemRecyclerAdapter extends ChoiceModeRecyclerAdapter<ExpandableItemRecyclerAdapter.EntryViewHolder, Entry> {
    private Category mCategory;
    private Context mContext;
    private RecyclerView mRecyclerView;

    public void removeOldEntries(List<Entry> newEntries) {
        Iterator<Entry> iterator = getItems().iterator();
        while (iterator.hasNext()) {
            Entry next = iterator.next();
            if (!newEntries.contains(next)) {
                int index = indexOf(next);
                iterator.remove();
                notifyItemRemoved(index);
            }
        }
    }

    public void addNewEntries(List<Entry> entries) {
        if (getItems() == null) {
            setItems(new ArrayList<Entry>());
        }

        List<Entry> addedEntries = new ArrayList<>();
        for (Entry next : entries) {
            if (!getItems().contains(next)) {
                getItems().add(next);
                addedEntries.add(next);
            }
        }

        Collections.sort(getItems());
        for (Entry addedEntry : addedEntries) {
            int index = indexOf(addedEntry);
            notifyItemInserted(index);
        }
    }

    public void addNewEntriesAndRemoveOld(List<Entry> entries) {
        addNewEntries(entries);
        removeOldEntries(entries);
    }

    public void remove(Set<Entry> selectedEntries) {
        List<Entry> diff = new ArrayList<>(getItems());
        diff.removeAll(selectedEntries);
        removeOldEntries(diff);
    }

    public void refresh(Set<Entry> selectedEntries) {
        for (Entry entry : selectedEntries) {
            notifyItemChanged(indexOf(entry));
        }
    }


    public interface ItemClickListener {
        void onItemClick(Entry entry);
    }

    private ItemClickListener mItemClickListener;
    private Set<Entry> mExpandedItemIds;

    public ExpandableItemRecyclerAdapter(List<Entry> entries, Category category, Context context, ItemClickListener itemClickListener, RecyclerView recyclerView, ChoiceModeListener listener) {
        super(entries, listener);
        mCategory = category;
        mContext = context;
        mItemClickListener = itemClickListener;
        mRecyclerView = recyclerView;
        mExpandedItemIds = new HashSet<>();
    }

    @Override
    void onBindSelectedViewHolder(EntryViewHolder holder, int position) {
        onBindNormalViewHolder(holder, position);
        holder.itemView.setBackgroundResource(R.color.list_background_selected);
    }

    @Override
    void onBindNormalViewHolder(final EntryViewHolder holder, int position) {
        final Entry currentEntry = get(position);

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
        holder.itemView.setBackgroundResource(R.color.list_background);
        int pad = mContext.getResources().getDimensionPixelSize(R.dimen.card_layout_padding);
        holder.itemView.setPadding(pad, pad, pad, pad);

        /* click listener */
        holder.clickListener = new EntryViewHolder.ClickListener() {
            @Override
            public void onSelectItem() {
                toggle(indexOf(currentEntry));
            }

            @Override
            public void onTitleClicked() {
                if (!isInSelectionMode()) {
                    toggleExpandingElement(currentEntry, holder.contentLayout);
                } else {
                    toggleIfActionMode(indexOf(currentEntry));
                }
            }

            @Override
            public void onDescriptionClicked() {
                if (!isInSelectionMode()) {
                    mItemClickListener.onItemClick(currentEntry);
                } else {
                    toggleIfActionMode(indexOf(currentEntry));
                }
            }
        };
    }

    @Override
    EntryViewHolder onCreateNormalViewHolder(ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_card_layout, parent, false);
        return new EntryViewHolder(itemView);
    }

    @Override
    EntryViewHolder onCreateSelectedViewHolder(ViewGroup parent) {
        return onCreateNormalViewHolder(parent);
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
                    if (clickListener != null) {
                        clickListener.onSelectItem();
                        return true;
                    }
                    return false;
                }
            });
            titleLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onTitleClicked();
                    }
                }
            });
            contentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
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

    private void toggleExpandingElement(Entry entry, final View contentParent) {
        if (mRecyclerView != null) {
            boolean isVisible = contentParent.getVisibility() == View.VISIBLE;
            if (isVisible) {
                collapse(entry, contentParent);
            } else {
                expand(entry, contentParent);
            }
        }
    }

    private void expand(Entry entry, final View contentParent) {
        ExpandCollapseHelper.animateExpanding(contentParent, mRecyclerView);
        mExpandedItemIds.add(entry);
    }

    private void collapse(Entry entry, final View contentParent) {
        ExpandCollapseHelper.animateCollapsing(contentParent);
        mExpandedItemIds.remove(entry);
    }
}
