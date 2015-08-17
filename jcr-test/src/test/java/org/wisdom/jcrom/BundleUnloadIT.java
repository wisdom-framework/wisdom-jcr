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

import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.test.parents.WisdomTest;

/**
 * Test used to check if all references to mapped classes are correctly removed
 * when a bundle is unloaded.
 * <p/>
 * User: Antoine Mischler <antoine@dooapp.com> Date: 19/06/15 Time: 15:29
 */
@Ignore
public class BundleUnloadIT extends WisdomTest {
	/**
	 * Make sure gc is called by invoking it until that dumbl object is gc'ed
	 */
	public static void forceGc() {
		Object obj = new Object();
		WeakReference ref = new WeakReference<Object>(obj);
		obj = null;
		while (ref.get() != null) {
			System.gc();
		}
	}

	@Inject
	private BundleContext context;

	@Inject
	private ApplicationConfiguration applicationConfiguration;

	private OSGiHelper osgi;

	private OSGiUtils osGiUtils;

	/**
	 * As class is in entity1 package, and that package is declared in the jcrom section of application.conf
	 * this class will be mapped by jcrom and have an associated JcrRepository
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws BundleException
	 */
	@Test
	public void a_mapped_class_should_unload_correctly() throws IOException, ClassNotFoundException,
			BundleException {
		final String CLASS_PATH = "org/wisdom/jcrom/entity1/BundleUnloadITEntity";
		loadAndUnloadBundleFor(CLASS_PATH);
	}

	/**
	 * Reference test.
	 * To make sure class is not mapped by jcrom, we put it in a "unmapped" package
	 *
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws BundleException
	 */
	@Test
	public void an_unmapped_class_should_be_unloaded_correctly() throws IOException,
			ClassNotFoundException, BundleException {
		String CLASS_PATH = "org/wisdom/jcrom/unmapped/BundleUnloadITEntity";
		loadAndUnloadBundleFor(CLASS_PATH);
	}

	private File createEntityClass(String classPath) throws IOException {
		File sourceFile = new File("target", classPath+".java");
		File classFile = new File("target", classPath+".class");
		final String packageName = classPath.substring(0, classPath.lastIndexOf('/')).replace('/', '.');
		final String className  = classPath.substring(classPath.lastIndexOf('/')+1);
		if (!classFile.exists()) {
			if (!sourceFile.exists()) {
				sourceFile.getParentFile().mkdirs();
				// create an entity to map
				String source = "package "+packageName+";\n" + "\n"
						+ "import org.jcrom.AbstractJcrEntity;\n"
						+ "import org.jcrom.annotations.JcrNode;\n"
						+ "import org.jcrom.annotations.JcrName;\n"
						+ "import org.jcrom.annotations.JcrPath;\n" + "\n"
						+ "@JcrNode(nodeType = \"test:testentity\")\n"
						+ "public class "+className+" {\n"
						+ "@JcrName\n" + "private String name;\n" +

						"@JcrPath\n" + "private String path;\n" + "}\n";

				new FileWriter(sourceFile).append(source).close();
			}
			classFile.getParentFile().mkdirs();
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			compiler.run(null, null, null, sourceFile.getPath());
		}
		return classFile;
	}

	private void loadAndUnloadBundleFor(String classPath)
			throws ClassNotFoundException, IOException {
		String className = classPath.replace('/', '.');
		File classFile = createEntityClass(classPath);
		// create a bundle containing this entity
		Bundle bundle = osgi
				.installAndStart(
						"local:/test",
						bundle().add(
								classPath+".class",
								new FileInputStream(classFile))
								.set(Constants.IMPORT_PACKAGE,
										"org.jcrom.annotations")
								.set(Constants.EXPORT_PACKAGE,
										"org.jcrom.annotations;uses=\"org.jcrom.annotations\";version=\"0.2.0\"")
								.build());
		// load that bundle
		// and the given class
		Class clazz = bundle
				.loadClass(className);
		// now create a reference queue and reference to make sure that class is unloaded
		ReferenceQueue referenceQueue = new ReferenceQueue();
		WeakReference weakReference = new WeakReference(clazz, referenceQueue);
		// now start deleting things
		clazz = null;
		osgi.uninstall(bundle);
		// uninstalling bundle is asynchronous, so brutally wait for it
		while (bundle.getState() != Bundle.UNINSTALLED) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		bundle = null;
		forceGc();
		Assertions.assertThat(weakReference.isEnqueued()).as("weak reference should have been enqueued, but is not").isTrue();
	}

	@Before
	public void setUp() throws IOException {
		osgi = new OSGiHelper(context);
		osGiUtils = new OSGiUtils(osgi);
	}

	@After
	public void tearDown() {
		try {
			osgi.dispose();
		} catch (Exception e) {
			// Ignore it
		}
	}

}
