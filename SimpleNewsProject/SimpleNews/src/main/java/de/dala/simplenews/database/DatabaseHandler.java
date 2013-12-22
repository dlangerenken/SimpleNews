//
//package de.dala.simplenews.database;
//
//import java.util.List;
//
//import melb.mSafe.model.Layer;
//import melb.mSafe.model.Building;
//import melb.mSafe.model.RouteGraph;
//import melb.mSafe.model.ServerMessage;
//import melb.mSafe.model.UserMessage;
//import android.content.Context;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.os.Message;
//
///**
// * The DatabaseHandler for the communication between Client and Client-Database
// *
// * @author Daniel Langerenken based on
// *         http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
// */
//public class DatabaseHandler extends SQLiteOpenHelper implements
//		IDatabaseHandler {
//
//	/**
//	 * Database Name and Version
//	 */
//	private static final int DATABASE_VERSION = 1;
//	private static final String DATABASE_NAME = "news_database";
//
//	/**
//	 * Table names
//	 */
//	private static final String TABLE_CATEGORY = "category";
//
//
//	// Ways = List of Nodes, Date, unique id, pedestrian/wheelchair type
//	// In case of node/edge-manipulation. If way is affected (blocked or new type) ->
//	// way into revision (with revision date). node/edge into revision (with date). new node/edge into list of nodes
//	// if node is removed -> node into revision. corresponding edges into revision, removed of "edges"
//	// if building changed, move all nodes,edges,routegraphs,messages,ways into revision.
//	// if routegraph changed, move routegraph into revision
//	// if messages reseted by server (e.g.), move all messages into revision
//	// if messages "expire" -> older then some hours? move into revision
//	// if node/edge changes, recalculate map.
//
//	public DatabaseHandler(Context context) {
//		super(context, DATABASE_NAME, null, DATABASE_VERSION);
//	}
//
//	/*
//	 * Creating Tables(non-Javadoc)
//	 *
//	 * @see
//	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
//	 * .SQLiteDatabase)
//	 */
//	@Override
//	public void onCreate(SQLiteDatabase db) {
//		// String createSeenDisasterTable = "CREATE TABLE " +
//		// TABLE_SEEN_DISASTER
//		// + "(" + seen_id + " LONG PRIMARY KEY," + seen_disaster_id
//		// + " LONG," + seen_user_id + " LONG)";
//		// db.execSQL(createSeenDisasterTable);
//	}
//
//	@Override
//	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		/*
//		 * Drop older table if existed
//		 */
//		// db.execSQL("DROP TABLE IF EXISTS " + TABLE_TUTORIALS);
//
//		/*
//		 * Create tables again
//		 */
//		onCreate(db);
//	}
//
//	@Override
//	public RouteGraph getLatestRouteGraph() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Building getLatestBuildingInformation() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Layer getLayerById(int id) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<Message> getLatestMessages() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<ServerMessage> getLatestServerMessages() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<UserMessage> getLatestUserMessages() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//}
