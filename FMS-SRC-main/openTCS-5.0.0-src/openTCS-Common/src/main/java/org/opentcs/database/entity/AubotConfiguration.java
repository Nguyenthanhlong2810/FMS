package org.opentcs.database.entity;

public class AubotConfiguration {
    private long id;
    private String name;
    private String value;
    private String description;

    public AubotConfiguration(String name, String value, String description) {
        this.id = 0;
        this.name = name;
        this.value = value;
        this.description = description;
    }

    public AubotConfiguration(long id, String name, String value, String description) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object[] toArray() {
        return new Object[] {name, value, description};
    }
}
