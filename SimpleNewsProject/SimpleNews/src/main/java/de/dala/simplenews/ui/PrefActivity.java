package de.dala.simplenews.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import de.dala.simplenews.R;

public class PrefActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
