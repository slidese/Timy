
package se.slide.timy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import se.slide.timy.db.DatabaseManager;
import se.slide.timy.model.Project;
import se.slide.timy.model.Report;

import java.util.List;

public class ProjectListFragment extends ListFragment {
    
    public static final String EXTRA_ID = "id";
    
    private int mId;
    private ResponseReceiver mReceiver;
    private ProjectArrayAdapter mAdapter;

    public static final ProjectListFragment getInstance(int id) {
        ProjectListFragment fragment = new ProjectListFragment();
        Bundle bdl = new Bundle(2);
        bdl.putInt(EXTRA_ID, id);
        fragment.setArguments(bdl);
        
        return fragment;
    }
    
    public ProjectListFragment() {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        mReceiver = new ResponseReceiver();
        //registerReceiver();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
        //attachAdapter();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        View view = (View) inflater.inflate(R.layout.project_listview, null);
        
        mId = getArguments().getInt(EXTRA_ID);
        
        registerForContextMenu(view);
        
        attachAdapter();
        
        return view;
    }
    
    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onContextItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Are we on the correct listview, check the groupId which should match the mId we set in menu.add
        if (item.getGroupId() == mId) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
            Project project = mAdapter.getItem(info.position);
            
            int menuItemIndex = item.getItemId();
            
            if (menuItemIndex == 0) {
                //
            }
            else if (menuItemIndex == 1) {
                List<Report> reports = DatabaseManager.getInstance().getAllReports(project.getId());
                
                if (reports == null || reports.size() == 0) {
                    DatabaseManager.getInstance().deleteProject(project);
                }
                else {
                    project.setActive(false);
                    DatabaseManager.getInstance().updateProject(project);
                }
                
                // TODO Make this dialog a "don't display again" using custom view, checkbox and preference
                // Display alert dialog as for category
                
                mAdapter.clear();
                mAdapter.addAll(DatabaseManager.getInstance().getAllProjects(mId));
                mAdapter.notifyDataSetChanged();
            }    
        }
        
        
        
        return super.onContextItemSelected(item);
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Project project = mAdapter.getItem(info.position);
        
        menu.setHeaderTitle(getString(R.string.listmenu_title) + " " + project.getName());
        menu.add(mId, 0, 0, getString(R.string.listmenu_edit));
        menu.add(mId, 1, 1, getString(R.string.listmenu_delete));
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        Project project = mAdapter.getItem(position);
        
        FragmentManager fm = getActivity().getSupportFragmentManager();
        HoursDialog dialog = HoursDialog.newInstance(project.getName(), project.getId());
        dialog.show(fm, "dialog_select_hours");
    }

    private void attachAdapter() {
        List<Project> projectList = DatabaseManager.getInstance().getAllProjects(mId);
        
        mAdapter = new ProjectArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_1, projectList);
        
        setListAdapter(mAdapter);
    }
    
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ProjectListFragment.ResponseReceiver.INTENT_ACTION_ADD_PROJECT);
        filter.addAction(ProjectListFragment.ResponseReceiver.INTENT_ACTION_DELETE_PROJECT);
        filter.addAction(ProjectListFragment.ResponseReceiver.INTENT_ACTION_CLEAR_ALL);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        getActivity().registerReceiver(mReceiver, filter);
    }

    private void unregisterReceiver() {
        getActivity().unregisterReceiver(mReceiver);
    }

    public class ResponseReceiver extends BroadcastReceiver {
        /** Actions */
        public static final String INTENT_ACTION_ADD_PROJECT        = "se.slide.timy.intent.action.ADD_PROJECT";
        public static final String INTENT_ACTION_DELETE_PROJECT     = "se.slide.timy.intent.action.DELETE_PROJECT";
        public static final String INTENT_ACTION_CLEAR_ALL          = "se.slide.timy.intent.action.CLEAR_ALL";
        
        /** Extras */
        public static final String CURRENT_PAGE                     = "current_page";

        @Override
        public void onReceive(Context context, Intent intent) {
            int currentPage = intent.getIntExtra(CURRENT_PAGE, 0);

            String action = intent.getAction();
            if (action.equals(INTENT_ACTION_ADD_PROJECT) && currentPage == mId) {
                mAdapter.clear();
                mAdapter.addAll(DatabaseManager.getInstance().getAllProjects(mId));
                mAdapter.notifyDataSetChanged();
            }
            if (action.equals(INTENT_ACTION_CLEAR_ALL) && currentPage == mId) {
                mAdapter.clear();
            }
            
        }
    }
}
