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
package org.wisdom.jcrom;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.wisdom.api.model.Crud;
import org.wisdom.jcrom.entity1.Hello;
import org.wisdom.jcrom.object.JcrCrud;
import org.wisdom.test.parents.WisdomTest;

import javax.inject.Inject;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 02/04/15
 * Time: 12:08
 */
public class BasicCrudIT extends WisdomTest {

    @Inject
    BundleContext context;

    OSGiHelper osgi;

    OSGiUtils osGiUtils;

    @Before
    public void setUp() throws InvalidSyntaxException, InterruptedException {
        osgi = new OSGiHelper(context);
        osGiUtils = new OSGiUtils(osgi);
    }

    @Test
    public void testSave() {
        Crud<Hello, String> helloCrud = osGiUtils.getCrud(Hello.class);
        Hello hello = new Hello();
        hello.setPath("/messages");
        hello.setName("Hello");
        helloCrud.save(hello);
        Hello hello1 = helloCrud.findOne("Hello");
        Assert.assertNotNull(hello1);
        Assert.assertEquals("/messages/Hello", hello1.getPath());
    }

    @Test
    public void testFindByPath() {
        JcrCrud<Hello, String> helloCrud = (JcrCrud<Hello, String>) osGiUtils.getCrud(Hello.class);
        Assert.assertNotNull(helloCrud);
        Hello hello = new Hello();
        hello.setPath("/messages");
        hello.setName("Hello2");
        helloCrud.save(hello);
        Hello hello1 = helloCrud.findByPath("/messages/Hello2");
        Assert.assertNotNull(hello1);
    }

    /**
     * https://github.com/wisdom-framework/wisdom-jcr/issues/34
     */
    @Test
    public void issue34() {
        JcrCrud<Hello, String> helloCrud = (JcrCrud<Hello, String>) osGiUtils.getCrud(Hello.class);
        Assert.assertNotNull(helloCrud);
        Hello hello = new Hello();
        hello.setPath("/entities/todo");
        hello.setName("a");
        helloCrud.save(hello);

        Assert.assertNotNull(helloCrud.findByPath("/entities/todo/a"));
        Assert.assertNotNull(helloCrud.findOne("a"));
        Assert.assertNull(helloCrud.findByPath("/a"));
        Assert.assertNull(helloCrud.findByPath("/entities/a"));

        Hello hello2 = new Hello();
        hello2.setPath("/entities");
        hello2.setName("a");
        helloCrud.save(hello2);

        Assert.assertNotNull(helloCrud.findByPath("/entities/todo/a"));
        Assert.assertNotNull(helloCrud.findOne("a"));
        Assert.assertNotNull(helloCrud.findByPath("/entities/a"));
        Assert.assertNull(helloCrud.findByPath("/a"));
    }

}
