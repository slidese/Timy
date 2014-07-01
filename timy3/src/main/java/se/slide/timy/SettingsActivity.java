
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.GooglePlayServicesUtil;
//import com.google.api.client.extensions.android.http.AndroidHttp;
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

import se.slide.timy.billing.IabHelper;
import se.slide.timy.billing.IabResult;
import se.slide.timy.billing.Purchase;
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
    
    /**
     * In app billing variables
     */

    // The helper object
    IabHelper mHelper;
    
    // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
    static final String SKU_SMALL = "donate_small";
    static final String SKU_MEDIUM = "donate_medium";
    static final String SKU_LARGE = "donate_large";
    
    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;

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

        /* base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
         * (that you got from the Google Play developer console). This is not your
         * developer public key, it's the *app-specific* public key.
         *
         * Instead of just storing the entire literal string here embedded in the
         * program,  construct the key at runtime from pieces or
         * use bit manipulation (for example, XOR with some other string) to hide
         * the actual key.  The key itself is not secret information, but we don't
         * want to make it easy for an attacker to replace the public key with one
         * of their own and then fake messages from the server.
         */
        
        /* 
         * We will ignore the advice given above because this is an open source app; you may what you wish with the code :)
         */
        String base64EncodedPublicKey = getString(R.string.google_base64_encoded_rsa_public_key);
        
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(false);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    // We should send something to Google Analytics...
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

            }
        });
        
        setupSimplePreferencesScreen();
    }
    
    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance(this).activityStop(this);
    }
    
    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
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
        
        // Donate
        Preference prefDonate = findPreference("donate");
        prefDonate.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
    }
    
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
             // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                
                if (preference.getKey().equals("donate")) {
                    
                    if (index == 0) {
                        // Small
                        onDonateButtonClicked(SKU_SMALL);
                    }
                    else if (index == 1) {
                        // Medium
                        onDonateButtonClicked(SKU_MEDIUM);
                    }
                    else if (index == 2) {
                        // Large
                        onDonateButtonClicked(SKU_LARGE);
                    }
                    
                    // Reset to default value; this is not a normal preference we would like to save
                    return false;
                }
                else {
                    // Set the summary to reflect the new value.
                    preference
                            .setSummary(index >= 0 ? listPreference.getEntries()[index]
                                    : null);

                    // Update the alarm
                    preference.getContext().sendBroadcast(
                            new Intent(preference.getContext(), BootReceiver.class));    
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

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
            /*
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
            */

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
            /*
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
            */

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

        // Pass on the activity result to the helper for handling
        if (mHelper == null || !mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
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
        else {
            // onActivityResult handled by IABUtil
        }
        
        
        
    }
    
    /**
     * User clicked to donate!
     * 
     */
    public void onDonateButtonClicked(String sku) {

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        
        /*
         * We will ignore this advice (as well) since we do not need to link this purchase to a "specific user" of our app, see this answer: http://stackoverflow.com/questions/14553515/why-is-it-important-to-set-the-developer-payload-with-in-app-billing
         */
        String payload = "";

        mHelper.launchPurchaseFlow(this, sku, RC_REQUEST, mPurchaseFinishedListener, payload);
    }
    
    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                return;
            }

            // Purchase successful

            if (purchase.getSku().equals(SKU_SMALL) || purchase.getSku().equals(SKU_MEDIUM) || purchase.getSku().equals(SKU_LARGE)) {
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            }

        }
    };
    
    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, thank our friendly user
                alert("Thank you! :)");
            }
            else {
                // something went wrong
                alert("Something went wrong...");
            }
            
        }
    };
    
    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }
    
    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton(R.string.ok, null);
        bld.create().show();
    }
    
}
