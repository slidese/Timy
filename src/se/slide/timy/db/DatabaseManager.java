
package se.slide.timy.db;

import android.content.Context;

import se.slide.timy.model.Category;
import se.slide.timy.model.Project;
import se.slide.timy.model.Report;

import java.sql.SQLException;
import java.util.List;

public class DatabaseManager {
    static private DatabaseManager instance;
    private DatabaseHelper helper;

    static public void init(Context ctx) {
        if (instance == null) {
            instance = new DatabaseManager(ctx);
        }
    }

    static public DatabaseManager getInstance() {
        return instance;
    }

    private DatabaseManager(Context ctx) {
        helper = new DatabaseHelper(ctx);
    }

    private DatabaseHelper getHelper() {
        return helper;
    }

    public List<Project> getAllProjects() {
        List<Project> projectLists = null;
        try {
            projectLists = getHelper().getProjectDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projectLists;
    }
    
    public List<Project> getAllProjects(int categoryId) {
        List<Project> projectLists = null;
        try {
            projectLists = getHelper().getProjectDao().query(getHelper().getProjectDao().queryBuilder().where().eq("belongsToCategoryId", categoryId).prepare());
            //alertLists = getHelper().getAlertDao().query(getHelper().getAlertDao().queryBuilder().where().like("level", "warning").and().ge("timeStamp", timestamp).and().eq("clearedTimesStamp", -1).prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projectLists;
    }

    public void addProject(Project f) {
        try {
            getHelper().getProjectDao().create(f);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<Category> getAllCategories() {
        List<Category> categoryLists = null;
        try {
            categoryLists = getHelper().getCategoryDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categoryLists;
    }

    public void addCategory(Category f) {
        try {
            getHelper().getCategoryDao().create(f);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<Report> getAllReports() {
        List<Report> reportLists = null;
        try {
            reportLists = getHelper().getReportDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reportLists;
    }

    public void addReport(Report f) {
        try {
            getHelper().getReportDao().create(f);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
