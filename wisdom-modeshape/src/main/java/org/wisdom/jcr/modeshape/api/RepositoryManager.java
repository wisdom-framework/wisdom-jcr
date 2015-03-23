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
package org.wisdom.jcr.modeshape.api;

import org.wisdom.api.http.Request;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Set;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 20/03/15
 * Time: 16:22
 */
public interface RepositoryManager {

    /**
     * Get a JCR Session for the named workspace in the named repository, using the supplied HTTP servlet request for
     * authentication information.
     *
     * @param request        the servlet request; may not be null or unauthenticated
     * @param repositoryName the name of the repository in which the session is created
     * @param workspaceName  the name of the workspace to which the session should be connected
     * @return an active session with the given workspace in the named repository
     * @throws javax.jcr.RepositoryException if the named repository does not exist or there was a problem obtaining the named repository
     */
    Session getSession(Request request,
                       String repositoryName,
                       String workspaceName) throws RepositoryException;

    /**
     * Returns the {@link javax.jcr.Repository} instance with the given name.
     *
     * @param repositoryName a {@code non-null} string
     * @return a {@link javax.jcr.Repository} instance, never {@code null}
     * @throws NoSuchRepositoryException if no repository with the given name exists.
     */
    Repository getRepository(String repositoryName) throws NoSuchRepositoryException;

    /**
     * Returns a set with all the names of the available repositories.
     *
     * @return a set with the names, never {@code null}
     */
    Set<String> getJcrRepositoryNames();

}
