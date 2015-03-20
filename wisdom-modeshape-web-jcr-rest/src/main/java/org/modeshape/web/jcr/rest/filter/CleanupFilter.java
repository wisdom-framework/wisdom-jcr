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
import org.modeshape.web.jcr.rest.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.http.Result;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;

import java.util.regex.Pattern;


/**
 * {@link Filter} implementation which will always close an active {@link javax.jcr.Session} instance, if such an instance
 * has been opened during a request.
 *
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
@Component
@Provides
@Instantiate
public class CleanupFilter implements Filter {

    private static final Pattern REGEX = Pattern.compile("/*");

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupFilter.class);

    @Override
    public Result call(Route route, RequestContext requestContext) throws Exception {
        LOGGER.trace("Executing cleanup filter...");
        AbstractHandler.cleanupActiveSession();
        return requestContext.proceed();
    }

    @Override
    public Pattern uri() {
        return REGEX;
    }

    @Override
    public int priority() {
        return 200;
    }

}
