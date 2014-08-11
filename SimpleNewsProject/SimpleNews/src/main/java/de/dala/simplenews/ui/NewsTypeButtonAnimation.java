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

import circularmenu.FloatingActionMenu;
import de.dala.simplenews.R;
import de.dala.simplenews.utilities.UIUtils;

/**
 * Created by Daniel on 16.07.2014.
 */
public class NewsTypeButtonAnimation  {
    private FloatingActionMenu mViewToAnimate;
    private ScrollClass myScrollClass;

    public void fadeIn(){
        if (myScrollClass != null) {
            myScrollClass.fadeIn();
        }
    }

    public void init(final AbsListView view, FloatingActionMenu viewToAnimate) {
        mViewToAnimate = viewToAnimate;
        initScrollClass(view);
        initClick();
    }

    private void initClick() {
        mViewToAnimate.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu menu) {

            }

            @Override
            public void onMenuClosed(FloatingActionMenu menu) {

            }
        });
    }

    private void initScrollClass(final AbsListView view) {
        myScrollClass = new ScrollClass() {
            int mLastFirstVisibleItem = 0;
            boolean sliding = false;
            int mTotalItemCount = 0;

            @Override public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                mTotalItemCount = totalItemCount;
                if (totalItemCount == 0){
                    fadeIn();
                }
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
                if (!sliding && mTotalItemCount > 0) {
                    final int height = mViewToAnimate.getMainActionView().getMeasuredHeight();
                    if (mViewToAnimate.getMainActionView().getVisibility() == View.VISIBLE) {
                        Animation animation = new TranslateAnimation(0, 0, 0,
                                height);
                        animation.setInterpolator(new AccelerateInterpolator(1.0f));
                        animation.setDuration(400);

                        if (mViewToAnimate.isOpen()){
                            animation.setStartOffset(400);
                        }

                        mViewToAnimate.getMainActionView().startAnimation(animation);
                        animation.setAnimationListener(new Animation.AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation animation) {
                                sliding = true;
                                mViewToAnimate.close(true);
                                interrupt();
                            }

                            @Override public void onAnimationRepeat(Animation animation) {}

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                sliding = false;
                                mViewToAnimate.close(false);
                                mViewToAnimate.getMainActionView().setVisibility(View.INVISIBLE);
                                appear();
                            }
                        });
                    }else{
                        appear();
                    }
                }
            }

            @Override
            public void fadeIn() {
                if (!sliding) {
                    final int height = mViewToAnimate.getMainActionView().getMeasuredHeight();
                    if (mViewToAnimate.getMainActionView().getVisibility() == View.INVISIBLE) {

                        Animation animation = new TranslateAnimation(0, 0,
                                height, 0);

                        animation.setInterpolator(new AccelerateInterpolator(1.0f));
                        animation.setDuration(400);
                        mViewToAnimate.getMainActionView().startAnimation(animation);
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                sliding = true;
                                mViewToAnimate.getMainActionView().setVisibility(View.VISIBLE);
                            }

                            @Override public void onAnimationRepeat(Animation animation) {}

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                sliding = false;
                            }
                        });
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
                        if (y < mLastMotionY && view.getFirstVisiblePosition() <= 0) {
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

    private abstract class ScrollClass implements AbsListView.OnScrollListener {
        Handler mHandler = new Handler();
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                fadeIn();
            }
        };

        abstract void fadeIn();
        abstract void fadeOut();

        public void appear() {
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, 800);
        }

        public void interrupt() {
            mHandler.removeCallbacks(mRunnable);
        }

    }
}
