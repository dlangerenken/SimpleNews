package de.dala.simplenews.network;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.reflect.TypeToken;

import de.dala.simplenews.Category;


public class NetworkCommunication {
   // private static String serverURL="www.google.de";

	public static void addRequest(Request<?> request) {
		VolleySingleton.getRequestQueue().add(request);
	}

   /* public static void loadRSSFeed(String serverURL, Response.Listener<RSSFeedSource> successListener,
                                           Response.ErrorListener errorListener) {
        Request request = new RssRequest(serverURL,
               successListener, errorListener);
        addRequest(request);
    } */

    public static void loadRSSFeed(String serverURL, Response.Listener<String> successListener,
                                   Response.ErrorListener errorListener) {
        Request request = new StringRequest(serverURL,
                successListener, errorListener);
        addRequest(request);
    }

    //RSS Feed as String first
    public static void loadRSSSourcesForCategory(int categoryId, String serverURL, Response.Listener<ArrayList<String>> successListener, Response.ErrorListener errorListener){
        Type categoryListType = new TypeToken<ArrayList<String>>() {
        }.getType();
        Request<ArrayList<String>> request = new GsonRequest<ArrayList<String>>(Request.Method.GET, serverURL + "?id="+categoryId,categoryListType,
                successListener, errorListener);
        addRequest(request);
    }

    //TODO maybe only categories first -> then offer download for sources
    public static void loadCategories(String serverURL,Response.Listener<ArrayList<Category>> successListener, Response.ErrorListener errorListener){
        Type categoryListType = new TypeToken<ArrayList<Category>>() {
        }.getType();
        Request<ArrayList<Category>> request = new GsonRequest<ArrayList<Category>>(Request.Method.GET, serverURL,categoryListType,
                successListener, errorListener);
        addRequest(request);
    }
}
