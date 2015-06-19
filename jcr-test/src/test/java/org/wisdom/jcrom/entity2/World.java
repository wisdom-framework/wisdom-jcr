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
package org.wisdom.jcrom.entity2;

import org.jcrom.AbstractJcrEntity;
import org.jcrom.annotations.JcrNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO write documentation<br>
 *<br>
 * Created at 25/03/2015 10:56.<br>
 *
 * @author Bastien
 *
 */
@JcrNode(nodeType = "test:world")
public class World extends AbstractJcrEntity {
    /**
     * The famous {@link Logger}
     */
    private static final Logger logger = LoggerFactory.getLogger(World.class);

}
