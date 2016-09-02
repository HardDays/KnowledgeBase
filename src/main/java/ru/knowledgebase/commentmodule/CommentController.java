package ru.knowledgebase.commentmodule;

import ru.knowledgebase.dbmodule.DataCollector;
import ru.knowledgebase.exceptionmodule.articleexceptions.ArticleNotFoundException;
import ru.knowledgebase.exceptionmodule.commentexceptions.CommentNotFoundException;
import ru.knowledgebase.exceptionmodule.databaseexceptions.DataBaseException;
import ru.knowledgebase.exceptionmodule.userexceptions.UserNotFoundException;
import ru.knowledgebase.exceptionmodule.userexceptions.WrongUserDataException;
import ru.knowledgebase.modelsmodule.articlemodels.Article;
import ru.knowledgebase.modelsmodule.commentmodels.Comment;
import ru.knowledgebase.modelsmodule.usermodels.User;

import java.util.List;

/**
 * Created by vova on 01.09.16.
 */
public class CommentController {
    private DataCollector collector = new DataCollector();

    private static volatile CommentController instance;

    /**
     * Get instance of a class
     * @return instance of a class
     */
    public static CommentController getInstance() {
        CommentController localInstance = instance;
        if (localInstance == null) {
            synchronized (CommentController.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new CommentController();
                }
            }
        }
        return localInstance;
    }

    public void add(Comment comment) throws Exception{
        try{
            collector.addComment(comment);
        }catch (Exception e){
            throw new DataBaseException();
        }
    }

    public void add(int userId, int articleId, String comment, String articleText) throws Exception{
        User user = null;
        Article article = null;
        if (articleText.length() == 0 || comment.length() == 0)
            throw new WrongUserDataException();
        try{
            user = collector.findUser(userId);
            article = collector.findArticle(articleId);
        }catch (Exception e){
            e.printStackTrace();
            throw new DataBaseException();
        }
        if (user == null)
            throw new UserNotFoundException();
        if (article == null)
            throw new ArticleNotFoundException();
        Article temp = article;
        while(!temp.isSection()){
            temp = temp.getParentArticle();
        }
        User admin = null;
        try {
            admin = collector.findMistakeViewers(temp).get(0);
        }catch (Exception e){
            throw new DataBaseException();
        }
        add(new Comment(user, admin, article, comment, articleText));
    }

    public List<Comment> findByAdmin(int adminId) throws Exception{
        User admin = null;
        try {
            admin = collector.findUser(adminId);
        }catch (Exception e){
            throw new DataBaseException();
        }
        if (admin == null)
            throw new UserNotFoundException();
        return collector.findCommentsByAdmin(admin);
    }

    public boolean canDeleteComment(int adminId, int commentId) throws Exception{
        Comment comment = null;
        try {
            comment = collector.findComment(commentId);
        }catch (Exception e){
            throw new DataBaseException();
        }
        if (comment == null)
            throw new CommentNotFoundException();
        return comment.getAdmin().getId() == adminId;
    }

    public void delete(Comment comment) throws Exception{
        if (comment == null)
            throw new CommentNotFoundException();
        try{
            collector.deleteComment(comment);
        }catch (Exception e){
            throw new DataBaseException();
        }
    }

    public void delete(int id) throws Exception{
        Comment comment = null;
        try{
            comment = collector.findComment(id);
        }catch (Exception e){
            throw new DataBaseException();
        }
        delete(comment);
    }

}
