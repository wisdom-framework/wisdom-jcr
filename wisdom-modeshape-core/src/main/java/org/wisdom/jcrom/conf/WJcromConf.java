package org.wisdom.jcrom.conf;

import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.configuration.Configuration;

import java.util.*;

/**
 * Created by antoine on 14/07/2014.
 */
public class WJcromConf {

    public static final String JCROM_PREFIX = "jcrom";

    private final String alias;

    private final String nameSpace;

    public WJcromConf(String alias, String nameSpace) {
        this.alias = alias;
        this.nameSpace = nameSpace;
    }

    public Dictionary<String, String> toDico() {
        Dictionary<String, String> dico = new Hashtable<String, String>(3);
        dico.put("name", alias);
        dico.put("package", nameSpace);
        return dico;
    }

    /**
     * Extract jcrom package from the configuration
     */
    public static Collection<WJcromConf> createFromApplicationConf(ApplicationConfiguration config) {
            Configuration jcrom = config.getConfiguration(JCROM_PREFIX);

        if (jcrom == null) {
            return Collections.EMPTY_SET;
        }

        Set<String> subkeys = new HashSet<String>();
        Collection<WJcromConf> subconfs = new ArrayList<WJcromConf>(subkeys.size());
        for (String key : jcrom.asMap().keySet()) {
            WJcromConf conf = new WJcromConf(key, (String) jcrom.asMap().get(key));
            subconfs.add(conf);
        }
        return subconfs;
    }

    public String getAlias() {
        return alias;
    }

    public String getNameSpace() {
        return nameSpace;
    }
}
