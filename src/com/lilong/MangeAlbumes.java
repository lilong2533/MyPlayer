package com.lilong;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.regex.Pattern;
import com.lilong.Variables.D_Music;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MangeAlbumes  extends Activity {
	long Album_Id ;//当前编辑的专辑ID
	String Album_Name ;
	int album_view_tag = 0;
	int remove_position = 0;
	
	EditText edit_album_name = null;
	TextView edit_last = null;
	TextView edit_last_2 = null;
	TextView edit_save = null;
	TextView edit_delete = null;
	
	ListView edit_local_file = null;
	ListView edit_music_list = null;
	
	//本地文件的File对象列表和文件名列表，后者作为显示数据
    LinkedList<File> local_music_list = new LinkedList<File>();
    ArrayList<String> local_musics_name = new ArrayList<String>();
    
    //新相册的File对象列表和文件名列表，后者作为显示数据
    LinkedList<File> album_music_list = new LinkedList<File>();
    LinkedList<File> temp1_music_list = new LinkedList<File>();
    LinkedList<File> temp2_music_list = new LinkedList<File>();
    ArrayList<String> album_musics_name = new ArrayList<String>();
	
    ContentResolver resolver = null;
	FilesOpration filesopration ;
    static final String PATH = "/sdcard/music";
    private String last_folder_path = PATH;//上一级文件夹路径
    
    private String Check_File_Path;//选择是否进入文件夹
    
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch(id){
		case 1:{
			Dialog dialog = new AlertDialog.Builder(MangeAlbumes.this)
			  .setMessage(Variables.check_new_album_name_IsNull)       
			  .setPositiveButton("确定", 
			     new DialogInterface.OnClickListener(){
				   public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}})
			  .create();  
			return dialog;
		}
		case 2:{
			Dialog dialog = new AlertDialog.Builder(MangeAlbumes.this)
			  .setMessage(Variables.check_new_album_Exist)       
			  .setPositiveButton("确定", 
			     new DialogInterface.OnClickListener(){
				   public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}})
			  .create();  
			return dialog;
		}
		case 3:{
			return click_folder();
		}
		case 4:{
			Dialog dialog = new AlertDialog.Builder(MangeAlbumes.this)
			  .setMessage("此文件已经添加！")       
			  .setPositiveButton("确定", 
			     new DialogInterface.OnClickListener(){
				   public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}})
			  .create();  
			return dialog;
		}
		}
		return null;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_music_list);
		resolver = getContentResolver();
		filesopration = new FilesOpration();
		
		edit_album_name = (EditText) this.findViewById(R.id.edit_album_name);
		
		edit_last = (TextView) this.findViewById(R.id.local_folder_last_edit);
		edit_last.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				show_local_music1(last_folder_path);
			}
		});
		edit_last_2 = (TextView) this.findViewById(R.id.edit_last_list);
		edit_last_2.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				show_all_album();
			}
		});
		
		edit_local_file = (ListView) this.findViewById(R.id.edit_all_music);
		edit_local_file.setOnItemClickListener(listener1);
		
		edit_music_list = (ListView) this.findViewById(R.id.edit_list);
		edit_music_list.setOnItemClickListener(listener2);
		
		edit_save = (TextView) this.findViewById(R.id.edit_save_list);
		edit_save.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				 sava_music_list();
			}
		});
		edit_delete = (TextView) this.findViewById(R.id.edit_delete_list);
		edit_delete.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				delete_album();
			}
		});
		
		show_local_music1(PATH);
		show_all_album();
	}
	
	public void prepare_local_music(String path){
		local_music_list.clear();
		local_musics_name.clear();
		File[] files = filesopration.scan_file(path);
		for(int i = 0;i < files.length;i++){
			local_music_list.add(files[i]);
			local_musics_name.add(files[i].getName());
		}
	}
	
	public void prepare_album_music(){
		album_music_list.clear();
		album_musics_name.clear();
		
		Cursor cur = resolver.query(D_Music.M_Content_URL, null,D_Music.Music_Album+"="+Album_Id, null, null);
	    Uri uri = ContentUris.withAppendedId(D_Music.A_Content_URL, Album_Id);
		Cursor cur_album = resolver.query(uri, null, null, null, null);
		if(cur_album != null){
			cur_album.moveToFirst();
			Album_Name = cur_album.getString(cur_album.getColumnIndex(D_Music.Album_name));
		}
		edit_album_name.setText(Album_Name);
		if(cur != null && cur.getCount() > 0){
	    	cur.moveToFirst();
    		do{
    			String name = cur.getString(cur.getColumnIndex(D_Music.Music_name));
    			String path = cur.getString(cur.getColumnIndex(D_Music.Music_path));
    			album_music_list.add(new File(path));
	    		album_musics_name.add(name);
    		}while(cur.moveToNext());
	    }
	}
	
	//显示本地文件
	public void show_local_music1(String path){
		prepare_local_music(path);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.new_album_temp1,R.id.local_music_name, local_musics_name);
		edit_local_file.setAdapter(adapter);
	}
	//显示播放列表
	public void show_all_album(){
		album_view_tag = 0;
		edit_album_name.setVisibility(View.INVISIBLE);
		edit_last_2.setVisibility(View.INVISIBLE);
		edit_save.setVisibility(View.INVISIBLE);
		edit_delete.setVisibility(View.INVISIBLE);
    	Cursor cur = resolver.query(D_Music.A_Content_URL, 
				null, null, null, null);
		String[] from = new String[]{D_Music.Album_name,D_Music.Album_time};
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.edit_show_all_list, cur, from, 
				new int[]{R.id.edit_Album_name,R.id.edit_Album_time});
		edit_music_list.setAdapter(adapter);
	}
    //显示某个播放列表
	public void show_local_music2(){
		//因为当点击本地的一个文件后也会调用这个方法。所以如果不加判断的话，就会在点击了本地的某个
		//文件后调用prepare_album_music将存在于数据库中的本Album的音乐文件再加载一次。
		if(album_view_tag == 0){
			prepare_album_music();
		}
		album_view_tag = 1;
		edit_album_name.setVisibility(View.VISIBLE);
		edit_last_2.setVisibility(View.VISIBLE);
		edit_save.setVisibility(View.VISIBLE);
		edit_delete.setVisibility(View.VISIBLE);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.new_album_temp2,R.id.new_album_musics, album_musics_name);
		edit_music_list.setAdapter(adapter);
	}
	public void delete_music(){
		String name = album_musics_name.get(remove_position);
		resolver.delete(D_Music.M_Content_URL, D_Music.Music_name+"=\""+name+"\"", null);
		album_music_list.remove(remove_position);
		album_musics_name.remove(remove_position);
		show_local_music2();
	}
	//保存编辑后的播放列表
	public void sava_music_list(){
		String albumName = edit_album_name.getText().toString();
		Log.i("new_album_name", albumName);
		if("".equals(albumName)){
			albumName = Album_Name;
		}
		if(Album_Name.equals(albumName)){
			save_list(albumName);
		}else{
			Cursor cur = resolver.query(D_Music.A_Content_URL, null, D_Music.Album_name+"=\""+albumName+"\"", null, null);
			if(cur.getCount() > 0){
				showDialog(2);
			}else{
				save_list(albumName);
			}
		}
	}
	public void save_list(String albumName){
		SimpleDateFormat myFmt=new SimpleDateFormat("yyyy/MM/dd");
		String time = myFmt.format(new Date());
		
		ContentValues vs = new ContentValues();
		vs.put(D_Music.Album_name, albumName);
		vs.put(D_Music.Album_time, time);
		Uri uri = ContentUris.withAppendedId(D_Music.A_Content_URL, Album_Id);
		resolver.update(uri, vs, null, null);
		//检索指定专辑目前存在于数据库中的全部数据，如果编辑过的专辑中的曲目在数据库中存在的话就不插入了
		Cursor cur_temp = resolver.query(D_Music.M_Content_URL, null,D_Music.Music_Album+"="+Album_Id, null, null);
		if(cur_temp != null && cur_temp.getCount() > 0){
			cur_temp.moveToFirst();
    		do{
    			String path = cur_temp.getString(cur_temp.getColumnIndex(D_Music.Music_path));
    			temp2_music_list.add(new File(path));
    		}while(cur_temp.moveToNext());
	    }
	    for(File file : temp2_music_list){
	    	if(album_music_list.contains(file)){
	    		album_music_list.remove(file);
	    	}
	    }
	    //---------------------------------
	    
	    if(!album_music_list.isEmpty()){
	    	for(File f : album_music_list){
	    		vs.clear();
	    		vs.put(D_Music.Music_name, f.getName());
	    		vs.put(D_Music.Music_Album, Album_Id);
	    		vs.put(D_Music.Music_path, f.getAbsolutePath());
	    		resolver.insert(D_Music.M_Content_URL, vs);
	    	}
	    }
	    show_all_album();
	}
	public void delete_album(){
		Uri uri = ContentUris.withAppendedId(D_Music.A_Content_URL,Album_Id);
		resolver.delete(uri, null, null);
		show_all_album();
	}
	
	//**************************************************
	OnItemClickListener listener1 = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> l, View v, int position,long id) {
			//----------------本地文件系统-----------------
//			if(album_view_tag != 0){
				String name = l.getItemAtPosition(position).toString();
				File file = local_music_list.get(position);
				//获取上一级文件夹路径。如果这里不加以判断的话，当用户进入一个没有子文件夹的文件夹(A)后。
				//如果点击了某个播放文件，则last_folder_path就会将文件夹A的路径记录。点击LAST就没有效果。
				if(file.isDirectory()){
					last_folder_path = file.getParentFile().getPath();
				}else{
					last_folder_path = file.getParentFile().getParentFile().getPath();
				}
				
				Log.i("last_folder_path", last_folder_path);
				if(name.contains(".")){
					if(Pattern.compile("^.+\\.mp3$").matcher(name.toLowerCase()).matches()){
						    //您点击的是mp3文件
						if(album_view_tag == 1){
							 //检索要插入的文件是否已经添加
							if(album_music_list.contains(file)){
								showDialog(4);
							}else{
								 album_music_list.add(file);
								 album_musics_name.add(name);
							}
							 show_local_music2();
						}
					}else{
						    //您点击的是文件夹或非mp3文件
					    	if(Pattern.compile("^.+\\.wma$").matcher(name.toLowerCase()).matches()){
					    		//在FileOpration类中过滤文件的方法有问题
					    	}else{
					    		Check_File_Path = file.getAbsolutePath();
					    		if(album_view_tag == 1){
									showDialog(3);
								}else{
									show_local_music1(Check_File_Path);
								}
					    	}
				    }
				}else{
					Check_File_Path = file.getAbsolutePath();
					if(album_view_tag == 1){
						showDialog(3);
					}else{
						show_local_music1(Check_File_Path);
					}
				}
//			}
		}
	};
	
	OnItemClickListener listener2 = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> l, View v, int position,long id) {
			Log.i("++++++++++++++", album_view_tag+"");
			switch(album_view_tag){
			case 0:{
				//点击了某个播放列表
				Album_Id = id;
				show_local_music2();
				break;
				}
			case 1:{
				//点击了某个播放列表中的音乐
				remove_position = position;
				delete_music();
				break;
				}
			}
	    }
	};
	
	public Dialog click_folder(){
		Dialog dialog = new AlertDialog.Builder(MangeAlbumes.this)
		  .setTitle("操作确认")
		  .setMessage("是否添加当前文件夹中音乐进入播放列表?")       
		  .setPositiveButton("确定", 
		     new DialogInterface.OnClickListener(){
			   public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				  filesopration.scan_file_into_list(Check_File_Path, temp1_music_list);
				  for(File f : temp1_music_list){
					  album_music_list.add(f);
					  album_musics_name.add(f.getName());
				  }
				  show_local_music2();
			}})
		  .setNeutralButton("取消", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				show_local_music1(Check_File_Path);
			}})
		  .create();   
		return dialog;
	}
	
}
