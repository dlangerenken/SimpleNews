package de.dala.simplenews.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.List;

/**
 * Created by Daniel on 01.08.2014.
 * from: https://github.com/github/android/blob/master/app/src/main/java/com/github/mobile/persistence/PersistableResource.java
 */
public interface IPersistableObject<E> {
    /**
     * @return a cursor capable of reading the required information out of the
     *         database.
     */
    Cursor getCursor();

    /**
     * @param cursor
     * @return a single item, read from this row of the cursor
     */
    E loadFrom(Cursor cursor);

    /**
     * Store supplied items in DB or update if already added
     *
     * @param items
     */
    void store(List<E> items);

    /**
     * Delete supplied items in DB
     */
    void delete();
}
