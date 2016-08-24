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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by antoine on 14/07/2014.
 */
public class JcromConfiguration {

    public static final String JCROM_PREFIX = "jcrom";

    public static final String PACKAGES_PREFIX = "packages";

    public static final String DYNAMIC_INSTANTIATION = "dynamic.instantiation";

    public static final String CLEAN_NAMES = "clean.names";

    public static final String REPOSITORY = "repository";

    public static final String CREATE_PATH = "create.path";

    private Configuration configuration;

    private String env;

    public JcromConfiguration(Configuration configuration, String env) {
        this.configuration = configuration;
        this.env = env;
    }

    public static JcromConfiguration fromApplicationConfiguration(ApplicationConfiguration applicationConfiguration) {
        if (applicationConfiguration.has(JCROM_PREFIX)) {
            String env = null;
            if (applicationConfiguration.isDev()) {
                env = "dev";
            } else if (applicationConfiguration.isTest()) {
                env = "test";
            } else if (applicationConfiguration.isProd()) {
                env = "prod";
            }
            return new JcromConfiguration(applicationConfiguration.getConfiguration(JCROM_PREFIX), env);
        }
        return null;
    }

    public boolean isDynamicInstantiation() {
        return configuration.getBooleanWithDefault(DYNAMIC_INSTANTIATION, true);
    }

    public boolean isCreatePath() {
        return configuration.getBooleanWithDefault(CREATE_PATH, true);
    }

    public boolean isCleanNames() {
        return configuration.getBooleanWithDefault(CLEAN_NAMES, true);
    }

    public String getRepository() {
        return configuration.get(env + "." + REPOSITORY);
    }

    public List<String> getPackages() {
        return configuration.getList(PACKAGES_PREFIX);
    }

    public Dictionary<String, String> toDictionary() {
        Dictionary<String, String> dico = new Hashtable<>(3);
        dico.put(DYNAMIC_INSTANTIATION, Boolean.toString(isDynamicInstantiation()));
        dico.put(CLEAN_NAMES, Boolean.toString(isCleanNames()));
        dico.put(PACKAGES_PREFIX, getPackages().stream().collect(Collectors.joining(",")));
        return dico;
    }

}
