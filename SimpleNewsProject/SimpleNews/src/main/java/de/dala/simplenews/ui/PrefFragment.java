package de.dala.simplenews.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.dala.simplenews.R;
import de.dala.simplenews.utilities.BaseNavigation;

public class PrefFragment extends PreferenceFragment implements BaseNavigation {

    public static Fragment getInstance() {
        return new PrefFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_preferences, container, false);
        if (rootView != null) {
            int color = getResources().getColor(R.color.background_window);
            rootView.setBackgroundColor(color);
        }
        addPreferencesFromResource(R.xml.preferences);
        return rootView;
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
