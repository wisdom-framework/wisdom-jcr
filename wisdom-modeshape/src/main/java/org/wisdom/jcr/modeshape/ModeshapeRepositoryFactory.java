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
package org.wisdom.jcr.modeshape;

import org.apache.felix.ipojo.annotations.*;
import org.infinispan.schematic.document.ParsingException;
import org.modeshape.jcr.ModeShapeEngine;
import org.modeshape.jcr.RepositoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.ApplicationConfiguration;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

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

    private Repository repository;

    org.modeshape.jcr.ModeShapeEngine engine;

    private static String MODESHAPE_CFG = "modeshape";

    @Validate
    private void start() throws ParsingException, FileNotFoundException, RepositoryException, InterruptedException {
        engine = new ModeShapeEngine();
        engine.start();
        RepositoryConfiguration config = null;

        if (applicationConfiguration.isTest()) {
            logger.info("~~~~~~~TEST MODE ~~~~~~");
            config = getModeshapeConfiguration("test");
        } else if (applicationConfiguration.isDev()) {
            logger.info("~~~~~~~DEV MODE ~~~~~~");
            config = getModeshapeConfiguration("dev");
        } else if (applicationConfiguration.isProd()) {
            logger.info("~~~~~~~PROD MODE ~~~~~~");
            config = getModeshapeConfiguration("prod");
        }
        repository = engine.deploy(config);
    }


    private RepositoryConfiguration getModeshapeConfiguration(String env) throws ParsingException, FileNotFoundException {
        return RepositoryConfiguration.read(new File(new File(applicationConfiguration.getBaseDir(), "conf"),
                applicationConfiguration.getConfiguration(MODESHAPE_CFG).get(env)));
    }

    @Invalidate
    private void stop() {
        engine.shutdown();
    }

    @Override
    public Repository getRepository(Map map) throws RepositoryException {
        return repository;
    }


}
