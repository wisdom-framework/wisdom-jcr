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
package org.wisdom.jcrom.runtime;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 11/03/15
 * Time: 17:05
 */
public class JcrQueryFactory {

    public static final String findAllQuery(String nodeType) {
        return "SELECT [jcr:path] FROM [" + nodeType + "]";
    }

    public static final String findOneQuery(String nodeType, String id) {
        return "SELECT * FROM [" + nodeType + "] WHERE [jcr:name] =" + "'" + id + "'";
    }

}
