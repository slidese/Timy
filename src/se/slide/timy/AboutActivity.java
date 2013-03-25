package se.slide.timy;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

public class AboutActivity extends FragmentActivity {

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        
        setContentView(R.layout.activity_about);
        
        String versionName = "1.0";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        
        TextView version = (TextView) findViewById(R.id.aboutAppVersion);
        version.setText("Version " + versionName);
        
    }

}
