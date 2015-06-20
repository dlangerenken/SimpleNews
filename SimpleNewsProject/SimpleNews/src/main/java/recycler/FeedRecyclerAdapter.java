package recycler;

import android.app.Activity;
import android.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.utilities.LightAlertDialog;
import de.dala.simplenews.utilities.UpdatingFeedTask;

public class FeedRecyclerAdapter extends ChoiceModeRecyclerAdapter<FeedRecyclerAdapter.FeedViewHolder, Feed> implements UpdatingFeedTask.UpdatingFeedListener {

    private Activity mContext;
    private Category mCategory;

    public FeedRecyclerAdapter(Activity context, Category category, ChoiceModeListener listener) {
        super(category.getFeeds(), listener);
        mContext = context;
        mCategory = category;
    }


    @Override
    void onBindNormalViewHolder(FeedViewHolder holder, final int position) {
        Feed feed = get(position);
        holder.name.setText(feed.getTitle() == null ? mContext.getString(R.string.feed_title_not_found) : feed.getTitle());
        holder.link.setText(feed.getXmlUrl());
        holder.show.setOnClickListener(new FeedItemClickListener(feed));
        holder.show.setChecked(feed.isVisible());
        holder.edit.setOnClickListener(new FeedItemClickListener(feed));

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toggle(position);
                return false;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleIfActionMode(position);
            }
        });
        holder.itemView.setBackgroundResource(R.drawable.card_background_white);
        int pad = mContext.getResources().getDimensionPixelSize(R.dimen.card_layout_padding);
        holder.itemView.setPadding(pad, pad, pad, pad);
    }

    @Override
    FeedViewHolder onCreateNormalViewHolder(ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_modify_item, parent, false);
        return new FeedViewHolder(itemView);
    }

    @Override
    FeedViewHolder onCreateSelectedViewHolder(ViewGroup parent) {
        return onCreateNormalViewHolder(parent);
    }

    @Override
    void onBindSelectedViewHolder(FeedViewHolder holder, int position) {
        onBindNormalViewHolder(holder, position);
        holder.itemView.setBackgroundResource(R.drawable.card_background_blue);
    }

    class FeedViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView link;
        CheckBox show;
        ImageView edit;

        public FeedViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name);
            link = (TextView) itemView.findViewById(R.id.link);
            show = (CheckBox) itemView.findViewById(R.id.show);
            edit = (ImageView) itemView.findViewById(R.id.edit);
        }
    }

    class FeedItemClickListener implements View.OnClickListener {
        private Feed feed;

        public FeedItemClickListener(Feed feed) {
            this.feed = feed;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.edit:
                    editClicked(feed);
                    break;
                case R.id.show:
                    boolean visible = !feed.isVisible();
                    feed.setVisible(visible);
                    DatabaseHandler.getInstance().updateFeed(feed);
                    break;
            }
        }
    }

    private void editClicked(final Feed feed) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.check_valid_rss_dialog, null);
        final ViewGroup inputLayout = (ViewGroup) view.findViewById(R.id.inputLayout);
        final View progress = view.findViewById(R.id.m_progress);
        final Button positive = (Button) inputLayout.findViewById(R.id.positive);
        final Button negative = (Button) inputLayout.findViewById(R.id.negative);
        final EditText input = (EditText) inputLayout.findViewById(R.id.input);

        final AlertDialog dialog = LightAlertDialog.Builder.create(mContext).setView(view).setTitle(R.string.rename_feed).create();

        input.setText(feed.getXmlUrl());
        View.OnClickListener dialogClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View clickedView) {
                switch (clickedView.getId()) {
                    case R.id.positive:
                        new UpdatingFeedTask(mContext, view, inputLayout, progress, dialog,
                                feed.getId(), FeedRecyclerAdapter.this, mCategory).execute(input.getText().toString());
                        break;
                    case R.id.negative:
                        dialog.dismiss();
                        break;
                }
            }
        };

        positive.setOnClickListener(dialogClickListener);
        negative.setOnClickListener(dialogClickListener);
        dialog.show();
    }

    public void removeFeeds(List<Feed> selectedFeeds) {
        remove(selectedFeeds);
        for (Feed feed : selectedFeeds) {
            DatabaseHandler.getInstance().removeFeeds(null, feed.getId(), false);
        }
    }

    @Override
    public void onFeedLoaded(Feed feed) {
        add(feed);
    }

    @Override
    public void onFeedUpdated(Feed feed) {
        update(feed);
    }
}
