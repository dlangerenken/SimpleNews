package de.dala.simplenews.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;


import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.utilities.ColorManager;
import de.dala.simplenews.utilities.PrefUtilities;
import de.dala.simplenews.utilities.Utilities;

public class OpmlRecyclerAdapter extends ChoiceModeRecyclerAdapter<OpmlRecyclerAdapter.OpmlViewHolder, Feed> {

    private final Context mContext;

    public OpmlRecyclerAdapter(Context context, List<Feed> feeds, ChoiceModeListener listener) {
        super(feeds, listener);
        mContext = context;
    }

    @Override
    void onBindSelectedViewHolder(OpmlViewHolder holder, int position) {
        onBindNormalViewHolder(holder, position);
        holder.itemView.setBackgroundColor(ColorManager.moreAlpha(PrefUtilities.getInstance().getCurrentColor(), 70));
    }

    @Override
    void onBindNormalViewHolder(final OpmlViewHolder holder, int position) {
        final Feed feed = get(position);
        holder.name.setText(feed.getTitle() == null ? mContext.getString(R.string.feed_title_not_found) : feed.getTitle());
        holder.link.setText(feed.getXmlUrl());
        holder.checkBox.setChecked(isItemChecked(position));
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle(feed);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                holder.checkBox.toggle();
                toggle(feed);
                return false;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                holder.checkBox.toggle();
                toggle(feed);
            }
        });
        int pad = mContext.getResources().getDimensionPixelSize(R.dimen.card_layout_padding);
        holder.itemView.setPadding(pad, pad, pad, pad);
        Utilities.setPressedColorRippleDrawable(mContext.getResources().getColor(R.color.list_background), PrefUtilities.getInstance().getCurrentColor(), holder.itemView);
    }

    @Override
    public OpmlViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.opml_list_item, parent, false);
        return new OpmlViewHolder(itemView);
    }

    class OpmlViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView link;
        final CheckBox checkBox;

        OpmlViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.title);
            link = (TextView) itemView.findViewById(R.id.url);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }
    }

}
