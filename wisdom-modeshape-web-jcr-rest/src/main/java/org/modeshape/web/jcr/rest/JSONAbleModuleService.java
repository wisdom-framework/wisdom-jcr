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
package org.modeshape.web.jcr.rest;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.felix.ipojo.annotations.Requires;
import org.modeshape.web.jcr.rest.model.*;
import org.wisdom.api.annotations.Service;
import org.wisdom.api.content.Json;

import java.io.IOException;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 18/03/15
 * Time: 10:43
 */
@Service(Module.class)
public class JSONAbleModuleService extends SimpleModule {

    @Requires
    Json json;

    public JSONAbleModuleService() {
        super("JSONAble Module");
        addSerializer(JSONAble.class, new JsonSerializer<JSONAble>() {
            @Override
            public void serialize(JSONAble jsonAble, JsonGenerator jsonGenerator,
                                  SerializerProvider serializerProvider)
                    throws IOException {
                jsonAble.toJSON(json).serialize(jsonGenerator, serializerProvider);
            }
        });
        addAbstractTypeMapping(JSONAble.class, RestException.class);
        addAbstractTypeMapping(JSONAble.class, RestNode.class);
        addAbstractTypeMapping(JSONAble.class, RestNodeType.class);
        addAbstractTypeMapping(JSONAble.class, RestProperty.class);
        addAbstractTypeMapping(JSONAble.class, RestProperty.class);
        addAbstractTypeMapping(JSONAble.class, RestPropertyType.class);
        addAbstractTypeMapping(JSONAble.class, RestQueryPlanResult.class);
        addAbstractTypeMapping(JSONAble.class, RestQueryResult.class);
        addAbstractTypeMapping(JSONAble.class, RestRepositories.class);
        addAbstractTypeMapping(JSONAble.class, RestWorkspaces.class);
    }

}
