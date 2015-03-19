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
package org.wisdom.jcrom.conf;

import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.configuration.Configuration;

import java.util.*;

/**
 * Created by antoine on 14/07/2014.
 */
public class JcromConfiguration {

    public static final String JCROM_PREFIX = "jcrom";

    private final String alias;

    private final String nameSpace;

    public JcromConfiguration(String alias, String nameSpace) {
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
    public static Collection<JcromConfiguration> createFromApplicationConf(ApplicationConfiguration config) {
        Configuration jcrom = config.getConfiguration(JCROM_PREFIX).getConfiguration("packages");

        if (jcrom == null) {
            return Collections.EMPTY_SET;
        }

        Set<String> subkeys = new HashSet<String>();
        Collection<JcromConfiguration> subconfs = new ArrayList<JcromConfiguration>(subkeys.size());
        for (String key : jcrom.asMap().keySet()) {
            JcromConfiguration conf = new JcromConfiguration(key, (String) jcrom.asMap().get(key));
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
