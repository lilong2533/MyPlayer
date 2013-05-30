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
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class NewAlbum extends Activity {
	ContentResolver resolver = null;
	
	EditText new_album = null;
	ListView all_music = null;
	ListView new_list = null;
	TextView local_last = null;
	TextView local_save = null;
	
	String new_album_name = null;
	//本地文件的File对象列表和文件名列表，后者作为显示数据
    LinkedList<File> local_music_list = new LinkedList<File>();
    ArrayList<String> local_musics_name = new ArrayList<String>();
    
    //新相册的File对象列表和文件名列表，后者作为显示数据
    LinkedList<File> album_music_list = new LinkedList<File>();
    LinkedList<File> temp_music_list = new LinkedList<File>();
    ArrayList<String> album_musics_name = new ArrayList<String>();
    
    FilesOpration filesopration ;
    static final String PATH = "/sdcard/music";
    private String last_folder_path = PATH;//上一级文件夹路径
    private String Check_File_Path;//选择是否进入文件夹

    
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch(id){
		case 1:{
			Dialog dialog = new AlertDialog.Builder(NewAlbum.this)
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
			Dialog dialog = new AlertDialog.Builder(NewAlbum.this)
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
		}
		return null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_album);
		resolver = this.getContentResolver();
		filesopration = new FilesOpration();

		new_album = (EditText) this.findViewById(R.id.new_album_name);
		
		all_music = (ListView) this.findViewById(R.id.all_music);
		all_music.setOnItemClickListener(listener1);
		
		new_list = (ListView) this.findViewById(R.id.new_list);
		new_list.setOnItemClickListener(listener2);
		
		local_last = (TextView) this.findViewById(R.id.local_folder_last);
		local_last.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				show_local_music1(last_folder_path);
			}
		});
		
		local_save = (TextView) this.findViewById(R.id.new_album_save);
		local_save.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				save_new_album();
			}
		});
		show_local_music1(PATH);
	}
	
	
	public void prepare_music(String path){
		local_music_list.clear();
		local_musics_name.clear();
		File[] files = filesopration.scan_file(path);
		for(int i = 0;i < files.length;i++){
			local_music_list.add(files[i]);
			local_musics_name.add(files[i].getName());
		}
	}
	
	public void show_local_music1(String path){
		prepare_music(path);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.new_album_temp1,R.id.local_music_name, local_musics_name);
		all_music.setAdapter(adapter);
	}
	
	public void show_local_music2(){
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.new_album_temp2,R.id.new_album_musics, album_musics_name);
		new_list.setAdapter(adapter);
	}
	
	public synchronized void save_new_album(){
		String albumName = new_album.getText().toString();
		Log.i("new_album_name", albumName);
		if(!"".equals(albumName)){
			Cursor cur = resolver.query(D_Music.A_Content_URL, null, D_Music.Album_name+"='"+albumName+"'", null, null);
			if(cur.getCount() > 0){
				this.showDialog(2);
			}else{
				SimpleDateFormat myFmt=new SimpleDateFormat("yyyy/MM/dd");
				String time = myFmt.format(new Date());
				
				ContentValues vs1 = new ContentValues();
				vs1.put(D_Music.Album_name, albumName.toString());
				vs1.put(D_Music.Album_time, time);
				Uri album_uri = resolver.insert(D_Music.A_Content_URL, vs1);
			    long id = new Long(album_uri.getPathSegments().get(1));
			    if(!album_music_list.isEmpty()){
			    	for(File f : album_music_list){
			    		vs1.clear();
			    		vs1.put(D_Music.Music_name, f.getName());
			    		vs1.put(D_Music.Music_Album, id);
			    		vs1.put(D_Music.Music_path, f.getAbsolutePath());
			    		resolver.insert(D_Music.M_Content_URL, vs1);
			    	}
			    }
			    Intent intent = new Intent(NewAlbum.this,MainActivity.class);
			    startActivity(intent);
			}
		}else{
			this.showDialog(1);
		}
	}
	
	public Dialog click_folder(){
		Dialog dialog = new AlertDialog.Builder(NewAlbum.this)
		  .setTitle("操作确认")
		  .setMessage("是否添加当前文件夹中音乐进入播放列表?")       
		  .setPositiveButton("确定", 
		     new DialogInterface.OnClickListener(){
			   public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				  filesopration.scan_file_into_list(Check_File_Path, temp_music_list);
				  for(File f : temp_music_list){
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
	
	OnItemClickListener listener1 = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> l, View v, int position,long id) {
			//----------------本地文件系统-----------------
			v.setFocusable(true);
			String name = l.getItemAtPosition(position).toString();
			File file = local_music_list.get(position);
			//获取上一级文件夹路径
			if(file.isDirectory()){
				last_folder_path = file.getParentFile().getPath();
			}else{
				last_folder_path = file.getParentFile().getParentFile().getPath();
			}
			
			Log.i("last_folder_path", last_folder_path);
			if(name.contains(".")){
				if(Pattern.compile("^.+\\.mp3$").matcher(name.toLowerCase()).matches()){
					    //您点击的是mp3文件
					 album_music_list.add(file);
					 album_musics_name.add(name);
					 show_local_music2();

				}else{
					    //您点击的是文件夹或非mp3文件
				    	if(Pattern.compile("^.+\\.wma$").matcher(name.toLowerCase()).matches()){
				    		//在FileOpration类中过滤文件的方法有问题
				    	}else{
				    		Check_File_Path = file.getAbsolutePath();
				    		showDialog(3);
				    	}
			    }
			}else{
				Check_File_Path = file.getAbsolutePath();
				showDialog(3);
			}
		}
	};
	
	OnItemClickListener listener2 = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> l, View v, int position,long id) {
			v.setFocusable(true);
			 album_music_list.remove(position);
			 album_musics_name.remove(position);
			 show_local_music2();
	    }
	};
	
	
	
	
	
	
	
	
	
	
	

}
