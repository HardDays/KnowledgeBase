package ru.knowledgebase.wrappermodule;

import ru.knowledgebase.commentmodule.CommentController;
import ru.knowledgebase.modelsmodule.commentmodels.Comment;
import ru.knowledgebase.responsemodule.ResponseBuilder;
import ru.knowledgebase.rolemodule.ArticleRoleController;
import ru.knowledgebase.usermodule.UserController;

import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by vova on 01.09.16.
 */
public class CommentWrapper {

    private CommentController commentController = CommentController.getInstance();
    private UserController userController = UserController.getInstance();
    private ArticleRoleController articleRoleController = ArticleRoleController.getInstance();

    /**
     * Add new comment
     * @param userId author of comment
     * @param token token of author
     * @param articleId id of article to comment
     * @param comment user comment
     * @param articleText text in article with mistake
     * @return Response object
     */
    public Response add(int userId, String token, int articleId, String comment, String articleText){
        try {
            boolean okToken = userController.checkUserToken(userId, token);
            boolean hasRights = articleRoleController.canAddMistakes(userId, articleId);
            if (!okToken)
                return ResponseBuilder.buildWrongTokenResponse();
            if (!hasRights)
                return ResponseBuilder.buildNoAccessResponse();
            commentController.add(userId, articleId, comment, articleText);
            return ResponseBuilder.buildCommentAddedResponse();
        }catch (Exception e){
            return ResponseBuilder.buildResponse(e);
        }
    }
    /**
     * Get list of comments
     * @param adminId id of admin
     * @param token token of admin
     * @return Response object
     */
    public Response get(int adminId, String token){
        try{
            boolean okToken = userController.checkUserToken(adminId, token);
            if (!okToken)
                return ResponseBuilder.buildWrongTokenResponse();
            List<Comment> comments = commentController.findByAdmin(adminId);
            return ResponseBuilder.buildCommentListResponse(comments);
        }catch (Exception e){
            return ResponseBuilder.buildResponse(e);
        }
    }
    /**
     * Delete comment
     * @param adminId id of admin
     * @param token token of admin
     * @return Response object
     */
    public Response delete(int adminId, String token, int commentId){
        try{
            boolean okToken = userController.checkUserToken(adminId, token);
            boolean hasRights = commentController.canDeleteComment(adminId, commentId);
            if (!okToken)
                return ResponseBuilder.buildWrongTokenResponse();
            if (!hasRights)
                return ResponseBuilder.buildWrongTokenResponse();
            commentController.delete(commentId);
            return ResponseBuilder.buildCommentDeleted();
        }catch (Exception e){
            return ResponseBuilder.buildResponse(e);
        }
    }


}