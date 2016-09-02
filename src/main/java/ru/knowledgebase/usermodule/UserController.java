package ru.knowledgebase.usermodule;

import ru.knowledgebase.dbmodule.DataCollector;
import ru.knowledgebase.exceptionmodule.databaseexceptions.DataBaseException;
import ru.knowledgebase.ldapmodule.LdapWorker;
import ru.knowledgebase.modelsmodule.usermodels.Token;
import ru.knowledgebase.modelsmodule.usermodels.User;
import ru.knowledgebase.exceptionmodule.userexceptions.UserAlreadyExistsException;
import ru.knowledgebase.exceptionmodule.userexceptions.UserNotFoundException;
import ru.knowledgebase.exceptionmodule.userexceptions.WrongPasswordException;
import ru.knowledgebase.exceptionmodule.userexceptions.WrongUserDataException;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.Date;
import java.util.Calendar;

/**
 * Created by vova on 17.08.16.
 */
public class UserController {

    private DataCollector collector = new DataCollector();
    private LdapWorker ldapWorker = LdapWorker.getInstance();

    private static volatile UserController instance;

    /**
     * Get instance of a class
     * @return instance of a class
     */
    public static UserController getInstance() {
        UserController localInstance = instance;
        if (localInstance == null) {
            synchronized (UserController.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new UserController();
                }
            }
        }
        return localInstance;
    }

    /**
     * Update token for authorized user
     * @param user user object
     * @return return updated token
     */
    private Token updateToken(User user) throws Exception{
        Date date = new Date(new java.util.Date().getTime());
        //token = user login + current date
        String tokenStr = DigestUtils.md5Hex(user.getLogin() + date.toString());
        Token token = new Token(user, tokenStr, date);
        try {
            Token oldToken = collector.getUserToken(user);
            //add token, if not exists, else = update with current date
            if (oldToken == null) {
                collector.addToken(token);
            } else {
                token.setId(oldToken.getId());
                collector.updateToken(token);
            }
        }catch (Exception e){
            throw new DataBaseException();
        }
        return token;
    }
    /**
     * Authorize user in database
     * @param login user login
     * @param password user password
     * @return return user token
     */
    public Token authorize(String login, String password) throws Exception{
        User user = null;
        //MD5 of password
        password = DigestUtils.md5Hex(password);
        try {
            user = collector.findUser(login);
        }catch (Exception e){
            throw new DataBaseException();
        }
        if (user == null) {
            throw new UserNotFoundException();
        } else if (!user.getPassword().equals(password)) {
            throw new WrongPasswordException();
        }
        return updateToken(user);
    }
    /**
     * Authorize user in database and LDAP
     * @param login user login
     * @param password user password
     * @return return user token
     */
    public Token authorizeLdap(String login, String password) throws Exception{
        ldapWorker.authorize(login, DigestUtils.md5Hex(password));
        return authorize(login, password);
    }
    /**
     * Register new user in database and LDAP
     * @param user formed user object
     */
    public User register(User user) throws Exception{
        ldapWorker.createUser(user.getLogin(), user.getPassword());
        try {
            return collector.addUser(user);
        }catch(org.springframework.dao.DataIntegrityViolationException e){
            //rollback LDAP
            ldapWorker.deleteUser(user.getLogin());
            throw new UserAlreadyExistsException();
        }catch(Exception e){
            //rollback LDAP
            ldapWorker.deleteUser(user.getLogin());
            throw new DataBaseException();
        }
    }
    /**
     * Register new user in database and LDAP
     * @param login user login
     * @param password user password
     */
    public User register(String login, String password) throws Exception{
        if (login.length() == 0 || password.length() == 0){
            throw new WrongUserDataException();
        }
        password = DigestUtils.md5Hex(password);
        return register(new User(login, password));
    }
    /**
     * Delete user from database and LDAP
     * @param user user object (important: id should be specified)
     */
    public void delete(User user) throws Exception{
        if (user == null)
            throw new UserNotFoundException();
        ldapWorker.deleteUser(user.getLogin());
        try {
            collector.deleteUser(user);
        }catch (Exception e){
            //rollback LDAP
            ldapWorker.createUser(user.getLogin(), user.getPassword());
            throw new DataBaseException();
        }

    }
    /**
     * Delete user from database and LDAP
     * @param id user id
     */
    public void delete(int id) throws Exception{
        User user = null;
        try {
            collector.findUser(id);
        }catch (Exception e){
            throw new DataBaseException();
        }
        delete(user);
    }
    /**
     * Delete userfrom database and LDAP
     * @param login user login
     */
    public void delete(String login) throws Exception{
        User user = null;
        try {
            user = collector.findUser(login);
        }catch (Exception e){
            throw new DataBaseException();
        }
        delete(user);
    }
    /**
     * Change user login and password in database and LDAP
     * @param id user id
     * @param newLogin new login
     * @param newPassword new password
     */
    public void update(int id, String newLogin, String newPassword) throws Exception {
        if (newLogin.length() == 0 || newPassword.length() == 0)
            throw new WrongUserDataException();
        User user = null;
        try{
            user = collector.findUser(id);
        }catch (Exception e){
            throw new DataBaseException();
        }
        if (user == null)
            throw new UserNotFoundException();
        if (collector.findUser(newLogin) != null)
            throw new UserAlreadyExistsException();
        String oldPassword = user.getPassword();
        String oldLogin = user.getLogin();
        newPassword = DigestUtils.md5Hex(newPassword);
        ldapWorker.changePassword(user.getLogin(), newPassword);
        ldapWorker.changeLogin(user.getLogin(), newLogin);
        try {
            user.setPassword(newPassword);
            user.setLogin(newLogin);
            collector.updateUser(user);
        }catch (Exception e){
            //rollback LDAP
            ldapWorker.changePassword(newLogin, oldPassword);
            ldapWorker.changePassword(newLogin, oldLogin);
            throw new DataBaseException();
        }
    }

    private long getDatePart(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    /**
     * Checks is user token valid and actual
     * @param id user id
     * @param token user token
     * @return true if valid and actual
     */
    public boolean checkUserToken(int id, String token) throws Exception{
        User user = null;
        Token tokenObj = null;
        try{
            user = collector.findUser(id);
            tokenObj = collector.getUserToken(user);
        }catch (Exception e){
            throw new DataBaseException();
        }

        if (tokenObj == null || user == null)
            return false;
        Date tokenDate = tokenObj.getEndDate();
        Date curDate = new Date(new java.util.Date().getTime());
        boolean notInspired = getDatePart(tokenDate) >=  getDatePart(curDate);
        return tokenObj.getToken().equals(token) && notInspired;
    }

}
