package de.dala.simplenews;

import android.app.Application;

import de.dala.simplenews.network.VolleySingleton;

/**
 * Created by Daniel on 19.12.13.
 */
public class MainApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        VolleySingleton.init(this);
    }
}
