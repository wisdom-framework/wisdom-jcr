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
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.jcrom.Jcrom;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wisdom.api.model.Crud;
import org.wisdom.jcrom.conf.JcromConfiguration;
import org.wisdom.jcrom.object.JcrCrud;

/**
 * An internal config class used to associated with a given bundle a set of Jcrom objects
 * @author ndelsaux
 *
 */
public class JcromBundleContext {
	private final Jcrom jcrom;
	private final JcromConfiguration jcromConfiguration;

    private final Map<Crud<?, ?>, ServiceRegistration> crudServiceRegistrations = new HashMap<>();

	public JcromBundleContext(Jcrom jcrom, JcromConfiguration jcromConfiguration) {
		super();
		this.jcrom = jcrom;
		this.jcromConfiguration = jcromConfiguration;
	}

	/**
	 * Create a crud service for given class and context, and registers it
	 * @param clazz
	 * @param context
	 * @param repository 
	 * @throws RepositoryException 
	 */
	public void addCrudService(Class clazz, BundleContext bundleContext, JcrRepository repository) throws RepositoryException {
        jcrom.map(clazz);
        JcrCrudService<? extends Object> jcromCrudService;
        jcromCrudService = new JcrCrudService<>(repository, jcrom, clazz);
        crudServiceRegistrations.put(jcromCrudService, registerCrud(bundleContext, jcromCrudService));
    }

	/**
	 * Register the given Crud service in OSGi registry
	 * @param context
	 * @param crud
	 * @return
	 */
    private ServiceRegistration registerCrud(BundleContext context, JcrCrudService crud) {
        Dictionary prop = jcromConfiguration.toDictionary();
        prop.put(Crud.ENTITY_CLASS_PROPERTY, crud.getEntityClass());
        prop.put(Crud.ENTITY_CLASSNAME_PROPERTY, crud.getEntityClass().getName());
        ServiceRegistration serviceRegistration = context.registerService(new String[]{Crud.class.getName(), JcrCrud.class.getName()}, crud, prop);
        return serviceRegistration;
    }
    
    public void unregister() {
    	removeCrudServices(crudServiceRegistrations.keySet());
    }

    protected void removeCrudServices(Collection<? extends Crud> cruds) {
        for (Crud crud : cruds) {
            crudServiceRegistrations.remove(crud).unregister();
        }
    }
    
    public Collection<Crud<?, ?>> getCruds() {
    	return crudServiceRegistrations.keySet();
    }
}
