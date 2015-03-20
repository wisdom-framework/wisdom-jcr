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
    RestQueryResult executeQuery(Request request,
                                 String repositoryName,
                                 String workspaceName,
                                 String language,
                                 String statement,
                                 long offset,
                                 long limit) throws RepositoryException;

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
    RestQueryPlanResult planQuery(Request request,
                                  String repositoryName,
                                  String workspaceName,
                                  String language,
                                  String statement,
                                  long offset,
                                  long limit) throws RepositoryException;
}
