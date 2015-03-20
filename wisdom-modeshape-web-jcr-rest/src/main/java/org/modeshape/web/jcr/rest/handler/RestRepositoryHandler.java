package org.modeshape.web.jcr.rest.handler;

import org.modeshape.web.jcr.rest.model.RestWorkspaces;
import org.wisdom.api.http.Request;

import javax.jcr.RepositoryException;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 20/03/15
 * Time: 11:34
 */
public interface RestRepositoryHandler {

    /**
     * Returns the list of workspaces available to this user within the named repository.
     *
     * @param request        the servlet request; may not be null
     * @param repositoryName the name of the repository; may not be null
     * @return the list of workspaces available to this user within the named repository, as a {@link RestWorkspaces} object
     * @throws javax.jcr.RepositoryException if there is any other error accessing the list of available workspaces for the repository
     */
    RestWorkspaces getWorkspaces(Request request,
                                 String repositoryName) throws RepositoryException;

}
