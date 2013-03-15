
package se.slide.timy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.ListView;

import com.viewpagerindicator.TitlePageIndicator;

import se.slide.timy.HoursDialog.HoursDialogListener;
import se.slide.timy.InputDialog.EditNameDialogListener;
import se.slide.timy.animations.ZoomOutPageTransformer;
import se.slide.timy.db.DatabaseManager;
import se.slide.timy.model.Category;
import se.slide.timy.model.Project;
import se.slide.timy.model.Report;
import se.slide.timy.service.SyncService;

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
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        //mViewPager.setOffscreenPageLimit(0);
        
        //Bind the title indicator to the adapter
        mIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mViewPager);
        
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
            
            Category category = mSectionsPagerAdapter.getCategory(mViewPager.getCurrentItem());
            
            if (category == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.add_category_first_title);
                builder.setMessage(R.string.add_category_first_message);
                builder.setPositiveButton(R.string.ok, new OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                
                return true;
            }
            
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
            
            if (deleteCategory() && !PreferenceManager.getDefaultSharedPreferences(this).getBoolean("never_notify_category", false)) {
                // TODO Make this dialog a "don't display again" using custom view, checkbox and preference
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(getLayoutInflater().inflate(R.layout.nodelete_alert_dialog, null));
                builder.setMessage(R.string.delete_category_message);
                builder.setTitle(R.string.delete_category_title);
                builder.setPositiveButton(getString(R.string.ok), new OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CheckBox neverNotifyMeAgain = (CheckBox) ((AlertDialog)dialog).findViewById(R.id.neverNotifyMeAgain);
                        if (neverNotifyMeAgain.isChecked())
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("never_notify_category", true).commit();
                        
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
    public void onFinishEditDialog(String text, int type, int icon) {
        if (type == InputDialog.TYPE_PROJECT) {
            Category category = mSectionsPagerAdapter.getCategory(mViewPager.getCurrentItem());
            
            if (category == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.add_category_first_title);
                builder.setMessage(R.string.add_category_first_message);
                builder.setPositiveButton(R.string.ok, new OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                
                return;
            }
            
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
        
        List<Report> reports = DatabaseManager.getInstance().getReport(projectId, date);
        if (reports.size() > 0)
            report = reports.get(0);
        
        report.setProjectId(projectId);
        report.setHours(hours);
        report.setMinutes(minutes);
        report.setDate(date);
        report.setComment(comment);
        report.setGoogleCalendarSync(false);
        
        DatabaseManager.getInstance().addOrUpdateReport(report);
        
        startService(new Intent(this, SyncService.class));
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
            if (categoryList == null || categoryList.isEmpty() || position > categoryList.size())
                return null;
            
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
