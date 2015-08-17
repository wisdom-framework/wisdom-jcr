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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import javax.jcr.Session;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.model.Crud;
import org.wisdom.api.model.Repository;
import org.wisdom.jcrom.conf.JcromConfiguration;

/**
 * Created by antoine on 14/07/2014.
 */
@Component
@Instantiate
@Provides(specifications = JcrRepository.class)
public class JcrRepository implements Repository<javax.jcr.Repository> {

    private Logger logger = LoggerFactory.getLogger(JcromCrudProvider.class);

    private javax.jcr.Repository repository;

    private JcromConfiguration jcromConfiguration;

    private Session session;
    
    private Set<JcromBundleContext> bundleContexts = new HashSet<>();

    @Requires
    ApplicationConfiguration applicationConfiguration;

    @Requires
    RepositoryFactory repositoryFactory;

    @Validate
    public void start() throws RepositoryException {
        jcromConfiguration = JcromConfiguration.fromApplicationConfiguration(applicationConfiguration);
        Thread.currentThread().setContextClassLoader(repositoryFactory.getClass().getClassLoader());
        logger.info("Loading JCR repository " + jcromConfiguration.getRepository());
        this.repository = repositoryFactory.getRepository(
                applicationConfiguration.getConfiguration("jcr")
                        .getConfiguration(jcromConfiguration.getRepository()).asMap());
        Thread.currentThread().setContextClassLoader(JcrRepository.class.getClassLoader());
        this.session = repository.login();
    }

    @Invalidate
    public void stop() {
    }

    public javax.jcr.Repository getRepository() {
        return repository;
    }

    public Session getSession() {
        return session;
    }

    @Override
    public Collection<Crud<?, ?>> getCrudServices() {
    	Collection<Crud<?,?>> returned = new HashSet<>();
    	for(JcromBundleContext context : bundleContexts) {
    		returned.addAll(context.getCruds());
    	}
    	return returned;
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

	public boolean addBundleContext(JcromBundleContext context) {
		return bundleContexts.add(context);
	}

	public boolean removeBundleContext(JcromBundleContext context) {
		return bundleContexts.remove(context);
	}
}
