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

    public T findOneByQuery(String query);

    public List<T> findByQuery(String query);

}
