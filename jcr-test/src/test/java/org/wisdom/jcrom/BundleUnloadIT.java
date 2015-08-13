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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.test.parents.WisdomTest;

import javax.inject.Inject;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

/**
 * Test used to check if all references to mapped classes are correctly removed when a bundle is unloaded.
 * <p/>
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 19/06/15
 * Time: 15:29
 */
public class BundleUnloadIT extends WisdomTest {

    @Inject
    private BundleContext context;

    @Inject
    private ApplicationConfiguration applicationConfiguration;

    private OSGiHelper osgi;

    private OSGiUtils osGiUtils;

    private List<Bundle> bundles;

    @Before
    public void setUp() {
        osgi = new OSGiHelper(context);
        osGiUtils = new OSGiUtils(osgi);
        bundles = new ArrayList<>();
    }

    @After
    public void tearDown() {
        for (Bundle bundle : bundles) {
            try {
                bundle.uninstall();
            } catch (BundleException e) {
                // Ignore it
            }
        }
        try {
            osgi.dispose();
        } catch (Exception e) {
            // Ignore it
        }
    }

    //@Test
    public void testUnload() throws IOException, ClassNotFoundException, BundleException {
        // create an entity to map
        String source = "package org.wisdom.jcrom.entity1;\n" +
                "\n" +
                "import org.jcrom.AbstractJcrEntity;\n" +
                "import org.jcrom.annotations.JcrNode;\n" +
                "import org.jcrom.annotations.JcrName;\n" +
                "import org.jcrom.annotations.JcrPath;\n" +
                "\n" +
                "@JcrNode(nodeType = \"test:testentity\")\n" +
                "public class BundleUnloadITEntity {\n" +
                "@JcrName\n" +
                "private String name;\n" +

                "@JcrPath\n" +
                "private String path;\n" +
                "}\n";

        File sourceFile = new File("target", "BundleUnloadITEntity.java");
        new FileWriter(sourceFile).append(source).close();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, sourceFile.getPath());

        // create a bundle containing this entity
        Bundle bundle = osgi.installAndStart("local:/test",
                bundle()
                        .add("org/wisdom/jcrom/entity1/BundleUnloadITEntity.class", new FileInputStream("target/BundleUnloadITEntity.class"))
                        .set(Constants.IMPORT_PACKAGE, "org.jcrom.annotations")
                        .set(Constants.EXPORT_PACKAGE, "org.jcrom.annotations;uses=\"org.jcrom.annotations\";version=\"0.2.0\"")
                        .build());
        bundles.add(bundle);
        Class clazz = bundle.loadClass("org.wisdom.jcrom.entity1.BundleUnloadITEntity");
        ReferenceQueue referenceQueue = new ReferenceQueue();
        WeakReference weakReference = new WeakReference(clazz, referenceQueue);
        assertThat(osGiUtils.getCrud(clazz)).isNotNull();
        bundle.stop();
        while (bundle.getState() != Bundle.RESOLVED) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        clazz = null;
        bundle = null;
        byte[] b = new byte[40000000];
        b[b.length - 1] = 1;
        System.gc();
        Assert.assertTrue(weakReference.isEnqueued());
    }

    /**
     * Reference test.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws BundleException
     */
    //@Test
    public void testUnloadUnmapped() throws IOException, ClassNotFoundException, BundleException {
        // create an entity to map
        String source = "package org.wisdom.jcrom.unmapped;\n" +
                "\n" +
                "import org.jcrom.AbstractJcrEntity;\n" +
                "import org.jcrom.annotations.JcrNode;\n" +
                "import org.jcrom.annotations.JcrName;\n" +
                "import org.jcrom.annotations.JcrPath;\n" +
                "\n" +
                "@JcrNode(nodeType = \"test:testentity\")\n" +
                "public class BundleUnloadITEntity {\n" +
                "@JcrName\n" +
                "private String name;\n" +

                "@JcrPath\n" +
                "private String path;\n" +
                "}\n";

        File sourceFile = new File("target", "BundleUnloadITEntity.java");
        new FileWriter(sourceFile).append(source).close();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, sourceFile.getPath());

        // create a bundle containing this entity
        Bundle bundle = osgi.installAndStart("local:/test",
                bundle()
                        .add("org/wisdom/jcrom/unmapped/BundleUnloadITEntity.class", new FileInputStream("target/BundleUnloadITEntity.class"))
                        .set(Constants.IMPORT_PACKAGE, "org.jcrom.annotations")
                        .set(Constants.EXPORT_PACKAGE, "org.jcrom.annotations;uses=\"org.jcrom.annotations\";version=\"0.2.0\"")
                        .build());
        bundles.add(bundle);
        Class clazz = bundle.loadClass("org.wisdom.jcrom.unmapped.BundleUnloadITEntity");
        ReferenceQueue referenceQueue = new ReferenceQueue();
        WeakReference weakReference = new WeakReference(clazz, referenceQueue);
        clazz = null;
        osgi.uninstall(bundle);
        while (bundle.getState() != Bundle.UNINSTALLED) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        bundle = null;
        byte[] b = new byte[40000000];
        b[b.length - 1] = 1;
        forceGc();
        Assert.assertTrue(weakReference.isEnqueued());
    }

    public static void forceGc() {
        Object obj = new Object();
        WeakReference ref = new WeakReference<Object>(obj);
        obj = null;
        while (ref.get() != null) {
            System.gc();
        }
    }


}
