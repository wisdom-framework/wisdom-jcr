/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
public interface RestItemHandler extends ItemHandler {

    /**
     * Retrieves the JCR {@link javax.jcr.Item} at the given path, returning its rest representation.
     *
     * @param request        the servlet request; may not be null or unauthenticated
     * @param repositoryName the URL-encoded repository name
     * @param workspaceName  the URL-encoded workspace name
     * @param path           the path to the item
     * @param depth          the depth of the node graph that should be returned if {@code path} refers to a node. @{code 0} means return
     *                       the requested node only. A negative value indicates that the full subgraph under the node should be returned. This
     *                       parameter defaults to {@code 0} and is ignored if {@code path} refers to a property.
     * @return a the rest representation of the item, as a {@link RestItem} instance.
     * @throws javax.jcr.RepositoryException if any JCR operations fail.
     */
    RestItem item(Request request,
                  String repositoryName,
                  String workspaceName,
                  String path,
                  int depth) throws RepositoryException;

    /**
     * Adds the content of the request as a node (or subtree of nodes) at the location specified by {@code path}.
     * <p>
     * The primary type and mixin type(s) may optionally be specified through the {@code jcr:primaryType} and
     * {@code jcr:mixinTypes} properties.
     * </p>
     *
     * @param request        the servlet request; may not be null or unauthenticated
     * @param repositoryName the URL-encoded repository name
     * @param workspaceName  the URL-encoded workspace name
     * @param path           the path to the item
     * @param requestBody    the JSON-encoded representation of the node or nodes to be added
     * @return the JSON-encoded representation of the node or nodes that were added. This will differ from {@code requestBody} in
     * that auto-created and protected properties (e.g., jcr:uuid) will be populated.
     * @throws javax.jcr.RepositoryException if any other error occurs while interacting with the repository
     */
    RestItem addItem(Request request,
                   String repositoryName,
                   String workspaceName,
                   String path,
                   String requestBody) throws RepositoryException;

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
     * @param path              the path to the item
     * @param requestContent    the JSON-encoded representation of the values and, possibly, properties to be set
     * @return the JSON-encoded representation of the node on which the property or properties were set.
     * @throws javax.jcr.RepositoryException if any error occurs at the repository level.
     */
    RestItem updateItem(Request request,
                        String rawRepositoryName,
                        String rawWorkspaceName,
                        String path,
                        String requestContent) throws RepositoryException;

    /**
     * Performs a bulk creation of items, using a single {@link javax.jcr.Session}. If any of the items cannot be created for whatever
     * reason, the entire operation fails.
     *
     * @param request        the servlet request; may not be null or unauthenticated
     * @param repositoryName the URL-encoded repository name
     * @param workspaceName  the URL-encoded workspace name
     * @param requestContent the JSON-encoded representation of the nodes and, possibly, properties to be added
     * @return a {@code non-null} {@link Result}
     * @throws javax.jcr.RepositoryException if any of the JCR operations fail
     * @see RestItemHandlerImpl#addItem(Request, String, String, String, String)
     */
    void addItems(Request request,
                      String repositoryName,
                      String workspaceName,
                      String requestContent) throws RepositoryException;

    /**
     * Performs a bulk updating of items, using a single {@link javax.jcr.Session}. If any of the items cannot be updated for whatever
     * reason, the entire operation fails.
     *
     * @param request        the servlet request; may not be null or unauthenticated
     * @param repositoryName the URL-encoded repository name
     * @param workspaceName  the URL-encoded workspace name
     * @param requestContent the JSON-encoded representation of the values and, possibly, properties to be set
     * @throws javax.jcr.RepositoryException if any of the JCR operations fail
     * @see RestItemHandlerImpl#updateItem(Request, String, String, String, String)
     */
    void updateItems(Request request,
                         String repositoryName,
                         String workspaceName,
                         String requestContent) throws RepositoryException;

    /**
     * Performs a bulk deletion of items, using a single {@link javax.jcr.Session}. If any of the items cannot be deleted for whatever
     * reason, the entire operation fails.
     *
     * @param request        the servlet request; may not be null or unauthenticated
     * @param repositoryName the URL-encoded repository name
     * @param workspaceName  the URL-encoded workspace name
     * @param requestContent the JSON-encoded array of the nodes to remove
     * @return a {@code non-null} {@link Result}
     * @throws javax.jcr.RepositoryException if any of the JCR operations fail
     * @see RestItemHandlerImpl#deleteItem(Request, String, String, String)
     */
    void deleteItems(Request request,
                         String repositoryName,
                         String workspaceName,
                         String requestContent) throws RepositoryException;
}
