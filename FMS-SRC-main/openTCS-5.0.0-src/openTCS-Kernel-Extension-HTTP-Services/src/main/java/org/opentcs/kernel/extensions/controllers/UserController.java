package org.opentcs.kernel.extensions.controllers;

import com.aubot.hibernate.database.DatabaseSessionFactory;
import com.aubot.hibernate.entities.UserEntity;
import com.aubot.hibernate.entities.UserPermissionEntity;
import org.opentcs.common.UserPerms;
import org.opentcs.kernel.extensions.services.UserService;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Session;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static spark.Spark.halt;

public class UserController {

    private final UserService userService;
    private final List<UserPerms> perms = Arrays.stream(UserPerms.values()).collect(Collectors.toList());
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    @Inject
    public UserController(DatabaseSessionFactory databaseSessionFactory) {
        this.userService = new UserService(databaseSessionFactory);
    }

    public ModelAndView login(Request request, Response response) {
        Map<String, Object> map = new HashMap<>();
        return new ModelAndView(map,"login");
    }

    public Object logout(Request request,Response response){
        request.session().removeAttribute("account");
        request.session().removeAttribute("lastUrl");
        response.redirect("/login");
        return null;
    }

    public ModelAndView handleLogin(Request request, Response response) {
        request.session().removeAttribute("account");
        Map<String, Object> map = new HashMap<>();
        String username = request.queryParams("fusername");
        String password = request.queryParams("fpassword");
        UserEntity user = userService.login(username,password);
        if(user != null) {
            userService.setLastLogin(user);
            map.put("user", user);
            Session sessionLogin = request.session(true);
            sessionLogin.maxInactiveInterval(3000);
            sessionLogin.attribute("account",user);
            String lastUrl = request.session().attribute("lastUrl");
            if (lastUrl != null) {
                request.session().removeAttribute("lastUrl");
                response.redirect(lastUrl);
            } else {
                response.redirect("/user/user-list");
            }
        }else{
            map.put("error", "Username or password is wrong!");
        }
        return new ModelAndView(map,"login");
    }

    public ModelAndView getUsers(Request request, Response response) {
        Map<String, Object> map = new HashMap<>();
        UserEntity account = request.session().attribute("account");
        if(account == null){
            response.redirect("/v1/login");
        }
        List<UserEntity> userEntities = userService.getAllUsers();
        map.put("users",userEntities);
        map.put("account",account);
        return new ModelAndView(map, "user-list");
    }

    public ModelAndView addUser(Request request, Response response) {
        Map<String, Object> map = new HashMap<>();
        map.put("errors", new HashMap<>());
        return new ModelAndView(map, "user-add");
    }

    public ModelAndView handleAddUser(Request request, Response response) {
        Map<String,Object> users = new HashMap<>(request.params());
        String username = request.queryParams("fname");
        String password = request.queryParams("fpassword");
        String repassword = request.queryParams("frepassword");
        String phone = request.queryParams("fphone");
        String email = request.queryParams("femail");
        Map<String, String> errors = validateUser(username, password, repassword, phone, email);
        users.put("errors", errors);
        users.put("username",username);
        users.put("password",password);
        users.put("phone",phone);
        users.put("email",email);
        if(errors.size() > 0){
            return new ModelAndView(users, "user-add");
        }
        boolean success = userService.saveUser(users);
        if(success) {
            response.redirect("/user/user-list");
        }
        return new ModelAndView(users, "user-add");
    }

    private Map<String, String> validateUser(String username, String password, String repassword, String phone, String email) {
        Map<String,String> errors = new HashMap<>();
        if(!checkUserUnique(username)){
            errors.put("username","Username is existed!");
        }
        if(!validatePassword(password)){
            errors.put("password","Password length is in range 8-30 characters");
        }
        if(!password.equals(repassword)){
            errors.put("repassword","Re-entered password does not match! ");
        }
        if(!validateEmail(email)){
            errors.put("email","Email is wrong!");
        }
        if(!validatePhone(phone)){
            errors.put("phone","Phone is wrong!");
        }
        return errors;
    }

    private boolean checkUserUnique(String username){
        List<String> usernames = userService.getAllUsers().stream().map(UserEntity::getUsername).collect(Collectors.toList());
        return !usernames.contains(username);
    }

    private boolean validatePassword(String password){
        return password.length() >= 8 && password.length() <= 30;
    }

    public boolean validateEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    private boolean validatePhone(String phone){
        return phone.length() <= 10;
    }

    public ModelAndView handleEditUser(Request request, Response response) {
        Map<String,Object> map = new HashMap<>();
        Map<String,Object> user = new HashMap<>();
        List<UserPerms> userPerms = new ArrayList<>();
        String id = request.queryParams("id");
//        String username = request.queryParams("fname");
        /*String password = request.queryParams("fpassword");*/
        String phone = request.queryParams("fphone");
        String email = request.queryParams("femail");
        Object objAdmin = request.queryParams("cbxAdmin");
        boolean isAdmin;
        isAdmin = objAdmin != null;
        user.put("id",id);
//        user.put("username",username);
   /*     user.put("password",password);*/
        user.put("phone",phone);
        user.put("email",email);
        user.put("isAdmin",isAdmin);
        Map<String, String> errors = validateUser("", "password", "password", phone, email);
        perms.forEach(perm -> {
            if(Objects.equals(request.queryParams(String.valueOf(perm)), "on")){
                userPerms.add(perm);
            }
        });

        user.put("userPerms",userPerms);
        map.put("user",user);
        map.put("errors",errors);
        map.put("perms",perms);
        map.put("userPerms",userPerms);

        return new ModelAndView(map, "user-info");
    }

    public ModelAndView modifyUser(Request request, Response response) {
        Map<String, Object> map = new HashMap<>();
        int id = Integer.parseInt(request.queryParams("id"));
        UserEntity user = userService.getUserInfo(id);
        map.put("user",user);
        List<UserPerms> userPerms = user.getPermissions().stream().map(UserPermissionEntity::getPerm).collect(Collectors.toList());
        map.put("userPerms",userPerms);
        map.put("perms",perms);
        map.put("errors", new HashMap<>());
        return new ModelAndView(map, "user-info");
    }


    public Object handleRemoveUser(Request request, Response response) {
        int id = Integer.parseInt(request.queryParams("id"));
        boolean success =  userService.removeUser(id);
        if(success){
            response.redirect("/user/user-list");
        }else {
            response.redirect("/user/user-add");
        }
        return success;
    }

}
