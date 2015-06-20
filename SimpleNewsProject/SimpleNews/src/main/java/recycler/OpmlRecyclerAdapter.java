package recycler;

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

public class OpmlRecyclerAdapter extends ChoiceModeRecyclerAdapter<OpmlRecyclerAdapter.OpmlViewHolder, Feed> {

    private Context mContext;

    public OpmlRecyclerAdapter(Context context, List<Feed> feeds, ChoiceModeListener listener) {
        super(feeds, listener);
        mContext = context;
    }

    @Override
    void onBindSelectedViewHolder(OpmlViewHolder holder, int position) {
        onBindNormalViewHolder(holder, position);
    }

    @Override
    void onBindNormalViewHolder(final OpmlViewHolder holder, final int position) {
        Feed feed = get(position);
        holder.name.setText(feed.getTitle() == null ? mContext.getString(R.string.feed_title_not_found) : feed.getTitle());
        holder.link.setText(feed.getXmlUrl());
        holder.checkBox.setChecked(isItemChecked(position));
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle(position);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                holder.checkBox.toggle();
                toggle(position);
                return false;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                holder.checkBox.toggle();
                toggle(position);
            }
        });
        int pad = mContext.getResources().getDimensionPixelSize(R.dimen.card_layout_padding);
        holder.itemView.setPadding(pad, pad, pad, pad);
    }

    @Override
    OpmlViewHolder onCreateNormalViewHolder(ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.opml_list_item, parent, false);
        return new OpmlViewHolder(itemView);
    }

    @Override
    OpmlViewHolder onCreateSelectedViewHolder(ViewGroup parent) {
        return onCreateNormalViewHolder(parent);
    }

    class OpmlViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView link;
        CheckBox checkBox;

        public OpmlViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.title);
            link = (TextView) itemView.findViewById(R.id.url);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }
    }

}
