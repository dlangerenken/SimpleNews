package de.dala.simplenews.recycler;

import android.app.Activity;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.utilities.PrefUtilities;
import de.dala.simplenews.utilities.UpdatingFeedTask;
import de.dala.simplenews.utilities.Utilities;

public class FeedRecyclerAdapter extends ChoiceModeRecyclerAdapter<FeedRecyclerAdapter.FeedViewHolder, Feed> {

    private final Activity mContext;
    private final Category mCategory;
    private final CategoryFeedsListener mListener;

    public interface CategoryFeedsListener {
        void onLongClick(Feed feed);
    }

    public FeedRecyclerAdapter(Activity context, Category category, CategoryFeedsListener cFListener) {
        super(category.getFeeds(), null);
        mContext = context;
        mCategory = category;
        mListener = cFListener;
    }


    @Override
    void onBindNormalViewHolder(FeedViewHolder holder, int position) {
        final Feed feed = get(position);
        holder.name.setText(feed.getTitle() == null ? mContext.getString(R.string.feed_title_not_found) : feed.getTitle());
        holder.link.setText(feed.getXmlUrl());
        holder.show.setOnClickListener(new FeedItemClickListener(feed));
        holder.show.setChecked(feed.isVisible());
        holder.edit.setOnClickListener(new FeedItemClickListener(feed));

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //toggle(feed);
                mListener.onLongClick(feed);
                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleIfActionMode(feed);
            }
        });
        Utilities.setPressedColorRippleDrawable(mContext.getResources().getColor(R.color.list_background), PrefUtilities.getInstance().getCurrentColor(), holder.itemView);
    }

    @Override
    public FeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_modify_item, parent, false);
        return new FeedViewHolder(itemView);
    }

    @Override
    void onBindSelectedViewHolder(FeedViewHolder holder, int position) {
        onBindNormalViewHolder(holder, position);
        holder.itemView.setBackgroundResource(R.color.list_background_selected);
    }

    class FeedViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView link;
        final CheckBox show;
        final ImageView edit;

        public FeedViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name);
            link = (TextView) itemView.findViewById(R.id.link);
            show = (CheckBox) itemView.findViewById(R.id.show);
            edit = (ImageView) itemView.findViewById(R.id.edit);
        }
    }

    class FeedItemClickListener implements View.OnClickListener {
        private final Feed feed;

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
        MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .title(R.string.rename_feed)
                .contentGravity(GravityEnum.CENTER)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .input(R.string.hint_add_entry, 0, new MaterialDialog.InputCallback() {

                    @Override
                    public void onInput(final MaterialDialog materialDialog, CharSequence charSequence) {
                        final View positive = materialDialog.getActionButton(DialogAction.POSITIVE);
                        final View negative = materialDialog.getActionButton(DialogAction.NEGATIVE);
                        UpdatingFeedTask feedTask = new UpdatingFeedTask(mContext, mCategory, new UpdatingFeedTask.UpdatingFeedListener() {
                            @Override
                            public void success(Feed feed) {
                                materialDialog.dismiss();
                                positive.setEnabled(true);
                                negative.setEnabled(true);
                                add(feed);
                            }

                            @Override
                            public void loading() {
                                positive.setEnabled(false);
                                negative.setEnabled(false);
                            }

                            @Override
                            public void fail() {
                                positive.setEnabled(true);
                                negative.setEnabled(true);
                            }
                        }, feed.getId());
                        feedTask.execute(charSequence.toString());
                    }
                })
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.cancel();
                    }
                })
                .autoDismiss(false).build();
        EditText text = dialog.getInputEditText();
        if (text != null) {
            text.setText(feed.getXmlUrl());
        }
        dialog.show();
    }

    public void removeFeeds(List<Feed> selectedFeeds) {
        remove(selectedFeeds);
        for (Feed feed : selectedFeeds) {
            DatabaseHandler.getInstance().removeFeeds(null, feed.getId(), false);
        }
    }
}
