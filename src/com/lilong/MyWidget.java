package com.lilong;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

public class MyWidget extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// TODO Auto-generated method stub
		RemoteViews view = new RemoteViews(context.getPackageName(),R.layout.my_widget);
		
		Intent intent1 = new Intent("com.lilong.pre");
		PendingIntent pintent1 = PendingIntent.getBroadcast(context, 0, intent1, 0);
		view.setOnClickPendingIntent(R.id.widget_pre, pintent1);
		
		Intent intent2 = new Intent("com.lilong.next");
		PendingIntent pintent2 = PendingIntent.getBroadcast(context, 0, intent2, 0);
		view.setOnClickPendingIntent(R.id.widget_nex, pintent2);
		
		Intent intent3 = new Intent("com.lilong.music");
		PendingIntent pintent3 = PendingIntent.getBroadcast(context, 0, intent3, 0);
		view.setOnClickPendingIntent(R.id.widget_music, pintent3);
		
		Intent intent4 = new Intent("com.lilong.pause");
		PendingIntent pintent4 = PendingIntent.getBroadcast(context, 0, intent4, 0);
		view.setOnClickPendingIntent(R.id.widget_pause, pintent4);
		
		SharedPreferences.Editor edit = context.getSharedPreferences("widgetInfo", Activity.MODE_PRIVATE).edit();
		edit.putInt("myWidget_length", appWidgetIds.length);
		for(int i = 0 ;i < appWidgetIds.length;i++){
			edit.putInt("myWidgetes"+i,appWidgetIds[i]);
		}
		edit.commit();
		
		appWidgetManager.updateAppWidget(appWidgetIds, view);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
	
	public static void updataMyWidget(Context context,AppWidgetManager appWidgetManager,int[] appWidgetIds,String newName){
		RemoteViews myview = new RemoteViews(context.getPackageName(),R.layout.my_widget);
		myview.setTextViewText(R.id.widget_music, newName);
		for(int i = 0 ; i < appWidgetIds.length;i++){
			appWidgetManager.updateAppWidget(appWidgetIds[i], myview);
		}
	}
	

}
