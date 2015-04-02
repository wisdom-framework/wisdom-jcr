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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.model.Crud;
import org.wisdom.api.model.Repository;
import org.wisdom.jcrom.conf.JcromConfiguration;
import org.wisdom.jcrom.object.JcrCrud;

import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import javax.jcr.Session;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

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

    private Jcrom jcrom;

    private Session session;

    private Map<JcrCrud, ServiceRegistration> crudServiceRegistrations = new HashMap<>();

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
        this.jcrom = new Jcrom(jcromConfiguration.isCleanNames(), jcromConfiguration.isDynamicInstantiation());
        Thread.currentThread().setContextClassLoader(JcrRepository.class.getClassLoader());
        this.session = repository.login();
    }

    @Invalidate
    public void stop() {
    }

    protected Crud addCrudService(Class entity, BundleContext bundleContext) throws RepositoryException {
        jcrom.map(entity);
        JcrCrudService<? extends Object> jcromCrudService;
        jcromCrudService = new JcrCrudService(this, entity);
        crudServiceRegistrations.put(jcromCrudService, registerCrud(bundleContext, jcromCrudService));
        return jcromCrudService;
    }

    private ServiceRegistration registerCrud(BundleContext context, JcrCrudService crud) {
        Dictionary prop = jcromConfiguration.toDictionary();
        prop.put(Crud.ENTITY_CLASS_PROPERTY, crud.getEntityClass());
        prop.put(Crud.ENTITY_CLASSNAME_PROPERTY, crud.getEntityClass().getName());
        ServiceRegistration serviceRegistration = context.registerService(new String[]{Crud.class.getName(), JcrCrud.class.getName()}, crud, prop);
        return serviceRegistration;
    }

    protected void removeCrudServices(Collection<Crud> cruds) {
        for (Crud crud : cruds) {
            crudServiceRegistrations.get(crud).unregister();
            crudServiceRegistrations.remove(crud);
        }
    }

    public javax.jcr.Repository getRepository() {
        return repository;
    }

    public Jcrom getJcrom() {
        return jcrom;
    }

    public Session getSession() {
        return session;
    }

    @Override
    public Collection<Crud<?, ?>> getCrudServices() {
        return (Collection) crudServiceRegistrations.keySet();
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
}
