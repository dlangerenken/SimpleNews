package de.dala.simplenews.utilities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.net.URL;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class UpdatingFeedTask extends AsyncTask<String, String, Feed> {
    private View view;
    private ViewGroup inputLayout;
    private View progress;
    private AlertDialog dialog;
    private Long isEditingId;
    private Activity mContext;
    private Category mCategory;
    private UpdatingFeedListener mUpdateFeedListener;

    public interface UpdatingFeedListener {
        void onFeedLoaded(Feed feed);

        void onFeedUpdated(Feed feed);
    }

    public UpdatingFeedTask(Activity context, View view, ViewGroup inputLayout, View progress, AlertDialog dialog, Long isEditingId,
                            UpdatingFeedListener updateFeedListener, Category category) {
        this.view = view;
        this.inputLayout = inputLayout;
        this.progress = progress;
        this.dialog = dialog;
        this.isEditingId = isEditingId;
        mContext = context;
        mUpdateFeedListener = updateFeedListener;
        mCategory = category;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        crossFade(progress, inputLayout);
    }

    @Override
    protected Feed doInBackground(String... params) {
        String feedUrl = params[0];
        if (!feedUrl.startsWith("http://")) {
            feedUrl = "http://" + feedUrl;
        }

        if (UIUtils.isValideUrl(feedUrl)) {
            try {
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed syndFeed = input.build(new XmlReader(new URL(feedUrl), mContext));
                if (syndFeed.getEntries() == null || syndFeed.getEntries().isEmpty()) {
                    return null;
                } else {
                    Feed feed = new Feed();
                    feed.setId(isEditingId);
                    feed.setCategoryId(mCategory.getId());
                    feed.setTitle(syndFeed.getTitle());
                    feed.setDescription(syndFeed.getDescription());
                    feed.setXmlUrl(feedUrl);
                    feed.setType(syndFeed.getFeedType());
                    long id = DatabaseHandler.getInstance().addFeed(mCategory.getId(), feed, true);
                    feed.setId(id);

                    return feed;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Feed feed) {
        super.onPostExecute(feed);
        if (feed != null) {
            if (isEditingId == null) {
                mUpdateFeedListener.onFeedLoaded(feed);
            } else {
                mUpdateFeedListener.onFeedUpdated(feed);
            }
            dialog.dismiss();
        } else {
            invalidFeedUrl(true);
        }
    }

    private void invalidFeedUrl(final boolean hideProgressBar) {
        if (mContext != null) {
            Animation shake = AnimationUtils.loadAnimation(mContext,
                    R.anim.shake);
            if (shake != null) {
                shake.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (hideProgressBar) {
                            crossFade(inputLayout, progress);
                            Crouton.makeText(mContext, mContext.getString(R.string.not_valid_format), Style.ALERT, inputLayout).show();
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                view.startAnimation(shake);
            }
        }
    }

    private void crossFade(final View firstView, final View secondView) {
        int mShortAnimationDuration = mContext.getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        ObjectAnimator.ofFloat(firstView, "alpha", 0f).start();
        firstView.setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(firstView, "alpha", 1f).setDuration(mShortAnimationDuration).start();

        ObjectAnimator animator = ObjectAnimator.ofFloat(firstView, "alpha", 0f).setDuration(mShortAnimationDuration);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                secondView.setVisibility(View.GONE);
            }
        });
        animator.start();
    }
}
