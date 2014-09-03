/*
 *   Copyright 2014 Oguz Bilgener
 */
package circularmenu.animation;

import android.graphics.Point;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.Collection;

import circularmenu.FloatingActionMenu;


/**
 * An example animation handler
 * Animates translation, rotation, scale and alpha at the same time using Property Animation APIs.
 */
public class DefaultAnimationHandler extends MenuAnimationHandler {

    /**
     * duration of animations, in milliseconds
     */
    protected static final int DURATION = 500;
    /**
     * duration to wait between each of
     */
    protected static final int LAG_BETWEEN_ITEMS = 20;
    /**
     * holds the current state of animation
     */
    private boolean animating;

    public DefaultAnimationHandler() {
        setAnimating(false);
    }

    @Override
    public void animateMenuOpening(Point center) {
        super.animateMenuOpening(center);

        setAnimating(true);

        Animator lastAnimation = null;
        for (int i = 0; i < menu.getSubActionItems().size(); i++) {
            View mView = menu.getSubActionItems().get(i).view;
            if (mView != null) {

                ViewHelper.setScaleX(mView, 0);
                ViewHelper.setScaleY(mView, 0);
                ViewHelper.setAlpha(mView, 0);

                AnimatorSet animationSet = new AnimatorSet();
                animationSet.playTogether(
                        ObjectAnimator.ofFloat(menu.getSubActionItems().get(i).view, "translationX", menu.getSubActionItems().get(i).x - center.x + menu.getSubActionItems().get(i).width / 2),
                        ObjectAnimator.ofFloat(menu.getSubActionItems().get(i).view, "translationY", menu.getSubActionItems().get(i).y - center.y + menu.getSubActionItems().get(i).height / 2),
                        ObjectAnimator.ofFloat(menu.getSubActionItems().get(i).view, "rotation", 720),
                        ObjectAnimator.ofFloat(menu.getSubActionItems().get(i).view, "scaleX", 1),
                        ObjectAnimator.ofFloat(menu.getSubActionItems().get(i).view, "scaleY", 1),
                        ObjectAnimator.ofFloat(menu.getSubActionItems().get(i).view, "alpha", 1)
                );
                animationSet.setDuration(DURATION);
                animationSet.setInterpolator(new OvershootInterpolator(0.9f));
                animationSet.addListener(new SubActionItemAnimationListener(menu.getSubActionItems().get(i), ActionType.OPENING));
                animationSet.setStartDelay((menu.getSubActionItems().size() - i) * LAG_BETWEEN_ITEMS);
                animationSet.start();

                if (i == 0) {
                    lastAnimation = animationSet;
                }
            }
        }
        if (lastAnimation != null) {
            lastAnimation.addListener(new LastAnimationListener());
        }

    }

    @Override
    public void animateMenuClosing(Point center) {
        super.animateMenuOpening(center);

        setAnimating(true);

        AnimatorSet lastAnimation = null;
        for (int i = 0; i < menu.getSubActionItems().size(); i++) {
            if (menu.getSubActionItems().get(i).view != null) {
                AnimatorSet animationSet = new AnimatorSet();
                animationSet.playTogether(
                        ObjectAnimator.ofFloat(menu.getSubActionItems().get(i).view, "translationX", -(menu.getSubActionItems().get(i).x - center.x + menu.getSubActionItems().get(i).width / 2)),
                        ObjectAnimator.ofFloat(menu.getSubActionItems().get(i).view, "translationY", -(menu.getSubActionItems().get(i).y - center.y + menu.getSubActionItems().get(i).height / 2)),
                        ObjectAnimator.ofFloat(menu.getSubActionItems().get(i).view, "rotation", -720),
                        ObjectAnimator.ofFloat(menu.getSubActionItems().get(i).view, "scaleX", 0),
                        ObjectAnimator.ofFloat(menu.getSubActionItems().get(i).view, "scaleY", 0),
                        ObjectAnimator.ofFloat(menu.getSubActionItems().get(i).view, "alpha", 0)
                );
                animationSet.setDuration(DURATION);
                animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
                animationSet.addListener(new SubActionItemAnimationListener(menu.getSubActionItems().get(i), ActionType.CLOSING));
                animationSet.setStartDelay((menu.getSubActionItems().size() - i) * LAG_BETWEEN_ITEMS);
                animationSet.start();

                if (i == 0) {
                    lastAnimation = animationSet;
                }
            }
        }
        if (lastAnimation != null) {
            lastAnimation.addListener(new LastAnimationListener());
        }
    }

    @Override
    public boolean isAnimating() {
        return animating;
    }

    @Override
    protected void setAnimating(boolean animating) {
        this.animating = animating;
    }

    protected class SubActionItemAnimationListener implements Animator.AnimatorListener {

        private FloatingActionMenu.Item subActionItem;
        private ActionType actionType;

        public SubActionItemAnimationListener(FloatingActionMenu.Item subActionItem, ActionType actionType) {
            this.subActionItem = subActionItem;
            this.actionType = actionType;
        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            restoreSubActionViewAfterAnimation(subActionItem, actionType);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            restoreSubActionViewAfterAnimation(subActionItem, actionType);
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }
}