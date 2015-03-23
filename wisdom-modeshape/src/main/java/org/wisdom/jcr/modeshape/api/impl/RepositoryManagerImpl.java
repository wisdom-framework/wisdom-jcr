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
package org.wisdom.jcr.modeshape.api.impl;

import org.apache.felix.ipojo.annotations.Requires;
import org.modeshape.common.annotation.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.annotations.Service;
import org.wisdom.api.http.Request;
import org.wisdom.jcr.modeshape.service.ModeshapeRepositoryFactory;
import org.wisdom.jcr.modeshape.api.NoSuchRepositoryException;
import org.wisdom.jcr.modeshape.RequestCredentials;
import org.wisdom.jcr.modeshape.WebJcrI18n;
import org.wisdom.jcr.modeshape.api.RepositoryManager;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import javax.jcr.Session;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manager for accessing JCR Repository instances. This manager uses the idiomatic way to find JCR Repository (and ModeShape
 * Repositories) instances via the {@link java.util.ServiceLoader} and {@link org.modeshape.jcr.api.RepositoriesContainer} mechanism.
 */
@ThreadSafe
@Service(RepositoryManager.class)
public class RepositoryManagerImpl implements RepositoryManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryManagerImpl.class);

    @Requires
    RepositoryFactory repositoryFactory;

    private RepositoryManagerImpl() {
    }


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
    @Override
    public Session getSession(Request request,
                              String repositoryName,
                              String workspaceName) throws RepositoryException {
        // Go through all the RepositoryFactory instances and try to create one ...
        Repository repository = getRepository(repositoryName);

        // If there's no authenticated user, try an anonymous login
        if (request == null || request.username() == null) {
            return repository.login(workspaceName);
        }

        return repository.login(new RequestCredentials(request), workspaceName);
    }

    /**
     * Returns the {@link javax.jcr.Repository} instance with the given name.
     *
     * @param repositoryName a {@code non-null} string
     * @return a {@link javax.jcr.Repository} instance, never {@code null}
     * @throws org.wisdom.jcr.modeshape.api.NoSuchRepositoryException if no repository with the given name exists.
     */
    @Override
    public Repository getRepository(String repositoryName) throws NoSuchRepositoryException {
        Repository repository = null;
        try {
            Map<String, String> map = new HashMap<>();
            map.put(org.modeshape.jcr.api.RepositoryFactory.REPOSITORY_NAME, repositoryName);
            repository = repositoryFactory.getRepository(map);
        } catch (RepositoryException e) {
            throw new NoSuchRepositoryException(WebJcrI18n.cannotInitializeRepository.text(repositoryName), e);
        }

        if (repository == null) {
            throw new NoSuchRepositoryException(WebJcrI18n.repositoryNotFound.text(repositoryName));
        }
        return repository;
    }

    /**
     * Returns a set with all the names of the available repositories.
     *
     * @return a set with the names, never {@code null}
     */
    @Override
    public Set<String> getJcrRepositoryNames() {
        try {
            return ((ModeshapeRepositoryFactory) repositoryFactory).getRepositoryNames();
        } catch (RepositoryException e) {
            LOGGER.error(WebJcrI18n.cannotLoadRepositoryNames.text(), e);
            return Collections.emptySet();
        }
    }

}
