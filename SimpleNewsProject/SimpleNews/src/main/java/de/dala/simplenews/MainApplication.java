package de.dala.simplenews;

import android.app.Application;

import com.rometools.rome.feed.RomeResourceInit;

import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.parser.XmlParser;
import de.dala.simplenews.utilities.ColorManager;
import de.dala.simplenews.utilities.PrefUtilities;

/**
 * Created by Daniel on 19.12.13.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        XmlParser.Init(this);
        DatabaseHandler.init(this, DatabaseHandler.DATABASE_NAME);
        PrefUtilities.init(this);
        ColorManager.init(this);
        RomeResourceInit.init(this);
    }
}
