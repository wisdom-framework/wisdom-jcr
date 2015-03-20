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

import org.jcrom.JcrEntity;
import org.jcrom.annotations.JcrNode;

/**
 * TODO write documentation<br>
 *<br>
 * Created at 19/03/2015 10:03.<br>
 *
 * @author Bastien
 *
 */
@JcrNode(classNameProperty = "className", nodeType = "wisdom:project")
public interface IProject extends JcrEntity {
}
