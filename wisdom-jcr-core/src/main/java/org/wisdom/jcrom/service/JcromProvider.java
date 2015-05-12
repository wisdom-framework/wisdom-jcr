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
package org.wisdom.jcrom.service;

import org.jcrom.Jcrom;
import org.wisdom.jcrom.conf.JcromConfiguration;

import javax.jcr.Session;

/**
 * Service for providing instance of Jcrom.
 *<br>
 * Created at 06/05/2015 17:59.<br>
 *
 * @author Bastien
 *
 */

public interface JcromProvider {

    public Jcrom getJcrom(JcromConfiguration jcromConfiguration, Session session);
}
