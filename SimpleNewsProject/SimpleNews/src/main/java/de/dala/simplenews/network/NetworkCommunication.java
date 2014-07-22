package de.dala.simplenews.network;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;


public class NetworkCommunication {

    private static final int TIMEOUT_MS = 5000;
    private static final int MAX_RETRIES = 2;
    private static final int BACKOFF_MULT = 2;

    private static RetryPolicy myRetryPolicy = new DefaultRetryPolicy(TIMEOUT_MS,
            MAX_RETRIES,
            BACKOFF_MULT);
    private static boolean customizedPolicy = true;

    public static void addRequest(Request<?> request) {
        if (customizedPolicy) {
            request.setRetryPolicy(myRetryPolicy);
        }
        VolleySingleton.getRequestQueue().add(request);
    }


    public static void loadRSSFeed(String serverURL, Response.Listener<String> successListener,
                                   Response.ErrorListener errorListener) {
        Request request = new StringRequest(serverURL,
                successListener, errorListener);
        addRequest(request);
    }

    public static void loadShortenedUrl(String url, Response.Listener<String> successListener, Response.ErrorListener errorListener) {
        Request request = new StringRequest(url, successListener, errorListener);
        addRequest(request);
    }

}
