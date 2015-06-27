package de.dala.simplenews.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import de.dala.simplenews.R;

public class PrefFragment extends PreferenceFragment {
    public static Fragment getInstance() {
        return new PrefFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
