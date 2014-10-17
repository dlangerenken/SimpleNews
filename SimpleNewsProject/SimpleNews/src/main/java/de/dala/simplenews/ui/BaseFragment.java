package de.dala.simplenews.ui;
import android.support.v4.app.FragmentManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.lang.reflect.Field;
import java.util.List;

import de.dala.simplenews.utilities.BaseNavigation;

/**
 * Created by Daniel on 29.08.2014.
 * http://stackoverflow.com/questions/14900738/nested-fragments-disappear-during-transition-animation
 */
public class BaseFragment extends Fragment implements FragmentManager.OnBackStackChangedListener{
    // Arbitrary value; set it to some reasonable default
    private static final int DEFAULT_CHILD_ANIMATION_DURATION = 600;
    private Fragment currentFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getChildFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        final Fragment parent = getParentFragment();

        // Apply the workaround only if this is a child fragment, and the parent
        // is being removed.
        if (!enter && parent != null && parent.isRemoving()) {
            // This is a workaround for the bug where child fragments disappear when
            // the parent is removed (as all children are first removed from the parent)
            // See https://code.google.com/p/android/issues/detail?id=55228
            Animation doNothingAnim = new AlphaAnimation(1, 1);
            doNothingAnim.setDuration(getNextAnimationDuration(parent, DEFAULT_CHILD_ANIMATION_DURATION));
            return doNothingAnim;
        } else {
            return super.onCreateAnimation(transit, enter, nextAnim);
        }
    }

    private static long getNextAnimationDuration(Fragment fragment, long defValue) {
        try {
            // Attempt to get the resource ID of the next animation that
            // will be applied to the given fragment.
            Field nextAnimField = Fragment.class.getDeclaredField("mNextAnim");
            nextAnimField.setAccessible(true);
            int nextAnimResource = nextAnimField.getInt(fragment);
            Animation nextAnim = AnimationUtils.loadAnimation(fragment.getActivity(), nextAnimResource);

            // ...and if it can be loaded, return that animation's duration
            return (nextAnim == null) ? defValue : nextAnim.getDuration();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return defValue;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return defValue;
        } catch (Resources.NotFoundException e){
            e.printStackTrace();
            return defValue;
        }
    }

    @Override
    public void onBackStackChanged() {
        Fragment visibleFragment = getVisibleFragment();
        if (visibleFragment instanceof NewsOverViewFragment){
            ((NewsOverViewFragment)visibleFragment).onBackStackChanged();
        }
        updateNavigation();
    }

    public void updateNavigation() {
        currentFragment = getVisibleFragment();
        if (currentFragment != null && currentFragment instanceof BaseNavigation) {
            BaseNavigation navigation = (BaseNavigation) currentFragment;
            ((MainActivity) getActivity()).updateNavigation(navigation.getNavigationDrawerId(), navigation.getTitle());
        }
    }

    public Fragment getVisibleFragment(){
        FragmentManager fragmentManager = getChildFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for(Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible() && !(fragment instanceof NavigationDrawerFragment)) {
                return fragment;
            }
        }
        return null;
    }
}
