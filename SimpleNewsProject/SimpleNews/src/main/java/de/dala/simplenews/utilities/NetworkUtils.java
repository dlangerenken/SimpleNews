package de.dala.simplenews.utilities;

import android.net.Uri;
import android.util.Log;


import java.io.IOException;
import java.util.List;

import de.dala.simplenews.common.Entry;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.network.NetworkCommunication;
import de.dala.simplenews.network.StringCallback;
import okhttp3.Call;

class NetworkUtils {
    private static void shortenWithAdfly(final List<Entry> entries) {
        for (final Entry entry : entries) {
            if (entry.getShortenedLink() != null || entry.getLink() == null) {
                continue;
            }
            String link = entry.getLink();
            String urlForCall = String.format("http://api.adf.ly/api.php?key=86a235af637887da35e4627465b784cb&uid=6090236&advert_type=int&domain=adf.ly&url=%s", Uri.encode(link));
            Log.d("CategoryUpdater", String.format("Shorten started for: %s", urlForCall));
            NetworkCommunication.loadShortenedUrl(urlForCall, new StringCallback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("CategoryUpdater", String.format("Entry with id: %s could not be shortened", entry.getId() + ""));
                }

                @Override
                public void success(String result) {
                    Log.d("CategoryUpdater", "Success in shorten: " + result);
                    if (!"error".equalsIgnoreCase(result)) {
                        entry.setShortenedLink(result);
                        DatabaseHandler.getInstance().updateEntry(entry);
                    } else {
                        Log.e("CategoryUpdater", String.format("Entry with id: %s could not be shortened", entry.getId() + ""));
                    }
                }
            });
        }
    }

    static void shortenIfNecessary(List<Entry> entries) {
        if (PrefUtilities.getInstance().shouldShortenLinks()) {
            shortenWithAdfly(entries);
        }
    }
}
