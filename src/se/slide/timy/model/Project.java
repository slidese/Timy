package se.slide.timy.model;

import com.j256.ormlite.field.DatabaseField;

import java.util.ArrayList;
import java.util.List;

public class Project {
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String name;
    
    @DatabaseField
    private int belongsToCategoryId;
    
    @DatabaseField
    private boolean active;
    
    @DatabaseField
    private int icon;
    
    private List<Report> reports;
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the belongsToCategoryId
     */
    public int getBelongsToCategoryId() {
        return belongsToCategoryId;
    }

    /**
     * @param belongsToCategoryId the belongsToCategoryId to set
     */
    public void setBelongsToCategoryId(int belongsToCategoryId) {
        this.belongsToCategoryId = belongsToCategoryId;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the icon
     */
    public int getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(int icon) {
        this.icon = icon;
    }

    /**
     * @return the reports
     */
    public List<Report> getReports() {
        if (reports == null)
            reports = new ArrayList<Report>();
        
        return reports;
    }

    /**
     * @param reports the reports to set
     */
    public void addReport(Report report) {
        if (reports == null)
            reports = new ArrayList<Report>();
        
        reports.add(report);
    }
}
    
    
