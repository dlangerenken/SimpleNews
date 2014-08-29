package com.rometools.rome.feed;

import android.content.Context;

import java.io.InputStream;

/**
 * Created by Daniel on 28.08.2014.
 */
public class RomeResourceInit {
    private static Context context;

    public static void init(Context context){
        RomeResourceInit.context = context;
    }


    public static InputStream openRawResource(int resourceName) {
        if (context != null) {
            return context.getResources().openRawResource(resourceName);
        }
        return null;
    }
}
