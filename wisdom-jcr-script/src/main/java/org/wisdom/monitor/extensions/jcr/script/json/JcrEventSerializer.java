/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2016 Wisdom Framework
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
package org.wisdom.monitor.extensions.jcr.script.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import java.io.IOException;

/**
 * Created by KEVIN on 26/01/2016.
 */
public class JcrEventSerializer extends JsonSerializer<Event> {
    @Override
    public void serialize(Event value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        try {
            gen.writeStringField("path", value.getPath());
            gen.writeNumberField("type", value.getType());
            gen.writeNumberField("date", value.getDate());
            gen.writeStringField("userID", value.getUserID());
        } catch (RepositoryException e) {
            throw new IOException(e);
        }
        gen.writeEndObject();
    }
}
