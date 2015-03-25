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
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.jcrom.runtime.JcromCrudProvider;
import org.wisdom.test.parents.WisdomTest;

import javax.inject.Inject;

/**
 * TODO write documentation<br>
 *<br>
 * Created at 25/03/2015 10:56.<br>
 *
 * @author Bastien
 *
 */
public class MultiPackageIT extends WisdomTest {
    /**
     * The famous {@link org.slf4j.Logger}
     */
    private static final Logger logger = LoggerFactory.getLogger(MultiPackageIT.class);

    @Inject
    private JcromCrudProvider jcromCrudProvider;

    @Test
    public void multiPackage() {
        Assert.assertEquals(2, jcromCrudProvider.getRepository().getCrudServices().size());
    }

}
