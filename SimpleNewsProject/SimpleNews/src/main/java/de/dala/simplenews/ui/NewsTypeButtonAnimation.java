package de.dala.simplenews.ui;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import circularmenu.FloatingActionMenu;

public class NewsTypeButtonAnimation  {
    private FloatingActionMenu mViewToAnimate;
    private ScrollClass myScrollClass;

    public void fadeIn(){
        if (myScrollClass != null) {
            myScrollClass.fadeIn();
        }
    }

    public void init(final RecyclerView view, FloatingActionMenu viewToAnimate) {
        mViewToAnimate = viewToAnimate;
        initScrollClass(view);
    }

    private void initScrollClass(final RecyclerView view) {
        myScrollClass = new ScrollClass() {
            boolean sliding = false;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dx > 0 || dy > 0){
                    fadeOut();
                }else if (dx < 0 || dy < 0){
                    fadeIn();
                }
            }


            @Override
            public void fadeOut() {
                if (!sliding) {
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
    }

    private abstract class ScrollClass extends RecyclerView.OnScrollListener {
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
