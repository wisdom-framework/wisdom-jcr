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
package org.wisdom.jcrom.filter;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.http.Result;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;
import org.wisdom.jcrom.runtime.JcrRepository;

import java.util.regex.Pattern;

@Component
@Provides
@Instantiate
public class CloseJcrSessionFilter implements org.wisdom.api.interception.Filter {

    private static final Logger logger = LoggerFactory.getLogger(CloseJcrSessionFilter.class.getName());

    private static final Pattern REGEX = Pattern.compile(".*");

    @Override
    public Result call(Route route, RequestContext context) throws Exception {
        Result result;
        try {
            result = context.proceed();
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            JcrRepository.cleanUpRequestContextSession();
        }
    }


    @Override
    public Pattern uri() {
        return REGEX;
    }

    @Override
    public int priority() {
        return 900;
    }

}
