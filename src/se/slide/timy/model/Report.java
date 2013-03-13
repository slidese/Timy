package se.slide.timy.model;

import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

public class Report {
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private int projectId;
    
    @DatabaseField
    private Date date;
    
    @DatabaseField
    private int hours;
    
    @DatabaseField
    private int minutes;

    @DatabaseField
    private String comment;
    
    @DatabaseField
    private boolean googleCalendarSync;
    
    @DatabaseField
    private String googleCalendarEventId;

    /**
     * @return the projectId
     */
    public int getProjectId() {
        return projectId;
    }

    /**
     * @param projectId the projectId to set
     */
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return the hours
     */
    public int getHours() {
        return hours;
    }

    /**
     * @param hours the hours to set
     */
    public void setHours(int hours) {
        this.hours = hours;
    }

    /**
     * @return the minutes
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * @param minutes the minutes to set
     */
    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the googleCalendarSync
     */
    public boolean isGoogleCalendarSync() {
        return googleCalendarSync;
    }

    /**
     * @param googleCalendarSync the googleCalendarSync to set
     */
    public void setGoogleCalendarSync(boolean googleCalendarSync) {
        this.googleCalendarSync = googleCalendarSync;
    }

    /**
     * @return the googleCalendarEventId
     */
    public String getGoogleCalendarEventId() {
        return googleCalendarEventId;
    }

    /**
     * @param googleCalendarEventId the googleCalendarEventId to set
     */
    public void setGoogleCalendarEventId(String googleCalendarEventId) {
        this.googleCalendarEventId = googleCalendarEventId;
    }

    
}
