package se.slide.timy.model;

import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

public class Project {
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String name;
    
    @DatabaseField
    private int belongsToCategoryId;

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
    
    
}
