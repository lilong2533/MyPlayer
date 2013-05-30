package com.lilong;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

public class Player extends Service {
	
	SharedPreferences uiState = null;
	final RemoteCallbackList<IMyCallback> callbackes = new RemoteCallbackList<IMyCallback>();
	private ArrayList<File> cur_list = null;
	private MediaPlayer myPlayer = null;
	
	private String current ;//当前播放曲目的绝对路径
	private int current_index = 0;//当前播放曲目的index
	private int list_max = 0;
	private boolean Single_Loop = false;//默认不是单曲循环，而是顺序播放
	private boolean List_Loop = true;//如果是顺序播放的话，则默认进行列表循环
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		myPlayer = Variables.myPlayer;
		cur_list = Variables.player_list;
		current_index = Variables.last_music_id;
		uiState = Variables.Share_Preferences;
		myPlayer.setOnCompletionListener(new OnCompletionListener(){
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				refresh_playing_state(3,false);
				if(!Single_Loop){
					music_playing_mode();
				}else{
					new_song();
					Play(current_index);
				}
			}
		});
		myPlayer.setOnErrorListener(new OnErrorListener(){
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// TODO Auto-generated method stub
				myPlayer.reset();
				return false;
			}
		});
	}
	
	
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		Log.i("mywidget", "onstart==============");
		Bundle bundle = intent.getExtras();
		String judge = bundle.getString("opration");
		if("pre".equals(judge)){
			Pre();
		}else if("next".equals(judge)){
			Next();
		}else if("music".equals(judge)){
			
		}else if("pause".equals(judge)){
			Pause();
		}
	}


	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.i("destroy player","+++++++");
		super.onDestroy();
		Log.i("destroy player","-------");
		Variables.last_music_id = current_index;
		SharedPreferences.Editor editor = uiState.edit();
		editor.putBoolean("file_or_path", Variables.file_or_list);
		if(Variables.file_or_list){
			editor.putLong("last_album_id", Variables.last_album_id);
			
		}else{
			editor.putString("last_album_path", Variables.last_album_path);
		}
		editor.putInt("last_music_id", Variables.last_music_id);
		editor.putBoolean("single_loop", Single_Loop);
		editor.putBoolean("list_loop", List_Loop);
		editor.putBoolean("isPlaying", myPlayer.isPlaying());
		editor.putInt("current_music_max", Variables.current_music_max);
		editor.commit();
		if(Variables.Exit){
			myPlayer.reset();
			Variables.Exit = false;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mbinder;
	}
	
	private final IMyService.Stub mbinder = new IMyService.Stub() {
		
		public void unregisterCallback(IMyCallback cb) throws RemoteException {
			// TODO Auto-generated method stub
			if(cb != null){
				callbackes.unregister(cb);
			}
		}
		
		public void registerCallback(IMyCallback cb) throws RemoteException {
			// TODO Auto-generated method stub
			if(cb != null){
				callbackes.register(cb);
			}
		}
		
		public void pre() throws RemoteException {
			// TODO Auto-generated method stub
		    Pre();
		}
		
		public void play(int index) throws RemoteException {
			Play(index);
		}
		
		public void pause() throws RemoteException {
			// TODO Auto-generated method stub
			Pause();
		}
		
		public void stop() throws RemoteException {
			// TODO Auto-generated method stub
			try{
					if(myPlayer.isPlaying()){
						myPlayer.reset();
						refresh_playing_state(3,false);
					}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		public void next() throws RemoteException {
			// TODO Auto-generated method stub
			Next();
		}

		public int getProgress() throws RemoteException {
			// TODO Auto-generated method stub
			return myPlayer.getCurrentPosition()/1000;
		}
		
		public void seekTo(int i) throws RemoteException {
			// TODO Auto-generated method stub
				if(myPlayer.isPlaying()){
					myPlayer.seekTo(i*1000);
				}
		}
		
		public int getCurrentMusic(){
			return current_index;
		}

		public String getCurrent() throws RemoteException {
			// TODO Auto-generated method stub
			return current;
		}

		public boolean IsPlaying() throws RemoteException {
			// TODO Auto-generated method stub
			return myPlayer.isPlaying();
		}

		public void setPlayingMode(boolean singleLoop, boolean listLoop)
				throws RemoteException {
			// TODO Auto-generated method stub
			Single_Loop = singleLoop;
		    List_Loop = listLoop;
		}
	};
	//******************************************************************************************************
	public void Play(int index){
		// TODO Auto-generated method stub
		Log.i("Player-index++++++", index+"");
		get_player_music(index);
		if(myPlayer != null){
			Log.i("current_path", current);
			myPlayer.reset();
			try {
				myPlayer.setDataSource(current);
				myPlayer.prepare();
				myPlayer.start();
				Variables.current_music_max = myPlayer.getDuration() / 1000;
				refresh_playing_state(1,false);
				refresh_widget();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				//文件不可读,提示
				String msg = cur_list.get(current_index).getName();
				error_notify(msg+" is Error File,Can't play!");
				if(!Single_Loop){
					music_playing_mode();
				}else{
					Next();
				}
			}
		}
	}
	
    public void Pause(){
		try{
			if(myPlayer.isPlaying()){
				myPlayer.pause();
				refresh_playing_state(2,true);
			}else{
				refresh_playing_state(2,false);
				myPlayer.start();
			}
	    }catch(Exception e){
		    e.printStackTrace();
	    }
    }
    
	public void Pre(){
		list_max = cur_list.size();
		current_index--;
		if(current_index < 0){
			current_index = 0;
		}
		new_song();
		Play(current_index);
	}
	
	public void Next(){
		list_max = cur_list.size();
		current_index++;
		if(current_index >= list_max){
		   current_index = list_max-1;
		}
		new_song();
		Play(current_index);
	}
	//*************************************************************************************************
	
	//控制播放列表
	public void get_player_music(int index){
		cur_list = Variables.player_list;
		if(!cur_list.isEmpty()){
			list_max = cur_list.size();
			current_index = index;
			Variables.mywidget_current_index = current_index;
			if(cur_list.get(index).isFile()){
				current = cur_list.get(index).getAbsolutePath();
				Variables.current_music_name =  current.substring(current.lastIndexOf("/")+1);
			}else{
				get_player_music(++index);
			}
		    
		}
	}
	//播放模式
	public void music_playing_mode(){
		cur_list = Variables.player_list;
		if(current_index == list_max-1){
			if(List_Loop){
				current_index = 0;
				new_song();
				Play(current_index);
			}else{
				myPlayer.reset();
			}
		}else{
			Next();
		}
	}
	
	//提示播放新歌
	public void new_song(){
		int N = callbackes.beginBroadcast();
		for(int i = 0 ;i < N ;i++){
			try {
				callbackes.getBroadcastItem(i).newSong();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		callbackes.finishBroadcast();
	}
	
	//更新播放状态
	public void refresh_playing_state(int state, boolean f){
		int N = callbackes.beginBroadcast();
		for(int i = 0 ;i < N ;i++){
			try {
				callbackes.getBroadcastItem(i).playingState(state,f);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		callbackes.finishBroadcast();
	}
	
	public void refresh_widget(){
		Intent intent = new Intent("com.lilong.refresh_widget");
		intent.putExtra("newName", Variables.current_music_name);
		sendBroadcast(intent);
	}
	
	//错误提示
	public void error_notify(String msg){
		int N = callbackes.beginBroadcast();
		for(int i = 0 ;i < N ;i++){
			try {
				callbackes.getBroadcastItem(i).musicError(msg);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		callbackes.finishBroadcast();
	}



	
	

}
