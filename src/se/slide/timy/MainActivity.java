
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
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.viewpagerindicator.TitlePageIndicator;

import se.slide.timy.HoursDialog.HoursDialogListener;
import se.slide.timy.InputDialog.EditNameDialogListener;
import se.slide.timy.ProjectListFragment.ProjectListInterface;
import se.slide.timy.animations.ZoomOutPageTransformer;
import se.slide.timy.db.DatabaseManager;
import se.slide.timy.model.Category;
import se.slide.timy.model.Project;
import se.slide.timy.model.Report;
import se.slide.timy.service.SyncService;

import java.util.Date;
import java.util.List;

public class MainActivity extends FragmentActivity implements EditNameDialogListener,
        ProjectListInterface {

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
    boolean hasProjectDataChanged = false;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Crashlytics.start(this);
        DatabaseManager.init(this);

        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Bind the title indicator to the adapter
        mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mViewPager);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(ShowNotification.EXTRA_CODE)) {
            
            
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(intent.getStringExtra(ShowNotification.EXTRA_TITLE));
            alertDialogBuilder.setMessage(intent.getStringExtra(ShowNotification.EXTRA_TEXT));
            alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialogBuilder.create().show();
        }
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mViewPager.getChildCount() < 1)
            menu.getItem(0).setVisible(false);
        else
            menu.getItem(0).setVisible(true);
            
        return super.onPrepareOptionsMenu(menu);
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onMenuItemSelected(int,
     * android.view.MenuItem)
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        if (item.getItemId() == R.id.menu_add_project) {

            if (mViewPager.getChildCount() < 1) {
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

            startActivityForResult(new Intent(this, ProjectActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), ProjectActivity.ACTIVITY_CODE);

            return true;
        }
        else if (item.getItemId() == R.id.menu_add_category) {

            FragmentManager fm = getSupportFragmentManager();
            InputDialog dialog = InputDialog.newInstance(getString(R.string.hint_add_category), -1,
                    null);
            dialog.show(fm, "dialog_add_category");

            return true;
        }
        else if (item.getItemId() == R.id.menu_edit_category) {

            if (mViewPager.getChildCount() < 1)
                return true;

            Category category = mSectionsPagerAdapter.getCategory(mViewPager.getCurrentItem());

            FragmentManager fm = getSupportFragmentManager();
            InputDialog dialog = InputDialog.newInstance(getString(R.string.hint_add_category),
                    category.getId(), category.getName());
            dialog.show(fm, "dialog_add_category");

            return true;
        }
        else if (item.getItemId() == R.id.menu_remove_category) {

            if (mViewPager.getChildCount() < 1)
                return true;

            final Category category = mSectionsPagerAdapter
                    .getCategory(mViewPager.getCurrentItem());

            // Ask to hide or to delete
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.delete_or_hide_title)
                    .setPositiveButton(R.string.ok, new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int selectedPosition = ((AlertDialog) dialog).getListView()
                                    .getCheckedItemPosition();
                            if (selectedPosition == 0) {
                                // Hide
                                category.setActive(false);
                                DatabaseManager.getInstance().updateCategory(category);
                            }
                            else {
                                // Delete
                                DatabaseManager.getInstance()
                                        .deleteCategoryAndItsProjects(category);
                            }

                            mSectionsPagerAdapter.updateCategoryList();
                            mIndicator.notifyDataSetChanged();
                            invalidateOptionsMenu();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setSingleChoiceItems(getResources().getStringArray(R.array.delete_or_hide), 0,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int item) {

                                }
                            });

            builder.create().show();

        }
        else if (item.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }

        return super.onMenuItemSelected(featureId, item);
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int,
     * android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ProjectActivity.ACTIVITY_CODE) {
            if (resultCode == RESULT_OK) {
                String name = data.getStringExtra(ProjectActivity.EXTRA_PROJECT_NAME);
                String colorId = data.getStringExtra(ProjectActivity.EXTRA_PROJECT_COLOR_ID);

                addProject(name, colorId);
            }
        }

    }

    public void addProject(String name, String colorId) {
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
        project.setName(name);
        project.setColorId(colorId);
        project.setActive(true);
        project.setBelongsToCategoryId(belongToCategoryId);

        final List<Project> projectList = DatabaseManager.getInstance().getProject(name);
        final List<Category> categoryList = DatabaseManager.getInstance().getAllCategories();
        if (projectList.size() > 0) {
            final CharSequence[] entries = new CharSequence[projectList.size()];
            // final CharSequence[] categories = new
            // CharSequence[categoryList.size()];
            // final String[] calendarIds = new String[projectList.size()];
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
                // calendarIds[i] = projectLists.get(i).getId();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.reactivate_old_project)
                    .setSingleChoiceItems(entries, 0, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int item) {
                            // dialogInterface.dismiss();
                        }
                    });
            builder.setPositiveButton(getString(R.string.yes_reactivate), new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ListView lw = ((AlertDialog) dialog).getListView();
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

    @Override
    public void onFinishEditDialog(final String text, int categoryId, int icon) {

        // Is this an edit?
        List<Category> categories = null;
        if (categoryId > 0) {
            categories = DatabaseManager.getInstance().getCategory(categoryId);

            if (categories != null && categories.size() > 0) {
                Category category = categories.get(0);

                if (category != null) {
                    category.setActive(true);
                    category.setName(text);

                    addOrUpdateCategory(category);
                    return;
                }
            }
        }

        // This is an add
        final List<Category> reactivatedCategories = DatabaseManager.getInstance()
                .getCategory(text);
        if (reactivatedCategories != null && reactivatedCategories.size() > 0) {
            // We have old categories to handle
            final CharSequence[] entries = new CharSequence[reactivatedCategories.size()];
            for (int i = 0; i < reactivatedCategories.size(); i++) {
                entries[i] = reactivatedCategories.get(i).getName();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.reactivate_old_category)
                    .setSingleChoiceItems(entries, 0, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int item) {
                            // dialogInterface.dismiss();
                        }
                    });
            builder.setPositiveButton(getString(R.string.yes_reactivate), new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ListView lw = ((AlertDialog) dialog).getListView();
                    Category category = reactivatedCategories.get(lw.getCheckedItemPosition());
                    category.setActive(true);

                    addOrUpdateCategory(category);

                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(getString(R.string.no_create_new), new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Category category = new Category();
                    category.setActive(true);
                    category.setName(text);

                    addOrUpdateCategory(category);

                    dialog.dismiss();
                }
            });
            builder.create().show();

        }
        else {
            // We have no old categories with the same name, so just create a
            // new one
            Category category = new Category();
            category.setActive(true);
            category.setName(text);

            addOrUpdateCategory(category);
        }

    }

    public void addOrUpdateCategory(Category category) {
        DatabaseManager.getInstance().addOrUpdate(category);
        mSectionsPagerAdapter.updateCategoryList();
        invalidateOptionsMenu();
    }

    public void addProject(Project project) {
        DatabaseManager.getInstance().addProject(project);

        hasProjectDataChanged = true;

        Intent intent = new Intent()
                .setAction(ProjectListFragment.ResponseReceiver.INTENT_ACTION_ADD_PROJECT)
                .putExtra(ProjectListFragment.ResponseReceiver.CURRENT_PAGE,
                        project.getBelongsToCategoryId());
        sendBroadcast(intent);
    }

    /*
     * @Override public void onAddHoursDialog(int projectId, int hours, int
     * minutes, Date date, String comment) { Report report = new Report();
     * List<Report> reports = DatabaseManager.getInstance().getReport(projectId,
     * date); if (reports.size() > 0) report = reports.get(0);
     * report.setProjectId(projectId); report.setHours(hours);
     * report.setMinutes(minutes); report.setDate(date);
     * report.setComment(comment); report.setGoogleCalendarSync(false);
     * DatabaseManager.getInstance().addOrUpdateReport(report); startService(new
     * Intent(this, SyncService.class)); }
     */

    /*
     * (non-Javadoc)
     * @see
     * se.slide.timy.ProjectListFragment.ProjectListInterface#hasProjectsChanged
     * ()
     */
    @Override
    public boolean hasProjectsChanged() {
        return hasProjectDataChanged;
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
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return categoryList.get(position).getName();
        }
    }

}
