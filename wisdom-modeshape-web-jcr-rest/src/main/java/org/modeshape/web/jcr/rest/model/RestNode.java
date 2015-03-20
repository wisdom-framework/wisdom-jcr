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
import org.modeshape.common.collection.Collections;
import org.wisdom.api.content.Json;

import java.util.*;

/**
 * A REST representation of a {@link javax.jcr.Node}
 * 
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
public final class RestNode extends RestItem {

    public static final String SELF_FIELD_NAME = "self";
    public static final String UP_FIELD_NAME = "up";
    public static final String ID_FIELD_NAME = "id";
    public static final String CHILDREN_FIELD_NAME = "children";

    private static final Set<String> RESERVED_FIELD_NAMES = Collections.unmodifiableSet(SELF_FIELD_NAME,
                                                                                        UP_FIELD_NAME,
                                                                                        ID_FIELD_NAME,
                                                                                        CHILDREN_FIELD_NAME);

    private final List<RestProperty> jcrProperties;
    private final List<RestNode> children;
    private final Map<String, String> customProperties;
    protected final String id;

    /**
     * Creates a new rest node
     * 
     * @param name a {@code non-null} string, representing the name
     * @param id the node identifier
     * @param url a {@code non-null} string, representing the url to this node
     * @param parentUrl a {@code non-null} string, representing the url to this node's parent
     */
    public RestNode( String name,
                     String id,
                     String url,
                     String parentUrl ) {
        super(name, url, parentUrl);
        this.id = id;
        jcrProperties = new ArrayList<RestProperty>();
        children = new ArrayList<RestNode>();
        customProperties = new TreeMap<String, String>();
    }

    /**
     * Adds a new child to this node.
     * 
     * @param child a {@code non-null} {@link RestNode}
     * @return this rest node.
     */
    public RestNode addChild( RestNode child ) {
        children.add(child);
        return this;
    }

    /**
     * Adds a new jcr property to this node.
     * 
     * @param property a {@code non-null} {@link RestProperty}
     * @return this rest node.
     */
    public RestNode addJcrProperty( RestProperty property ) {
        jcrProperties.add(property);
        return this;
    }

    /**
     * Adds a custom property to this node, meaning a property which is not among the standard JCR properties
     * 
     * @param name a {@code non-null} String, representing the name of the custom property
     * @param value a {@code non-null} String, representing the value of the custom property
     * @return this instance, with the custom property added
     */
    public RestNode addCustomProperty( String name,
                                       String value ) {
        customProperties.put(name, value);
        return this;
    }

    @Override
    public ObjectNode toJSON(Json json) {
        ObjectNode node = json.newObject();

        // do these first so that they appear first in the JSON ...
        node.put(SELF_FIELD_NAME, url);
        node.put(UP_FIELD_NAME, parentUrl);
        node.put(ID_FIELD_NAME, id);

        addCustomProperties(node);
        addJcrProperties(node);
        addChildren(node, json);

        return node;
    }

    private boolean isReservedField( String fieldName ) {
        return RESERVED_FIELD_NAMES.contains(fieldName);
    }

    private void addChildren( ObjectNode node, Json json ) {
        // children
        if (!children.isEmpty()) {
            ObjectNode children = json.newObject();
            for (RestNode child : this.children) {
                children.put(child.name, child.toJSON(json));
            }
            node.put(CHILDREN_FIELD_NAME, children);
        }
    }

    private void addJcrProperties( ObjectNode node ) {
        // properties
        for (RestProperty restProperty : jcrProperties) {
            if (isReservedField(restProperty.name)) continue; // skip
            if (restProperty.isMultiValue()) {
                ArrayNode arrayNode = node.putArray(restProperty.name);
                for (String value: restProperty.getValues()) {
                    arrayNode.add(value);
                }
            } else if (restProperty.getValue() != null) {
                node.put(restProperty.name, restProperty.getValue());
            }
        }
    }

    private void addCustomProperties( ObjectNode node ) {
        // custom properties
        for (String customPropertyName : customProperties.keySet()) {
            if (isReservedField(customPropertyName)) continue; // skip
            node.put(customPropertyName, customProperties.get(customPropertyName));
        }
    }
}
