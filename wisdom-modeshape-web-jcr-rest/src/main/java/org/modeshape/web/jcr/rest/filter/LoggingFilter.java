/*
 * ModeShape (http://www.modeshape.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.modeshape.web.jcr.rest.filter;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.http.Result;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;

import java.util.regex.Pattern;

/**
 * {@link Filter} which will print out various logging information in DEBUG mode.
 *
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
@Component
@Provides
@Instantiate
public class LoggingFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);

    private static final Pattern REGEX = Pattern.compile("/*");

    @Override
    public Result call(Route route, RequestContext requestContext) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Received request: {0}", requestContext.route().getUrl());
            LOGGER.debug("Executing method: {0}", requestContext.route().getControllerMethod());
        }
        return requestContext.proceed();
    }

    @Override
    public Pattern uri() {
        return REGEX;
    }

    @Override
    public int priority() {
        return 100;
    }
}
