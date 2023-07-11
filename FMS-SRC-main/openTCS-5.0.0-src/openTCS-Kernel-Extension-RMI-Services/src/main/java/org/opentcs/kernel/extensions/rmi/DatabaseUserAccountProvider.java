package org.opentcs.kernel.extensions.rmi;

import com.aubot.hibernate.database.DatabaseSessionFactory;
import com.aubot.hibernate.entities.UserEntity;
import com.aubot.hibernate.entities.UserPermissionEntity;
import com.google.inject.Inject;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.opentcs.access.PasswordHash;
import org.opentcs.common.GuestUserCredentials;
import org.opentcs.common.UserPerms;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseUserAccountProvider implements UserAccountProvider {
    DatabaseSessionFactory databaseSessionFactory;

    @Inject
    DatabaseUserAccountProvider(DatabaseSessionFactory databaseSessionFactory) {
        this.databaseSessionFactory = databaseSessionFactory;
    }

    public List<UserEntity> findAllUser() {
        Session session = databaseSessionFactory.getSession();
        Query query = session.createQuery("from UserEntity u where u.deleteAt = null");
        return (List<UserEntity>) query.list();
    }

    @Override
    public Set<UserAccount> getUserAccounts() {
        Set<UserAccount> userAccounts = new HashSet<>();
        for (UserEntity user : findAllUser()){
            //Set<UserPerms> perms = user.getUserPermissions().stream().map(userPermissionEntity -> userPermissionEntity.getUserPermId().getPermission()).collect(Collectors.toSet());
            userAccounts.add(new UserAccount(user.getUsername(), user.getPassword(), getUserPermission(user)));
        }
        if(userAccounts.isEmpty()){
            UserEntity adminEntity = new UserEntity();
            adminEntity.setUsername(GuestUserCredentials.ADMIN);
            adminEntity.setPassword(PasswordHash.hash(GuestUserCredentials.PASSWORD));
            adminEntity.setCreateAt(new Timestamp(System.currentTimeMillis()));
            adminEntity.setAdmin(true);
            Session session = databaseSessionFactory.getSession();
            session.save("UserEntity",adminEntity);
            session.close();
        }
        return userAccounts;
    }

    @Override
    public void changePassword(UserAccount account, String oldPassword, String newPassword) {
        Session session = databaseSessionFactory.getSession();
        Query<UserEntity> query = session.createQuery("from UserEntity u where u.username = :username and u.deleteAt = null");
        query.setParameter("username", account.getUserName());
        UserEntity user = query.uniqueResult();
        if (user.getPassword().equals(PasswordHash.hash(oldPassword))) {
            user.setPassword(PasswordHash.hash(newPassword));
            session.update(user);
        }
        session.close();
    }

    @Override
    public UserAccount getUserAccount(String username, String password) {
        Session session = databaseSessionFactory.getSession();
        Query query = session.createQuery("from UserEntity U where U.username = :username and U.password = :password and U.deleteAt = null");
        query.setParameter("username",username);
        query.setParameter("password", PasswordHash.hash(password));

        UserEntity user =  (UserEntity) query.uniqueResult();
        if (user == null) {
             return null;
        }
        session.close();
        return new UserAccount(user.getUsername(),user.getPassword(), getUserPermission(user));
    }

    private Set<UserPermission> getUserPermission(UserEntity user){
        Set<UserPermission> userPerms = new HashSet<>();
        if(user.isAdmin()){
            return Arrays.stream(UserPermission.values()).collect(Collectors.toCollection(HashSet::new));
        }

        userPerms.add(UserPermission.READ_DATA);
        Set<UserPerms> perms = user.getPermissions().stream().map(UserPermissionEntity::getPerm).collect(Collectors.toSet());
        if (perms.contains(UserPerms.PERSIST_MAP)) {
            userPerms.add(UserPermission.MODIFY_MODEL);
        }
        if (perms.contains(UserPerms.MANAGE_VEHICLE)) {
            userPerms.add(UserPermission.MODIFY_VEHICLES);
        }

        return userPerms;
    }
}
