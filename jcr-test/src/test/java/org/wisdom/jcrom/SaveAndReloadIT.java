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

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.wisdom.api.model.Crud;
import org.wisdom.jcrom.dummy.Baby;
import org.wisdom.jcrom.dummy.Daddy;
import org.wisdom.test.parents.WisdomTest;

@Ignore
public class SaveAndReloadIT extends WisdomTest {

	private static final String DADDY_PATH = "/daddies";
	private static final String DADDY_NAME = "pops";
	private static final String BABY_PATH = "/baby";
	private static final String BABY_NAME = "junior";

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
	public void saveThenReload() {
		Baby baby = new Baby();
		baby.setPath(BABY_PATH);
		baby.setName(BABY_NAME);
		Assert.assertTrue(baby.isSucking());

		Crud<Baby, String> crud = osGiUtils.getCrud(Baby.class);
		crud.save(baby);

		Baby loadedBaby = crud.findOne(BABY_NAME);
		Assert.assertTrue(loadedBaby.isSucking());
		loadedBaby.setSucking(false);
		crud.save(loadedBaby);
		
		Baby reloadedBaby = crud.findOne(BABY_NAME);
		Assert.assertFalse(reloadedBaby.isSucking());
	}
	
	@Test
	public void saveThenReloadViaParent() {
		Daddy daddy = new Daddy();
		daddy.setPath(DADDY_PATH);
		daddy.setName(DADDY_NAME);
		Baby baby = new Baby();
		baby.setName(BABY_NAME);
		baby.setPath(DADDY_PATH + BABY_PATH);
		daddy.setBaby(baby);
		Assert.assertTrue(baby.isSucking());
		
		Crud<Daddy, String> dadCrud = osGiUtils.getCrud(Daddy.class);
		daddy = dadCrud.save(daddy);
		
		Crud<Baby, String> kidCrud = osGiUtils.getCrud(Baby.class);
		Baby foundBaby = kidCrud.findOne(baby.getName());
		foundBaby.setSucking(false);
		kidCrud.save(foundBaby);
		
		Daddy foundDad = dadCrud.findOne(daddy.getName());
		Assert.assertFalse("This baby sucks!", foundDad.getBaby().isSucking());
	}
}
