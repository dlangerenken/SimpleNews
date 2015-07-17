package de.dala.simplenews.recycler;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

        void onOpenClick(Entry entry);

        void onLongClick(Entry entry);
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

        /* click listener */
        holder.clickListener = new EntryViewHolder.ClickListener() {
            @Override
            public void onSelectItem() {
                //toggle(currentEntry);
                if (mItemClickListener != null) {
                    mItemClickListener.onLongClick(currentEntry);
                }
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
                    if (mItemClickListener != null) {
                        mItemClickListener.onOpenClick(currentEntry);
                    }
                } else {
                    toggleIfActionMode(currentEntry);
                }
            }
        };
        if (mExpandedItemIds.contains(currentEntry)) {
            expand(currentEntry, holder.contentLayout, false);
        } else {
            collapse(currentEntry, holder.contentLayout, false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.itemView.setClickable(true);
            RippleDrawable drawable = Utilities.getPressedColorRippleDrawable(mContext.getResources().getColor(R.color.list_background), mCategory.getPrimaryColor());
            holder.itemView.setBackground(drawable);
        } else {
            holder.itemView.setBackgroundResource(mContext.getResources().getColor(R.color.list_background));
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
        public View mainContent;

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
        }

        public ClickListener clickListener;


        public EntryViewHolder(View itemView) {
            super(itemView);
            titleLayout = (LinearLayout) itemView.findViewById(R.id.card_title);
            contentLayout = (LinearLayout) itemView.findViewById(R.id.card_content);
            mainContent = itemView.findViewById(R.id.main_content);

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
            contentLayout.setOnTouchListener(new OnTouch());
            titleLayout.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (clickListener != null) {
                                clickListener.onTitleClick();
                            }
                        }
                    });
            titleLayout.setOnTouchListener(new OnTouch());

        }

        private class OnTouch implements View.OnTouchListener {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Convert to card view coordinates. Assumes the host view is
                // a direct child and the card view is not scrollable.
                float x = event.getX() + v.getLeft();
                float y = event.getY() + v.getTop();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Simulate motion on the card view.
                    //itemView.onTouchEvent(event);
                    itemView.drawableHotspotChanged(x, y);
                }

                // Simulate pressed state on the card view.
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        itemView.setPressed(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        itemView.setPressed(false);
                        break;
                }

                // Pass all events through to the host view.
                return false;
            }
        }
    }

    private void setImageDrawable(ImageView entryType, Entry entry) {
        Drawable drawable = null;
        if (entry.getFavoriteDate() != null && entry.getFavoriteDate() > 0) {
            drawable = mContext.getResources().getDrawable(R.drawable.ic_fav_color);
        } else if (entry.getVisitedDate() != null && entry.getVisitedDate() > 0) {
            drawable = mContext.getResources().getDrawable(R.drawable.ic_seen_color);
        }
        entryType.setImageDrawable(drawable);
    }

    private void toggleExpandingElement(Entry entry, final View contentParent) {
        if (mRecyclerView != null) {
            boolean isVisible = contentParent.getVisibility() == View.VISIBLE;
            if (isVisible) {
                collapse(entry, contentParent, true);
            } else {
                expand(entry, contentParent, true);
            }
        }
    }

    private void expand(Entry entry, final View contentParent, boolean animated) {
        ExpandCollapseHelper.animateExpanding(contentParent, mRecyclerView, animated);
        mExpandedItemIds.add(entry);
    }

    private void collapse(Entry entry, final View contentParent, boolean animated) {
        ExpandCollapseHelper.animateCollapsing(contentParent, animated);
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
