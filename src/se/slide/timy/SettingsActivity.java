
package se.slide.timy;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.ColorDefinition;
import com.google.api.services.calendar.model.Colors;

import se.slide.timy.db.DatabaseManager;
import se.slide.timy.model.Color;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {

    static final int REQUEST_GET_PERMISSIONS = 1;
    static final int REQUEST_ACCOUNT_PICKER = 2;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 3;

    ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen() {

        PreferenceCategory fakeHeader;

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_sync);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_data_sync);

        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_about);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_about);

        // Reminder
        Preference remindMe = findPreference("remind_me");
        remindMe.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = (Boolean) newValue;

                Context context = preference.getContext();

                if (checked)
                    sendBroadcast(new Intent(context, BootReceiver.class));
                else {
                    AlarmManager mgr = (AlarmManager) context
                            .getSystemService(Context.ALARM_SERVICE);
                    Intent i = new Intent(context, AlarmReceiver.class);
                    PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
                    mgr.cancel(pi);
                }

                return true;
            }
        });

        // Remind me at
        Preference remindMeAt = findPreference("remind_me_at");
        remindMeAt.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String stringValue = getFormattedTime(newValue.toString());
                preference.setSummary(stringValue);

                sendBroadcast(new Intent(preference.getContext(), BootReceiver.class));

                return true;
            }
        });

        String remindMeAtSummary = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(remindMeAt.getKey(), "");

        if (remindMeAtSummary != null && remindMeAtSummary.length() > 0) {
            remindMeAt.setSummary(getFormattedTime(remindMeAtSummary));
        }

        // Remind me at
        Preference remindMeWhen = findPreference("remind_me_when");
        remindMeWhen.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                @SuppressWarnings("unchecked")
                String value = getFormattedDays((Set<String>) newValue);
                preference.setSummary(value);

                return true;
            }
        });

        Set<String> values = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getStringSet(remindMeWhen.getKey(), new HashSet<String>());

        String remindMeWhenSummary = getFormattedDays(values);
        remindMeWhen.setSummary(remindMeWhenSummary);

        // Account
        Preference syncGoogleAccountPref = findPreference("sync_google_calendar_account");
        syncGoogleAccountPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                String accountName = PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), "");

                GoogleAccountCredential credential;
                credential = GoogleAccountCredential.usingOAuth2(preference.getContext(),
                        CalendarScopes.CALENDAR);
                credential.setSelectedAccountName(accountName);

                // Check for Google Play
                if (checkGooglePlayServicesAvailable()) {
                    try {
                        startActivityForResult(credential.newChooseAccountIntent(),
                                REQUEST_ACCOUNT_PICKER);
                    } catch (ActivityNotFoundException e) {
                        // I'm not sure but I think this is the error (when
                        // testing on AVD)
                        Toast.makeText(preference.getContext(), R.string.no_google_play_on_device,
                                Toast.LENGTH_SHORT).show();
                    }
                }

                return false;
            }
        });

        String accountName = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(syncGoogleAccountPref.getKey(), "");
        syncGoogleAccountPref.setSummary(accountName);

        // Google Calendar
        Preference syncGoogleCalendarPref = findPreference("sync_google_calendar_calendar");
        syncGoogleCalendarPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                /*
                boolean hasPermissions = PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getBoolean("sync_google_permission", false);
                
                if (!hasPermissions) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(preference.getContext());
                    alertDialogBuilder.setTitle(R.string.google_permission_title);
                    alertDialogBuilder.setMessage(R.string.google_permission_message);
                    alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialogBuilder.create().show();
                    
                    return true;
                }
                */
                
                String accountName = PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString("sync_google_calendar_account", "");

                if (accountName == null || accountName.length() < 1) {
                    final Preference accountPref = findPreference("sync_google_calendar_account");
                    accountPref.getOnPreferenceClickListener().onPreferenceClick(accountPref);

                    return true;
                }

                getCalendarData(accountName, GetColorsAsyncTask.TASK_MODE_ALL);

                return true;
            }
        });

        String calendarName = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("sync_google_calendar_calendar_name", "");
        syncGoogleCalendarPref.setSummary(calendarName);
    }

    @SuppressLint("SimpleDateFormat")
    private String getFormattedTime(String prefTime) {
        String[] pieces = prefTime.split(":");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(pieces[0]));
        cal.add(Calendar.MINUTE, Integer.parseInt(pieces[1]));

        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        try {
            Date time = format.parse(prefTime);
            return android.text.format.DateFormat.getTimeFormat(this).format(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String getFormattedDays(Set<String> values) {

        StringBuilder builder = new StringBuilder();
        if (values.contains("1"))
            builder.append("Mon");

        if (values.contains("2")) {
            if (builder.length() > 0)
                builder.append(", ");
            builder.append("Tue");
        }
        if (values.contains("3")) {
            if (builder.length() > 0)
                builder.append(", ");
            builder.append("Wed");
        }
        if (values.contains("4")) {
            if (builder.length() > 0)
                builder.append(", ");
            builder.append("Thu");
        }
        if (values.contains("5")) {
            if (builder.length() > 0)
                builder.append(", ");
            builder.append("Fri");
        }
        if (values.contains("6")) {
            if (builder.length() > 0)
                builder.append(", ");
            builder.append("Sat");
        }
        if (values.contains("7")) {
            if (builder.length() > 0)
                builder.append(", ");
            builder.append("Sun");
        }

        return builder.toString();
    }

    private class GetColorsAsyncTask extends AsyncTask<Void, Void, List<Color>> {
        
        public static final int TASK_MODE_PERMISSION_ONLY = 0;
        public static final int TASK_MODE_ALL = 1;

        private String accountName;
        private WeakReference<Activity> weakActivity;
        private int mode;
        private int error;
        private Intent intent;

        public GetColorsAsyncTask(String accountName, Activity activity, int mode) {
            this.accountName = accountName;
            this.mode = mode;
            this.error = 0;
            weakActivity = new WeakReference<Activity>(activity);
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Activity activity = weakActivity.get();
            if (activity != null) {
                ((SettingsActivity) activity).showProgressDialog(true);
                
                if (mode == TASK_MODE_ALL)
                    ((SettingsActivity) activity).setProgressTitle(R.string.getting_colors);
                else
                    ((SettingsActivity) activity).setProgressTitle(R.string.checking_permission);
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected List<Color> doInBackground(Void... params) {
            try {
                final com.google.api.services.calendar.Calendar client;
                final HttpTransport transport = AndroidHttp.newCompatibleTransport();
                final JsonFactory jsonFactory = new GsonFactory();

                GoogleAccountCredential credential;
                credential = GoogleAccountCredential.usingOAuth2(weakActivity.get(),
                        CalendarScopes.CALENDAR);
                credential.setSelectedAccountName(accountName);
                // Calendar client
                client = new com.google.api.services.calendar.Calendar.Builder(
                        transport, jsonFactory, credential).setApplicationName("Timy/1.0")
                        .build();

                // Fire the request
                Colors eventColors = client.colors().get().execute();
                Set<Entry<String, ColorDefinition>> sets = eventColors.getEvent().entrySet();

                List<Color> colors = new ArrayList<Color>();
                for (Entry<String, ColorDefinition> entry : sets) {
                    Color color = new Color();
                    color.setId(entry.getKey());

                    ColorDefinition colorDefinitions = entry.getValue();
                    color.setBackgroundColor(colorDefinitions.getBackground());
                    color.setForegroundColor(colorDefinitions.getForeground());

                    colors.add(color);
                }

                return colors;
            } catch (UserRecoverableAuthIOException e) {
                error = 1;
                intent = e.getIntent();
            } catch (IOException e) {
                error = 2;
                e.printStackTrace();
            }

            return null;
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(List<Color> result) {
            super.onPostExecute(result);

            Activity activity = weakActivity.get();
            if (activity != null) {
                
                if (error == 0) {
                    
                    if (result != null)
                        ((SettingsActivity) activity).saveColors(result);
                
                    if (mode == TASK_MODE_ALL)
                        ((SettingsActivity) activity).getCalendars(accountName);
                    else
                        ((SettingsActivity) activity).showProgressDialog(false);
                }
                else {
                    
                    ((SettingsActivity) activity).showProgressDialog(false);
                    
                    if (error == 1)
                        startActivityForResult(intent, REQUEST_GET_PERMISSIONS);
                }
                
            }
        }

    }
    
    /*
    public void showRequestPermissionDialog(Intent intent) {
        startActivityForResult(intent, REQUEST_ACCOUNT_PICKER);
    }
    */
    
    public void setProgressTitle(int resId) {
        if (mProgress != null)
            mProgress.setMessage(getString(resId));
    }

    public void showProgressDialog(boolean show) {
        if (show) {
            mProgress = ProgressDialog.show(this, getString(R.string.please_wait),
                    getString(R.string.getting_calendar_info), true);
        }
        else {
            if (mProgress != null && mProgress.isShowing())
                mProgress.dismiss();
        }
    }

    public void saveColors(List<Color> colors) {
        DatabaseManager.getInstance().saveColors(colors);
    }

    private class GetCalendarsAsyncTask extends AsyncTask<Void, Void, CalendarList> {

        private String accountName;
        private WeakReference<Activity> weakActivity;

        public GetCalendarsAsyncTask(String accountName, Activity activity) {
            this.accountName = accountName;
            weakActivity = new WeakReference<Activity>(activity);
        }

        /* (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            
            Activity activity = weakActivity.get();
            if (activity != null) {
                ((SettingsActivity) activity).setProgressTitle(R.string.getting_calendar_info);
            }
        }

        @Override
        protected CalendarList doInBackground(Void... urls) {

            try {
                final com.google.api.services.calendar.Calendar client;
                final HttpTransport transport = AndroidHttp.newCompatibleTransport();
                final JsonFactory jsonFactory = new GsonFactory();

                GoogleAccountCredential credential;
                credential = GoogleAccountCredential.usingOAuth2(weakActivity.get(),
                        CalendarScopes.CALENDAR);
                credential.setSelectedAccountName(accountName);
                // Calendar client
                client = new com.google.api.services.calendar.Calendar.Builder(
                        transport, jsonFactory, credential).setApplicationName(getString(R.string.app_name))
                        .build();

                // Fire the request
                String FIELDS = "id,summary";
                final String FEED_FIELDS = "items(" + FIELDS + ")";
                CalendarList feed = client.calendarList().list().setFields(FEED_FIELDS).execute();

                return feed;
            } catch (UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), REQUEST_ACCOUNT_PICKER);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(CalendarList result) {
            super.onPostExecute(result);

            Activity activity = weakActivity.get();
            if (activity != null && result != null) {
                ((SettingsActivity) activity).showProgressDialog(false);
                ((SettingsActivity) activity).displayCalendarList(result);
            }
        }

    }

    public void getCalendarData(String accountName, int mode) {
        GetColorsAsyncTask getColors = new GetColorsAsyncTask(accountName, SettingsActivity.this, mode);
        getColors.execute();

    }

    public void getCalendars(String accountName) {
        GetCalendarsAsyncTask getCalendars = new GetCalendarsAsyncTask(accountName,
                SettingsActivity.this);
        getCalendars.execute();
    }

    public void displayCalendarList(CalendarList feed) {
        List<CalendarListEntry> list = feed.getItems();

        final CharSequence[] entries = new CharSequence[list.size()];
        final String[] calendarIds = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            entries[i] = list.get(i).getSummary();
            calendarIds[i] = list.get(i).getId();
        }

        int calendarId = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getInt("sync_google_calendar_calendar", 0);

        if (calendarId > entries.length)
            calendarId = 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a calendar")
                .setSingleChoiceItems(entries, calendarId, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int item) {
                        PreferenceManager
                                .getDefaultSharedPreferences(SettingsActivity.this)
                                .edit()
                                .putString("sync_google_calendar_calendar_name",
                                        entries[item].toString()).commit();
                        PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit()
                                .putString("sync_google_calendar_calendar_id", calendarIds[item])
                                .commit();
                        PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit()
                                .putInt("sync_google_calendar_calendar", item).commit();
                        Preference syncGoogleCalendarPref = findPreference("sync_google_calendar_calendar");
                        syncGoogleCalendarPref.setSummary(entries[item]);
                        dialogInterface.dismiss();
                    }
                });

        builder.create().show();

    }

    /** Check that Google Play services APK is installed and up to date. */
    private boolean checkGooglePlayServicesAvailable() {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }
        return true;
    }

    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode, SettingsActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see android.preference.PreferenceActivity#onActivityResult(int, int,
     * android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == Activity.RESULT_OK
                && data != null && data.getExtras() != null) {
            String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);

            Preference syncGoogleAccountPref = findPreference("sync_google_calendar_account");
            syncGoogleAccountPref.getEditor()
                    .putString("sync_google_calendar_account", accountName).commit();
            syncGoogleAccountPref.setSummary(accountName);
            
            // To trigger the permission dialog, request something from the API
            getCalendarData(accountName, GetColorsAsyncTask.TASK_MODE_PERMISSION_ONLY);
        }
        else if (requestCode == REQUEST_GET_PERMISSIONS && resultCode == Activity.RESULT_OK
                && data != null && data.getExtras() != null) {
            
            // Save permission
            Preference syncGoogleAccountPref = findPreference("sync_google_calendar_account");
            syncGoogleAccountPref.getEditor()
                    .putBoolean("sync_google_permission", true).commit();
            
        }
        else if (requestCode == REQUEST_GET_PERMISSIONS && resultCode == Activity.RESULT_CANCELED) {
            
            // Save permission - make sure MainActivity shows dialog if this is null/bad
            Preference syncGoogleAccountPref = findPreference("sync_google_calendar_account");
            syncGoogleAccountPref.getEditor()
                    .putBoolean("sync_google_permission", false).commit();
            
        }
    }
    
}
