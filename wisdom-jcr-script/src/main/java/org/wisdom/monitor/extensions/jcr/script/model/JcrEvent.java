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
package org.wisdom.monitor.extensions.jcr.script.model;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import java.util.Map;

/**
 * Created by KEVIN on 26/01/2016.
 */
public class JcrEvent implements Event {

    int type;
    String path;
    String userID;
    String identifier;
    Map info;
    String userData;
    long date;

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getUserID() {
        return userID;
    }

    @Override
    public String getIdentifier() throws RepositoryException {
        return identifier;
    }

    @Override
    public Map getInfo() throws RepositoryException {
        return info;
    }

    @Override
    public String getUserData() throws RepositoryException {
        return userData;
    }

    @Override
    public long getDate() throws RepositoryException {
        return date;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setInfo(Map info) {
        this.info = info;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
