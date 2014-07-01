package se.slide.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class Statics {

    public static String getAppVersion(Context context) {
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        String v = "N/A";
        if (pInfo != null)
            v = pInfo.versionName;
        
        String message = String.format(context.getResources().getString(R.string.version), v);
        
        return message;
    }
    
}
