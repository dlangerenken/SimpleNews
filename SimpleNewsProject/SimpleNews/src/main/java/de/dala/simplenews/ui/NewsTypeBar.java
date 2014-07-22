package de.dala.simplenews.ui;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.dala.simplenews.R;
import de.dala.simplenews.utilities.UIUtils;

/**
 * Created by Daniel on 16.07.2014.
 */
public class NewsTypeBar extends LinearLayout {

    public static final int ALL = 0;
    private int entryType = ALL;
    public static final int FAV = 1;
    public static final int RECENT = 2;
    public static final int UNREAD = 3;

    private View allEntryButton;
    private View favEntryButton;
    private View recentEntryButton;
    private View unreadEntryButton;
    private TextView allEntryTextView;
    private TextView favEntryTextView;
    private TextView recentEntryTextView;
    private TextView unreadEntryTextView;

    private INewsTypeClicked newsTypeClickedInterface;


    private ScrollClass myScrollClass;

    public interface INewsTypeClicked {
        void newsTypeClicked(int type);
    }

    public NewsTypeBar(Context context) {
        super(context);
    }

    public NewsTypeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void init(Context context){
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.news_type_bar, this);
        initButtons();
    }

    public void fadeIn(){
        if (myScrollClass != null) {
            myScrollClass.fadeIn();
        }
    }

    public void init(final int color, final INewsTypeClicked newsTypeClicked, final AbsListView view) {
        newsTypeClickedInterface = newsTypeClicked;
        init(getContext());
        allEntryButton.setBackgroundDrawable(UIUtils.getStateListDrawableByColor(color));
        favEntryButton.setBackgroundDrawable(UIUtils.getStateListDrawableByColor(color));
        recentEntryButton.setBackgroundDrawable(UIUtils.getStateListDrawableByColor(color));
        unreadEntryButton.setBackgroundDrawable(UIUtils.getStateListDrawableByColor(color));

        initScrollClass(view);
    }

    private void initScrollClass(final AbsListView view) {
        myScrollClass = new ScrollClass() {
            int mLastFirstVisibleItem = 0;
            boolean sliding = false;

            @Override public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (view.getId() == view.getId()) {
                    final int currentFirstVisibleItem = view.getFirstVisiblePosition();
                    if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                        fadeIn();
                    } else if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                        fadeOut();
                    }
                    mLastFirstVisibleItem = currentFirstVisibleItem;
                }
            }

            @Override
            public void fadeOut() {
                if (!sliding) {
                    final int height = getMeasuredHeight();
                    if (getVisibility() == View.VISIBLE) {
                        Animation animation = new TranslateAnimation(0, 0, 0,
                                height);
                        animation.setInterpolator(new AccelerateInterpolator(1.0f));
                        animation.setDuration(400);

                        startAnimation(animation);
                        animation.setAnimationListener(new Animation.AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation animation) {
                                sliding = true;
                                interrupt();
                            }

                            @Override public void onAnimationRepeat(Animation animation) {}

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                sliding = false;
                                setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }
            }

            @Override
            public void fadeIn() {
                if (!sliding) {
                    final int height = getMeasuredHeight();
                    if (getVisibility() == View.INVISIBLE) {

                        Animation animation = new TranslateAnimation(0, 0,
                                height, 0);

                        animation.setInterpolator(new AccelerateInterpolator(1.0f));
                        animation.setDuration(400);
                        startAnimation(animation);
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                sliding = true;
                                setVisibility(View.VISIBLE);
                            }

                            @Override public void onAnimationRepeat(Animation animation) {}

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                sliding = false;
                                disappear();
                            }
                        });
                    } else {
                        disappear();
                    }
                }
            }

        };

        view.setOnScrollListener(myScrollClass);
        view.setOnTouchListener(new View.OnTouchListener() {
            private int mLastMotionY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int y = (int) event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        if (y < mLastMotionY && view.getFirstVisiblePosition() == 0) {
                            myScrollClass.fadeIn();
                        }
                        break;
                    case MotionEvent.ACTION_DOWN:
                        mLastMotionY = (int) event.getY();
                        break;
                }
                return false;
            }

        });
    }

    private void initButtons() {
        allEntryButton = findViewById(R.id.entry_type_all);
        favEntryButton = findViewById(R.id.entry_type_fav);
        recentEntryButton = findViewById(R.id.entry_type_recently);
        unreadEntryButton = findViewById(R.id.entry_type_unread);

        allEntryTextView = (TextView) findViewById(R.id.entry_type_text_all);
        favEntryTextView = (TextView) findViewById(R.id.entry_type_text_fav);
        recentEntryTextView = (TextView) findViewById(R.id.entry_type_text_recently);
        unreadEntryTextView = (TextView) findViewById(R.id.entry_type_text_unread);

        allEntryTextView.setTextColor(UIUtils.getColorTextStateList());
        favEntryTextView.setTextColor(UIUtils.getColorTextStateList());
        recentEntryTextView.setTextColor(UIUtils.getColorTextStateList());
        unreadEntryTextView.setTextColor(UIUtils.getColorTextStateList());


        switch (entryType) {
            case ALL:
                allEntryButton.setSelected(true);
                allEntryTextView.setSelected(true);
                break;
            case FAV:
                favEntryButton.setSelected(true);
                favEntryTextView.setSelected(true);
                break;
            case RECENT:
                recentEntryButton.setSelected(true);
                recentEntryTextView.setSelected(true);
                break;
            case UNREAD:
                unreadEntryButton.setSelected(true);
                unreadEntryTextView.setSelected(true);
                break;
        }
        allEntryButton.setOnClickListener(new EntryTypeClickListener(ALL));
        favEntryButton.setOnClickListener(new EntryTypeClickListener(FAV));
        recentEntryButton.setOnClickListener(new EntryTypeClickListener(RECENT));
        unreadEntryButton.setOnClickListener(new EntryTypeClickListener(UNREAD));
    }

    private class EntryTypeClickListener implements View.OnClickListener {
        private int type;

        public EntryTypeClickListener(int type) {
            this.type = type;
        }

        @Override
        public void onClick(View v) {
            allEntryButton.setSelected(ALL == type);
            allEntryTextView.setSelected(ALL == type);

            favEntryButton.setSelected(FAV == type);
            favEntryTextView.setSelected(FAV == type);

            recentEntryButton.setSelected(RECENT == type);
            recentEntryTextView.setSelected(RECENT == type);

            unreadEntryButton.setSelected(UNREAD == type);
            unreadEntryTextView.setSelected(UNREAD == type);

            entryType = type;

            if (newsTypeClickedInterface != null){
                newsTypeClickedInterface.newsTypeClicked(entryType);
            }
        }
    }

    private abstract class ScrollClass implements AbsListView.OnScrollListener {
        Handler mHandler = new Handler();
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                fadeOut();
            }
        };

        abstract void fadeIn();

        public void disappear() {
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, 5 * 1000);
        }

        public void interrupt() {
            mHandler.removeCallbacks(mRunnable);
        }

        abstract void fadeOut();
    }
}
