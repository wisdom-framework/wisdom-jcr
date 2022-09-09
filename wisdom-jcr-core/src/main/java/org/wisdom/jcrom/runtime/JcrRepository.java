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
package org.wisdom.jcrom.runtime;

import org.apache.felix.ipojo.annotations.*;
import org.jcrom.Jcrom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.http.Context;
import org.wisdom.api.model.Crud;
import org.wisdom.api.model.Repository;
import org.wisdom.jcrom.conf.JcromConfiguration;
import org.wisdom.jcrom.service.AutoCloseableSessionProvider;
import org.wisdom.jcrom.service.JcromProvider;

import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import javax.jcr.Session;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by antoine on 14/07/2014.
 */
@Component
@Instantiate
@Provides(specifications = JcrRepository.class)
public class JcrRepository implements Repository<javax.jcr.Repository> {

    private static final Logger logger = LoggerFactory.getLogger(JcrRepository.class);

    protected static ThreadLocal<Session> SESSION = new ThreadLocal<>();

    private javax.jcr.Repository repository;

    private JcromConfiguration jcromConfiguration;

    @Requires
    ApplicationConfiguration applicationConfiguration;

    @Requires
    RepositoryFactory repositoryFactory;

    @Requires
    JcromProvider jcromProvider;

    @Requires
    AutoCloseableSessionProvider autoCloseableSessionProvider;

    private Collection<Crud<?, ?>> crudServices = new HashSet<>();

    @Validate
    public void start() throws RepositoryException {
        jcromConfiguration = JcromConfiguration.fromApplicationConfiguration(applicationConfiguration);
        Thread.currentThread().setContextClassLoader(repositoryFactory.getClass().getClassLoader());
        logger.info("Loading JCR repository " + jcromConfiguration.getRepository());
        this.repository = repositoryFactory.getRepository(
                applicationConfiguration.getConfiguration("jcr")
                        .getConfiguration(jcromConfiguration.getRepository()).asMap());
        Thread.currentThread().setContextClassLoader(JcrRepository.class.getClassLoader());
    }

    @Invalidate
    public void stop() {
    }

    public javax.jcr.Repository getRepository() {
        return repository;
    }

    public Session getSession() {
        org.wisdom.api.http.Context context = Context.CONTEXT.get();
        if (context == null) {
            // we are not in the context of a request, the session should have been opened manually
            if (SESSION.get() == null) {
                throw new IllegalStateException("Please open a session using JcrRepository#login() before accessing the " +
                        "session and make sure to close it with JcrRepository#logout() after you are done with the session.");
            }
            return SESSION.get();
        } else if (SESSION.get() == null) {
            // we are in the context of a request, perform automatic login
            login();
        }
        return SESSION.get();
    }

    private Session createSession() {
        try {
            return repository.login();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Collection<Crud<?, ?>> getCrudServices() {
        return crudServices;
    }


    @Override
    public String getName() {
        return jcromConfiguration.getRepository();
    }

    @Override
    public String getType() {
        return "jcr-repository";
    }

    @Override
    public Class<javax.jcr.Repository> getRepositoryClass() {
        return javax.jcr.Repository.class;
    }

    @Override
    public javax.jcr.Repository get() {
        return repository;
    }

    public JcromConfiguration getJcromConfiguration() {
        return jcromConfiguration;
    }

    public Jcrom createJcrom() {
        return jcromProvider.getJcrom(jcromConfiguration);
    }

    public boolean addCrudService(Crud<?, ?> arg0) {
        return crudServices.add(arg0);
    }

    public boolean removeCrudService(Crud<?, ?> arg0) {
        return crudServices.remove(arg0);
    }

    public AutoCloseable login() {
        if (SESSION.get() == null) {
            logger.debug("Opening JCR session");
            Session session = autoCloseableSessionProvider.createAutoCloseableSession(createSession());
            SESSION.set(session);
        } else {
            logger.debug("Already logged in for this thread, using existing session");
        }
        return (AutoCloseableSession) SESSION.get();
    }

    public static void cleanUpRequestContextSession() {
        try {
            logger.debug("Closing jcr session");
            if (SESSION.get() != null) {
                SESSION.get().logout();
                SESSION.remove();
                logger.info("Jcr session closed");
            } else {
                logger.debug("No jcr session was opened");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
