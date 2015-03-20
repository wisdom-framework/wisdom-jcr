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
package org.wisdom.jcr.modeshape;

import org.wisdom.api.http.Request;

import javax.jcr.Credentials;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 20/03/15
 * Time: 16:47
 */
public class RequestCredentials implements Credentials {

    private final Request request;

    public RequestCredentials(Request request) {
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }

}
