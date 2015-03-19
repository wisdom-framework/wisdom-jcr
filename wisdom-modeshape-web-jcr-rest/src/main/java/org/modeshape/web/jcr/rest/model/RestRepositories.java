/*
 * ModeShape (http://www.modeshape.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.modeshape.web.jcr.rest.model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.wisdom.api.content.Json;

import java.util.*;

/**
 * A REST representation of a collection of {@link Repository repositories}
 *
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
public final class RestRepositories implements JSONAble {

    private final List<Repository> repositories;

    /**
     * Creates an empty instance.
     */
    public RestRepositories() {
        repositories = new ArrayList<Repository>();
    }

    /**
     * Adds a repository to the list.
     *
     * @param name a {@code non-null} string, the name of the repository.
     * @param url  a {@code non-null} string, the absolute url to the repository
     * @return a {@link Repository} instance.
     */
    public Repository addRepository(String name,
                                    String url) {
        Repository repository = new Repository(name, url);
        repositories.add(repository);
        return repository;
    }

    @Override
    public ObjectNode toJSON(Json json) {
        ObjectNode result = json.newObject();
        ArrayNode repositories = result.putArray("repositories");
        for (Repository repository : this.repositories) {
            repositories.add(repository.toJSON(json));
        }
        return result;
    }

    public final class Repository implements JSONAble {
        private final String name;
        private final String url;
        private final Map<String, List<String>> metadata;
        private int activeSessionsCount;

        protected Repository(String name,
                             String url) {
            this.name = name;
            this.url = url;
            this.metadata = new TreeMap<String, List<String>>();
        }

        /**
         * Adds metadata to this repository.
         *
         * @param key   a a {@code non-null} string, the key/title of the metadata.
         * @param value a list of values for the above key.
         */
        public void addMetadata(String key,
                                List<String> value) {
            if (key != null && value != null && !value.isEmpty()) {
                metadata.put(key, value);
            }
        }

        /**
         * Sets the number of active sessions for this repository.
         *
         * @param activeSessionsCount the number of active sessions
         */
        public void setActiveSessionsCount(int activeSessionsCount) {
            this.activeSessionsCount = activeSessionsCount;
        }

        @Override
        public ObjectNode toJSON(Json json) {
            ObjectNode object = json.newObject();
            object.put("name", name);
            object.put("workspaces", url);
            object.put("activeSessionsCount", activeSessionsCount);
            ObjectNode metadata = object.putObject("metadata");
            for (String metadataKey : this.metadata.keySet()) {
                List<String> values = this.metadata.get(metadataKey);
                if (values.size() == 1) {
                    metadata.put(metadataKey, values.get(0));
                } else {
                    ArrayNode metadataArrayNode = metadata.putArray(metadataKey);
                    for (String value: values) {
                        metadataArrayNode.add(value);
                    }
                }
            }
            return object;
        }
    }
}
