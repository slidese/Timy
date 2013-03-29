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
import java.util.Set;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        
        boolean prefRemind = sharedPreferences.getBoolean("remind_me", false);
        String prefTime = sharedPreferences.getString("remind_me_at", null);
        Set<String> prefDays = sharedPreferences.getStringSet("remind_me_when", null); 
        
        if (prefRemind && prefTime != null && fireAlarmToday(prefDays)) {
            
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
            
            mgr.set(AlarmManager.RTC,
                    cal.getTimeInMillis(),
                    pi);
        }
        
        
    }
    
    private boolean fireAlarmToday(Set<String> prefDays) {
        if (prefDays == null)
            return false;
        
        Calendar checkDay = Calendar.getInstance();
        int dayOfWeek = checkDay.get(Calendar.DAY_OF_WEEK);
        
        for (String day : prefDays) {
            if (dayOfWeek == Calendar.MONDAY && day.equals("1"))
                return true;
            if (dayOfWeek == Calendar.TUESDAY && day.equals("2"))
                return true;
            if (dayOfWeek == Calendar.WEDNESDAY && day.equals("3"))
                return true;
            if (dayOfWeek == Calendar.THURSDAY && day.equals("4"))
                return true;
            if (dayOfWeek == Calendar.FRIDAY && day.equals("5"))
                return true;
            if (dayOfWeek == Calendar.SATURDAY && day.equals("6"))
                return true;
            if (dayOfWeek == Calendar.SUNDAY && day.equals("7"))
                return true;
        }
        
        return false;
    }

}
