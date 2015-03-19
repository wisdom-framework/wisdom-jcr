package org.modeshape.web.jcr.rest.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.wisdom.api.content.Json;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 18/03/15
 * Time: 10:31
 */
public interface JSONAble {

    public ObjectNode toJSON(Json json);

}
