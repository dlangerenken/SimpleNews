package de.dala.simplenews.network;//package com.qwertee.network;
//
//import android.content.Context;
//
//import com.android.volley.Request;
//import com.android.volley.Request.Method;
//import com.android.volley.Response.ErrorListener;
//import com.android.volley.Response.Listener;
//import com.android.volley.toolbox.StringRequest;
//
//@Deprecated
//public class DummyNetworkCommunication {
//
//	private static String tees = "tees/";
//	private static final String previousTeeURL = tees + "previous";
//	public static String serverURL = "http://www.qwertee.com/";
//	private static String voteURL = tees + "vote/newest";
//	private static String FAQ = "help";
//	private static String teesToBuyURL = ""; // on front page
//
//	public static void addRequest(Request<?> request, Context context) {
//		VolleySingleton.getRequestQueue().add(request);
//	}
//
//	/**
//	 * based on
//	 * http://stackoverflow.com/questions/18065568/how-can-one-change-the
//	 * -default-disk-cache-behavior-in-volley Images don't expire within a day
//	 * 
//	 * @param method
//	 * @param url
//	 * @param successListener
//	 * @param errorListener
//	 * @return
//	 */
//	public static StringRequest getStringRequest(int method, String url,
//			Listener<String> successListener, ErrorListener errorListener) {
//		StringRequest request = new StringRequest(method, url, successListener,
//				errorListener);
//		return request;
//	}
//
//	public static void loadVotes(Listener<String> successListener,
//			ErrorListener errorListener, Context context) {
//		Request<?> request = getStringRequest(Method.GET, serverURL + voteURL,
//				successListener, errorListener);
//		addRequest(request, context);
//	}
//
//	public static void loadTeesToBuy(Listener<String> successListener,
//			ErrorListener errorListener, Context context) {
//		StringRequest request = getStringRequest(Method.GET, serverURL
//				+ teesToBuyURL, successListener, errorListener);
//		addRequest(request, context);
//	}
//
//	public static void loadFAQ(Listener<String> successListener,
//			ErrorListener errorListener, Context context) {
//		StringRequest request = getStringRequest(Method.GET, serverURL + FAQ,
//				successListener, errorListener);
//		addRequest(request, context);
//	}
//
//	public static void loadPreviousTees(Listener<String> successListener,
//			ErrorListener errorListener, Context context) {
//		StringRequest request = getStringRequest(Method.GET, serverURL
//				+ previousTeeURL, successListener, errorListener);
//		addRequest(request, context);
//	}
//}
