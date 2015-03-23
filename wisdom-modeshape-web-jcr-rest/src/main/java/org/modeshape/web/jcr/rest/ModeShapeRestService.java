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
/*
 * ModeShape (http://www.modeshape.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.modeshape.web.jcr.rest;

import org.apache.felix.ipojo.annotations.Requires;
import org.modeshape.common.annotation.Immutable;
import org.modeshape.common.util.StringUtil;
import org.modeshape.web.jcr.rest.handler.*;
import org.modeshape.web.jcr.rest.model.RestException;
import org.modeshape.web.jcr.rest.model.RestItem;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.FileItem;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

import javax.jcr.Binary;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.io.InputStream;

/**
 * RESTEasy handler to provide the JCR resources at the URIs below. Please note that these URIs assume a context of
 * {@code /resources} for the web application.
 * <table border="1">
 * <tr>
 * <th>URI Pattern</th>
 * <th>Description</th>
 * <th>Supported Methods</th>
 * </tr>
 * <tr>
 * <td>/resources</td>
 * <td>returns a list of accessible repositories</td>
 * <td>GET</td>
 * </tr>
 * <tr>
 * <td>/resources/{repositoryName}</td>
 * <td>returns a list of accessible workspaces within that repository</td>
 * <td>GET</td>
 * </tr>
 * <tr>
 * <td>/resources/{repositoryName}/{workspaceName}</td>
 * <td>returns a list of operations within the workspace</td>
 * <td>GET</td>
 * </tr>
 * <tr>
 * <td>/resources/{repositoryName}/{workspaceName}/items/{path}</td>
 * <td>accesses/creates/updates/deletes the item (node or property) at the path. For POST and PUT, the body of the request is
 * expected to be valid JSON</td>
 * <td>GET, POST, PUT, DELETE</td>
 * </tr>
 * <tr>
 * <td>/resources/{repositoryName}/{workspaceName}/items</td>
 * <td>performs bulk create/update/delete of items. For POST, PUT and DELETE the body of the request is expected to be valid JSON</td>
 * <td>POST, PUT, DELETE</td>
 * </tr>
 * <tr>
 * <tr>
 * <td>/resources/{repositoryName}/{workspaceName}/nodes/{id}</td>
 * <td>accesses/updates/deletes the with the given identifier. For POST and PUT, the body of the request is expected to be valid
 * JSON</td>
 * <td>GET, PUT, DELETE</td>
 * </tr>
 * <td>/resources/{repositoryName}/{workspaceName}/binary/{path}</td>
 * <td>accesses/creates/updates a binary property at the path</td>
 * <td>GET, POST, PUT. The binary data is expected to be written to the body of the request.</td> </tr>
 * <tr>
 * <td>/resources/{repositoryName}/{workspaceName}/nodetypes/{name}</td>
 * <td>accesses a node type from the repository, at the given name</td>
 * <td>GET</td>
 * </tr>
 * <tr>
 * <td>/resources/{repositoryName}/{workspaceName}/nodetypes</td>
 * <td>imports a CND file into the repository. The binary content of the CND file is expected to be the body of the request.</td>
 * <td>POST</td>
 * </tr>
 * <tr>
 * <td>/resources/{repositoryName}/{workspaceName}/query</td>
 * <td>executes the query in the body of the request with a language specified by the content type (application/jcr+xpath,
 * application/jcr+sql, application/jcr+sql2, or application/search)</td>
 * <td>POST</td>
 * </tr>
 * </table>
 * <h3>Binary data</h3>
 * <p>
 * When working with binary values, the <i>/resources/{repositoryName}/{workspaceName}/binary/{path}</i> method should be used.
 * When returning information involving binary values (either nodes with binary properties or binary properties directly), the
 * response will contain an URL which can be then called to retrieve the actual content of the binary value.
 * </p>
 *
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
@Immutable
@Controller
@Path("/")
public final class ModeShapeRestService extends DefaultController {

    @Requires
    private RestServerHandler serverHandler;

    @Requires
    private RestRepositoryHandler repositoryHandler;

    @Requires
    private RestItemHandler itemHandler;

    @Requires
    private RestNodeHandler nodeHandler;

    @Requires
    private RestQueryHandler queryHandler;

    @Requires
    private RestBinaryHandler binaryHandler;

    @Requires
    private RestNodeTypeHandler nodeTypeHandler;

    /**
     * Returns the list of JCR repositories available on this server
     *
     * @return the list of JCR repositories available on this server, as a {@link org.modeshape.web.jcr.rest.model.RestRepositories} instance.
     */
    @Route(method = HttpMethod.GET, uri = "/")
    public Result getRepositories() {
        return ok(serverHandler.getRepositories(request()));
    }

    /**
     * Returns the list of workspaces available to this user within the named repository.
     *
     * @param rawRepositoryName the name of the repository; may not be null
     * @return the list of workspaces available to this user within the named repository, as a {@link org.modeshape.web.jcr.rest.model.RestWorkspaces} instance.
     * @throws javax.jcr.RepositoryException if there is any other error accessing the list of available workspaces for the repository
     */
    @Route(method = HttpMethod.GET, uri = "/{repositoryName}")
    public Result getWorkspaces(@PathParameter("repositoryName") String rawRepositoryName) throws RepositoryException {
        return ok(repositoryHandler.getWorkspaces(request(), rawRepositoryName));
    }

    /**
     * Retrieves the binary content of the binary property at the given path, allowing 2 extra (optional) parameters: the
     * mime-type and the content-disposition of the binary value.
     *
     * @param repositoryName     a non-null {@link String} representing the name of a repository.
     * @param workspaceName      a non-null {@link String} representing the name of a workspace.
     * @param path               a non-null {@link String} representing the absolute path to a binary property.
     * @param mimeType           an optional {@link String} representing the "already-known" mime-type of the binary. Can be {@code null}
     * @param contentDisposition an optional {@link String} representing the client-preferred content disposition of the respose.
     *                           Can be {@code null}
     * @return the binary stream of the requested binary property or NOT_FOUND if either the property isn't found or it isn't a
     * binary
     * @throws javax.jcr.RepositoryException if any JCR related operation fails, including the case when the path to the property isn't
     *                                       valid.
     */
    @Route(method = HttpMethod.GET, uri = "{repositoryName}/{workspaceName}/" + RestHelper.BINARY_METHOD_NAME + "{path:.+}")
    public Result getBinary(@PathParameter("repositoryName") String repositoryName,
                            @PathParameter("workspaceName") String workspaceName,
                            @PathParameter("path") String path,
                            @Parameter("mimeType") String mimeType,
                            @Parameter("contentDisposition") String contentDisposition) throws RepositoryException {
        Property binaryProperty = binaryHandler.getBinaryProperty(request(), repositoryName, workspaceName, path);
        if (binaryProperty.getType() != PropertyType.BINARY) {
            return notFound(new RestException("The property " + binaryProperty.getPath() + " is not a binary").toString());
        }
        Binary binary = binaryProperty.getBinary();
        if (StringUtil.isBlank(mimeType)) {
            mimeType = binaryHandler.getDefaultMimeType(binaryProperty);
        }
        if (StringUtil.isBlank(contentDisposition)) {
            contentDisposition = binaryHandler.getDefaultContentDisposition(binaryProperty);
        }

        Result result = ok(binary.getStream()).as(mimeType);
        result.getHeaders().put("Content-Disposition", contentDisposition);
        return result;
    }

    /**
     * Retrieves the node type definition with the given name from the repository.
     *
     * @param repositoryName a non-null {@link String} representing the name of a repository.
     * @param workspaceName  a non-null {@link String} representing the name of a workspace.
     * @param nodeTypeName   a non-null {@link String} representing the name of a node type.
     * @return the node type information.
     * @throws javax.jcr.RepositoryException if any JCR related operation fails.
     */
    @Route(method = HttpMethod.GET, uri = "{repositoryName}/{workspaceName}/" + RestHelper.NODE_TYPES_METHOD_NAME + "/{nodeTypeName:.+}")
    public Result getNodeType(@PathParameter("repositoryName") String repositoryName,
                              @PathParameter("workspaceName") String workspaceName,
                              @PathParameter("nodeTypeName") String nodeTypeName) throws RepositoryException {
        return ok(nodeTypeHandler.getNodeType(request(), repositoryName, workspaceName, nodeTypeName));
    }

    /**
     * Imports a single CND file into the repository. The CND file should be submitted as the body of the request.
     *
     * @param repositoryName         a non-null {@link String} representing the name of a repository.
     * @param workspaceName          a non-null {@link String} representing the name of a workspace.
     * @param allowUpdate            an optional parameter which indicates whether existing node types should be updated (overridden) or not.
     * @param requestBodyInputStream a {@code non-null} {@link java.io.InputStream} instance, representing the body of the request.
     * @return a list with the registered node types if the operation was successful, or an appropriate error code otherwise.
     * @throws javax.jcr.RepositoryException if any JCR related operation fails.
     */
    @Route(method = HttpMethod.POST, uri = "{repositoryName}/{workspaceName}/" + RestHelper.NODE_TYPES_METHOD_NAME)
    public Result postCND(@PathParameter("repositoryName") String repositoryName,
                          @PathParameter("workspaceName") String workspaceName,
                          @PathParameter("allowUpdate") @DefaultValue("true") boolean allowUpdate,
                          @Body InputStream requestBodyInputStream) throws RepositoryException {
        return nodeTypeHandler.importCND(request(), repositoryName, workspaceName, allowUpdate, requestBodyInputStream);
    }

    /**
     * Imports a single CND file into the repository, using a {@link FormParameter} request. The CND file is
     * expected to be submitted from an HTML element with the name <i>file</i>
     *
     * @param repositoryName a non-null {@link String} representing the name of a repository.
     * @param workspaceName  a non-null {@link String} representing the name of a workspace.
     * @param allowUpdate    an optional parameter which indicates whether existing node types should be updated (overridden) or not.
     * @param form           a {@link org.wisdom.api.http.FileItem} instance representing the HTML form from which the cnd was submitted
     * @return a {@code non-null} {@link Result}
     * @throws javax.jcr.RepositoryException if any JCR operations fail
     * @throws IllegalArgumentException      if the submitted form does not contain an HTML element named "file".
     */
    @Route(method = HttpMethod.POST, uri = "{repositoryName}/{workspaceName}/" + RestHelper.NODE_TYPES_METHOD_NAME)
    public Result postCNDViaForm(@PathParameter("repositoryName") String repositoryName,
                                 @PathParameter("workspaceName") String workspaceName,
                                 @QueryParameter("allowUpdate") @DefaultValue("true") boolean allowUpdate,
                                 @FormParameter("upload") FileItem form) throws RepositoryException {
        return nodeTypeHandler.importCND(request(), repositoryName, workspaceName, allowUpdate, form.stream());
    }

    /**
     * Retrieves an item from a workspace
     *
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param path              the path to the item
     * @param depth             the depth of the node graph that should be returned if {@code path} refers to a node. @{code 0} means return
     *                          the requested node only. A negative value indicates that the full subgraph under the node should be returned. This
     *                          parameter defaults to {@code 0} and is ignored if {@code path} refers to a property.
     * @return a {@code non-null} {@link RestItem}
     * @throws javax.jcr.RepositoryException if any JCR error occurs
     * @see javax.jcr.Session#getItem(String)
     */
    @Route(method = HttpMethod.GET, uri = "{repositoryName}/{workspaceName}/" + RestHelper.ITEMS_METHOD_NAME + "{path:.*}")
    public Result getItem(@PathParameter("repositoryName") String rawRepositoryName,
                          @PathParameter("workspaceName") String rawWorkspaceName,
                          @PathParameter("path") String path,
                          @QueryParameter("depth") @DefaultValue("0") int depth) throws RepositoryException {
        return ok(itemHandler.item(request(), rawRepositoryName, rawWorkspaceName, path, depth));
    }

    /**
     * Adds the content of the request as a node (or subtree of nodes) at the location specified by {@code path}.
     * <p>
     * The primary type and mixin type(s) may optionally be specified through the {@code jcr:primaryType} and
     * {@code jcr:mixinTypes} properties as request attributes.
     * </p>
     *
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param path              the path to the item
     * @param requestContent    the JSON-encoded representation of the node or nodes to be added
     * @return a {@code non-null} {@link Result} instance which either contains the node or an error code.
     * @throws javax.jcr.RepositoryException if any other error occurs
     */
    @Route(method = HttpMethod.POST, uri = "{repositoryName}/{workspaceName}/" + RestHelper.ITEMS_METHOD_NAME + "{path:.+}")
    public Result postItem(@PathParameter("repositoryName") String rawRepositoryName,
                           @PathParameter("workspaceName") String rawWorkspaceName,
                           @PathParameter("path") String path,
                           @Body String requestContent) throws RepositoryException {
        return ok(itemHandler.addItem(request(), rawRepositoryName, rawWorkspaceName, path, requestContent));
    }

    /**
     * Performs a bulk creation of items via a single session, using the body of the request, which is expected to be a valid JSON
     * object. The format of the JSON request must be an object of the form:
     * <ul>
     * <li>{ "node1_path" : { node1_body }, "node2_path": { node2_body } ... }</li>
     * <li>{ "property1_path" : { property1_body }, "property2_path": { property2_body } ... }</li>
     * <li>{ "property1_path" : { property1_body }, "node1_path": { node1_body } ... }</li>
     * </ul>
     * where each body (either of a property or of a node) is expected to be a JSON object which has the same format as the one
     * used when creating a single item.
     *
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param requestContent    the JSON-encoded representation of the node or nodes to be added
     * @return a {@code non-null} {@link Result} instance which either contains the item or an error code.
     * @throws javax.jcr.RepositoryException if any other error occurs
     * @see ModeShapeRestService#postItem(String, String, String, String)
     */
    @Route(method = HttpMethod.POST, uri = "{repositoryName}/{workspaceName}/" + RestHelper.ITEMS_METHOD_NAME)
    public Result postItems(@PathParameter("repositoryName") String rawRepositoryName,
                            @PathParameter("workspaceName") String rawWorkspaceName,
                            @Body String requestContent) throws RepositoryException {
        itemHandler.addItems(request(), rawRepositoryName, rawWorkspaceName, requestContent);
        return ok();
    }

    /**
     * Deletes the item at {@code path}.
     *
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param path              the path to the item
     * @return a {@code non-null} {@link Result} instance.
     * @throws javax.jcr.RepositoryException if any other error occurs
     */
    @Route(method = HttpMethod.DELETE, uri = "{repositoryName}/{workspaceName}/" + RestHelper.ITEMS_METHOD_NAME + "{path:.+}")
    public Result deleteItem(@PathParameter("repositoryName") String rawRepositoryName,
                             @PathParameter("workspaceName") String rawWorkspaceName,
                             @PathParameter("path") String path) throws RepositoryException {
        itemHandler.deleteItem(request(), rawRepositoryName, rawWorkspaceName, path);
        return noContent();
    }

    /**
     * Performs a bulk deletion of nodes via a single session, using the body of the request, which is expected to be a valid JSON
     * array. The format of the JSON request must an array of the form:
     * <ul>
     * <li>["node1_path", "node2_path",...]</li>
     * <li>["property1_path", "property2_path",...]</li>
     * <li>["property1_path", "node1_path",...]</li>
     * </ul>
     *
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param requestContent    the JSON-encoded representation of the node or nodes to be added
     * @return a {@code non-null} {@link Result} instance.
     * @throws javax.jcr.RepositoryException if any other error occurs
     * @see ModeShapeRestService#deleteItem(String, String, String)
     */
    @Route(method = HttpMethod.DELETE, uri = "{repositoryName}/{workspaceName}/" + RestHelper.ITEMS_METHOD_NAME)
    public Result deleteItems(@PathParameter("repositoryName") String rawRepositoryName,
                              @PathParameter("workspaceName") String rawWorkspaceName,
                              @Body String requestContent) throws RepositoryException {
        itemHandler.deleteItems(request(), rawRepositoryName, rawWorkspaceName, requestContent);
        return ok();
    }

    /**
     * Updates the node or property at the path.
     * <p>
     * If path points to a property, this method expects the request content to be either a JSON array or a JSON string. The array
     * or string will become the values or value of the property. If path points to a node, this method expects the request
     * content to be a JSON object. The keys of the objects correspond to property names that will be set and the values for the
     * keys correspond to the values that will be set on the properties.
     * </p>
     *
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param path              the path to the item
     * @param requestContent    the JSON-encoded representation of the values and, possibly, properties to be set
     * @return a {@link RestItem} instance representing the modified item.
     * @throws javax.jcr.RepositoryException if any other error occurs
     */
    @Route(method = HttpMethod.PUT, uri = "{repositoryName}/{workspaceName}/" + RestHelper.ITEMS_METHOD_NAME + "{path:.+}")
    public Result putItem(@PathParameter("repositoryName") String rawRepositoryName,
                          @PathParameter("workspaceName") String rawWorkspaceName,
                          @PathParameter("path") String path,
                          @Body String requestContent) throws RepositoryException {
        return ok(itemHandler.updateItem(request(), rawRepositoryName, rawWorkspaceName, path, requestContent));
    }

    /**
     * Performs a bulk update of items via a single session, using the body of the request, which is expected to be a valid JSON
     * object. The format of the JSON request must be an object of the form:
     * <ul>
     * <li>{ "node1_path" : { node1_body }, "node2_path": { node2_body } ... }</li>
     * <li>{ "property1_path" : { property1_body }, "property2_path": { property2_body } ... }</li>
     * <li>{ "property1_path" : { property1_body }, "node1_path": { node1_body } ... }</li>
     * </ul>
     * where each body (either of a property or of a node) is expected to be a JSON object which has the same format as the one
     * used when updating a single item.
     *
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param requestContent    the JSON-encoded representation of the values and, possibly, properties to be set
     * @return a {@code non-null} {@link org.wisdom.api.http.Result}
     * @throws javax.jcr.RepositoryException if any other error occurs
     * @see ModeShapeRestService#putItem(String, String, String, String)
     */
    @Route(method = HttpMethod.PUT, uri = "{repositoryName}/{workspaceName}/" + RestHelper.ITEMS_METHOD_NAME)
    public Result putItems(@PathParameter("repositoryName") String rawRepositoryName,
                           @PathParameter("workspaceName") String rawWorkspaceName,
                           @Body String requestContent) throws RepositoryException {
        itemHandler.updateItems(request(), rawRepositoryName, rawWorkspaceName, requestContent);
        return ok();
    }

    /**
     * Creates or updates a binary property in the repository, at the given path. The binary content is expected to be written
     * directly to the request body.
     *
     * @param repositoryName         a non-null {@link String} representing the name of a repository.
     * @param workspaceName          a non-null {@link String} representing the name of a workspace.
     * @param path                   a non-null {@link String} representing the absolute path to a binary property.
     * @param requestBodyInputStream a non-null {@link java.io.InputStream} stream which represents the body of the request, where the
     *                               binary content is expected.
     * @return a representation of the binary property that was created/updated.
     * @throws javax.jcr.RepositoryException if any JCR related operation fails.
     */
    @Route(method = HttpMethod.POST, uri = "{repositoryName}/{workspaceName}/" + RestHelper.BINARY_METHOD_NAME + "{path:.+}")
    public Result postBinary(@PathParameter("repositoryName") String repositoryName,
                             @PathParameter("workspaceName") String workspaceName,
                             @PathParameter("path") String path,
                             @Body InputStream requestBodyInputStream) throws RepositoryException {
        return binaryHandler.updateBinary(request(), repositoryName, workspaceName, path, requestBodyInputStream, true);
    }

    /**
     * Updates a binary property in the repository, at the given path. If the binary property does not exist, the NOT_FOUND http
     * response code is returned. The binary content is expected to be written directly to the request body.
     *
     * @param repositoryName         a non-null {@link String} representing the name of a repository.
     * @param workspaceName          a non-null {@link String} representing the name of a workspace.
     * @param path                   a non-null {@link String} representing the absolute path to an existing binary property.
     * @param requestBodyInputStream a non-null {@link java.io.InputStream} stream which represents the body of the request, where the
     *                               binary content is expected.
     * @return a representation of the binary property that was updated.
     * @throws javax.jcr.RepositoryException if any JCR related operation fails.
     */
    @Route(method = HttpMethod.PUT, uri = "{repositoryName}/{workspaceName}/" + RestHelper.BINARY_METHOD_NAME + "{path:.+}")
    public Result putBinary(@PathParameter("repositoryName") String repositoryName,
                            @PathParameter("workspaceName") String workspaceName,
                            @PathParameter("path") String path,
                            @Body InputStream requestBodyInputStream) throws RepositoryException {
        return binaryHandler.updateBinary(request(), repositoryName, workspaceName, path, requestBodyInputStream, false);
    }

    /**
     * Creates/updates a binary file into the repository, at {@code path}.
     * The binary file is expected to be submitted from an HTML element with the name <i>file</i>
     *
     * @param repositoryName a non-null {@link String} representing the name of a repository.
     * @param workspaceName  a non-null {@link String} representing the name of a workspace.
     * @param path           the path to the binary property
     * @param form           a {@link org.wisdom.api.http.FileItem} instance representing the HTML form from which the binary was submitted
     * @return a {@code non-null} {@link Result}
     * @throws javax.jcr.RepositoryException if any JCR related operation fails.
     * @see ModeShapeRestService#postBinary(String, String, String, java.io.InputStream)
     */
    @Route(method = HttpMethod.POST, uri = "{repositoryName}/{workspaceName}/" + RestHelper.BINARY_METHOD_NAME + "{path:.+}")
    public Result postBinaryViaForm(@PathParameter("repositoryName") String repositoryName,
                                    @PathParameter("workspaceName") String workspaceName,
                                    @PathParameter("path") String path,
                                    @FormParameter("upload") FileItem form) throws RepositoryException {
        return binaryHandler.updateBinary(request(), repositoryName, workspaceName, path, form.stream(), true);
    }

    /**
     * Creates/updates a binary file into the repository, at {@code path}.
     * The binary content is expected to be submitted from an HTML element with the name <i>file</i>.
     * <p>
     * Depending on the whether any node exists or not at {@code path}, this method behaves in different ways:
     * <ul>
     * <li>If {@code path} exists on the server, it is expected to point to an existing [nt:file] node, for which the
     * [jcr:content]/[jcr:data] property will be updated/set</li>
     * <li>If {@code path} doesn't exist or only a <b>subpath</b> exists on the server, then for each missing segment but the last
     * an [nt:folder] node will be created. The last segment of the path will always represent the name of the [nt:file] node
     * which will be created together with its content: [jcr:content]/[jcr:data].</li>
     * </ul>
     * For example: issuing a POST request via this method to a path at which no node exists - {@code node1/node2/node3} - will
     * trigger the creation of the corresponding nodes with the types -
     * {@code [nt:folder]/[nt:folder]/[nt:file]/[jcr:content]/[jcr:data]}
     * </p>
     *
     * @param repositoryName a non-null {@link String} representing the name of a repository.
     * @param workspaceName  a non-null {@link String} representing the name of a workspace.
     * @param filePath       the path to the binary property
     * @param form           a {@link org.wisdom.api.http.FileItem} instance representing the HTML form from which the binary was submitted
     * @return a {@code non-null} {@link Result}
     * @throws javax.jcr.RepositoryException if any JCR related operation fails.
     */
    @Route(method = HttpMethod.POST, uri = "{repositoryName}/{workspaceName}/" + RestHelper.UPLOAD_METHOD_NAME + "{path:.+}")
    public Result uploadBinaryViaForm(@PathParameter("repositoryName") String repositoryName,
                                      @PathParameter("workspaceName") String workspaceName,
                                      @PathParameter("path") String filePath,
                                      @FormParameter("upload") FileItem form) throws RepositoryException {
        return binaryHandler.uploadBinary(request(), repositoryName, workspaceName, filePath, form.stream());
    }

    /**
     * Creates/updates a binary file into the repository, <b>using the body of the request as the contents of the binary file</b>,
     * at {@code path}.
     * <p>
     * Depending on the whether any node exists or not at {@code path}, this method behaves in different ways:
     * <ul>
     * <li>If {@code path} exists on the server, it is expected to point to an existing [nt:file] node, for which the
     * [jcr:content]/[jcr:data] property will be updated/set</li>
     * <li>If {@code path} doesn't exist or only a <b>subpath</b> exists on the server, then for each missing segment but the last
     * an [nt:folder] node will be created. The last segment of the path will always represent the name of the [nt:file] node
     * which will be created together with its content: [jcr:content]/[jcr:data].</li>
     * </ul>
     * For example: issuing a POST request via this method to a path at which no node exists - {@code node1/node2/node3} - will
     * trigger the creation of the corresponding nodes with the types -
     * {@code [nt:folder]/[nt:folder]/[nt:file]/[jcr:content]/[jcr:data]}
     * </p>
     *
     * @param repositoryName         a non-null {@link String} representing the name of a repository.
     * @param workspaceName          a non-null {@link String} representing the name of a workspace.
     * @param filePath               the path to the binary property
     * @param requestBodyInputStream a non-null {@link java.io.InputStream} stream which represents the body of the request, where the
     *                               binary content is expected.
     * @return a {@code non-null} {@link Result}
     * @throws javax.jcr.RepositoryException if any JCR related operation fails.
     */
    @Route(method = HttpMethod.POST, uri = "{repositoryName}/{workspaceName}/" + RestHelper.UPLOAD_METHOD_NAME + "{path:.+}")
    public Result uploadBinary(@PathParameter("repositoryName") String repositoryName,
                               @PathParameter("workspaceName") String workspaceName,
                               @PathParameter("path") String filePath,
                               @Body InputStream requestBodyInputStream) throws RepositoryException {
        return binaryHandler.uploadBinary(request(), repositoryName, workspaceName, filePath, requestBodyInputStream);
    }

    /**
     * Executes the XPath query contained in the body of the request against the give repository and workspace.
     *
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param offset            the offset to the first row to be returned. If this value is greater than the size of the result set, no
     *                          records will be returned. If this value is less than 0, results will be returned starting from the first record in
     *                          the result set.
     * @param limit             the maximum number of rows to be returned. If this value is greater than the size of the result set, the
     *                          entire result set will be returned. If this value is less than zero, the entire result set will be returned. The
     *                          results are counted from the record specified in the offset parameter.
     * @param requestContent    the query expression
     * @return a {@code non-null} {@link org.modeshape.web.jcr.rest.model.RestQueryResult} instance.
     * @throws javax.jcr.RepositoryException if any JCR error occurs
     */
    @SuppressWarnings("deprecation")
    @Route(method = HttpMethod.POST, uri = "{repositoryName}/{workspaceName}/query")
    //@Consumes( "application/jcr+xpath" )
    public Result postXPathQuery(@PathParameter("repositoryName") String rawRepositoryName,
                                 @PathParameter("workspaceName") String rawWorkspaceName,
                                 @QueryParameter("offset") @DefaultValue("-1") long offset,
                                 @QueryParameter("limit") @DefaultValue("-1") long limit,
                                 @Body String requestContent) throws RepositoryException {
        return ok(queryHandler.executeQuery(request(), rawRepositoryName, rawWorkspaceName, Query.XPATH, requestContent, offset,
                limit));
    }

    /**
     * Executes the JCR-SQL query contained in the body of the request against the give repository and workspace.
     * <p>
     * The query results will be JSON-encoded in the response body.
     * </p>
     *
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param offset            the offset to the first row to be returned. If this value is greater than the size of the result set, no
     *                          records will be returned. If this value is less than 0, results will be returned starting from the first record in
     *                          the result set.
     * @param limit             the maximum number of rows to be returned. If this value is greater than the size of the result set, the
     *                          entire result set will be returned. If this value is less than zero, the entire result set will be returned. The
     *                          results are counted from the record specified in the offset parameter.
     * @param requestContent    the query expression
     * @return a {@code non-null} {@link org.modeshape.web.jcr.rest.model.RestQueryResult} instance.
     * @throws javax.jcr.RepositoryException if any JCR error occurs
     */
    @SuppressWarnings("deprecation")
    //@Consumes( "application/jcr+sql" )
    @Route(method = HttpMethod.POST, uri = "{repositoryName}/{workspaceName}/query")
    public Result postJcrSqlQuery(@PathParameter("repositoryName") String rawRepositoryName,
                                  @PathParameter("workspaceName") String rawWorkspaceName,
                                  @QueryParameter("offset") @DefaultValue("-1") long offset,
                                  @QueryParameter("limit") @DefaultValue("-1") long limit,
                                  @Body String requestContent) throws RepositoryException {
        return ok(queryHandler.executeQuery(request(), rawRepositoryName, rawWorkspaceName, Query.SQL, requestContent, offset, limit));
    }

    /**
     * Executes the JCR-SQL2 query contained in the body of the request against the give repository and workspace.
     * <p>
     * The query results will be JSON-encoded in the response body.
     * </p>
     *
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param offset            the offset to the first row to be returned. If this value is greater than the size of the result set, no
     *                          records will be returned. If this value is less than 0, results will be returned starting from the first record in
     *                          the result set.
     * @param limit             the maximum number of rows to be returned. If this value is greater than the size of the result set, the
     *                          entire result set will be returned. If this value is less than zero, the entire result set will be returned. The
     *                          results are counted from the record specified in the offset parameter.
     * @param requestContent    the query expression
     * @return a {@code non-null} {@link org.modeshape.web.jcr.rest.model.RestQueryResult} instance.
     * @throws javax.jcr.RepositoryException if any JCR error occurs
     */
    @Route(method = HttpMethod.POST, uri = "{repositoryName}/{workspaceName}/query")
    //@Consumes("application/jcr+sql2")
    public Result postJcrSql2Query(@PathParameter("repositoryName") String rawRepositoryName,
                                   @PathParameter("workspaceName") String rawWorkspaceName,
                                   @QueryParameter("offset") @DefaultValue("-1") long offset,
                                   @QueryParameter("limit") @DefaultValue("-1") long limit,
                                   @Body String requestContent) throws RepositoryException {
        return ok(queryHandler.executeQuery(request(), rawRepositoryName, rawWorkspaceName, Query.JCR_SQL2, requestContent, offset,
                limit));
    }

    /**
     * Executes the JCR-SQL query contained in the body of the request against the give repository and workspace.
     * <p>
     * The query results will be JSON-encoded in the response body.
     * </p>
     *
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param offset            the offset to the first row to be returned. If this value is greater than the size of the result set, no
     *                          records will be returned. If this value is less than 0, results will be returned starting from the first record in
     *                          the result set.
     * @param limit             the maximum number of rows to be returned. If this value is greater than the size of the result set, the
     *                          entire result set will be returned. If this value is less than zero, the entire result set will be returned. The
     *                          results are counted from the record specified in the offset parameter.
     * @param requestContent    the query expression
     * @return a {@code non-null} {@link org.modeshape.web.jcr.rest.model.RestQueryResult} instance.
     * @throws javax.jcr.RepositoryException if any JCR error occurs
     */
    @Route(method = HttpMethod.POST, uri = "{repositoryName}/{workspaceName}/query")
    //@Consumes("application/jcr+search")
    public Result postJcrSearchQuery(@PathParameter("repositoryName") String rawRepositoryName,
                                     @PathParameter("workspaceName") String rawWorkspaceName,
                                     @QueryParameter("offset") @DefaultValue("-1") long offset,
                                     @QueryParameter("limit") @DefaultValue("-1") long limit,
                                     @Body String requestContent) throws RepositoryException {
        return ok(queryHandler.executeQuery(request(), rawRepositoryName, rawWorkspaceName,
                org.modeshape.jcr.api.query.Query.FULL_TEXT_SEARCH, requestContent, offset, limit));
    }

    /**
     * Executes the XPath query contained in the body of the request against the give repository and workspace.
     * <p>
     * The string representation of the query plan will be returned in the response body.
     * </p>
     *
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param offset            the offset to the first row to be returned. If this value is greater than the size of the result set, no
     *                          records will be returned. If this value is less than 0, results will be returned starting from the first record in
     *                          the result set.
     * @param limit             the maximum number of rows to be returned. If this value is greater than the size of the result set, the
     *                          entire result set will be returned. If this value is less than zero, the entire result set will be returned. The
     *                          results are counted from the record specified in the offset parameter.
     * @param requestContent    the query expression
     * @return a {@code non-null} response containing a string representation of the query plan
     * @throws javax.jcr.RepositoryException if any JCR error occurs
     */
    @SuppressWarnings("deprecation")
    @Route(method = HttpMethod.POST, uri = "{repositoryName}/{workspaceName}/queryPlan")
    //@Consumes("application/jcr+xpath")
    public Result postXPathQueryPlan(@PathParameter("repositoryName") String rawRepositoryName,
                                     @PathParameter("workspaceName") String rawWorkspaceName,
                                     @QueryParameter("offset") @DefaultValue("-1") long offset,
                                     @QueryParameter("limit") @DefaultValue("-1") long limit,
                                     @Body String requestContent) throws RepositoryException {
        return ok(queryHandler.planQuery(request(), rawRepositoryName, rawWorkspaceName, Query.XPATH, requestContent, offset, limit));
    }

    /**
     * Executes the JCR-SQL query contained in the body of the request against the give repository and workspace.
     * <p>
     * The string representation of the query plan will be returned in the response body.
     * </p>
     *
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param offset            the offset to the first row to be returned. If this value is greater than the size of the result set, no
     *                          records will be returned. If this value is less than 0, results will be returned starting from the first record in
     *                          the result set.
     * @param limit             the maximum number of rows to be returned. If this value is greater than the size of the result set, the
     *                          entire result set will be returned. If this value is less than zero, the entire result set will be returned. The
     *                          results are counted from the record specified in the offset parameter.
     * @param requestContent    the query expression
     * @return a {@code non-null} response containing a string representation of the query plan
     * @throws javax.jcr.RepositoryException if any JCR error occurs
     */
    @SuppressWarnings("deprecation")
    @Route(method = HttpMethod.POST, uri = "{repositoryName}/{workspaceName}/queryPlan")
    //@Consumes("application/jcr+sql")
    public Result postJcrSqlQueryPlan(@PathParameter("repositoryName") String rawRepositoryName,
                                      @PathParameter("workspaceName") String rawWorkspaceName,
                                      @QueryParameter("offset") @DefaultValue("-1") long offset,
                                      @QueryParameter("limit") @DefaultValue("-1") long limit,
                                      @Body String requestContent) throws RepositoryException {
        return ok(queryHandler.planQuery(request(), rawRepositoryName, rawWorkspaceName, Query.SQL, requestContent, offset, limit));
    }

    /**
     * Executes the JCR-SQL2 query contained in the body of the request against the give repository and workspace.
     * <p>
     * The string representation of the query plan will be returned in the response body.
     * </p>
     *
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param offset            the offset to the first row to be returned. If this value is greater than the size of the result set, no
     *                          records will be returned. If this value is less than 0, results will be returned starting from the first record in
     *                          the result set.
     * @param limit             the maximum number of rows to be returned. If this value is greater than the size of the result set, the
     *                          entire result set will be returned. If this value is less than zero, the entire result set will be returned. The
     *                          results are counted from the record specified in the offset parameter.
     * @param requestContent    the query expression
     * @return a {@code non-null} response containing a string representation of the query plan
     * @throws javax.jcr.RepositoryException if any JCR error occurs
     */
    @Route(method = HttpMethod.POST, uri = "{repositoryName}/{workspaceName}/queryPlan")
    //@Consumes("application/jcr+sql2")
    public Result postJcrSql2QueryPlan(@PathParameter("repositoryName") String rawRepositoryName,
                                       @PathParameter("workspaceName") String rawWorkspaceName,
                                       @QueryParameter("offset") @DefaultValue("-1") long offset,
                                       @QueryParameter("limit") @DefaultValue("-1") long limit,
                                       @Body String requestContent) throws RepositoryException {
        return ok(queryHandler.planQuery(request(), rawRepositoryName, rawWorkspaceName, Query.JCR_SQL2, requestContent, offset,
                limit));
    }

    /**
     * Compute the plan for the JCR-SQL query contained in the body of the request against the give repository and workspace.
     * <p>
     * The string representation of the query plan will be returned in the response body.
     * </p>
     *
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param offset            the offset to the first row to be returned. If this value is greater than the size of the result set, no
     *                          records will be returned. If this value is less than 0, results will be returned starting from the first record in
     *                          the result set.
     * @param limit             the maximum number of rows to be returned. If this value is greater than the size of the result set, the
     *                          entire result set will be returned. If this value is less than zero, the entire result set will be returned. The
     *                          results are counted from the record specified in the offset parameter.
     * @param requestContent    the query expression
     * @return a {@code non-null} response containing a string representation of the query plan
     * @throws javax.jcr.RepositoryException if any JCR error occurs
     */
    @Route(method = HttpMethod.POST, uri = "{repositoryName}/{workspaceName}/queryPlan")
    //@Consumes("application/jcr+search")
    public Result postJcrSearchQueryPlan(@PathParameter("repositoryName") String rawRepositoryName,
                                         @PathParameter("workspaceName") String rawWorkspaceName,
                                         @QueryParameter("offset") @DefaultValue("-1") long offset,
                                         @QueryParameter("limit") @DefaultValue("-1") long limit,
                                         @Body String requestContent) throws RepositoryException {
        return ok(queryHandler.planQuery(request(), rawRepositoryName, rawWorkspaceName,
                org.modeshape.jcr.api.query.Query.FULL_TEXT_SEARCH, requestContent, offset, limit));
    }

    /**
     * Retrieves from a workspace the node with the specified identifier.
     *
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param id                the node identifier of the existing item
     * @param depth             the depth of the node graph that should be returned. @{code 0} means return the requested node only. A
     *                          negative value indicates that the full subgraph under the node should be returned. This parameter defaults to
     *                          {@code 0}.
     * @return a {@code non-null} {@link RestItem}
     * @throws javax.jcr.RepositoryException if any JCR error occurs
     * @see javax.jcr.Session#getNodeByIdentifier(String)
     */
    @Route(method = HttpMethod.GET, uri = "{repositoryName}/{workspaceName}/" + RestHelper.NODES_METHOD_NAME + "/{id:.*}")
    public Result getNodeWithId(@PathParameter("repositoryName") String rawRepositoryName,
                                  @PathParameter("workspaceName") String rawWorkspaceName,
                                  @PathParameter("id") String id,
                                  @QueryParameter("depth") @DefaultValue("0") int depth) throws RepositoryException {
        return ok(nodeHandler.nodeWithId(request(), rawRepositoryName, rawWorkspaceName, id, depth));
    }

    /**
     * Updates the node with the given identifier
     * <p>
     * This method expects the request content to be a JSON object. The keys of the objects correspond to property names that will
     * be set and the values for the keys correspond to the values that will be set on the properties.
     * </p>
     *
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param id                the node identifier of the existing item
     * @param requestContent    the JSON-encoded representation of the values and, possibly, properties to be set
     * @return a {@link RestItem} instance representing the modified item.
     * @throws javax.jcr.RepositoryException if any other error occurs
     */
    @Route(method = HttpMethod.PUT, uri = "{repositoryName}/{workspaceName}/" + RestHelper.NODES_METHOD_NAME + "/{id:.+}")
    public Result putNodeWithId(@PathParameter("repositoryName") String rawRepositoryName,
                                  @PathParameter("workspaceName") String rawWorkspaceName,
                                  @PathParameter("id") String id,
                                  @Body String requestContent) throws RepositoryException {
        return ok(nodeHandler.updateNodeWithId(request(), rawRepositoryName, rawWorkspaceName, id, requestContent));
    }

    /**
     * Deletes the subgraph at the node with the given identifier.
     *
     * @param rawRepositoryName the URL-encoded repository name
     * @param rawWorkspaceName  the URL-encoded workspace name
     * @param id                the node identifier of the existing item
     * @return a {@code non-null} {@link org.wisdom.api.http.Result} instance.
     * @throws javax.jcr.RepositoryException if any other error occurs
     */
    @Route(method = HttpMethod.DELETE, uri = "{repositoryName}/{workspaceName}/" + RestHelper.NODES_METHOD_NAME + "/{id:.+}")
    public Result deleteNodeWithId(@PathParameter("repositoryName") String rawRepositoryName,
                                   @PathParameter("workspaceName") String rawWorkspaceName,
                                   @PathParameter("id") String id) throws RepositoryException {
        nodeHandler.deleteNodeWithId(request(), rawRepositoryName, rawWorkspaceName, id);
        return noContent();
    }
}
