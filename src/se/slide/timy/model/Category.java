package se.slide.timy.model;

import com.j256.ormlite.field.DatabaseField;

public class Category {
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String name;

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
     * @return the id
     */
    public int getId() {
        return id;
    }
    
}
