package de.dala.simplenews.network;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class NetworkCommunication {

    private static final OkHttpClient client = new OkHttpClient();

	private static void addRequest(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
	}

    public static void loadShortenedUrl(String url, StringCallback callback){
        addRequest(url, callback);
    }

}
