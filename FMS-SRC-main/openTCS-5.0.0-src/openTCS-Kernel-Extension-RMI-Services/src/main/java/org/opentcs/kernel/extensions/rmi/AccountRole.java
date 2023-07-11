package org.opentcs.kernel.extensions.rmi;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class AccountRole {

    public enum Role {
        STAFF,
        ADMIN;

        public static Role getRole(boolean isAdmin){
            if(isAdmin){
                return Role.ADMIN;
            }else{
                return Role.STAFF;
            }
        }
    }

    Set<UserPermission> perms = new HashSet<>();

    AccountRole(boolean isAdmin){
        setPerms(isAdmin);
    }

    private void setPerms(boolean isAdmin){
        if(isAdmin){
            perms = EnumSet.allOf(UserPermission.class);
        }else{
            perms = EnumSet.allOf(UserPermission.class);
            perms.remove(UserPermission.MODIFY_VEHICLES);
        }
    }

    public Set<UserPermission> getPerms(){
        return perms;
    }

}
