package ru.knowledgebase.usermodule;

import org.junit.*;
import static org.junit.Assert.*;

import ru.knowledgebase.articlemodule.ArticleController;
import ru.knowledgebase.dbmodule.DataCollector;
import ru.knowledgebase.exceptionmodule.ldapexceptions.LdapException;
import ru.knowledgebase.exceptionmodule.roleexceptions.RoleNotAssignedException;
import ru.knowledgebase.ldapmodule.LdapWorker;
import ru.knowledgebase.modelsmodule.articlemodels.Article;
import ru.knowledgebase.modelsmodule.rolemodels.ArticleRole;
import ru.knowledgebase.modelsmodule.rolemodels.GlobalRole;
import ru.knowledgebase.modelsmodule.rolemodels.UserGlobalRole;
import ru.knowledgebase.modelsmodule.usermodels.Token;
import ru.knowledgebase.modelsmodule.usermodels.User;
import ru.knowledgebase.exceptionmodule.userexceptions.UserAlreadyExistsException;
import ru.knowledgebase.exceptionmodule.userexceptions.UserNotFoundException;
import ru.knowledgebase.exceptionmodule.userexceptions.WrongPasswordException;
import ru.knowledgebase.exceptionmodule.userexceptions.WrongUserDataException;
import ru.knowledgebase.rolemodule.ArticleRoleController;
import ru.knowledgebase.rolemodule.GlobalRoleController;

import java.sql.Timestamp;

/**
 * Created by vova on 18.08.16.
 */
public class UserControllerTest {
    private DataCollector collector = DataCollector.getInstance();
    private UserController c = UserController.getInstance();

    private User user;
    private User user2;
    private GlobalRole role;
    private ArticleRole role2;
    private Article base;
    private Article article1;
    private Article article2;
    private Article article3;

    @Before
    public void prepareAll() throws Exception{
        try{
            user = collector.findUser("testeeee1");
            user2 = collector.findUser("testeeee2");

        }catch (Exception e){

        }
        if (user == null)
            user = UserController.getInstance().register("testeeee1", "1", "t1@m",
                    "rrr", "ttt", "aaaa", "ssss", "111", "444", null, null);
        if (user2 == null)
            user2 = UserController.getInstance().register("testeeee2", "2", "t1@m",
                    "rrr", "ttt", "aaaa", "ssss", "111", "444", null, null);
        try{
            base = ArticleController.getInstance().getBaseArticle();
        }catch (Exception e){

        }
        if (base == null)
            base = ArticleController.getInstance().addBaseArticle("s", "f", user.getId(), null, null, null);

        try{
            role = collector.addGlobalRole(new GlobalRole());
            role2 = collector.addArticleRole(new ArticleRole());
            article1 = ArticleController.getInstance().addArticle("1", "f", user.getId(), base.getId(), null, null, null, true);
            article2 = ArticleController.getInstance().addArticle("2", "f", user.getId(), base.getId(), null, null, null, true);
            article3 = ArticleController.getInstance().addArticle("3", "f", user.getId(), article2.getId(), null, null, null, true);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @After
    public void deleteAll() throws Exception{
        try{
            collector.deleteArticle(article1.getId());
            collector.deleteArticle(article2.getId());
            //  collector.deleteArticle(article3.getId());
            collector.deleteArticle(base.getId());
            collector.deleteUser(user.getId());
            collector.deleteUser(user2.getId());
            collector.deleteGlobalRole(role.getId());
            collector.deleteArticleRole(role2.getId());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void delete1() throws Exception {
        ArticleRoleController.getInstance().assignUserRole(user2.getId(), article1.getId(), role2.getId());
        c.delete(user2.getId());
        assertTrue(collector.findUserArticleRole(user2, article1) == null);
    }

    @Test
    public void delete2() throws Exception {
        GlobalRoleController.getInstance().assignUserRole(user2.getId(), role.getId());
        c.delete(user2.getId());
        assertTrue(collector.findUserGlobalRole(user2) == null);
    }

    @Test
    public void delete3() throws Exception {
        c.authorize("testeeee2", "2");
        c.delete(user2.getId());
        assertTrue(collector.getUserToken(user2) == null);
        //collector.deleteToken(token);
    }
}