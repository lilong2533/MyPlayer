package com.lilong;

import java.io.File;
import java.util.ArrayList;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;

public class Variables {
	public static final String Authority = "com.lilong";
	
	public static SharedPreferences Share_Preferences;
	public static final MediaPlayer myPlayer = new MediaPlayer();
	public static boolean file_or_list = false;//当前播放的是列表文件还是本地文件
	public static long last_album_id = 0;//上次播放的列表编号
	public static String last_album_path = "/sdcard/music";
	public static int last_music_id = 0;
	
	public static boolean Is_Playing = false;
	public static boolean Is_First_Time1 = true;
	public static boolean Is_First_Time2 = true;
	public static boolean Exit = false;
	
	public static long Current_Album = 1;
	public static String current_music_name = "";
	public static int current_music_max = 0;
	public static ArrayList<File> player_list = new ArrayList<File>();
	public static int mywidget_current_index = 0;
	public static final int NEW_SONG = 0;
	public static final int CLEAR_STATE = NEW_SONG +1;
	public static final int NEW_TIME = CLEAR_STATE +1;
	public static final int NEW_ERROR = NEW_TIME +1;
	public static final int NEW_SEEKBAR = NEW_ERROR+1 ;
	public static final int NEW_ANIMATION = NEW_SEEKBAR +1;
	public static final int NEW_TITLE = NEW_ANIMATION +1;
	
	public static final String check_new_album_name_IsNull = "专辑名称不允许为空！";
	public static final String check_new_album_Exist = "列表名存在！";
	
	static class D_Music{
		public static final Uri A_Content_URL = Uri.parse("content://"+ Authority +"/album");
		public static final Uri M_Content_URL = Uri.parse("content://"+ Authority +"/music");
		public static final String A_Content_Type = "vnd.android.cursor.dir/vnd.com.lilong.album";
		public static final String A_Content_Item_Type = "vnd.android.cursor.item/vnd.com.lilong.album";
		public static final String M_Content_Type = "vnd.android.cursor.dir/vnd.com.lilong.music";
		public static final String M_Content_Item_Type = "vnd.android.cursor.item/vnd.com.lilong.music";
		
		
		public static final String Database_name = "MyMusic.db";
		public static final String Album = "T_Album";
		public static final String Musices = "T_Music";
		public static final int Database_version = 4;
		
        public static final String Album_id = "_id";
        public static final String Album_name = "A_name";
        public static final String Album_time = "A_time";
        
        public static final String Music_id = "_id";
        public static final String Music_name = "M_name";
        public static final String Music_Album = "M_A_id";
        public static final String Music_path = "M_path";
        
		public static final String Create_Album = "create table " + Album +"("
		                                           + Album_id +" integer primary key autoincrement,"
		                                           + Album_name +" text not null,"
		                                           + Album_time +" integer);";
		public static final String Create_Music = "create table "+ Musices +"("
		                                           + Music_id +" integer primary key autoincrement,"
		                                           + Music_name + " text ,"
		                                           + Music_Album + " integer comment 'constraint forign key " 
		                                               + Music_Album + " references " + Album+"(" + Album_id + ")',"
		                                           + Music_path +" text);";
		
	}
	

}
