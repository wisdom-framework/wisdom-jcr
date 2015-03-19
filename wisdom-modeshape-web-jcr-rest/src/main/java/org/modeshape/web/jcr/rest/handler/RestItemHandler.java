package org.modeshape.web.jcr.rest.handler;

import org.modeshape.web.jcr.rest.model.RestItem;
import org.wisdom.api.http.Request;
import org.wisdom.api.http.Result;

import javax.jcr.RepositoryException;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 18/03/15
 * Time: 17:47
 */
public interface RestItemHandler {

    RestItem item(Request request,
                  String repositoryName,
                  String workspaceName,
                  String path,
                  int depth) throws RepositoryException;

    RestItem addItem(Request request,
                   String repositoryName,
                   String workspaceName,
                   String path,
                   String requestBody) throws RepositoryException;

    RestItem updateItem(Request request,
                        String rawRepositoryName,
                        String rawWorkspaceName,
                        String path,
                        String requestContent) throws RepositoryException;

    void addItems(Request request,
                      String repositoryName,
                      String workspaceName,
                      String requestContent) throws RepositoryException;

    void updateItems(Request request,
                         String repositoryName,
                         String workspaceName,
                         String requestContent) throws RepositoryException;

    void deleteItems(Request request,
                         String repositoryName,
                         String workspaceName,
                         String requestContent) throws RepositoryException;
}
