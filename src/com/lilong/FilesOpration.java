package com.lilong;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.regex.Pattern;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.lilong.Variables.D_Music;

public class FilesOpration{
	ContentResolver resolver = null;
	String[] files_path = null;
	String[] files_name = null;
	File[] file_files = null;
	boolean all_music_opration = true;
	File[] file_files_temp = null;
	
	boolean IsFirsttimeRun = true;
	
	long all_music_id = 1;
	public FilesOpration(String path,ContentResolver resolver){
		this.resolver = resolver;
		create_all_music_list();
		if(Variables.Is_First_Time1){
			resolver.delete(D_Music.M_Content_URL,D_Music.Music_Album+"="+1, null);
			scan_all_file(path);
			scan_current_all();
			Variables.Is_First_Time1 = false;
			
		}
	}
	public FilesOpration(){
	}
	
	//*************第一次运行程序的时候创建一个“默认的播放列表”，它存储的是music下所有的音乐文件
	public long create_all_music_list(){
		Cursor cur = resolver.query(D_Music.A_Content_URL, null, D_Music.Album_name+"="+"'All Music'", null, null);
		if(cur.getCount() <= 0){
			SimpleDateFormat myFmt=new SimpleDateFormat("yyyy/MM/dd");
			String time = myFmt.format(new Date());
			ContentValues vs = new ContentValues();
			vs.put(D_Music.Album_name, "All Music");
			vs.put(D_Music.Album_time, time);
			Uri uri = resolver.insert(D_Music.A_Content_URL, vs);
			return new Long(uri.getPathSegments().get(1));
		}else{
			return -1;
		}
	}
	public long get_all_music_list_id(){
		return all_music_id;
	}
	
	//*************扫描全部音乐文件并将其信息插入数据库
	public void scan_all_file(String path){
    	File file = new File(path);
        file_files = file.listFiles(new FilenameFilter(){
			public boolean accept(File dir, String filename) {
				// TODO Auto-generated method stub
			    if(dir.isDirectory()){
			    	return true;
			    }else{
			    	return Pattern.compile("^.+\\.mp3$").matcher(filename).matches();
			    }
			}
    	});
        if(all_music_opration){
        	file_files_temp = file_files;
        	all_music_opration = false;
        }
        insert_files(file_files);
        
	 }
	public void scan_current_all(){
		for(int i = 0 ;i < file_files_temp.length;i++){
        	if(file_files_temp[i].isDirectory()){
        		scan_all_file(file_files_temp[i].getPath());
        	}
        }
	}
	public void insert_files(File[] file_files){
		files_path = new String[file_files.length];
		files_name = new String[file_files.length];
		 //如果是第一次运行程序的话就将搜索到的.mp3文件的信息插入到数据库中
		for(int i =0; i < file_files.length;i++){
			files_path[i] = file_files[i].getAbsolutePath();
			files_name[i] = file_files[i].getName();
			String s = files_name[i];
			if(s.contains(".")){
				 if(Pattern.compile("^.+\\.mp3$").matcher(s.toLowerCase()).matches()){
					    ContentValues vs = new ContentValues();
						vs.put(D_Music.Music_name, files_name[i]);
						vs.put(D_Music.Music_Album, all_music_id);
						vs.put(D_Music.Music_path, files_path[i]);
						resolver.insert(D_Music.M_Content_URL, vs);
				  }
			}
		}
		
	}
	//**************为了在MainActivity中音乐文件，扫描某个目录下的所有音乐文件
	public File[] scan_file(String path){
		File file = new File(path);
        file_files = file.listFiles(new FilenameFilter(){
			public boolean accept(File dir, String filename) {
				// TODO Auto-generated method stub
			    if(dir.isDirectory()){
			    	return true;
			    }else{
			    	return Pattern.compile("^.+\\.mp3$").matcher(filename).matches();
			    }
			}
    	});
        return file_files;
	}
	
	//获取某个目录下所有文件的路径和文件名
	public void get_files_name(File[] file_files){
		files_path = new String[file_files.length];
		files_name = new String[file_files.length];
	  
		for(int i =0; i < file_files.length;i++){
			files_path[i] = file_files[i].getAbsolutePath();
			files_name[i] = file_files[i].getName();
		}
	}
	
	//将制定文件夹下的所有文件插入进入提供的列表中
	public void scan_file_into_list(String path,LinkedList<File> store){
		store.clear();
		File file = new File(path);
        File[] files = file.listFiles(new FilenameFilter(){
			public boolean accept(File dir, String filename) {
				// TODO Auto-generated method stub
			    return Pattern.compile("^.+\\.mp3$").matcher(filename).matches();
			}
    	});
		for(int i = 0 ; i < files.length ; i++){
			store.add(files[i]);
		}
	}
	
}
