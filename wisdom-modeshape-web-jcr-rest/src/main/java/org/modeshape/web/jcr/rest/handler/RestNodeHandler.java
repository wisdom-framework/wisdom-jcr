package org.modeshape.web.jcr.rest.handler;

import org.modeshape.web.jcr.rest.model.RestItem;
import org.wisdom.api.http.Request;

import javax.jcr.RepositoryException;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 20/03/15
 * Time: 11:32
 */
public interface RestNodeHandler {

    /**
     * Retrieves the JCR {@link javax.jcr.Item} at the given path, returning its rest representation.
     *
     * @param request        the servlet request; may not be null or unauthenticated
     * @param repositoryName the URL-encoded repository name
     * @param workspaceName  the URL-encoded workspace name
     * @param id             the node identifier
     * @param depth          the depth of the node graph that should be returned if {@code path} refers to a node. @{code 0} means return
     *                       the requested node only. A negative value indicates that the full subgraph under the node should be returned. This
     *                       parameter defaults to {@code 0} and is ignored if {@code path} refers to a property.
     * @return a the rest representation of the item, as a {@link RestItem} instance.
     * @throws javax.jcr.RepositoryException if any JCR operations fail.
     */
    RestItem nodeWithId(Request request,
                        String repositoryName,
                        String workspaceName,
                        String id,
                        int depth) throws RepositoryException;

    /**
     * Updates the properties at the path.
     * <p>
     * If path points to a property, this method expects the request content to be either a JSON array or a JSON string. The array
     * or string will become the values or value of the property. If path points to a node, this method expects the request
     * content to be a JSON object. The keys of the objects correspond to property names that will be set and the values for the
     * keys correspond to the values that will be set on the properties.
     * </p>
     *
     * @param request           the servlet request; may not be null or unauthenticated
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param id                the node identifier
     * @param requestContent    the JSON-encoded representation of the values and, possibly, properties to be set
     * @return the JSON-encoded representation of the node on which the property or properties were set.
     * @throws javax.jcr.RepositoryException if any error occurs at the repository level.
     */
    RestItem updateNodeWithId(Request request,
                              String rawRepositoryName,
                              String rawWorkspaceName,
                              String id,
                              String requestContent) throws RepositoryException;

    /**
     * Deletes the subgraph at the node with the specified id, including all descendants.
     *
     * @param request           the servlet request; may not be null or unauthenticated
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param id                the node identifier
     * @throws javax.jcr.RepositoryException if any other error occurs
     */
    void deleteNodeWithId(Request request,
                          String rawRepositoryName,
                          String rawWorkspaceName,
                          String id) throws RepositoryException;

}
