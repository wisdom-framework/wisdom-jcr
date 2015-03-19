package org.modeshape.web.jcr.rest.handler;

import org.modeshape.web.jcr.rest.model.RestQueryPlanResult;
import org.modeshape.web.jcr.rest.model.RestQueryResult;
import org.wisdom.api.http.Request;

import javax.jcr.RepositoryException;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 19/03/15
 * Time: 16:18
 */
public interface RestQueryHandler {
    RestQueryResult executeQuery(Request request,
                                 String repositoryName,
                                 String workspaceName,
                                 String language,
                                 String statement,
                                 long offset,
                                 long limit) throws RepositoryException;

    RestQueryPlanResult planQuery(Request request,
                                  String repositoryName,
                                  String workspaceName,
                                  String language,
                                  String statement,
                                  long offset,
                                  long limit) throws RepositoryException;
}
