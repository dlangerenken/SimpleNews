package de.dala.simplenews.network;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;


public class NetworkCommunication {

    static OkHttpClient client = new OkHttpClient();

	public static void addRequest(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
	}


    public static void loadRSSFeed(String serverURL, StringCallback callback) {
        addRequest(serverURL, callback);
    }

    public static void loadShortenedUrl(String url, StringCallback callback){
        addRequest(url, callback);
    }

}
