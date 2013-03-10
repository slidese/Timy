
package se.slide.timy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.viewpagerindicator.TitlePageIndicator;

import se.slide.timy.HoursDialog.HoursDialogListener;
import se.slide.timy.InputDialog.EditNameDialogListener;
import se.slide.timy.db.DatabaseManager;
import se.slide.timy.model.Category;
import se.slide.timy.model.Project;
import se.slide.timy.model.Report;

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
        
        TitlePageIndicator mIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mViewPager);
        
        List<Report> reports = DatabaseManager.getInstance().getAllReports();
        int size = reports.size();

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
        
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onFinishEditDialog(String text, int type) {
        if (type == InputDialog.TYPE_PROJECT) {
            Project project = new Project();
            project.setName(text);
            project.setBelongsToCategoryId(mViewPager.getCurrentItem());
            
            DatabaseManager.getInstance().addProject(project);
            mSectionsPagerAdapter.updateCategoryList();
            
            Intent intent = new Intent()
                .setAction(ProjectListFragment.ResponseReceiver.INTENT_ACTION_ADD_PROJECT)
                .putExtra(ProjectListFragment.ResponseReceiver.CURRENT_PAGE, mViewPager.getCurrentItem());
            sendBroadcast(intent);
        }
        else {
            Category category = new Category();
            category.setName(text);
            
            DatabaseManager.getInstance().addCategory(category);
            mSectionsPagerAdapter.updateCategoryList();
        }
        
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
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        //private int mNumberOfCategories = 0;
        List<Category> categoryList;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            //mNumberOfCategories = DatabaseManager.getInstance().getAllCategories().size();
            updateCategoryList();
            
        }
        
        public void updateCategoryList() {
            categoryList = DatabaseManager.getInstance().getAllCategories();
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            
            Category category = categoryList.get(position);
            
            Fragment fragment = ProjectListFragment.getInstance(position);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return categoryList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            /*
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase();
                case 1:
                    return getString(R.string.title_section2).toUpperCase();
                case 2:
                    return getString(R.string.title_section3).toUpperCase();
            }
            */
            
            return categoryList.get(position).getName();
            
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            // Create a new TextView and set its text to the fragment's section
            // number argument value.
            TextView textView = new TextView(getActivity());
            textView.setGravity(Gravity.CENTER);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return textView;
        }
    }

}
