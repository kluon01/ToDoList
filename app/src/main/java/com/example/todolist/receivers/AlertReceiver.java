package com.example.todolist.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.todolist.presenter.NotificationHelper;

import androidx.core.app.NotificationCompat;

public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification(intent.getStringExtra("task"), intent.getStringExtra("desc"));
        notificationHelper.getManager().notify(1,nb.build());
    }

    public void startToDoAlarm(Context context, long alarmTime, long id, String task, String desc) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlertReceiver.class);
        intent.putExtra("task", task);
        intent.putExtra("desc", desc);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,(int)id,intent,0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
    }

    public void cancelToDoAlarm(Context context, long id) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,(int)id,intent,0);
        pendingIntent.cancel();
        alarmManager.cancel(pendingIntent);
    }
}
