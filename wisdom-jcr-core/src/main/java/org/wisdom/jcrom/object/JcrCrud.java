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
package org.wisdom.jcrom.object;

import org.wisdom.api.model.Crud;

import java.io.Serializable;
import java.util.List;

/**
 * Specific Crud interface for JCR allowing to execute JCR queries.
 */
public interface JcrCrud<T, I extends Serializable> extends Crud<T, I> {

    /**
     * Retieve the first entity returned by the given query in the specified language.
     *
     * @param statement the query to execute
     * @param language  the language of the query.
     * @return null if the query does not return any result
     */
    public T findOneByQuery(String statement, String language);

    /**
     * Execute a JCR query in the specified language.
     *
     * @param statement the query to execute
     * @param language  the language of the query
     * @return the list of found entites. An empty list if no entity was found.
     */
    public List<T> findByQuery(String statement, String language);

    /**
     * Retrieve an entity from its JCR absolute path.
     *
     * @param absolutePath
     * @return
     */
    public T findByPath(String absolutePath);

    /**
     * Load the node at the path of the given entity as an object of the given class.
     *
     * @param entity
     * @param clazz
     * @param <A>
     * @return the node at the path of the given entity loaded as an object of the given class
     */
    public <A> A getAs(T entity, Class<A> clazz);

    /**
     * Load the node at the given path as an object of the given class.
     *
     * @param path
     * @param clazz
     * @param <A>
     * @return the node at the given path loaded as an object of the given class
     */
    public <A> A getAs(String path, Class<A> clazz);

}
