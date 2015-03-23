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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.jcrom.conf.JcromConfiguration;

import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;

/**
 * Created by antoine on 14/07/2014.
 */
@Component(name = JcromCrudProvider.COMPONENT_NAME)
@Instantiate(name = JcromCrudProvider.INSTANCE_NAME)
public class JcromCrudProvider implements BundleTrackerCustomizer<JcrRepository> {

    private Logger logger = LoggerFactory.getLogger(JcromCrudProvider.class);

    public static final String COMPONENT_NAME = "wisdom:jcrom:crudservice:factory";
    public static final String INSTANCE_NAME = "wisdom:jcrom:crudservice:provider";

    @Requires
    private ApplicationConfiguration applicationConfiguration;

    private JcromConfiguration jcromConfiguration;

    private final BundleContext context;

    private BundleTracker<JcrRepository> bundleTracker;

    private Collection<JcrRepository> repos = new HashSet<>();

    private JcrRepository repository;

    @Requires
    private RepositoryFactory repositoryFactory;

    public JcromCrudProvider(BundleContext bundleContext) {
        context = bundleContext;
    }

    @Validate
    private void start() {
        jcromConfiguration = JcromConfiguration.fromApplicationConfiguration(applicationConfiguration);
        if (jcromConfiguration == null) {
            logger.info("Confs is empty, stopping");
            return;
        }
        bundleTracker = new BundleTracker<>(context, Bundle.ACTIVE, this);
        bundleTracker.open();
    }

    @Invalidate
    private void stop() {
        if (jcromConfiguration.getPackages().isEmpty()) {
            return;
        }
        if (bundleTracker != null) {
            bundleTracker.close();
        }
    }

    @Override
    public JcrRepository addingBundle(Bundle bundle, BundleEvent bundleEvent) {
        if (jcromConfiguration != null) {
            for (String p : jcromConfiguration.getPackages()) {
                Enumeration<URL> enums = bundle.findEntries(packageNameToPath(p), "*.class", true);

                if (enums == null) {
                    break;
                }

                if (repository == null) {
                    try {
                        repository = new JcrRepository(jcromConfiguration, repositoryFactory, applicationConfiguration);
                    } catch (RepositoryException e) {
                        logger.error("Can not access repository: " + jcromConfiguration.getRepository(), e);
                    }
                }

                //Load the entities from the bundle
                do {
                    URL entry = enums.nextElement();
                    try {
                        logger.info("Enable mapping in jcrom for " + entry);
                        String className = urlToClassName(entry);
                        Class clazz = bundle.loadClass(className);
                        repository.addCrudService(clazz, context);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (RepositoryException e) {
                        e.printStackTrace();
                    }
                } while (enums.hasMoreElements());
                logger.debug("Crud service has been added for " + p);
            }
        }
        return repository;
    }

    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent bundleEvent, JcrRepository jcrRepository) {
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent bundleEvent, JcrRepository jcrRepository) {
        jcrRepository.destroy();
        jcrRepository.getSession().logout();
    }

    private static String urlToClassName(URL url) {
        String path = url.getPath();
        return path.replace("/", ".").substring(1, path.lastIndexOf("."));

    }

    private static String packageNameToPath(String packageName) {
        return "/" + packageName.replace(".", "/");
    }

}
