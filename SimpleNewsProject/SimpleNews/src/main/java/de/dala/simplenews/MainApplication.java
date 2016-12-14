package de.dala.simplenews;

import android.app.Application;

import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.utilities.ColorManager;
import de.dala.simplenews.utilities.PrefUtilities;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseHandler.init(this);
        PrefUtilities.init(this);
        ColorManager.init(this);
    }
}
