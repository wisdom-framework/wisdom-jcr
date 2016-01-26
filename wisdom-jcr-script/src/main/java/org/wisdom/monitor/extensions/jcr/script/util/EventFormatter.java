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
package org.wisdom.monitor.extensions.jcr.script.util;

import javax.jcr.observation.Event;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by KEVIN on 26/01/2016.
 */
public class EventFormatter {

    public String format (int eventCode){
        if (eventCode== Event.NODE_ADDED){
            return "Node added";
        }
        if (eventCode==Event.NODE_MOVED){
            return "Node moved";
        }
        if (eventCode==Event.NODE_REMOVED){
            return "Node removed";
        }
        if (eventCode==Event.PROPERTY_ADDED){
            return "Property added";
        }
        if (eventCode==Event.PROPERTY_CHANGED){
            return "Property changed";
        }
        if (eventCode==Event.PROPERTY_REMOVED){
            return "Property removed";
        }
        if (eventCode==Event.PERSIST){
            return "Persist";
        }
        return "Unknown event type ["+eventCode+"]";
    }

    public String dateFormat(long timemillis){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return dateFormat.format(new Date(timemillis));
    }

}
