package recycler;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ocpsoft.pretty.time.PrettyTime;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Entry;
import de.dala.simplenews.utilities.ExpandCollapseHelper;
import de.dala.simplenews.utilities.UIUtils;
import de.dala.simplenews.utilities.Utilities;


public class ExpandableItemRecyclerAdapter extends ChoiceModeRecyclerAdapter<ExpandableItemRecyclerAdapter.EntryViewHolder, Entry> {
    private Category mCategory;
    private Context mContext;
    private RecyclerView mRecyclerView;

    private ItemClickListener mItemClickListener;
    private Set<Entry> mExpandedItemIds;

    public interface ItemClickListener {
        void onSaveClick(Entry entry);

        void onOpenClick(Entry entry);
    }

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
    void onBindNormalViewHolder(final EntryViewHolder holder, final int position) {
        final Entry currentEntry = get(position);

        long current = Math.min(new Date().getTime(), currentEntry.getDate());
        String formattedDate = new PrettyTime().format(new Date(current));

        /* title */
        holder.infoTextView.setText(String.format("%s - %s", currentEntry.getSrcName(), formattedDate));
        holder.infoTextView.setTextColor(mCategory.getSecondaryColor());
        holder.infoTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        UIUtils.setTextMaybeHtml(holder.titleTextView, currentEntry.getTitle());
        setImageDrawable(holder.imageView, currentEntry);

        int secondaryColor = mCategory.getSecondaryColor();
        holder.colorBorderView.setBackgroundColor(secondaryColor);
        holder.colorBorderView.setBackgroundColor(secondaryColor);
        UIUtils.setTextMaybeHtml(holder.descriptionTextView, currentEntry.getDescription());

        /* general */
        holder.itemView.setBackgroundResource(R.color.list_background);

        /* click listener */
        holder.clickListener = new EntryViewHolder.ClickListener() {
            @Override
            public void onSelectItem() {
                toggle(currentEntry);
            }

            @Override
            public void onTitleClick() {
                if (!isInSelectionMode()) {
                    toggleExpandingElement(currentEntry, holder.contentLayout);
                } else {
                    toggleIfActionMode(currentEntry);
                }
            }

            @Override
            public void onDescriptionClick() {
                if (!isInSelectionMode()) {
                    mItemClickListener.onOpenClick(currentEntry);
                } else {
                    toggleIfActionMode(currentEntry);
                }
            }


            @Override
            public void onSaveClick() {
                if (!isInSelectionMode()) {
                    mItemClickListener.onSaveClick(currentEntry);
                }
            }
        };
        if (mExpandedItemIds.contains(currentEntry)) {
            expand(currentEntry, holder.contentLayout);
        } else {
            collapse(currentEntry, holder.contentLayout);
        }
    }

    @Override
    public EntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_card_layout, parent, false);
        return new EntryViewHolder(itemView);
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

            void onTitleClick();

            void onDescriptionClick();

            void onSaveClick();
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
            contentLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (clickListener != null) {
                        clickListener.onSelectItem();
                        return true;
                    }
                    return false;
                }
            });

            contentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onDescriptionClick();
                    }
                }
            });
            titleLayout.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (clickListener != null) {
                                clickListener.onTitleClick();
                            }
                        }
                    }

            );
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


    public void removeOldEntries(List<Entry> newEntries) {
        remove(Utilities.nonIntersection(getItems(), newEntries));
    }

    public void addNewEntriesAndRemoveOld(List<Entry> entries) {
        add(entries);
        removeOldEntries(entries);
    }
}
