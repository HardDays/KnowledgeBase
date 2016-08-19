package ru.knowledgebase.dbmodule;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.knowledgebase.dbmodule.dataservices.*;
import ru.knowledgebase.modelsmodule.*;

import java.util.List;

/**
 * Created by root on 16.08.16.
 */
public class DataCollector {
    private ArticleService articleService;
    private TokenService tokenService;
    private UserService userService;
    private SectionRoleService sectionRoleService;
    private GlobalRoleService globalRoleService;
    private UserSectionRoleService userSectionRoleService;

    public DataCollector() {
        ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring-config.xml");
        articleService = (ArticleService) context.getBean("articleService");
        tokenService = (TokenService) context.getBean("tokenService");
        userService = (UserService) context.getBean("userService");
        sectionRoleService = (SectionRoleService) context.getBean("sectionRoleService");
        globalRoleService = (GlobalRoleService) context.getBean("globalRoleService");
        userSectionRoleService = (UserSectionRoleService) context.getBean("userSectionRoleService");

    }

    public void addUser(User user) throws Exception{
        userService.create(user);
    }

    // to add
    public void addToken(Token token) throws Exception{
        tokenService.create(token);
    }

    public void updateToken(Token token) throws Exception{
        tokenService.update(token);
    }

    public void deleteToken(Token token) throws Exception{
        tokenService.delete(token);
    }

    public void updateUser(User user) throws Exception{
        userService.update(user);
    }

    public User findUserByLogin(String login) throws Exception{
        return userService.findByLogin(login);
    }

    public Token getUserToken(User user) throws Exception{
        return tokenService.getUserToken(user);
    }

    public void deleteUser(User user) throws Exception{
        userService.delete(user);
    }

    public void addSectionRole(SectionRole sectionRole) throws Exception{
        sectionRoleService.create(sectionRole);
    }

    public List<SectionRole> getSectionRoles() throws Exception{
        return sectionRoleService.getAll();
    }


    public void addGlobalRole(GlobalRole sectionRole) throws Exception{
        globalRoleService.create(sectionRole);
    }

    public List<GlobalRole> getGlobalRoles() throws Exception{
        return globalRoleService.getAll();
    }


    public void addUserSectionRole(UserSectionRole role) throws Exception{
        userSectionRoleService.create(role);
    }


}
