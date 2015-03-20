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

package org.modeshape.web.jcr.rest.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.felix.ipojo.annotations.Requires;
import org.modeshape.common.util.StringUtil;
import org.modeshape.web.jcr.rest.model.RestItem;
import org.wisdom.api.annotations.Service;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.Request;
import org.wisdom.api.http.Result;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.*;

/**
 * An extension to the {@link ItemHandler} which is used by {@link org.modeshape.web.jcr.rest.ModeShapeRestService} to interact
 * with properties and nodes.
 *
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
@Service(RestItemHandler.class)
public final class RestItemHandlerImpl extends ItemHandler implements RestItemHandler {

    @Requires
    Json json;

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
    @Override
    public RestItem item(Request request,
                         String repositoryName,
                         String workspaceName,
                         String path,
                         int depth) throws RepositoryException {
        Session session = getSession(request, repositoryName, workspaceName);
        Item item = itemAtPath(path, session);
        return createRestItem(request, depth, session, item);
    }

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
    @Override
    public RestItem addItem(Request request,
                            String repositoryName,
                            String workspaceName,
                            String path,
                            String requestBody) throws RepositoryException {
        JsonNode requestBodyJSON = stringToJSONObject(requestBody);

        String parentAbsPath = parentPath(path);
        String newNodeName = newNodeName(path);

        Session session = getSession(request, repositoryName, workspaceName);
        Node parentNode = (Node) session.getItem(parentAbsPath);
        Node newNode = addNode(parentNode, newNodeName, requestBodyJSON);

        session.save();
        RestItem restNewNode = createRestItem(request, 0, session, newNode);
        return restNewNode;
    }

    @Override
    protected Json getJson() {
        return json;
    }

    @Override
    protected JsonNode getProperties(JsonNode jsonNode) {
        ObjectNode properties = json.newObject();
        for (Iterator<?> keysIterator = jsonNode.fields(); keysIterator.hasNext(); ) {
            String key = keysIterator.next().toString();
            if (CHILD_NODE_HOLDER.equalsIgnoreCase(key)) {
                continue;
            }
            properties.put(key, jsonNode.get(key));
        }
        return properties;
    }

    private String newNodeName(String path) {
        int lastSlashInd = path.lastIndexOf('/');
        String name = lastSlashInd == -1 ? path : path.substring(lastSlashInd + 1);
        // Remove any SNS index ...
        name = name.replaceAll("\\[\\d+\\]$", "");
        return name;
    }

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
    @Override
    public RestItem updateItem(Request request,
                               String rawRepositoryName,
                               String rawWorkspaceName,
                               String path,
                               String requestContent) throws RepositoryException {
        Session session = getSession(request, rawRepositoryName, rawWorkspaceName);
        Item item = itemAtPath(path, session);
        item = updateItem(item, stringToJSONObject(requestContent));
        session.save();

        return createRestItem(request, 0, session, item);
    }

    private ObjectNode stringToJSONObject(String requestBody) {
        return StringUtil.isBlank(requestBody) ? json.newObject() : (ObjectNode) json.parse(requestBody);
    }

    private ArrayNode stringToJSONArray(String requestBody) {
        return StringUtil.isBlank(requestBody) ? json.newArray() : (ArrayNode) json.parse(requestBody);
    }

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
    @Override
    public void addItems(Request request,
                         String repositoryName,
                         String workspaceName,
                         String requestContent) throws RepositoryException {
        ObjectNode requestBody = stringToJSONObject(requestContent);
        if (requestBody.size() != 0) {
            Session session = getSession(request, repositoryName, workspaceName);
            TreeMap<String, JsonNode> nodesByPath = createNodesByPathMap(requestBody);
            addMultipleNodes(request, nodesByPath, session);
        }
    }

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
    @Override
    public void updateItems(Request request,
                            String repositoryName,
                            String workspaceName,
                            String requestContent) throws RepositoryException {
        ObjectNode requestBody = stringToJSONObject(requestContent);
        if (requestBody.size() != 0) {
            Session session = getSession(request, repositoryName, workspaceName);
            TreeMap<String, JsonNode> nodesByPath = createNodesByPathMap(requestBody);
            List<RestItem> result = updateMultipleNodes(request, session, nodesByPath);
        }
    }

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
    @Override
    public void deleteItems(Request request,
                            String repositoryName,
                            String workspaceName,
                            String requestContent) throws RepositoryException {
        ArrayNode requestArray = stringToJSONArray(requestContent);
        if (requestArray.size() == 0) {
            return;
        }

        Session session = getSession(request, repositoryName, workspaceName);
        TreeSet<String> pathsInOrder = new TreeSet<>();
        for (int i = 0; i < requestArray.size(); i++) {
            pathsInOrder.add(absPath(requestArray.get(i).toString()));
        }
        List<String> pathsInOrderList = new ArrayList<>(pathsInOrder);
        Collections.reverse(pathsInOrderList);
        for (String path : pathsInOrderList) {
            doDelete(path, session);
        }
        session.save();
    }

    private List<RestItem> updateMultipleNodes(Request request,
                                               Session session,
                                               TreeMap<String, JsonNode> nodesByPath)
            throws RepositoryException {
        List<RestItem> result = new ArrayList<RestItem>();
        for (String nodePath : nodesByPath.keySet()) {
            Item item = session.getItem(nodePath);
            item = updateItem(item, nodesByPath.get(nodePath));
            result.add(createRestItem(request, 0, session, item));
        }
        session.save();
        return result;
    }

    private TreeMap<String, JsonNode> createNodesByPathMap(JsonNode requestBodyJSON) {
        TreeMap<String, JsonNode> nodesByPath = new TreeMap<String, JsonNode>();
        for (Iterator<?> iterator = requestBodyJSON.fields(); iterator.hasNext(); ) {
            String key = iterator.next().toString();
            String nodePath = absPath(key);
            JsonNode nodeJSON = requestBodyJSON.get(key);
            nodesByPath.put(nodePath, nodeJSON);
        }
        return nodesByPath;
    }

    private void addMultipleNodes(Request request,
                                  TreeMap<String, JsonNode> nodesByPath,
                                  Session session) throws RepositoryException {
        List<RestItem> result = new ArrayList<RestItem>();

        for (String nodePath : nodesByPath.keySet()) {
            String parentAbsPath = parentPath(nodePath);
            String newNodeName = newNodeName(nodePath);

            Node parentNode = (Node) session.getItem(parentAbsPath);
            Node newNode = addNode(parentNode, newNodeName, nodesByPath.get(nodePath));
            RestItem restNewNode = createRestItem(request, 0, session, newNode);
            result.add(restNewNode);
        }

        session.save();
    }

}
