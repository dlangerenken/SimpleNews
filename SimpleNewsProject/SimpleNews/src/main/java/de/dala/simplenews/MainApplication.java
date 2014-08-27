package de.dala.simplenews;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;

import java.io.InputStream;

import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.network.VolleySingleton;
import de.dala.simplenews.parser.XmlParser;
import de.dala.simplenews.utilities.ColorManager;
import de.dala.simplenews.utilities.PrefUtilities;

/**
 * Created by Daniel on 19.12.13.
 */
public class MainApplication extends Application {
    static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        Crashlytics.start(this);
        XmlParser.Init(this);
        VolleySingleton.init(this);
        DatabaseHandler.init(this, DatabaseHandler.DATABASE_NAME);
        PrefUtilities.init(this);
        ColorManager.init(this);
        context = this;
    }

    public static InputStream openRawResource(int resourceName){
        return context.getResources().openRawResource(resourceName);
    }
}
