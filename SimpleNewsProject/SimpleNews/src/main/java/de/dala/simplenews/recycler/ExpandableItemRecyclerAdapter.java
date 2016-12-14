package de.dala.simplenews.recycler;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
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
import de.dala.simplenews.utilities.PrefUtilities;
import de.dala.simplenews.utilities.Utilities;


public class ExpandableItemRecyclerAdapter extends BaseRecyclerAdapter<ExpandableItemRecyclerAdapter.EntryViewHolder, Entry> {
    private final Category mCategory;
    private final Context mContext;
    private final RecyclerView mRecyclerView;

    private final ItemClickListener mItemClickListener;
    private final Set<Entry> mExpandedItemIds;
    private boolean shouldMarkUnreadEntries;

    public interface ItemClickListener {

        void onOpenClick(Entry entry);

        void onLongClick(Entry entry);

        void onExpandedClick(Entry currentEntry);
    }

    public ExpandableItemRecyclerAdapter(List<Entry> entries, Category category, Context context, ItemClickListener itemClickListener, RecyclerView recyclerView) {
        super(entries);
        mCategory = category;
        mContext = context;
        mItemClickListener = itemClickListener;
        mRecyclerView = recyclerView;
        mExpandedItemIds = new HashSet<>();
        shouldMarkUnreadEntries = PrefUtilities.getInstance().shouldMarkUnreadEntries();
    }

    @Override
    public void onBindViewHolder(EntryViewHolder holder, int position) {
        if (position >= getItems().size()) {
            holder.itemView.setVisibility(View.INVISIBLE);
            return;
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
        }
        Entry currentEntry = get(position);
        int secondaryColor = mCategory.getSecondaryColor();

        String formattedDate = "";
        if (currentEntry.getDate() != null) {
            long current = Math.min(new Date().getTime(), currentEntry.getDate());
            formattedDate = new PrettyTime().format(new Date(current));
        }

        /* title */
        holder.infoTextView.setText(String.format("%s - %s", currentEntry.getSrcName(), formattedDate));
        holder.infoTextView.setTextColor(secondaryColor);
        holder.titleTextView.setText(currentEntry.getTitle());

        if (shouldMarkUnreadEntries && currentEntry.isUnseen()) {
            holder.titleTextView.setTypeface(null, Typeface.BOLD);
        } else {
            holder.titleTextView.setTypeface(null, Typeface.NORMAL);
        }
        setImageDrawable(holder.imageView, currentEntry);

        holder.colorBorderView.setBackgroundColor(secondaryColor);
        String description = currentEntry.getDescription();
        if (description == null || "".equals(description)) {
            description = mContext.getString(R.string.no_description_available);
        }
        holder.descriptionTextView.setText(description);

        /* click listener */
        holder.clickListener = new EntryClickListener(currentEntry, holder);
        if (mExpandedItemIds.contains(currentEntry)) {
            expand(currentEntry, holder.contentLayout, false);
        } else {
            collapse(currentEntry, holder.contentLayout, false);
            Utilities.setPressedColorRippleDrawable(ContextCompat.getColor(mContext, R.color.list_background), PrefUtilities.getInstance().getCurrentColor(), holder.itemView);
        }
    }

    @Override
    public void update(Entry entry) {
        if (shouldMarkUnreadEntries) {
            super.update(entry);
        }
    }

    private class EntryClickListener implements EntryViewHolder.ClickListener {
        private Entry mEntry;
        private EntryViewHolder mViewHolder;

        EntryClickListener(Entry entry, EntryViewHolder viewHolder) {
            mEntry = entry;
            mViewHolder = viewHolder;
        }

        @Override
        public void onSelectItem() {
            if (mItemClickListener != null) {
                mItemClickListener.onLongClick(mEntry);
            }
        }

        @Override
        public void onTitleClick() {
            toggleExpandingElement(mEntry, mViewHolder.contentLayout);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mItemClickListener != null) {
                        mItemClickListener.onExpandedClick(mEntry);
                    }
                }
            }, 300);
        }

        @Override
        public void onDescriptionClick() {
            if (mItemClickListener != null) {
                mItemClickListener.onOpenClick(mEntry);
            }
        }
    }

    @Override
    public EntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new EntryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.news_card_layout, parent, false));
    }

    static class EntryViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout titleLayout;
        final LinearLayout contentLayout;
        final View mainContent;

        /* Content */
        final View colorBorderView;
        final TextView descriptionTextView;

        /* Title */
        final ImageView imageView;
        final TextView titleTextView;
        final TextView infoTextView;

        interface ClickListener {
            void onSelectItem();

            void onTitleClick();

            void onDescriptionClick();
        }

        ClickListener clickListener;


        EntryViewHolder(View itemView) {
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
            drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_fav_color);
        } else if (entry.getVisitedDate() != null && entry.getVisitedDate() > 0) {
            drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_seen_color);
        }
        if (drawable != null) {
            entryType.setVisibility(View.VISIBLE);
            entryType.setImageDrawable(drawable);
        } else {
            entryType.setVisibility(View.GONE);
        }
    }


    private void toggleExpandingElement(Entry entry, final View contentParent) {
        if (mRecyclerView != null) {
            if (contentParent.getVisibility() == View.VISIBLE) {
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
