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
package org.wisdom.monitor.extensions.jcr;

import javax.jcr.*;
import javax.jcr.nodetype.NodeType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Vianney on 27/11/2015.
 */
public class JcrExplorerModel {

    private Node node;
    private List<Node> subnodes = new ArrayList<>();
    private Map<String, String> properties = new HashMap<>();
    private List<String> mixins = new ArrayList<>();

    public static JcrExplorerModel build(Node node) throws Exception {
        return new JcrExplorerModel(node);
    }

    private JcrExplorerModel(Node node) throws RepositoryException {
        this.node = node;
        buildSubNodes();
        buildProperties();
        buildMixins();
    }

    public Node getNode() {
        return node;
    }

    public List<Node> getSubnodes() {
        return subnodes;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public List<String> getMixins() {
        return mixins;
    }

    private void buildSubNodes() throws RepositoryException {
        NodeIterator nodeIterator = node.getNodes();
        while (nodeIterator.hasNext()) {
            Node nextNode = nodeIterator.nextNode();
            subnodes.add(nextNode);
        }
    }

    private void buildProperties() throws RepositoryException {
        PropertyIterator propertyIterator = node.getProperties();
        while (propertyIterator.hasNext()) {
            Property property = propertyIterator.nextProperty();
            if (!node.getPrimaryNodeType().isNodeType("nt:resource") || !property.getName().equals("jcr:data")) {
                properties.put(property.getName(), getPropertyValue(property));
            }
        }
    }

    private void buildMixins() throws RepositoryException {
        for (NodeType nodeType : node.getMixinNodeTypes()) {
            mixins.add(nodeType.getName());
        }
    }

    private String getPropertyValue(Property property) {
        try {
            String result = "";
            if (!property.isMultiple()) {
                result += showProperly(property.getValue());
            } else {
                for (Value value : property.getValues()) {
                    result += showProperly(value) + " - ";
                }
                result = result.substring(0, result.lastIndexOf(" - "));
            }

            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private String showProperly(Value value) throws RepositoryException, IOException, ClassNotFoundException {
        if (value.getType() == PropertyType.BINARY) {
            return new ObjectInputStream(value.getBinary().getStream()).readObject().toString();
        }
        return value.getString();
    }
}
