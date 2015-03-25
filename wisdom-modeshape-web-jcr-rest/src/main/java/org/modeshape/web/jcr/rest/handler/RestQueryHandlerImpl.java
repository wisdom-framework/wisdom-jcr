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

package org.modeshape.web.jcr.rest.handler;

import org.apache.felix.ipojo.annotations.Requires;
import org.modeshape.common.util.StringUtil;
import org.modeshape.web.jcr.rest.RestHelper;
import org.modeshape.web.jcr.rest.model.RestQueryPlanResult;
import org.modeshape.web.jcr.rest.model.RestQueryResult;
import org.wisdom.api.annotations.Service;
import org.wisdom.api.http.Request;
import org.wisdom.jcr.modeshape.api.RepositoryManager;

import javax.jcr.*;
import javax.jcr.query.*;
import java.util.*;

/**
 * A REST handler used for executing queries against a repository and returning REST representations of the query results.
 *
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
@Service(RestQueryHandler.class)
public final class RestQueryHandlerImpl extends AbstractHandler implements RestQueryHandler {

    private static final String MODE_URI = "mode:uri";
    private static final String UNKNOWN_TYPE = "unknown-type";
    private static final Set<String> SKIP_QUERY_PARAMETERS = new HashSet<>(Arrays.asList("offset", "limit"));

    @Requires
    RepositoryManager repositoryManager;

    /**
     * Executes a the given query string (based on the language information) against a JCR repository, returning a rest model
     * based result.
     *
     * @param repositoryName a non-null, URL encoded {@link String} representing the name of a repository
     * @param workspaceName  a non-null, URL encoded {@link String} representing the name of a workspace
     * @param language       a non-null String which should be a valid query language, as recognized by the
     *                       {@link javax.jcr.query.QueryManager}
     * @param statement      a non-null String which should be a valid query string in the above language.
     * @param offset         a numeric value which indicates the index in the result set from where results should be returned.
     * @param limit          a numeric value indicating the maximum number of rows to return.
     * @return a {@link RestQueryHandlerImpl} instance
     * @throws javax.jcr.RepositoryException if any operation fails at the JCR level
     */
    @Override
    public RestQueryResult executeQuery(Request request,
                                        String repositoryName,
                                        String workspaceName,
                                        String language,
                                        String statement,
                                        long offset,
                                        long limit) throws RepositoryException {
        assert repositoryName != null;
        assert workspaceName != null;
        assert language != null;
        assert statement != null;
        Session session = getSession(request, repositoryName, workspaceName);
        Query query = createQuery(language, statement, session);
        bindExtraVariables(request, session.getValueFactory(), query);

        QueryResult result = query.execute();
        RestQueryResult restQueryResult = new RestQueryResult();

        String[] columnNames = result.getColumnNames();
        setColumns(result, restQueryResult, columnNames);

        String baseUrl = RestHelper.repositoryUrl(request);

        setRows(offset, limit, session, result, restQueryResult, columnNames, baseUrl);

        return restQueryResult;
    }

    /**
     * Executes a the given query string (based on the language information) against a JCR repository, returning a rest model
     * based result.
     *
     * @param request        a non-null {@link Request}
     * @param repositoryName a non-null, URL encoded {@link String} representing the name of a repository
     * @param workspaceName  a non-null, URL encoded {@link String} representing the name of a workspace
     * @param language       a non-null String which should be a valid query language, as recognized by the
     *                       {@link javax.jcr.query.QueryManager}
     * @param statement      a non-null String which should be a valid query string in the above language.
     * @param offset         a numeric value which indicates the index in the result set from where results should be returned.
     * @param limit          a numeric value indicating the maximum number of rows to return.
     * @return a response containing the string representation of the query plan
     * @throws javax.jcr.RepositoryException if any operation fails at the JCR level
     */
    @Override
    public RestQueryPlanResult planQuery(Request request,
                                         String repositoryName,
                                         String workspaceName,
                                         String language,
                                         String statement,
                                         long offset,
                                         long limit) throws RepositoryException {
        assert repositoryName != null;
        assert workspaceName != null;
        assert language != null;
        assert statement != null;

        Session session = getSession(request, repositoryName, workspaceName);
        org.modeshape.jcr.api.query.Query query = createQuery(language, statement, session);
        bindExtraVariables(request, session.getValueFactory(), query);

        org.modeshape.jcr.api.query.QueryResult result = query.explain();
        String plan = result.getPlan();
        return new RestQueryPlanResult(plan, statement, language, query.getAbstractQueryModelRepresentation());
    }

    private void setRows(long offset,
                         long limit,
                         Session session,
                         QueryResult result,
                         RestQueryResult restQueryResult,
                         String[] columnNames,
                         String baseUrl) throws RepositoryException {
        RowIterator resultRows = result.getRows();
        if (offset > 0) {
            resultRows.skip(offset);
        }
        if (limit < 0) {
            limit = Long.MAX_VALUE;
        }

        while (resultRows.hasNext() && limit > 0) {
            limit--;
            Row resultRow = resultRows.nextRow();

            RestQueryResult.RestRow restRow = createRestRow(session, result, restQueryResult, columnNames, baseUrl, resultRow);
            createLinksFromNodePaths(result, baseUrl, resultRow, restRow);

            restQueryResult.addRow(restRow);
        }
    }

    private void createLinksFromNodePaths(QueryResult result,
                                          String baseUrl,
                                          Row resultRow,
                                          RestQueryResult.RestRow restRow) throws RepositoryException {
        if (result.getSelectorNames().length == 1) {
            String defaultPath = encodedPath(resultRow.getPath());
            if (!StringUtil.isBlank(defaultPath)) {
                restRow.addValue(MODE_URI, RestHelper.urlFrom(baseUrl, RestHelper.ITEMS_METHOD_NAME, defaultPath));
            }
        } else {
            for (String selectorName : result.getSelectorNames()) {
                try {
                    String selectorPath = encodedPath(resultRow.getPath(selectorName));
                    restRow.addValue(MODE_URI + "-" + selectorName,
                            RestHelper.urlFrom(baseUrl, RestHelper.ITEMS_METHOD_NAME, selectorPath));
                } catch (RepositoryException e) {
                    logger.debug(e.getMessage(), e);
                }
            }
        }
    }

    private RestQueryResult.RestRow createRestRow(Session session,
                                                  QueryResult result,
                                                  RestQueryResult restQueryResult,
                                                  String[] columnNames,
                                                  String baseUrl,
                                                  Row resultRow) throws RepositoryException {
        RestQueryResult.RestRow restRow = restQueryResult.new RestRow();
        Map<Value, String> binaryPropertyPaths = null;

        for (String columnName : columnNames) {
            Value value = resultRow.getValue(columnName);
            if (value == null) {
                continue;
            }
            String propertyPath = null;
            // because we generate links for binary properties, we need the path of the property which has the value
            if (value.getType() == PropertyType.BINARY) {
                if (binaryPropertyPaths == null) {
                    binaryPropertyPaths = binaryPropertyPaths(resultRow, result.getSelectorNames());
                }
                propertyPath = binaryPropertyPaths.get(value);
            }

            String valueString = valueToString(propertyPath, value, baseUrl, session);
            restRow.addValue(columnName, valueString);
        }
        return restRow;
    }

    private Map<Value, String> binaryPropertyPaths(Row row,
                                                   String[] selectorNames) throws RepositoryException {
        Map<Value, String> result = new HashMap<Value, String>();
        Node node = row.getNode();
        if (node != null) {
            result.putAll(binaryPropertyPaths(node));
        }

        for (String selectorName : selectorNames) {
            Node selectedNode = row.getNode(selectorName);
            if (selectedNode != null && selectedNode != node) {
                result.putAll(binaryPropertyPaths(selectedNode));
            }
        }
        return result;
    }

    private Map<Value, String> binaryPropertyPaths(Node node) throws RepositoryException {
        Map<Value, String> result = new HashMap<Value, String>();
        for (PropertyIterator propertyIterator = node.getProperties(); propertyIterator.hasNext(); ) {
            Property property = propertyIterator.nextProperty();
            if (property.getType() == PropertyType.BINARY) {
                result.put(property.getValue(), property.getPath());
            }
        }
        return result;
    }

    private void setColumns(QueryResult result,
                            RestQueryResult restQueryResult,
                            String[] columnNames) {
        if (result instanceof org.modeshape.jcr.api.query.QueryResult) {
            org.modeshape.jcr.api.query.QueryResult modeShapeQueryResult = (org.modeshape.jcr.api.query.QueryResult) result;
            String[] columnTypes = modeShapeQueryResult.getColumnTypes();
            for (int i = 0; i < columnNames.length; i++) {
                restQueryResult.addColumn(columnNames[i], columnTypes[i]);
            }
        } else {
            for (String columnName : columnNames) {
                restQueryResult.addColumn(columnName, UNKNOWN_TYPE);
            }
        }
    }

    private org.modeshape.jcr.api.query.Query createQuery(String language,
                                                          String statement,
                                                          Session session) throws RepositoryException {
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        return (org.modeshape.jcr.api.query.Query) queryManager.createQuery(statement, language);
    }

    private void bindExtraVariables(Request uriInfo,
                                    ValueFactory valueFactory,
                                    Query query) throws RepositoryException {
        if (uriInfo == null) {
            return;
        }
        // Extract the query parameters and bind as variables ...
        for (Map.Entry<String, List<String>> entry : uriInfo.parameters().entrySet()) {
            String variableName = entry.getKey();
            List<String> variableValues = entry.getValue();
            if (variableValues == null || variableValues.isEmpty() || SKIP_QUERY_PARAMETERS.contains(variableName)) {
                continue;
            }

            // Grab the first non-null value ...
            Iterator<String> valuesIterator = variableValues.iterator();
            String variableValue = null;
            while (valuesIterator.hasNext() && variableValue == null) {
                variableValue = valuesIterator.next();
            }
            if (variableValue == null) {
                continue;
            }
            // Bind the variable value to the variable name ...
            query.bindValue(variableName, valueFactory.createValue(variableValue));
        }
    }

    @Override
    protected RepositoryManager getRepositoryManager() {
        return repositoryManager;
    }
}
