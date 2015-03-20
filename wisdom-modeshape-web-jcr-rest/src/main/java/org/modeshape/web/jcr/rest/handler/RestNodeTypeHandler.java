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

import org.modeshape.web.jcr.rest.model.RestNodeType;
import org.wisdom.api.http.Request;
import org.wisdom.api.http.Result;

import javax.jcr.RepositoryException;
import java.io.InputStream;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 20/03/15
 * Time: 11:33
 */
public interface RestNodeTypeHandler {

    /**
     * Retrieves the {@link RestNodeType rest node type representation} of the {@link javax.jcr.nodetype.NodeType} with the given name.
     *
     * @param request        a non-null {@link org.wisdom.api.http.Request}
     * @param repositoryName a non-null, URL encoded {@link String} representing the name of a repository
     * @param workspaceName  a non-null, URL encoded {@link String} representing the name of a workspace
     * @param nodeTypeName   a non-null, URL encoded {@link String} representing the name of type
     * @return a {@link RestNodeType} instance.
     * @throws javax.jcr.RepositoryException if any JCR related operation fails, including if the node type cannot be found.
     */
    RestNodeType getNodeType(Request request,
                             String repositoryName,
                             String workspaceName,
                             String nodeTypeName) throws RepositoryException;

    /**
     * Imports a CND file into the repository, providing that the repository's {@link javax.jcr.nodetype.NodeTypeManager} is a valid ModeShape
     * node type manager.
     *
     * @param request        a non-null {@link Request}
     * @param repositoryName a non-null, URL encoded {@link String} representing the name of a repository
     * @param workspaceName  a non-null, URL encoded {@link String} representing the name of a workspace
     * @param allowUpdate    a flag which indicates whether existing types should be updated or not.
     * @param cndInputStream a {@link java.io.InputStream} which is expected to be the input stream of a CND file.
     * @return a non-null {@link Result} instance
     * @throws javax.jcr.RepositoryException if any JCR related operation fails
     */
    Result importCND(Request request,
                     String repositoryName,
                     String workspaceName,
                     boolean allowUpdate,
                     InputStream cndInputStream) throws RepositoryException;
}
