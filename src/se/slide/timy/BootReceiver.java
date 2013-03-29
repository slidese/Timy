package se.slide.timy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Date;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        
        boolean prefRemind = sharedPreferences.getBoolean("remind_me", false);
        String prefTime = sharedPreferences.getString("remind_me_at", null);
        
        if (prefRemind && prefTime != null) {
            
            String[] pieces = prefTime.split(":");
            Calendar cal = Calendar.getInstance();
            Date now = new Date();
            cal.setTime(now);
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(pieces[0]));
            cal.set(Calendar.MINUTE, Integer.parseInt(pieces[1]));
            
            if (cal.getTime().getTime() < now.getTime())
                cal.add(Calendar.DAY_OF_MONTH, 1);
            
            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(context, AlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
            
            mgr.cancel(pi);
            
            mgr.setRepeating(AlarmManager.RTC, cal.getTimeInMillis(), 1*24*60*60*1000, pi);
            
            //mgr.setRepeating(AlarmManager.RTC, cal.getTimeInMillis(), 10000, pi);
            
        }
        
    }

}
