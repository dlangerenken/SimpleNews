package de.dala.simplenews.ui;

/**
 * Created by Daniel on 22.04.2014.
 */

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.dala.simplenews.R;
import de.dala.simplenews.utilities.BaseNavigation;

/**
 * Created by Daniel on 22.02.14.
 */
public class PrefFragment extends PreferenceFragment implements BaseNavigation {

    public static Fragment getInstance() {
        Fragment fragment = new PrefFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(R.color.background_window));
        return view;
    }

    @Override
    public String getTitle() {
        Context mContext = getActivity();
        if (mContext != null) {
            return mContext.getString(R.string.pref_fragment_title);
        }
        return "SimpleNews"; // Should not be called
    }

    @Override
    public int getNavigationDrawerId() {
        return NavigationDrawerFragment.SETTINGS;
    }
}

