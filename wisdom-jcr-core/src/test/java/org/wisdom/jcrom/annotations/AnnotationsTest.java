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
package org.wisdom.jcrom.annotations;

import org.jcrom.util.ReflectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO write documentation<br>
 *<br>
 * Created at 19/03/2015 10:01.<br>
 *
 * @author Bastien
 *
 */
public class AnnotationsTest {
    /**
     * The famous {@link org.slf4j.Logger}
     */
    private static final Logger logger = LoggerFactory.getLogger(AnnotationsTest.class);

    @Test
    public void findParentAnnotation() {
        FooProject fooProject = new FooProject();

        String nodetype = ReflectionUtils.getJcrNodeAnnotation(FooProject.class).nodeType();
        Assert.assertTrue(nodetype.equals("wisdom:project"));
    }
}
