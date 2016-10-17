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
package org.wisdom.jcr.modeshape.service;

import org.apache.felix.ipojo.annotations.*;
import org.modeshape.jcr.ModeShapeEngine;
import org.modeshape.jcr.RepositoryConfiguration;
import org.modeshape.schematic.document.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.ApplicationConfiguration;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Set;

/**
 * Created by antoine on 22/07/2014.
 */
@Component(name = "org:wisdom:jcr:modeshape:factory")
@Provides(specifications = RepositoryFactory.class)
@Instantiate
public class ModeshapeRepositoryFactory implements RepositoryFactory {

    private Logger logger = LoggerFactory.getLogger(ModeshapeRepositoryFactory.class);

    @Requires
    ApplicationConfiguration applicationConfiguration;

    private static String JCR = "jcr";

    /**
     * The container which hold the engine and which is responsible for initializing & returning the repository.
     */
    private final ModeShapeEngine engine = new ModeShapeEngine();

    @Validate
    private void start() throws RepositoryException, InterruptedException {
        engine.start();
    }


    private Map getRepositoryConfiguration(String repositoryName) {
        return applicationConfiguration.getConfiguration(JCR).getConfiguration(repositoryName).asMap();
    }

    @Invalidate
    private void stop() {
        engine.shutdown();
    }

    @Override
    public Repository getRepository(Map parameters) throws RepositoryException {
        String name = (String) parameters.get(org.modeshape.jcr.api.RepositoryFactory.REPOSITORY_NAME);
        logger.info("Accessing repository " + name);
        Map defaultParameters = getRepositoryConfiguration(name);
        defaultParameters.putAll(parameters);
        if (!engine.getRepositoryNames().contains(name)) {
            try {
                File configurationFile = new File(new File(applicationConfiguration.getBaseDir(), "conf"), (String) defaultParameters.get(org.modeshape.jcr.api.RepositoryFactory.URL));
                engine.deploy(RepositoryConfiguration.read(configurationFile));
            } catch (ParsingException e) {
                logger.error(e.getMessage(), e);
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return engine.getRepository(name);
    }

    public Set<String> getRepositoryNames() throws RepositoryException {
        return engine.getRepositoryNames();
    }

}
