package com.lilong;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;
import com.lilong.Variables.D_Music;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends ListActivity {
	private static final String PATH = "/sdcard/music"; 
	
	private ContentResolver resolver = null;
	private SharedPreferences uiState = null;
	private FilesOpration fileOpration = null;
	private MyAsyncTask mySeekBarTask = new MyAsyncTask();
	private ArrayList<File> first_player_list = new ArrayList<File>();
	private NotificationManager notificationMag ;
	private String ErrorMessage;
	private Boolean isRefresh = false;
	
	private String[] files_path = null;
	private String[] files_name = null;
	private File[] file_files = null;
	
	private Message msg1;

	private String last_folder_path = PATH;//上一级文件夹路径
	
	//××××标题显示的数据××××
	private String music_time;
	private String music_state;
	private boolean clear_state = false;
	
	private IMyService myService = null;//与Player交互的接口
	private int view_tag = 0;/*ListView点击事件的标记.0 -->本地文件列表      1 -->播放列表     2-->确定的曲目
	                                                                        当点击进入播放列表A后，自动将A中的曲目存放如second_player_list中。
	                                                                        如果用户一旦点击了A中的某个曲目后，则用second_player_list去替换first_player_list
	                         */
	private long current_album = 1;
	private int current_music_index = 0;
	
	private boolean Single_Loop = false;
	private boolean List_Loop = true;
	
	private View myView = null;//设置播放模式的面板
	boolean isAnimation = false;
	private Animation myAnimation;
	private Dialog dialog = null;
	private RadioGroup radioG = null;
	private RadioButton r_button1 = null;
	private RadioButton r_button2 = null;
	private CheckBox c_button = null;
	private TextView v_music_time = null;
	private TextView v_music_info = null;
	private TextView refresh = null;
	private TextView all_list = null;
	private TextView cur_list = null;
	private TextView last_folder = null;
	private TextView play_mode = null;
	private TextView exit = null;
	
	private SeekBar seek_bar = null;
	private int seek_bar_progress = 10;
	
	private Button b_pre = null;
	private Button b_play = null;
	private Button b_pause = null;
	private Button b_stop = null;
	private Button b_next = null;
	
	private final int item_1 = 1;
	private final int item_2 = item_1 + 1;
	
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch(id){
		case 1:{
			dialog = new AlertDialog.Builder(MainActivity.this)
		 	   .setTitle("设置播放模式")
		 	   .setView(myView)
		 	   .setPositiveButton("确定",new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						 play_mode();
					}
		 	   })
		 	   .create();
			return dialog;
		}
		}
		return null;
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        resolver = getContentResolver();//与ConetentProvider交互的接口
        fileOpration = new FilesOpration(PATH,resolver);//操作本地music音乐文件的接口
        notificationMag = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        //获取上次程序退出时的播放信息
        uiState = getPreferences(Activity.MODE_PRIVATE);
        Variables.Share_Preferences = uiState;
        Variables.file_or_list = uiState.getBoolean("file_or_path", false);
        Variables.last_album_id = uiState.getLong("last_album_id", -1);
        Variables.last_album_path = uiState.getString("last_album_path", PATH);
        Variables.last_music_id = uiState.getInt("last_music_id", -1);
        Variables.Is_Playing = uiState.getBoolean("isPlaying", false);
        Variables.current_music_max = uiState.getInt("current_music_max", 0);
        Single_Loop = uiState.getBoolean("single_loop", false);
        List_Loop = uiState.getBoolean("list_loop", true);
        //**************************
        LayoutInflater factory = LayoutInflater.from(MainActivity.this);
        myView = factory.inflate(R.layout.playing_mode, null);//
        
        v_music_info = (TextView) this.findViewById(R.id.music_info);
        v_music_time = (TextView) this.findViewById(R.id.music_time);
        refresh = (TextView) this.findViewById(R.id.refresh);
        refresh.setVisibility(View.INVISIBLE);
        all_list = (TextView) this.findViewById(R.id.all_list);
        cur_list = (TextView) this.findViewById(R.id.cur_list);
        last_folder = (TextView) this.findViewById(R.id.last_folder);
        play_mode = (TextView) this.findViewById(R.id.p_mode);
        exit = (TextView) this.findViewById(R.id.exit);
        exit.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Variables.Exit = true;
				clear_state = true;
				notificationMag.cancel(1);
				if(mySeekBarTask != null){
					mySeekBarTask.cancel(true);
				}
				stopService(new Intent(MainActivity.this,Player.class));
				finish();
			}
        });
        
        b_pre = (Button) this.findViewById(R.id.pre);
        b_play = (Button) this.findViewById(R.id.play);
        b_pause = (Button) this.findViewById(R.id.pause);
        b_stop = (Button) this.findViewById(R.id.stop);
        b_next = (Button) this.findViewById(R.id.nex);
        seek_bar = (SeekBar) this.findViewById(R.id.pro_bar);
        
        //对话框控件
        radioG = (RadioGroup) myView.findViewById(R.id.mygroup);
        r_button1 = (RadioButton) myView.findViewById(R.id.radio1);
        r_button2 = (RadioButton) myView.findViewById(R.id.radio2);
        
        if(!Single_Loop){
        	r_button1.setChecked(true);
        }else{
        	r_button2.setChecked(true);
        }
        radioG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
		    	if(checkedId == r_button1.getId()){
				    Single_Loop = false;
			    }else{
				    Single_Loop = true;
				    c_button.setChecked(false);
			    }
			}
        });
        
        c_button = (CheckBox) myView.findViewById(R.id.check1);
        if(!Single_Loop){
        	if(List_Loop){
            	c_button.setChecked(true);
            }
        }else{
        	c_button.setChecked(false);
        }
        
        c_button.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked){
					List_Loop = true;
				}else{
					List_Loop = false;
				}
			}
        });
        //****************
        refresh.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// 更新AllMusic播放列表
				refresh();
			}
        });
        all_list.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// 显示播放列表
				setTitle("用戶播放列表");
				show_all_list();
			}
        });
        
        cur_list.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// 显示当前播放列表
				setTitle("当前播放列表");
				show_cur_list();
			}
        });
        
        last_folder.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// 返回上一级目录
				setTitle("本地播放列表");
				show_all(last_folder_path);
			}
        });
        
        play_mode.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// 设置播放模式
				setTitle("播放模式设置");
			    set_play_mode();
			}
        	
        });
        
        b_pre.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				try {
					myService.pre();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        });
        
        b_play.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(view_tag != 1){
					play(current_music_index);
					Variables.file_or_list = true;
				}
			}
        });
        
        b_pause.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				//mySeekBar.yield();
				try {
					myService.pause();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        });
        
        b_stop.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				music_state = " 停止播放";
				try {
					myService.stop();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        });
        
        b_next.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				try {
					myService.next();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        });
        //进度条操作
        seek_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser){
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onStopTrackingTouch(SeekBar seekBar) {
				try {
					myService.seekTo(seekBar.getProgress());
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        });
        //读取本地音乐信息文件，启动上次播放的音乐
        if( Variables.file_or_list){
        	if(Variables.last_album_id != -1 && Variables.last_music_id != -1){
        		view_tag = 2;
            	Cursor cur = resolver.query(D_Music.M_Content_URL, null,
        				D_Music.Music_Album+"="+Variables.last_album_id, null, null);
        		temp(cur);
        		current_music_index = Variables.last_music_id;
        		current_album = Variables.last_album_id;
        		  if(!Variables.Is_First_Time2){
        			if(Variables.Is_Playing){
        				seek_bar.setMax(Variables.current_music_max);
        				mySeekBarTask.execute((Void)null);
              		}
        	      }
        		  Variables.Is_First_Time2 = false;
        	}else{
        		show_all_list();
        	}
        }else{
        	if(Variables.last_music_id != -1){
        		current_music_index = Variables.last_music_id;
        		show_all(Variables.last_album_path);
        		 if(!Variables.Is_First_Time2){
         			if(Variables.Is_Playing){
         				 seek_bar.setMax(Variables.current_music_max);
        				mySeekBarTask.execute((Void)null);
               		}
         	      }
        		 Variables.Is_First_Time2 = false;
        	}else{
        		show_all(Variables.last_album_path);
        	}
        	
        }
        Variables.player_list = first_player_list;
    	//绑定Service
        bind_PlayerService();
    }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mySeekBarTask.cancel(true);
		unbind_PlayerService();
	}
    
    //***********************************************************Album Manage
    public void play_mode(){
    	try {
			myService.setPlayingMode(Single_Loop, List_Loop);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    //点击Mune按扭调用这个函数,如何动态改变Menu中的元素
    public boolean onPrepareOptionsMenu(Menu menu) { 
		menu.findItem(item_1).setVisible(true);
		menu.findItem(item_2).setVisible(true);
        return super.onPrepareOptionsMenu(menu);  
    }   
    public boolean onCreateOptionsMenu(Menu menu){
    	super.onCreateOptionsMenu(menu);
    	menu.add(0, item_1, 0, "添加");
    	menu.add(0, item_2, 0, "管理");
    	return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i("I had click Menu", "");
		switch(item.getItemId()){
    	case item_1:
    		add_Album(); break;
    	case item_2:
    		edit_Album(); break;
    	}
    	return super.onOptionsItemSelected(item);
	}
	
	public void add_Album(){
		Log.i("you will add new Album", "");
		Intent intent = new Intent(MainActivity.this,NewAlbum.class);
		startActivity(intent);
	}
	public void edit_Album(){
		Log.i("you will edit Album", "");
		Intent intent = new Intent(MainActivity.this,MangeAlbumes.class);
		startActivity(intent);
		
	}

    //**************************************************************** 文件操作
    //获取某个目录下所有文件的名字及路径
    public void get_files_name(File[] file_files){
		files_path = new String[file_files.length];
		files_name = new String[file_files.length];
	  
		for(int i =0; i < file_files.length;i++){
			files_path[i] = file_files[i].getAbsolutePath();
			files_name[i] = file_files[i].getName();
		}
	}
    
  //当点击一个文件时,判断该文件是不是目录.如果是的话就显示该文件中的内容
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		switch(view_tag){
		case 0:{
			//----------------本地文件系统-----------------
			String s = l.getItemAtPosition(position).toString();
			//获取上一级文件夹路径
			if(file_files[position].isDirectory()){
				last_folder_path = file_files[position].getParentFile().getPath();
			}else{
				last_folder_path = file_files[position].getParentFile().getParentFile().getPath();
			}
			
			Log.i("last_folder_path", last_folder_path);
			if(s.contains(".")){
				if(Pattern.compile("^.+\\.mp3$").matcher(s.toLowerCase()).matches()){
					    //您点击的是mp3文件
						play(position);
						Variables.file_or_list = false;
			    }else{
					    //您点击的是文件夹或非mp3文件
				    	if(Pattern.compile("^.+\\.wma$").matcher(s.toLowerCase()).matches()){
				    		//在FileOpration类中过滤文件的方法有问题
				    	}else{
				    		show_all(files_path[position]);
				    	}
			    }
			}else{
		    	show_all(files_path[position]);
			}
			break;
		}
		case 1:{
			show_list_item(id);
			//获取专辑编号
			Variables.last_album_id = id;
			break;
		}
		case 2:{
		    	play(position);
		    	Variables.file_or_list = true;
			break;
		}
		}
	}
 
    //××××××××××××××××××××××××××××××××××*********************************与Service交互的操作
	public void bind_PlayerService(){
		//启动PlayerService
		Intent intent = new Intent(this,Player.class);
		bindService(intent, myService_con, Context.BIND_AUTO_CREATE);
	}
	public void unbind_PlayerService(){
		unbindService(myService_con);
	}
    ServiceConnection myService_con = new ServiceConnection(){

		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			myService = IMyService.Stub.asInterface(service);
			try {
				myService.registerCallback(myCallback);
				play_mode();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			try {
				myService.unregisterCallback(myCallback);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			myService = null;
		}};
		
	IMyCallback myCallback = new IMyCallback.Stub() {
		
		public void newSong() throws RemoteException {
			// TODO Auto-generated method stub
			current_music_index = myService.getCurrentMusic();
			msg1 = myhandler1.obtainMessage(Variables.NEW_SONG);
			myhandler1.sendMessage(msg1);
		   }

		public void playingState(int i ,boolean b) throws RemoteException {
			// TODO Auto-generated method stub
			switch(i){
			case 1:{
				music_state = " 正在播放";
				current_music_index = myService.getCurrentMusic();
				msg1 = myhandler1.obtainMessage(Variables.NEW_SONG);
				myhandler1.sendMessage(msg1);
				
				seek_bar.setMax(Variables.current_music_max);
				if(!mySeekBarTask.isCancelled()){
					mySeekBarTask.cancel(true);
				}
				mySeekBarTask = new MyAsyncTask();
				mySeekBarTask.execute((Void)null);
				
				msg1 = myhandler5.obtainMessage(Variables.NEW_TITLE);
				myhandler5.sendMessage(msg1);
				break;}
			case 2:{
				if(b){
					music_state = " 暂停播放";
				}else{
					music_state = " 正在播放";
				}
				msg1 = myhandler5.obtainMessage(Variables.NEW_TITLE);
				myhandler5.sendMessage(msg1);
				break;}
			case 3:{
				music_state = " 停止播放";
				clear_state = true;
				msg1 = myhandler3.obtainMessage(Variables.CLEAR_STATE);
				myhandler3.sendMessage(msg1);
				break;}
			}
		}

		public void musicError(String msg) throws RemoteException {
			    //music文件不可读
			    ErrorMessage = msg;
		        msg1 = myhandler4.obtainMessage(Variables.NEW_ERROR);
				myhandler4.sendMessage(msg1);
		}
	};
    
    //----------------------刷新进度条的AsyncTask-------------------
    
    class MyAsyncTask extends AsyncTask<Void, Void, Integer>{
		@Override
		protected Integer doInBackground(Void... params) {
			refresh_seek_bar();
			return null;
		}
	}
    public void refresh_seek_bar(){
    	int mit,sec,max_mit,max_sec;
		try {
			clear_state = false;
			isAnimation = true;
			while(!clear_state){
				//正在播放|用户没有浏览播放界面
			    if(myService != null){
			    	seek_bar_progress = myService.getProgress();
	        		seek_bar.setProgress(seek_bar_progress);
	        		
	        		mit = seek_bar_progress/60;
	        		sec = seek_bar_progress%60;
	        		max_mit = Variables.current_music_max/60;
	        		max_sec = Variables.current_music_max%60;
	        		
	        		music_time = mit+":"+sec+" / "+max_mit+":"+max_sec ;
					current_music_index = myService.getCurrentMusic();
	    			msg1 = myhandler2.obtainMessage(Variables.NEW_TIME);
	    			myhandler2.sendMessage(msg1);
	                //set music_info's animation
    				if(isAnimation){
	    				msg1 = myhandler2_1.obtainMessage(Variables.NEW_ANIMATION);
		    			myhandler2_1.sendMessage(msg1);
		    			isAnimation = false;
	    			}
			    }
	    	    Thread.sleep(2000);
	    	}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    //----------------------------------------------------------------
    //开始播放新的歌曲，Notification 提示
    Handler myhandler1 = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Mynotify();
		}
    };
    public void Mynotify(){
    	Log.i("**********", "************");
    	Notification notification = new Notification(R.drawable.sun,Variables.current_music_name,
				System.currentTimeMillis());
    	//notify.tickerText = "++++++++++=";
    	notification.defaults = Notification.DEFAULT_ALL;
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this,MainActivity.class), 0);
    	notification.setLatestEventInfo(MainActivity.this, "当前播放", Variables.current_music_name, contentIntent);
		notificationMag.notify(1, notification);
    }
    
    //当歌曲播放的时候刷新标题栏
    Handler myhandler2 = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			v_music_time.setText(music_time);
		}
    };
    Handler myhandler2_1 = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int temp = Variables.current_music_name.length()*10;
			Log.i("temp's values", temp+"");
			myAnimation = new TranslateAnimation(250,-temp,0,0);
			myAnimation.setDuration(15000);
			myAnimation.setRepeatCount(50);
			myAnimation.setRepeatMode(1);
			myAnimation.setInterpolator(new LinearInterpolator());
			v_music_info.startAnimation(myAnimation);
			v_music_info.setText(Variables.current_music_name);
		}
    };
    //播放完毕，清空music_info | music_time
    Handler myhandler3 = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			setTitle(music_state);
			if(clear_state){
				seek_bar.setProgress(0);
				v_music_time.setText("");
				v_music_info.setText("");
				//clear_state = false;
			}
		}
    };
    Handler myhandler4 = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Toast.makeText(MainActivity.this,ErrorMessage, Toast.LENGTH_SHORT).show();
		}
    };
    Handler myhandler5 = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			setTitle(music_state);
		}
    };

    //***************************************************************控制播放
    public void play(int index){
    	Log.i("index+++++++", index+"");
    	Variables.player_list = first_player_list;
//    	Variables.Current_Album  = current_album;
    	try {
			myService.play(index);
			current_music_index = myService.getCurrentMusic();
			music_state = " 正在播放";
			Log.i("current_music_index", current_music_index+"");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    //*****************************************************ALL | CUR | MOD 对应的操作
    //显示本地/sdcard/music文件下的所有文件
    public void show_all(String path){
    	view_tag = 0;
    	refresh.setVisibility(View.INVISIBLE);
    	Variables.last_album_path = path;
    	file_files = fileOpration.scan_file(path);
    	first_player_list.clear();
    	for(int i = 0;i < file_files.length;i++){
    		first_player_list.add(file_files[i]);
    	}
		get_files_name(file_files);
		setListAdapter(new ArrayAdapter<String>(this,R.layout.show_all,R.id.File_name,files_name));
    }
    
    //显示所有播放列表，至少存在  All Music
    public void show_all_list(){
    	view_tag = 1;
    	refresh.setVisibility(View.INVISIBLE);
    	Cursor cur = resolver.query(D_Music.A_Content_URL, 
				null, null, null, null);
		String[] from = new String[]{D_Music.Album_name,D_Music.Album_time};
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.show_all_list, cur, from, 
				new int[]{R.id.Album_name,R.id.Album_time});
		setListAdapter(adapter);
    }
    
    public void show_list_item(long id){
    	view_tag = 2;
    	if(id == 1){
    		refresh.setVisibility(View.VISIBLE);
    	}else{
    		refresh.setVisibility(View.INVISIBLE);
    	}
    	Cursor cur = resolver.query(D_Music.M_Content_URL, null, 
		          D_Music.Music_Album +" = "+id, null, null);
    	current_album = id;
    	temp(cur);
    }
    //显示当前播放曲目所在的列表，如果当前没有播放则显示上一次播放的列表，如果上次播放列表不存在显示All Music
    public void show_cur_list(){
    	try {
    		if(myService.IsPlaying()){
    			if(Variables.file_or_list){
    	    		Cursor cur = resolver.query(D_Music.M_Content_URL, null,
    	    				D_Music.Music_Album+"="+current_album, null, null);
    	    		temp(cur);
    	    	}else{
					String temp = myService.getCurrent();
					if(temp != null){
						show_all(new File(temp).getParentFile().getPath());
					}
    	    	}
    		}
    	} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public void temp(Cursor cur ){
    	first_player_list.clear();
    	if(cur != null && cur.getCount() > 0){
    		cur.moveToFirst();
    		do{
    			String path = cur.getString(cur.getColumnIndex(D_Music.Music_path));
    			first_player_list.add(new File(path));
    		}while(cur.moveToNext());
    	}
    	String[] from = new String[]{D_Music.Music_name};
    	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.show_all, cur, from, 
				new int[]{R.id.File_name});
    	setListAdapter(adapter);
    }
    //设置播放模式
    public void set_play_mode(){
    	this.showDialog(1);
    }
    // 更新AllMusic播放列表
    public void refresh(){
    	isRefresh = true;
    	resolver.delete(D_Music.M_Content_URL,D_Music.Music_Album+"="+1, null);
    	fileOpration.scan_all_file(PATH);
     	if(isRefresh){
    		show_list_item(1);
    		isRefresh = false;
    	}
    }
    
    

    
    
    
    
    
    
    
    
    
    
    
    
    
}