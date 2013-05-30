package com.lilong;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class MyBroadcast extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if("com.lilong.pre".equals(intent.getAction())){
			Bundle bundle = new Bundle();
			bundle.putString("opration", "pre");
			Intent next_intent = new Intent(context,Player.class);
			next_intent.putExtras(bundle);
			context.startService(next_intent);
			
		}else if("com.lilong.next".equals(intent.getAction())){
			Bundle bundle = new Bundle();
			bundle.putString("opration", "next");
			Intent next_intent = new Intent(context,Player.class);
			next_intent.putExtras(bundle);
			context.startService(next_intent);
		}else if("com.lilong.pause".equals(intent.getAction())){
			Bundle bundle = new Bundle();
			bundle.putString("opration", "pause");
			Intent next_intent = new Intent(context,Player.class);
			next_intent.putExtras(bundle);
			context.startService(next_intent);
		}else if("com.lilong.music".equals(intent.getAction())){
			Intent next_intent = new Intent(context,MainActivity.class);
			next_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(next_intent);
		}else if("com.lilong.refresh_widget".equals(intent.getAction())){
			Bundle bundle = intent.getExtras();
			String newName = bundle.getString("newName");
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			SharedPreferences sp = context.getSharedPreferences("widgetInfo", Activity.MODE_PRIVATE);
			int widget_length = sp.getInt("myWidget_length", 0);
			int appWidgetIds[] = new int[widget_length];
			for(int i = 0 ;i < widget_length;i++){
				appWidgetIds[i] = sp.getInt("myWidgetes"+i, 0);
			}
			MyWidget.updataMyWidget(context, appWidgetManager,appWidgetIds, newName);
		}
	}

}
