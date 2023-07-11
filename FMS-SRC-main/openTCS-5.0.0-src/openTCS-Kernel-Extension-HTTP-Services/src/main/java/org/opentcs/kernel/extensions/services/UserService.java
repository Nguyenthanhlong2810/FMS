package org.opentcs.kernel.extensions.services;

import com.aubot.hibernate.database.DatabaseSessionFactory;
import com.aubot.hibernate.entities.UserEntity;
import com.aubot.hibernate.entities.UserPermissionEntity;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.opentcs.access.PasswordHash;
import org.opentcs.common.UserPerms;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class UserService {

    private final DatabaseSessionFactory databaseHandler;
    @Inject
    public UserService(DatabaseSessionFactory databaseHandler) {
        this.databaseHandler = Objects.requireNonNull(databaseHandler);
    }

    public UserEntity login(String username,String password){
        Session session = databaseHandler.getSession();
        session.beginTransaction();
        String hql = "FROM UserEntity E WHERE E.username = :username and E.password =:password";
        Query query = session.createQuery(hql);
        query.setParameter("username",username.toLowerCase());
        query.setParameter("password",PasswordHash.hash(password));
        UserEntity userEntity = (UserEntity) query.uniqueResult();
        return userEntity;
    }

    public List<UserEntity> getAllUsers(){
        Session session = databaseHandler.getSession();
        Query query = session.createQuery("from UserEntity U where U.deleteAt = null");
        return (List<UserEntity>)query.list();
    }

    public UserEntity getUserInfo(int id){
        Session session = databaseHandler.getSession();
        return session.get(UserEntity.class,id);
    }

    public void setLastLogin(UserEntity user){
        Session session = databaseHandler.getSession();
        UserEntity userEntity = session.get(UserEntity.class,user.getId());
        userEntity.setLastLogin(new Timestamp(System.currentTimeMillis()));
        session.save(userEntity);
        session.close();
    }

    public boolean saveUser(Map<String,Object> userMap) {
        List<UserPerms> defaultPerms = Arrays.asList(UserPerms.values());
        try (Session session = databaseHandler.getSession()) {
            session.beginTransaction();
            UserEntity user = new UserEntity();
            boolean edit = userMap.containsKey("id");
            if (edit) {
                int id = Integer.parseInt((String) userMap.get("id"));

                user = session.get(UserEntity.class, id);
                user.setUpdateAt(new Timestamp(System.currentTimeMillis()));

                user.setAdmin((Boolean) userMap.get("isAdmin"));
                Set<UserPermissionEntity> oldPermEntities = user.getPermissions();
                List<UserPerms> userPerms = oldPermEntities.stream().map(UserPermissionEntity::getPerm).collect(Collectors.toList());

                List<UserPerms> perms = (List<UserPerms>) userMap.get("userPerms");
                List<UserPerms> deniedPerms = defaultPerms.stream().filter(perm -> !perms.contains(perm)).collect(Collectors.toList());

                oldPermEntities.removeIf(userPermissionEntity -> deniedPerms.contains(userPermissionEntity.getPerm()));
                for(UserPerms perm : perms) {
                    if(userPerms.contains(perm)){
                        continue;
                    }
                    UserPermissionEntity userPermissionentity = new UserPermissionEntity();
                    userPermissionentity.setUser(user);
                    userPermissionentity.setPerm(perm);
                    oldPermEntities.add(userPermissionentity);
                    saveUserPerms(userPermissionentity);
                }
                user.setPermissions(oldPermEntities);
                List<UserPermissionEntity> userPermEntities = getUserPerms(user);
                for(UserPermissionEntity userPermissionentity : userPermEntities){
                    if(deniedPerms.contains(userPermissionentity.getPerm())){
                        removePerm(userPermissionentity.getId());
                    }
                }
            } else {
                user.setUsername(((String) userMap.get("username")).toLowerCase());
                user.setCreateAt(new Timestamp(System.currentTimeMillis()));
                user.setPassword(PasswordHash.hash((String) userMap.get("password")));
            }
            /*user.setPassword(PasswordHash.hash((String) userMap.get("password")));*/
            user.setPhone((String) userMap.get("phone"));
            user.setEmail((String) userMap.get("email"));
            session.saveOrUpdate(user);
            session.getTransaction().commit();
        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    public boolean removeUser(int id){
        Session session = databaseHandler.getSession();
        session.beginTransaction();
        UserEntity user = session.get(UserEntity.class,id);
        user.setDeleteAt(new Timestamp(System.currentTimeMillis()));
        session.getTransaction().commit();
        session.close();
        return true;
    }

    public List<UserPermissionEntity> getUserPerms(UserEntity user){
        Session session = databaseHandler.getSession();
        session.beginTransaction();
        String hql = "FROM UserPermissionEntity U WHERE U.user = :user";
        Query query = session.createQuery(hql);
        query.setParameter("user",user);
        return query.getResultList();
    }

    public void saveUserPerms(UserPermissionEntity userPermissionentity){
        Session session = databaseHandler.getSession();
        session.beginTransaction();
        session.save(userPermissionentity);
        session.getTransaction().commit();
        session.close();

    }

    public void removePerm(int id){
        Session session = databaseHandler.getSession();
        session.beginTransaction();
        UserPermissionEntity userPermissionentity = session.get(UserPermissionEntity.class,id);
        session.delete(userPermissionentity);
        session.getTransaction().commit();
        session.close();
    }

}
