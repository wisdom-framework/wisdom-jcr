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
import org.wisdom.jcrom.conf.WJcromConf;

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
public class JcromCrudProvider implements BundleTrackerCustomizer<Collection<JcrRepository>> {

    private Logger logger = LoggerFactory.getLogger(JcromCrudProvider.class);

    public static final String COMPONENT_NAME = "wisdom:jcrom:crudservice:factory";
    public static final String INSTANCE_NAME = "wisdom:jcrom:crudservice:provider";
    public static final String JCR_CONFIG = "jcr";
    public static final String REPOSITORY_CONFIG = "repository";
    public static final String DEFAULT_REPOSITORY_INSTANCE = "org.wisdom.jcrom.runtime.JcrRepository";

    @Requires
    private ApplicationConfiguration applicationConfiguration;

    private Collection<WJcromConf> confs;

    private final BundleContext context;

    private BundleTracker<Collection<JcrRepository>> bundleTracker;

    private Collection<JcrRepository> repos = new HashSet<>();

    @Requires
    private RepositoryFactory repositoryFactory;

    public JcromCrudProvider(BundleContext bundleContext) {
        context = bundleContext;
    }

    @Validate
    private void start() {
        confs = WJcromConf.createFromApplicationConf(applicationConfiguration);
        if (confs.isEmpty()) {
            logger.info("Confs is empty, stopping");
            return;
        }
        bundleTracker = new BundleTracker<>(context, Bundle.ACTIVE, this);
        bundleTracker.open();
    }

    @Invalidate
    private void stop() {
        if (confs.isEmpty()) {
            return;
        }
        if (bundleTracker != null) {
            bundleTracker.close();
        }
    }


    @Override
    public Collection<JcrRepository> addingBundle(Bundle bundle, BundleEvent bundleEvent) {
        for (WJcromConf conf : confs) {
            Enumeration<URL> enums = bundle.findEntries(packageNameToPath(conf.getNameSpace()), "*.class", true);

            if (enums == null || !enums.hasMoreElements()) {
                break; //next configuration
            }

            //Create a pull for this configuration
            JcrRepository repo = null;
            try {
                repo = new JcrRepository(conf, repositoryFactory);
            } catch (Exception e) {
                logger.error("Cannot access to jcr repository " + conf.getAlias(), e);
            }

            //Load the entities from the bundle
            do {
                URL entry = enums.nextElement();
                try {
                    logger.info("Enable mapping in jcrom for " + entry);
                    String className = urlToClassName(entry);
                    Class clazz = bundle.loadClass(className);
                    repo.addCrudService(clazz);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
            } while (enums.hasMoreElements());

            logger.debug("Crud service has been added for " + conf.getNameSpace() + " in " + conf.getAlias() + "  jcrom.");

            //register all crud service available in this repo
            repo.registerAllCrud(context);

            //add this configuration repo
            repos.add(repo);
        }
        return repos;
    }

    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent bundleEvent, Collection<JcrRepository> jcrRepositories) {
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent bundleEvent, Collection<JcrRepository> jcrRepositories) {
        for (JcrRepository repo : jcrRepositories) {
            repo.destroy();
            repo.getSession().logout();
        }
        jcrRepositories.clear();
    }

    private static String urlToClassName(URL url) {
        String path = url.getPath();
        return path.replace("/", ".").substring(1, path.lastIndexOf("."));

    }

    private static String packageNameToPath(String packageName) {
        return "/" + packageName.replace(".", "/");
    }

}
