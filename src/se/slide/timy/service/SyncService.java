
package se.slide.timy.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import se.slide.timy.db.DatabaseManager;
import se.slide.timy.model.Report;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;

public class SyncService extends Service {
    
    public static final int ERROR_BAD_ACCOUNT = 1;
    public static final int ERROR_BAD_CALENDAR = 2;
    public static final int ERROR_NETWORK = 3;
    
    private static final String TAG = "SyncService";

    public static boolean isRunning = false;
    
    private CreateCalendarEventsTask mTask;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Created service");
        
        // ORMLite needs to be initiated
        DatabaseManager.init(this);
        
        SyncService.isRunning = true;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Started service");
        
        // Download alerts
        createEvents();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroyed service.");
        
        SyncService.isRunning = false;
    }

    // Uses AsyncTask to download the XML feed from the server.
    public void createEvents() {
        if (mTask == null)
            mTask = createTask();
        
        if (mTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
            
            mTask = createTask(); // The task has been run once, we need to create a new
            mTask.execute();
            
        } else if (mTask.getStatus().equals(AsyncTask.Status.PENDING)) {
            
            mTask.execute();
            
        } else if (mTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            // If one is currently running, we don't have to run another task
        }
        
    }
    
    public void runAgain() {
        List<Report> reports = DatabaseManager.getInstance().getAllReports();
        
        if (reports.size() > 0)
            createEvents();
    }
    
    private CreateCalendarEventsTask createTask() {
        String accountName = PreferenceManager.getDefaultSharedPreferences(this).getString("sync_google_calendar_account", null);
        String calendarId = PreferenceManager.getDefaultSharedPreferences(this).getString("sync_google_calendar_calendar_id", null);
        
        List<Report> reports = DatabaseManager.getInstance().getAllReports();
        
        return new CreateCalendarEventsTask(this, accountName, calendarId, reports);
    }

    private class CreateCalendarEventsTask extends AsyncTask<String, Void, Integer> {
        
        private WeakReference<Service> weakService;
        private String accountName;
        private String calendarId;
        private List<Report> reports;
        
        public CreateCalendarEventsTask(Service service, String accountName, String calendarId, List<Report> reports) {
            weakService = new WeakReference<Service>(service);
            this.accountName = accountName;
            this.calendarId = calendarId;
            this.reports = reports;
        }
        
        /* (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            
            
        }

        @Override
        protected Integer doInBackground(String... urls) {

            if (accountName == null)
                return SyncService.ERROR_BAD_ACCOUNT;
            if (calendarId == null)
                return SyncService.ERROR_BAD_CALENDAR;
            
            GoogleAccountCredential credential;
            final com.google.api.services.calendar.Calendar client;
            final HttpTransport transport = AndroidHttp.newCompatibleTransport();
            final JsonFactory jsonFactory = new GsonFactory();
            
            // Google Accounts
            credential = GoogleAccountCredential.usingOAuth2(weakService.get(), CalendarScopes.CALENDAR);
            credential.setSelectedAccountName(accountName);
            // Calendar client
            client = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential).setApplicationName("Timy/1.0")
                .build();
            
            for (int i = 0; i < reports.size(); i++) {
                Report report = reports.get(i);
                
                String allDayTime = DateFormat.format("yyyy-MM-dd", report.getDate()).toString();
                DateTime dt = new DateTime(allDayTime);    
                
                Event event = new Event();
                event.setSummary("Reported: " + report.getHours() + ":" + report.getMinutes());
                event.setStart(new EventDateTime().setDate(dt));
                event.setEnd(new EventDateTime().setDate(dt));
                
                try {
                    client.events().insert(calendarId, event).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }
            
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            
            //runAgain();
        }
    }

}
