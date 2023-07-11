package org.opentcs.common;

public enum UserPerms {
    PERSIST_MAP(1),
    COMMAND_VEHICLE(2),
    MANAGE_VEHICLE(3);
    private final int id;

    UserPerms(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

}
