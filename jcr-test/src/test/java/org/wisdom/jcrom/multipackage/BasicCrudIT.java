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
package org.wisdom.jcrom.multipackage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.wisdom.api.model.Crud;
import org.wisdom.jcrom.multipackage.entity1.Hello;
import org.wisdom.test.parents.WisdomTest;

import javax.inject.Inject;
import java.util.List;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 02/04/15
 * Time: 12:08
 */
public class BasicCrudIT extends WisdomTest {

    @Inject
    BundleContext context;

    OSGiHelper osgi;

    @Before
    public void setUp() throws InvalidSyntaxException, InterruptedException {
        osgi = new OSGiHelper(context);
    }

    @Test
    public void testSave() {
        osgi.waitForService(Crud.class, null, 5000);
        final List<Crud> cruds = osgi.getServiceObjects(Crud.class);
        Crud<Hello, String> helloCrud = null;
        for (Crud crud: cruds) {
           if (crud.getEntityClass().equals(Hello.class)) {
               helloCrud = crud;
               break;
           }
        }
        Assert.assertNotNull(helloCrud);
        Hello hello = new Hello();
        hello.setPath("/messages");
        hello.setName("Hello");
        helloCrud.save(hello);
        Hello hello1 = helloCrud.findOne("Hello");
        Assert.assertNotNull(hello1);
        Assert.assertEquals("/messages/Hello", hello1.getPath());
    }

}
