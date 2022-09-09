/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2022 Wisdom Framework
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
package org.wisdom.jcrom.runtime;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.concurrent.ExecutionContext;
import org.wisdom.api.concurrent.ExecutionContextService;

@Component
@Provides
@Instantiate
public class SessionExecutionContextService implements ExecutionContextService {

    private static final Logger logger = LoggerFactory.getLogger(SessionExecutionContextService.class.getName());

    @Override
    public String name() {
        return "jcr-session-context";
    }

    @Override
    public ExecutionContext prepare() {
        return new JcrSessionExecutionContext();
    }

    private static class JcrSessionExecutionContext implements ExecutionContext {

        @Override
        public void apply() {
        }

        @Override
        public void unapply() {
            JcrRepository.cleanUpRequestContextSession();
        }

    }

}
