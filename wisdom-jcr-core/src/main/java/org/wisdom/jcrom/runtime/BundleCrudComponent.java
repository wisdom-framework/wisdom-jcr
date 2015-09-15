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

import java.net.URL;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.jcrom.Jcrom;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.model.Crud;
import org.wisdom.jcrom.object.JcrCrud;

public class BundleCrudComponent {
    private Logger logger = LoggerFactory.getLogger(BundleCrudComponent.class);

	private JcrRepository repository;

    private Map<JcrCrud, ServiceRegistration> crudServiceRegistrations = new HashMap<>();

	private Jcrom jcrom;


	public BundleCrudComponent(JcrRepository repository) {
		this.repository = repository;
	}

	public void addEntity(Bundle bundle, BundleContext context, URL entry) {
        try {
            logger.info("Enable mapping in jcrom for " + entry);
            String className = JcromCrudProvider.urlToClassName(entry);
            Class clazz = bundle.loadClass(className);
            createCrudService(repository, clazz, context);
        } catch (ClassNotFoundException e) {
            logger.debug(e.getMessage());
        } catch (RepositoryException e) {
            logger.debug(e.getMessage());
        } catch (NullPointerException e) {
            logger.debug(e.getMessage());
        }
	}

    protected void createCrudService(JcrRepository repository, Class entity, BundleContext bundleContext) throws RepositoryException {
        getJcrom().map(entity);
        JcrCrudService<? extends Object> jcromCrudService = new JcrCrudService(repository, getJcrom(), entity);
        crudServiceRegistrations.put(jcromCrudService, registerCrud(repository, bundleContext, jcromCrudService));
        // Finally, register crud service in repository
        repository.addCrudService(jcromCrudService);
    }
    
    private Jcrom createJcrom() {
    	return repository.createJcrom();
    }

    private Jcrom getJcrom() {
    	if(jcrom==null) {
    		jcrom = createJcrom();
    	}
    	return jcrom;
	}

	private ServiceRegistration registerCrud(JcrRepository repository, BundleContext context, JcrCrudService crud) {
        Dictionary prop = repository.getJcromConfiguration().toDictionary();
        prop.put(Crud.ENTITY_CLASS_PROPERTY, crud.getEntityClass());
        prop.put(Crud.ENTITY_CLASSNAME_PROPERTY, crud.getEntityClass().getName());
        ServiceRegistration serviceRegistration = context.registerService(new String[]{Crud.class.getName(), JcrCrud.class.getName()}, crud, prop);
        return serviceRegistration;
    }

	public void remove() {
        for (Crud<?, ?> crud : crudServiceRegistrations.keySet()) {
            crudServiceRegistrations.get(crud).unregister();
            repository.removeCrudService(crud);
        }
        crudServiceRegistrations.clear();
	}

}
