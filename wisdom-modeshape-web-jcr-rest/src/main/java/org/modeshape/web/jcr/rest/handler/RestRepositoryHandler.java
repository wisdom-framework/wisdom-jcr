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
