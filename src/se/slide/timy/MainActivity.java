
package se.slide.timy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.viewpagerindicator.TitlePageIndicator;

import se.slide.timy.HoursDialog.HoursDialogListener;
import se.slide.timy.InputDialog.EditNameDialogListener;
import se.slide.timy.db.DatabaseManager;
import se.slide.timy.model.Category;
import se.slide.timy.model.Project;
import se.slide.timy.model.Report;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class MainActivity extends FragmentActivity implements EditNameDialogListener, HoursDialogListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    TitlePageIndicator mIndicator;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        DatabaseManager.init(this);
        
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        
        //Bind the title indicator to the adapter
        //UnderlinePageIndicator titleIndicator = (UnderlinePageIndicator)findViewById(R.id.indicator);
        //titleIndicator.setViewPager(mViewPager);
        
        mIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mViewPager);
        
        //tempy();

    }
    
    public void tempy() {
        List<Report> reports = DatabaseManager.getInstance().getAllReports();
        //int size = reports.size();
        
        GoogleAccountCredential credential;
        final com.google.api.services.calendar.Calendar client;
        final HttpTransport transport = AndroidHttp.newCompatibleTransport();
        final JsonFactory jsonFactory = new GsonFactory();
        
        // Google Accounts
        credential = GoogleAccountCredential.usingOAuth2(this, CalendarScopes.CALENDAR);
        credential.setSelectedAccountName("www.slide.se@gmail.com");
        // Calendar client
        client = new com.google.api.services.calendar.Calendar.Builder(
            transport, jsonFactory, credential).setApplicationName("Timy/1.0")
            .build();
        
        //startActivityForResult(credential.newChooseAccountIntent(), 2);
        
        MyAsyncTask task = new MyAsyncTask(client);
        task.execute();
        
    }
    
    private class MyAsyncTask extends AsyncTask<Void, Void, Integer> {
        com.google.api.services.calendar.Calendar client;
        
        public MyAsyncTask(com.google.api.services.calendar.Calendar client) {
            this.client = client;
        }
        
        @Override
        protected Integer doInBackground(Void... urls) {
            
            try {
                String FIELDS = "id,summary";
                final String FEED_FIELDS = "items(" + FIELDS + ")";
                @SuppressWarnings("unused")
                CalendarList feed = client.calendarList().list().setFields(FEED_FIELDS).execute();
                
                String m = "Mike";
                
                
            } catch (UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), 2);
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            return 0;
        }
    }
    
    /** Check that Google Play services APK is installed and up to date. */
    private boolean checkGooglePlayServicesAvailable() {
      final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
      if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
        //showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        return false;
      }
      return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onMenuItemSelected(int, android.view.MenuItem)
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        
        if (item.getItemId() == R.id.menu_add_project) {
            
            FragmentManager fm = getSupportFragmentManager();
            InputDialog dialog = InputDialog.newInstance(getString(R.string.hint_add_project), InputDialog.TYPE_PROJECT);
            dialog.show(fm, "dialog_add_project");
            
            
            
            return true;
        }
        else if (item.getItemId() == R.id.menu_add_category) {
            
            FragmentManager fm = getSupportFragmentManager();
            InputDialog dialog = InputDialog.newInstance(getString(R.string.hint_add_category), InputDialog.TYPE_CATEGORY);
            dialog.show(fm, "dialog_add_category");
            
            return true;
        }
        else if (item.getItemId() == R.id.menu_delete_category) {
            
            if (deleteCategory()) {
                // TODO Make this dialog a "don't display again" using custom view, checkbox and preference
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                
                builder.setMessage(R.string.delete_category_message);
                builder.setTitle(R.string.delete_category_title);
                builder.setPositiveButton(getString(R.string.ok), new OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        
                        dialog.dismiss();
                    }
                });
                
                builder.create().show();
            }
            
            mSectionsPagerAdapter.updateCategoryList();
            mIndicator.notifyDataSetChanged();
            
            
        }
        else if (item.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
        
        return super.onMenuItemSelected(featureId, item);
    }
    
    public boolean deleteCategory() {
        boolean haveReports = false;
        
        Category category = mSectionsPagerAdapter.getCategory(mViewPager.getCurrentItem());
        List<Project> projectList = DatabaseManager.getInstance().getAllProjects(category.getId());
        for (int i = 0; i < projectList.size(); i++) {
            Project project = projectList.get(i);
            List<Report> reports = DatabaseManager.getInstance().getAllReports(project.getId());
            
            if (reports == null || reports.size() == 0) {
                DatabaseManager.getInstance().deleteProject(project);
            }
            else {
                haveReports = true;
                project.setActive(false);
                DatabaseManager.getInstance().updateProject(project);
            }
            
        }
        
        if (haveReports) {
            category.setActive(false);
            DatabaseManager.getInstance().updateCategory(category);
        }
        else {
            DatabaseManager.getInstance().deleteCategory(category);
        }
        
        return haveReports;
    }

    @Override
    public void onFinishEditDialog(String text, int type) {
        if (type == InputDialog.TYPE_PROJECT) {
            Category category = mSectionsPagerAdapter.getCategory(mViewPager.getCurrentItem());
            final int belongToCategoryId = (category == null) ? 0 : category.getId();
            
            final Project project = new Project();
            project.setName(text);
            project.setActive(true);
            project.setBelongsToCategoryId(belongToCategoryId);
            
            final List<Project> projectList = DatabaseManager.getInstance().getProject(text);
            final List<Category> categoryList = DatabaseManager.getInstance().getAllCategories();
            if (projectList.size() > 0) {
                final CharSequence[] entries = new CharSequence[projectList.size()];
                //final CharSequence[] categories = new CharSequence[categoryList.size()];
                //final String[] calendarIds = new String[projectList.size()];
                for (int i = 0; i < projectList.size(); i++) {
                    Project oldProject = projectList.get(i);
                    
                    StringBuilder builder = new StringBuilder();                    
                    for (int a = 0; a < categoryList.size(); a++) {
                        if (categoryList.get(a).getId() == oldProject.getBelongsToCategoryId()) {
                            builder.append(categoryList.get(a).getName());
                            builder.append(": ");
                            break;
                        }
                    }
                    
                    builder.append(projectList.get(i).getName());
                    
                    entries[i] = builder.toString();
                    //calendarIds[i] = projectLists.get(i).getId();
                }
                
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.reactivate_old_project)
                    .setSingleChoiceItems(entries, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int item) {
                        //dialogInterface.dismiss();
                    }
                });
                builder.setPositiveButton(getString(R.string.yes_reactivate), new OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListView lw = ((AlertDialog)dialog).getListView();
                        Project oldProject = projectList.get(lw.getCheckedItemPosition());
                        oldProject.setActive(true);
                        oldProject.setBelongsToCategoryId(belongToCategoryId);
                        addProject(oldProject);
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(getString(R.string.no_create_new), new OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addProject(project);
                        dialog.dismiss();
                        
                    }
                });
                builder.create().show();
            }
            else {
                addProject(project);
            }
            
            
        }
        else {
            Category category = new Category();
            category.setName(text);
            category.setActive(true);
            
            DatabaseManager.getInstance().addCategory(category);
            mSectionsPagerAdapter.updateCategoryList();
        }
        
    }
    
    public void addProject(Project project) {
        DatabaseManager.getInstance().addProject(project);
        //mSectionsPagerAdapter.updateCategoryList();
        
        Intent intent = new Intent()
            .setAction(ProjectListFragment.ResponseReceiver.INTENT_ACTION_ADD_PROJECT)
            .putExtra(ProjectListFragment.ResponseReceiver.CURRENT_PAGE, project.getBelongsToCategoryId());
        sendBroadcast(intent);
    }
    
    @Override
    public void onAddHoursDialog(int projectId, int hours, int minutes, Date date, String comment) {
        Report report = new Report();
        report.setProjectId(projectId);
        report.setHours(hours);
        report.setMinutes(minutes);
        report.setDate(date);
        report.setComment(comment);
        
        DatabaseManager.getInstance().addReport(report);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        List<Category> categoryList;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            updateCategoryList();
            
        }
        
        public void updateCategoryList() {
            categoryList = DatabaseManager.getInstance().getAllActiveCategories();
            notifyDataSetChanged();
            
        }
        
        public Category getCategory(int position) {
            return categoryList.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            Category category = categoryList.get(position);
            
            Fragment fragment = ProjectListFragment.getInstance(category.getId());
            return fragment;
        }

        @Override
        public int getCount() {
            return categoryList.size();
        }
        
        @Override
        public int getItemPosition(Object object){
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return categoryList.get(position).getName();
        }
    }

}
