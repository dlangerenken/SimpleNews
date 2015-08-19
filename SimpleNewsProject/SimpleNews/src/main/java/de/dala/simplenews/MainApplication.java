package de.dala.simplenews;

import android.app.Application;

import com.rometools.rome.feed.RomeResourceInit;

import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.utilities.ColorManager;
import de.dala.simplenews.utilities.PrefUtilities;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseHandler.init(this, DatabaseHandler.DATABASE_NAME);
        PrefUtilities.init(this);
        ColorManager.init(this);
        RomeResourceInit.init(this);
    }
}
