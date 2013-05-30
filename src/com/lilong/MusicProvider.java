package com.lilong;


import com.lilong.Variables.D_Music;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class MusicProvider extends ContentProvider {
	private static final UriMatcher Mymatcher ;
	private MusicDatabase myDatabase = null;
	private SQLiteDatabase db_w = null;
	private SQLiteDatabase db_r = null;

	static{
		Mymatcher = new UriMatcher(UriMatcher.NO_MATCH);
		Mymatcher.addURI(Variables.Authority, "album", 1);
		Mymatcher.addURI(Variables.Authority, "album/#", 2);
		Mymatcher.addURI(Variables.Authority, "music", 5);
		Mymatcher.addURI(Variables.Authority, "music/#", 6);
	}
	
	@Override
	public boolean onCreate() {
		myDatabase = new MusicDatabase(getContext());
		// TODO Auto-generated method stub
		//openDatabase();
		return true;
	}
	
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		switch(Mymatcher.match(uri)){
		case 1: return D_Music.A_Content_Type;
		case 2: return D_Music.A_Content_Item_Type;
		case 5: return D_Music.M_Content_Type;
		case 6: return D_Music.M_Content_Item_Type;
		default: throw new IllegalArgumentException("Unkonw URI:"+uri);
		}
	}

	//**************************************************针对ContentProvider的数据库操作
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		switch(Mymatcher.match(uri)){
		case 1:{
			delete_Album(selection, null);
			break;
		}
		case 2:{	
			String id = uri.getPathSegments().get(1);
			delete_Music(D_Music.Music_Album+"="+id, null);
			delete_Album(D_Music.Album_id +"="+id, null);
			
			break;
		}
		case 5:{
			delete_Music(selection, null);
			break;
		}
		case 6:{
			String id = uri.getPathSegments().get(1);
			delete_Music(D_Music.Music_id+"="+id, null);
			break;
		}
		}
		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		Uri myuri = null;
		switch(Mymatcher.match(uri)){
		case 1: {
			if(values != null){
				String name = values.getAsString(D_Music.Album_name);
				String time = values.getAsString(D_Music.Album_time);
				if(values.containsKey(D_Music.Album_name)){
					if(values.containsKey(D_Music.Album_time)){
						long rowId = insert_Album(name,time);
						Log.i("A_rowId", rowId+"");
						if(rowId > 0){
							myuri = ContentUris.withAppendedId(D_Music.A_Content_URL, rowId);
							getContext().getContentResolver().notifyChange(myuri, null);
						}
					}
				}
			}
			break;}
		case 5: {
			if(values != null){
				String name = values.getAsString(D_Music.Music_name);
				long album = values.getAsLong(D_Music.Music_Album);
				String path = values.getAsString(D_Music.Music_path);
				if(values.containsKey(D_Music.Music_name)){
					if(values.containsKey(D_Music.Music_Album)){
						if(values.containsKey(D_Music.Music_path)){
							long rowId = insert_Music(name,album,path);
							Log.i("M_rowId", rowId+"");
							if(rowId > 0){
								myuri = ContentUris.withAppendedId(D_Music.M_Content_URL, rowId);
								getContext().getContentResolver().notifyChange(myuri, null);
							}
						}
					}
				}
			}
			break;}
		default: throw new IllegalArgumentException("Unkonw URI:"+uri);
		}
		return myuri;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		Cursor c = null;
		switch(Mymatcher.match(uri)){
		case 1: {
			c = query_Album(selection,selectionArgs,sortOrder);
			break;}
		case 2: {
			String id = uri.getPathSegments().get(1);
			c = query_Album(D_Music.Album_id + " = " + id,null,sortOrder);
			break;}
		case 5: {
			c = query_Music(selection,selectionArgs,sortOrder);
			break;}
		case 6: {
			String id = uri.getPathSegments().get(1);
			c = query_Music(D_Music.Music_id + " = " + id,selectionArgs,sortOrder);
			break;}
		default: throw new IllegalArgumentException("Unkonw URI:"+uri);
		}
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		switch(Mymatcher.match(uri)){
		case 2:{	
			String id = uri.getPathSegments().get(1);
			updata_Album(Long.parseLong(id),values);
			break;
		}
		case 6:{
			//更新音乐信息
			break;
		}
		}
		return 0;
	}
	
	//*************************************************创建数据库及数据表
	class MusicDatabase extends SQLiteOpenHelper{

		public MusicDatabase(Context context) {
			super(context, D_Music.Database_name, null, D_Music.Database_version);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
//			db.execSQL("drop table if exists "+D_Music.Album );
//			db.execSQL("drop table if exists "+D_Music.Musices );
			
			db.execSQL(D_Music.Create_Album);
			Log.i("*..create table Album", D_Music.Create_Album);
			db.execSQL(D_Music.Create_Music);
			Log.i("*..create table Music", D_Music.Create_Music);
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			Cursor cur = db.query(D_Music.Album,null, null, null, null, null, null);
		    if(cur.getCount()<=1){
		    	db.execSQL("drop table if exists "+D_Music.Album +"");
		    	db.execSQL(D_Music.Create_Album);
		    	db.execSQL("drop table if exists "+D_Music.Musices +"");
		    	db.execSQL(D_Music.Create_Music);
		    }
		}
	}
	
	//打开数据库
	public void openDatabase(){
		myDatabase = new MusicDatabase(getContext());
		db_w = myDatabase.getWritableDatabase();
		db_r = myDatabase.getReadableDatabase();
	}
	//关闭数据库
	public void closeDatabase(){
		myDatabase.close();
	}
	//**************************************************针对SQLite的数据库操作
	public long insert_Album(String name,String num){
		db_w = myDatabase.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(D_Music.Album_name, name);
		cv.put(D_Music.Album_time, num);
		return db_w.insert(D_Music.Album, null, cv);
	}
	public long insert_Music(String name,long album,String path){
		db_w = myDatabase.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(D_Music.Music_name, name);
		cv.put(D_Music.Music_Album, album);
		cv.put(D_Music.Music_path, path);
		return db_w.insert(D_Music.Musices, null, cv);
			
	}
	public void delete_Album(String where,String[] whereArgs){
		db_w = myDatabase.getWritableDatabase();
		db_w.delete(D_Music.Album, where, whereArgs);
		
	}
	public void delete_Music(String where,String[] whereArgs){
		db_w = myDatabase.getWritableDatabase();
		db_w.delete(D_Music.Musices, where, whereArgs);
	}
	public void updata_Album(long id,ContentValues vs){
		db_w = myDatabase.getWritableDatabase();
		db_w.update(D_Music.Album, vs, D_Music.Album_id+"="+id, null);
	}
	public Cursor query_Album(String selection,String[] selectionArgs,String orderBy){
		db_r = myDatabase.getReadableDatabase();
		Cursor cur = db_r.query(D_Music.Album, 
				 new String[]{D_Music.Album_id,D_Music.Album_name,D_Music.Album_time}, 
				 selection, 
				 selectionArgs,
				 null,
				 null,
				 orderBy);
		return cur;
	}
	public Cursor query_Music(String selection,String[] selectionArgs,String orderBy){
		db_r = myDatabase.getReadableDatabase();
		Cursor cur = db_r.query(D_Music.Musices, 
				 new String[]{D_Music.Music_id,D_Music.Music_name,D_Music.Music_Album,D_Music.Music_path}, 
				 selection, 
				 selectionArgs,
				 null,
				 null,
				 orderBy);
		return cur;
	}
	
	
	
	
	//***************************************************

}
