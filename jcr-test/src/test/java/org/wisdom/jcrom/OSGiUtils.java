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
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.wisdom.api.model.Crud;

import java.util.List;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 19/06/15
 * Time: 17:31
 */
public class OSGiUtils {

    private final OSGiHelper osgi;

    public OSGiUtils(OSGiHelper osgi) {
        this.osgi = osgi;
    }

    public <T> Crud<T, String> getCrud(Class<T> clazz) {
        osgi.waitForService(Crud.class, null, 5000);
        final List<Crud> cruds = osgi.getServiceObjects(Crud.class);
        Crud<T, String> helloCrud = null;
        for (Crud crud : cruds) {
            if (crud.getEntityClass().equals(clazz)) {
                helloCrud = crud;
                break;
            }
        }
        Assert.assertNotNull(helloCrud);
        return helloCrud;
    }

}
