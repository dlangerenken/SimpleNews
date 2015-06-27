package de.dala.simplenews.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;

import de.dala.simplenews.R;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        FragmentTransaction t = getFragmentManager().beginTransaction();
        t.replace(R.id.container, PrefFragment.getInstance());
        t.commit();
    }

}
