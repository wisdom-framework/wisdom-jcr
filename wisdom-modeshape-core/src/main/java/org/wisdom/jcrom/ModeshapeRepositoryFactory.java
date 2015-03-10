package org.wisdom.jcrom;

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
@Component(name = "com:dooapp:modeshape:factory")
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
            config = getModeshapeConfig("test");
        } else if (applicationConfiguration.isDev()) {
            logger.info("~~~~~~~DEV MODE ~~~~~~");
            config = getModeshapeConfig("dev");
        } else if (applicationConfiguration.isProd()) {
            logger.info("~~~~~~~PROD MODE ~~~~~~");
            config = getModeshapeConfig("prod");
        }
        repository = engine.deploy(config);
    }


    private RepositoryConfiguration getModeshapeConfig(String env) throws ParsingException, FileNotFoundException {
        return RepositoryConfiguration.read(new File(new File(applicationConfiguration.getBaseDir(), "conf"), applicationConfiguration.getConfiguration(MODESHAPE_CFG).get(env)));
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
